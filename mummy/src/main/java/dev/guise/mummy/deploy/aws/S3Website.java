/*
 * Copyright © 2019 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.guise.mummy.deploy.aws;

import static com.globalmentor.java.Conditions.*;
import static com.globalmentor.java.Enums.*;
import static com.globalmentor.net.HTTP.*;
import static com.globalmentor.net.URIs.*;
import static dev.guise.mummy.Artifact.*;
import static dev.guise.mummy.GuiseMummy.*;
import static java.util.Collections.*;
import static java.util.Objects.*;
import static java.util.stream.Collectors.*;
import static java.util.stream.Stream.*;
import static org.zalando.fauxpas.FauxPas.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import org.jspecify.annotations.*;

import org.slf4j.Logger;

import com.globalmentor.lex.Identifier;
import com.globalmentor.net.*;
import com.globalmentor.text.StringTemplate;

import io.confound.config.Configuration;
import dev.guise.mummy.*;
import dev.guise.mummy.deploy.ContentDeliveryTarget;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.regions.*;
import software.amazon.awssdk.services.s3.*;
import software.amazon.awssdk.services.s3.model.*;

/// Deploys a site to [AWS S3](https://aws.amazon.com/s3/), configured as an S3 bucket website.
/// @author Garret Wilson
public class S3Website extends S3 {

	/// The section relative key for the alternative buckets in the configuration; defaults to the root-relative form of
	/// [GuiseMummy#CONFIG_KEY_SITE_ALT_DOMAINS] resolved against any [GuiseMummy#CONFIG_KEY_DOMAIN] in the global configuration.
	public static final String CONFIG_KEY_ALT_BUCKETS = "altBuckets";

	/// The section relative key for the approach to be used for implementing redirects for resource alt locations on S3 websites. The value is the lowercase,
	/// kebab-case form of [RedirectMeans] (e.g. `routing-rule`).
	public static final String CONFIG_KEY_REDIRECT_MEANS = "redirectMeans";

	/// The default value to use for the means of redirection if none is specified in the configuration.
	/// @see #CONFIG_KEY_REDIRECT_MEANS
	public static final RedirectMeans DEFAULT_REDIRECT_MEANS = RedirectMeans.OPTIMAL;

	/// The section relative key for the number of redirects required to switch to using object-redirects for non-collection alt locations.
	/// @apiNote This number must not be greater than `50`, because as of May 2020 AWS S3 does not allow more than this number of routing rule-based
	///          redirects; otherwise the following error will occur:
	///          `software.amazon.awssdk.services.s3.model.S3Exception: … routing rules provided,
	///          the number of routing rules in a website configuration is limited to 50. (Service: S3, Status Code: 400, Request ID: …)`
	public static final String CONFIG_KEY_REDIRECT_COUNT_OPTIMAL_THRESHOLD = "redirectCountOptimalThreshold";

	/// The default number of redirects required to switch to using object-redirects for non-collection alt locations.
	/// @apiNote This redirect count optimal threshold must not be greater than `50`, because as of May 2020 AWS S3 does not allow more than this number
	///          of routing rule-based redirects; otherwise the following error will occur:
	///          `software.amazon.awssdk.services.s3.model.S3Exception: … routing rules provided,
	///          the number of routing rules in a website configuration is limited to 50. (Service: S3, Status Code: 400, Request ID: …)`
	/// @see #CONFIG_KEY_REDIRECT_COUNT_OPTIMAL_THRESHOLD
	public static final int DEFAULT_REDIRECT_COUNT_OPTIMAL_OPTIMAL_THRESHOLD = 30;

	/// The S3 website means to be used for effecting redirects.
	public enum RedirectMeans implements Identifier {

		/// All redirects use objects except for those that require routing rules (notably collection redirects).
		OBJECT,
		/// All redirects use routing rules.
		ROUTING_RULE,
		/// Functions like [#ROUTING_RULE] if the total number of redirects is below the optimal threshold
		/// [S3Website#getRedirectCountOptimalThreshold()]; otherwise functions like [#OBJECT].
		OPTIMAL;
	}

	/// The regions that use a dash (`-`) instead of a dot (`.`) to separate `s3-website` from the region in the endpoint domain string.
	private static final Set<Region> WEBSITE_ENDPOINT_DASH_REGIONS = Set.of(Region.US_EAST_1, Region.US_WEST_1, Region.US_WEST_2, Region.AP_SOUTHEAST_1,
			Region.AP_SOUTHEAST_2, Region.AP_NORTHEAST_1, Region.EU_WEST_1, Region.SA_EAST_1);

	private static final String ENDPOINT_S3_WEBSITE_REGION_DELIMITER_DASH = String.valueOf('-');
	private static final String ENDPOINT_S3_WEBSITE_REGION_DELIMITER_DOT = String.valueOf('.');

	/// String template for a bucket web site URL, with the following string parameters:
	///
	/// 1. bucket name
	/// 2. S3 website region delimiter: dot (`.`) or dash (`-`)
	/// 3. region ID
	/// 4. region domain
	/// @implNote The [Website Endpoints](https://docs.aws.amazon.com/AmazonS3/latest/dev/WebsiteEndpoints.html) documentation indicates that there are
	///           two forms based upon region, one using a dot (`.`) character and the other using a dash (`-`) character, but the dot form
	///           seems to work as well, as some have [indicated](https://stackoverflow.com/questions/46627060/how-to-resolve-aws-s3-url-says-west-bucket-says-east#comment80219601_46627148)
	///           that AWS may be standardizing on the dot form. Nevertheless this implementation distinguishes between the two forms to provide the most correct values
	///           as per the documentation.
	/// @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/WebsiteEndpoints.html">Website Endpoints</a>
	/// @see <a href="https://docs.aws.amazon.com/general/latest/gr/rande.html#s3_website_region_endpoints">Amazon Simple Storage Service Website Endpoints</a>
	/// @see <a href="https://stackoverflow.com/q/57480708/421049">Get AWS S3 bucket web static site URL programmatically using Java SDK v2</a>
	private final static StringTemplate BUCKET_WEBSITE_ENDPOINT_TEMPLATE = StringTemplate.builder().parameter(StringTemplate.STRING_PARAMETER).text(".s3-website")
			.parameter(StringTemplate.STRING_PARAMETER).parameter(StringTemplate.STRING_PARAMETER).text(".").parameter(StringTemplate.STRING_PARAMETER).build();

	/// Returns the endpoint domain for static web site hosting for the given bucket in the indicated region.
	/// @param bucket The name of the bucket.
	/// @param region The region in which the bucket is located.
	/// @return A endpoint domain for accessing the static web site hosted in the identified bucket.
	/// @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/WebsiteEndpoints.html">Website Endpoints</a>
	/// @see <a href="https://docs.aws.amazon.com/general/latest/gr/rande.html#s3_website_region_endpoints">Amazon Simple Storage Service Website Endpoints</a>
	/// @see PartitionMetadata#hostname()
	public static String getBucketWebsiteEndpoint(@NonNull final String bucket, @NonNull final Region region) {
		final String s3WebsiteRegionDelimiter = WEBSITE_ENDPOINT_DASH_REGIONS.contains(region) ? ENDPOINT_S3_WEBSITE_REGION_DELIMITER_DASH
				: ENDPOINT_S3_WEBSITE_REGION_DELIMITER_DOT;
		return BUCKET_WEBSITE_ENDPOINT_TEMPLATE.apply(bucket, s3WebsiteRegionDelimiter, region.id(), region.metadata().domain());
	}

	/// Returns the URL for static web site hosting for the given bucket in the indicated region.
	/// @param bucket The name of the bucket.
	/// @param region The region in which the bucket is located.
	/// @return A URL for accessing the static web site hosted in the identified bucket.
	/// @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/WebsiteEndpoints.html">Website Endpoints</a>
	/// @see <a href="https://docs.aws.amazon.com/general/latest/gr/rande.html#s3_website_region_endpoints">Amazon Simple Storage Service Website Endpoints</a>
	/// @see PartitionMetadata#hostname()
	public static URI getBucketWebsiteUrl(@NonNull final String bucket, @NonNull final Region region) {
		return createURI(HTTP_URI_SCHEME, null, getBucketWebsiteEndpoint(bucket, region), -1, URIs.ROOT_PATH, null, null);
	}

	/// {@inheritDoc}
	/// @implSpec This implementation returns a set of just `"http"`, as S3 websites support only HTTP (not HTTPS) access.
	@Override
	public Set<String> getSupportedProtocols() {
		return Set.of(HTTP_URI_SCHEME);
	}

	private final Set<String> altBuckets;

	/// Returns the S3 buckets, if any, to serve as alternatives and redirect to the primary bucket.
	/// @return The S3 buckets, if any, to serve as alternatives and redirect to the primary bucket.
	public Set<String> getAltBuckets() {
		return altBuckets;
	}

	/// {@inheritDoc}
	/// @implSpec This version returns A stream of all bucket names, starting with the primary bucket [#getBucket()], followed by the alternatives
	///           [#getAltBuckets()] in no guaranteed order.
	@Override
	public Stream<String> buckets() {
		return concat(super.buckets(), getAltBuckets().stream());
	}

	private final DomainName siteDomain;

	/// Returns the explicit site domain if specified, which may be the same or different than the bucket name.
	/// @return The explicit site domain if specified, which may be the same or different than the bucket name.
	public Optional<DomainName> getSiteDomain() {
		return Optional.ofNullable(siteDomain);
	}

	private final RedirectMeans redirectMeans;

	/// Returns the S3 website means to be used for effecting redirects.
	/// @return The S3 website means to be used for effecting redirects.
	public RedirectMeans getRedirectMeans() {
		return redirectMeans;
	}

	private final int redirectCountOptimalThreshold;

	/// Returns the number of redirects required to switch to using object-redirects for non-collection alt locations.
	/// @return The number of redirects required to switch to using object-redirects for non-collection alt locations.
	public int getRedirectCountOptimalThreshold() {
		return redirectCountOptimalThreshold;
	}

	private final Set<S3ArtifactRedirectDeployObject> routingRuleRedirectObjects = new LinkedHashSet<>();

	/// Returns the map of redirects to be implemented using routing rules.
	/// @return The map of redirects to be implemented using routing rules.
	protected Set<S3ArtifactRedirectDeployObject> getRoutingRuleRedirectObjects() {
		return routingRuleRedirectObjects;
	}

	/// Configuration constructor.
	///
	/// The region is retrieved from `region` in the local configuration. The bucket name is retrieved from `bucket` in the
	/// local configuration, falling back to `siteDomain` and finally `domain` in the context
	/// configuration if not specified. The bucket alternatives are retrieved from `altBuckets`, falling back to
	/// `siteAltDomains` in the context configuration if not specified. The site domain if explicitly set is retrieved from
	/// `siteDomain`, resolved as appropriate to any project domain. The redirect means is retrieved from
	/// `redirectMeans` in the local configuration, defaulting to `OPTIMAL`. The threshold for optimal redirects is
	/// retrieved from `redirectCountOptimalThreshold` in the local configuration, defaulting to `30`.
	/// @implSpec This method calls [#getConfiguredBucket(Configuration, Configuration)] and [#getConfiguredAltBuckets(Configuration, Configuration)]
	///           to determine the bucket and alternative buckets.
	/// @implSpec This method calls [GuiseMummy#findConfiguredSiteDomain(Configuration)] to determine the site domain, if any.
	/// @param context The context of static site generation.
	/// @param localConfiguration The local configuration for this deployment target, which may be a section of the project configuration.
	/// @see AWS#CONFIG_KEY_DEPLOY_AWS_PROFILE
	/// @see #CONFIG_KEY_REGION
	/// @see #CONFIG_KEY_BUCKET
	/// @see #CONFIG_KEY_ALT_BUCKETS
	/// @see GuiseMummy#CONFIG_KEY_SITE_DOMAIN
	/// @see GuiseMummy#CONFIG_KEY_SITE_ALT_DOMAINS
	/// @see GuiseMummy#CONFIG_KEY_DOMAIN
	/// @see #getConfiguredBucket(Configuration, Configuration)
	/// @see #getConfiguredAltBuckets(Configuration, Configuration)
	public S3Website(@NonNull final MummyContext context, @NonNull final Configuration localConfiguration) {
		this(context.getConfiguration().findString(AWS.CONFIG_KEY_DEPLOY_AWS_PROFILE).orElse(null), Region.of(localConfiguration.getString(CONFIG_KEY_REGION)),
				getConfiguredBucket(context.getConfiguration(), localConfiguration), getConfiguredAltBuckets(context.getConfiguration(), localConfiguration),
				findConfiguredSiteDomain(context.getConfiguration()).orElse(null),
				localConfiguration.findString(CONFIG_KEY_REDIRECT_MEANS).map(fromSerializionOf(RedirectMeans.class)).orElse(DEFAULT_REDIRECT_MEANS),
				localConfiguration.findInt(CONFIG_KEY_REDIRECT_COUNT_OPTIMAL_THRESHOLD).orElse(DEFAULT_REDIRECT_COUNT_OPTIMAL_OPTIMAL_THRESHOLD));
	}

	/// Region, bucket, and alternative buckets constructor. The alternative buckets will be stored as a set.
	/// @apiNote This redirect count optimal threshold must not be greater than `50`, because as of May 2020 AWS S3 does not allow more than this number
	///          of routing rule-based redirects; otherwise the following error will occur:
	///          `software.amazon.awssdk.services.s3.model.S3Exception: … routing rules provided,
	///          the number of routing rules in a website configuration is limited to 50. (Service: S3, Status Code: 400, Request ID: …)`
	/// @param profile The name of the AWS profile to use for retrieving credentials, or `null` if the default credential provider should be used.
	/// @param region The AWS region of deployment.
	/// @param bucket The bucket into which the site should be deployed.
	/// @param altBuckets The bucket alternatives, if any, to redirect to the primary bucket.
	/// @param siteDomain The full-qualified domain name of the site. If specified, it will be used in cases that which a site other than the bucket is to be
	///          indicated, such as in redirect hostname, to prevent e.g. a CloudFront distribution redirecting back to the bucket URL.
	/// @param redirectMeans The S3 website means to be used for effecting redirects.
	/// @param redirectCountOptimalThreshold The number of redirects required to switch to using object-redirects for non-collection alt locations.
	/// @throws IllegalArgumentException if the given site domain is not absolute.
	public S3Website(@Nullable String profile, @NonNull final Region region, @NonNull String bucket, @NonNull final Collection<String> altBuckets,
			@Nullable DomainName siteDomain, @NonNull final RedirectMeans redirectMeans, final int redirectCountOptimalThreshold) {
		super(profile, region, bucket);
		this.altBuckets = new LinkedHashSet<>(altBuckets); //maintain order to help with reporting and debugging
		if(siteDomain != null) {
			siteDomain.checkArgumentAbsolute();
		}
		this.siteDomain = siteDomain;
		this.redirectMeans = requireNonNull(redirectMeans);
		this.redirectCountOptimalThreshold = checkArgumentNotNegative(redirectCountOptimalThreshold);
	}

	/// Injection constructor that accepts a pre-built S3 client with default redirect settings.
	/// @apiNote The caller is responsible for ensuring that the provided `s3Client` is configured consistently with the
	///         given `region`. The `S3Client` API does not expose its internal configuration, so this constructor cannot
	///         validate consistency. This constructor is therefore not part of the public API.
	/// @param region The AWS region of deployment.
	/// @param bucket The bucket into which the site should be deployed.
	/// @param s3Client The pre-built S3 client.
	S3Website(@NonNull final Region region, @NonNull String bucket, @NonNull final S3Client s3Client) {
		super(null, region, bucket, s3Client);
		this.altBuckets = Set.of();
		this.siteDomain = null;
		this.redirectMeans = RedirectMeans.OBJECT;
		this.redirectCountOptimalThreshold = DEFAULT_REDIRECT_COUNT_OPTIMAL_OPTIMAL_THRESHOLD;
	}

	/// Determines the alternative buckets to use, if any. This method determines the alternative buckets in the following order:
	///
	/// 1. The key [#CONFIG_KEY_ALT_BUCKETS] relative to the S3 configuration.
	/// 2. The site domain `siteAltDomains` resolved to any project domain, to the domain name root (i.e. with the ending delimiter
	///    removed), retrieved from the global configuration.
	/// @implSpec This method calls [GuiseMummy#findConfiguredSiteAltDomains(Configuration)].
	/// @param globalConfiguration The configuration containing all the configuration values.
	/// @param localConfiguration The local configuration for S3, which may be a section of the project configuration.
	/// @return The configured alternative bucket names, if any.
	/// @see #CONFIG_KEY_ALT_BUCKETS
	/// @see GuiseMummy#CONFIG_KEY_SITE_DOMAIN
	protected static Collection<String> getConfiguredAltBuckets(@NonNull final Configuration globalConfiguration,
			@NonNull final Configuration localConfiguration) {
		return localConfiguration.findCollection(CONFIG_KEY_ALT_BUCKETS, String.class)
				.or(() -> findConfiguredSiteAltDomains(globalConfiguration)
						.map(altDomains -> altDomains.stream().map(DomainName.ROOT::relativize).map(DomainName::toString).collect(toCollection(LinkedHashSet::new))))
				.orElse(emptyList());
	}

	/// {@inheritDoc}
	/// @implSpec In addition to creating the main bucket, this implementation creates and configures the alt buckets as needed.
	@Override
	public void prepare(final MummyContext context) throws IOException {
		super.prepare(context);
		final S3Client s3Client = getS3Client();
		try {
			//create alternative buckets
			final Region region = getRegion();
			for(final String altBucket : getAltBuckets()) {
				getLogger().info("Preparing AWS S3 bucket `{}` in region `{}` to serve as an alternative.", altBucket, region);
				final boolean altBucketExists = bucketExists(altBucket);
				getLogger().debug("Alternative bucket `{}` exists? {}.", altBucket, altBucketExists);
				if(!altBucketExists) { //create the bucket if it doesn't exist
					getLogger().info("Creating S3 alternative bucket `{}` in AWS region `{}`.", altBucket, region);
					s3Client.createBucket(request -> request.bucket(altBucket).createBucketConfiguration(configuringCreateBucketForRegion(region)));
				}
			}
		} catch(final SdkException sdkException) {
			throw new IOException(sdkException);
		}
	}

	/// {@inheritDoc}
	/// @implSpec This implementation sets the policy to public. If there are any [ContentDeliveryTarget] deploy targets that use this target as an origin
	///           target, access will be restricted to only those clients which provide one of those user agents.
	/// @implSpec This implementation sets the bucket policy irrespective of whether a policy already exists.
	/// @see S3#policyPublicReadGetForBucket(String)
	/// @see S3#policyPublicReadGetForBucketRequiringAnyUserAgentOf(String, Iterable)
	/// @see MummyContext#getDeployTargets()
	/// @see ContentDeliveryTarget#getOriginTarget(MummyContext)
	/// @see ContentDeliveryTarget#getUserAgentIdentifications()
	@Override
	protected void setBucketPolicy(final MummyContext context, final String bucket, final boolean hasPolicy) throws IOException {
		final Logger logger = getLogger();
		try {
			logger.info("Setting policy of S3 bucket `{}` to public.", bucket);
			final Set<String> userAgents = contentDeliveryTargets(context)
					//collect the possible user agent identifications the content delivery targets will be using, maintaining order to assist in debugging
					.flatMap(target -> target.getUserAgentIdentifications().stream()).collect(toCollection(LinkedHashSet::new));
			logger.debug("Found content delivery target user agents: {}.", userAgents);
			//if the S3 website is serving as the origin for any content delivery targets, restrict the policy to only clients from those targets
			final String policy = !userAgents.isEmpty() ? policyPublicReadGetForBucketRequiringAnyUserAgentOf(bucket, userAgents)
					: policyPublicReadGetForBucket(bucket);
			logger.debug("Using S3 bucket policy: `{}`.", policy);
			getS3Client().putBucketPolicy(request -> request.bucket(bucket).policy(policy));
		} catch(final SdkException sdkException) {
			throw new IOException(sdkException);
		}
	}

	/// {@inheritDoc}
	/// @implSpec After deployment to the primary bucket, this version configures the primary and alternative buckets for website access.
	/// @return The URL of the primary bucket website.
	@Override
	public Optional<URI> deploy(@NonNull final MummyContext context, @NonNull Artifact rootArtifact) throws IOException {
		super.deploy(context, rootArtifact);
		final S3Client s3Client = getS3Client();
		final String bucket = getBucket();
		try { //configure bucket for web site with appropriate redirects
			getLogger().info("Configuring S3 bucket `{}` for web site access.", bucket);
			final Configuration configuration = context.getConfiguration();
			final Set<S3ArtifactRedirectDeployObject> routingRuleRedirectObjects = getRoutingRuleRedirectObjects();
			if(!routingRuleRedirectObjects.isEmpty()) {
				getLogger().info("Configuring {} redirect routing rule(s).", routingRuleRedirectObjects.size());
			}
			final Optional<String> foundSiteHostName = getSiteDomain().map(DomainName.ROOT::relativize).map(DomainName::toString);
			//we support HTTPS either this deploy target (S3 websites usually don't) or one of our content delivery targets (e.g. CloudFront usually does) supports HTTPS
			final boolean isHttpsSupported = concat(Stream.of(this), contentDeliveryTargets(context))
					.filter(deployTarget -> deployTarget.getSupportedProtocols().contains(HTTPS_URI_SCHEME)).findAny().isPresent();
			final Set<RoutingRule> routingRules = routingRuleRedirectObjects.stream().map(redirectObject -> {
				final String redirectKey = redirectObject.getKey();
				return RoutingRule.builder().condition(condition -> condition.keyPrefixEquals(redirectKey)).redirect(redirect -> {
					//If both the redirect key (e.g. `foo/`) is a collection, and the target artifact can contain other artifacts
					//((e.g. a directory, which basically means it is a collection as well), we'll replace the entire key prefix
					//to allow redirects for contained artifacts as well. Otherwise replace the entire key in case an artifact
					//key matches the prefix of another non-redirected path (not expected to occur frequently in practice). Examples:
					//* `foo/index.xhtml` to `foo/index.html`: Replace entire key. 
					//* `foo/index.html` to `foo/`: Replace entire key. 
					//* `foo/` to `foo`: Replace entire key. 
					//* `foo/` to `bar`: Replace entire key. 
					//* `foo/` to `bar/`: Replace key prefix. 
					final String redirectTargetKey = redirectObject.getRedirectTargetKey();
					if(isCollectionPath(redirectKey) && redirectObject.getTargetArtifact() instanceof CollectionArtifact) {
						redirect.replaceKeyPrefixWith(redirectTargetKey);
					} else {
						redirect.replaceKeyWith(redirectTargetKey);
					}
					//Set the host name if we know the site domain name, to prevent any distribution from redirecting back to the bucket URL.
					//See https://stackoverflow.com/q/58477877/421049 .
					foundSiteHostName.ifPresent(redirect::hostName);
					//If we know we support HTTPS, set the "protocol" so that redirection will occur directly to HTTPS with no intermediate HTTP redirect.
					//See https://stackoverflow.com/q/61705411/421049 .
					if(isHttpsSupported) {
						redirect.protocol(Protocol.HTTPS);
					}
				}).build();
			}).collect(toCollection(LinkedHashSet::new)); //maintain order to help with debugging
			final WebsiteConfiguration.Builder websiteConfigurationBuilder = WebsiteConfiguration.builder();

			//set the index document, if any, based upon the collection content resource name
			findCollectionContentResourceName(configuration).ifPresent(indexDocumentSuffix -> {
				websiteConfigurationBuilder.indexDocument(IndexDocument.builder().suffix(indexDocumentSuffix).build());
			});
			if(!routingRules.isEmpty()) {
				websiteConfigurationBuilder.routingRules(routingRules.toArray(RoutingRule[]::new));
			}
			s3Client.putBucketWebsite(request -> request.bucket(bucket).websiteConfiguration(websiteConfigurationBuilder.build()));

			//configure alternative buckets for web sites
			for(final String altBucket : getAltBuckets()) {
				getLogger().info("Configuring S3 alternative bucket `{}` for web site redirection.", altBucket);
				s3Client.putBucketWebsite(builder -> builder.bucket(altBucket).websiteConfiguration(config -> config.redirectAllRequestsTo(redirect -> {
					redirect.hostName(bucket);
					if(isHttpsSupported) { //redirect directly to HTTPS if we know we support it; compare routing rule redirects above
						redirect.protocol(Protocol.HTTPS);
					}
				})));
			}
		} catch(final SdkException sdkException) {
			throw new IOException(sdkException);
		}

		return Optional.of(getBucketWebsiteUrl(getBucket(), getRegion()));
	}

	/// {@inheritDoc}
	/// @implSpec This implementation plans the entire site categorizing redirect objects in [#getDeployObjectsByKey()] or
	///           [#getRoutingRuleRedirectObjects()] as if [RedirectMeans#OBJECT] were specified, and then moves objects between the two appropriately
	///           as specified by [#getRedirectMeans()].
	@Override
	@SuppressWarnings("fallthrough")
	protected void plan(MummyContext context, Artifact rootArtifact) throws IOException {
		super.plan(context, rootArtifact); //start with the default planning
		final RedirectMeans redirectMeans = getRedirectMeans();
		getLogger().debug("Using redirect means `{}`.", redirectMeans);
		switch(redirectMeans) {
			case OBJECT:
				break; //redirection was already configured for object mode during planning
			case OPTIMAL:
				{
					//subtract the existing routing rule redirects to see how many we have left to meet the threshold; this could be negative
					final int threshold = getRedirectCountOptimalThreshold();
					final int routingRuleRedirectCount = getRoutingRuleRedirectObjects().size();
					getLogger().debug("Redirect optimal threshold {}; already have {} routing rule redirects.", threshold, routingRuleRedirectCount);
					final int remainingThreshold = getRedirectCountOptimalThreshold() - routingRuleRedirectCount;
					//if we have a positive remaining threshold and the number of object redirects meet that threshold (the limit operation will never go above the threshold by definition)
					if(remainingThreshold <= 0 || getDeployObjectsByKey().values().stream().filter(S3ArtifactRedirectDeployObject.class::isInstance)
							.limit(remainingThreshold).count() == remainingThreshold) {
						break; //use the current configuration, which has already been set up for OBJECT
					}
				}
			//if we didn't hit the redirect count threshold, we can fall through and optimize using routing rules
			case ROUTING_RULE:
				{
					//reconfigure all redirect objects to be routing rules by moving them to the routing rule set
					final Set<S3ArtifactRedirectDeployObject> routingRuleRedirectObjects = getRoutingRuleRedirectObjects();
					final Iterator<S3DeployObject> deployObjectIterator = getDeployObjectsByKey().values().iterator();
					while(deployObjectIterator.hasNext()) {
						final S3DeployObject deployObject = deployObjectIterator.next();
						if(deployObject instanceof S3ArtifactRedirectDeployObject redirectDeployObject) {
							routingRuleRedirectObjects.add(redirectDeployObject);
							deployObjectIterator.remove();
						}
					}
				}
				break;
			default:
				throw new AssertionError("Unrecognized redirect means `%s`.".formatted(redirectMeans));
		}
	}

	/// {@inheritDoc}
	/// @implSpec This version additionally stores S3 redirect deploy objects for any alt locations as if [RedirectMeans#OBJECT] were specified: those
	///           redirects that require routing rules are stored in [#getRoutingRuleRedirectObjects()], and all others are stored in
	///           [#getDeployObjectsByKey()].
	/// @see Artifact#PROPERTY_TAG_MUMMY_ALT_LOCATION
	@Override
	protected void planResource(final MummyContext context, final URI rootTargetPathUri, final Artifact artifact,
			final UriPath resourceReference, final Path contentFile, final String key) throws IOException {
		super.planResource(context, rootTargetPathUri, artifact, resourceReference, contentFile, key);
		artifact.getResourceDescription().findPropertyValue(PROPERTY_TAG_MUMMY_ALT_LOCATION).filter(CharSequence.class::isInstance).map(Object::toString)
				.map(UriPath::parse).map(altLocationReference -> resolve(artifact.getTargetPath().toUri(), altLocationReference)) //convert to absolute file system URI
				.map(altLocationUri -> UriPath.relativize(rootTargetPathUri, altLocationUri)) //relativize to the site root
				.ifPresent(throwingConsumer(altLocationReference -> {
					if(!altLocationReference.isSubPath()) {
						throw new IOException("Artifact for resource %s specifies an alternative location %s which is outside the site boundaries.".formatted(
								resourceReference, altLocationReference));
					}
					final String altKey = URIs.decode(altLocationReference.toString()); // canonical resource name, not URI-encoded
					final String artifactKey = URIs.decode(resourceReference.toString()); // canonical resource name, not URI-encoded
					getLogger().debug("Planning deployment redirect for artifact {} from S3 key `{}` to S3 key `{}`.", artifact, altKey, artifactKey);
					final S3ArtifactRedirectDeployObject redirectDeployObject = new S3ArtifactRedirectDeployObject(altKey, artifactKey, artifact);
					if(redirectDeployObject.isRoutingRuleRequired()) { //set up the redirect initialize as if `object` redirect means were specified
						getRoutingRuleRedirectObjects().add(redirectDeployObject);
					} else {
						getDeployObjectsByKey().put(altKey, redirectDeployObject);
					}
				}));
	}

	/// {@inheritDoc}
	/// @implSpec This version adds an object redirect as appropriate for redirect deploy objects.
	/// @see S3ArtifactRedirectDeployObject
	/// @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/how-to-page-redirect.html">AWS S3 configuring a webpage redirect</a>
	@Override
	protected PutObjectRequest.Builder preparePutObject(final MummyContext context, final S3DeployObject deployObject) {
		final PutObjectRequest.Builder builder = super.preparePutObject(context, deployObject);
		if(deployObject instanceof S3ArtifactRedirectDeployObject redirectDeployObject) {
			//The redirect path must be absolute, and the must be encoded, presumably because this is the literal HTTP `Location` header content.
			//See https://tools.ietf.org/html/rfc7231#section-7.1.2 .
			builder.websiteRedirectLocation(UriPath.encode(ROOT_PATH + redirectDeployObject.getRedirectTargetKey()));
		}
		return builder;
	}

	/// {@inheritDoc}
	/// @implSpec This version returns redirect information for redirect deploy objects.
	/// @see S3ArtifactRedirectDeployObject
	@Override
	protected Optional<String> findDetailLabel(final S3DeployObject deployObject) {
		if(deployObject instanceof S3ArtifactRedirectDeployObject redirectDeployObject) {
			return Optional.of("redirect: `%s`".formatted(redirectDeployObject.getRedirectTargetKey())); //redirect: `foo/bar`
		}
		return super.findDetailLabel(deployObject);
	}

}
