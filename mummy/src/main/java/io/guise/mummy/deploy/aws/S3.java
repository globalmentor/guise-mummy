/*
 * Copyright © 2019 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.mummy.deploy.aws;

import static com.globalmentor.java.Conditions.*;
import static com.globalmentor.net.HTTP.*;
import static com.globalmentor.net.URIs.*;
import static io.guise.mummy.Artifact.*;
import static io.guise.mummy.GuiseMummy.*;
import static io.guise.mummy.deploy.aws.AWS.*;
import static java.util.Collections.*;
import static java.util.Objects.*;
import static java.util.stream.Collectors.*;
import static java.util.stream.Stream.*;
import static org.zalando.fauxpas.FauxPas.*;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Stream;

import javax.annotation.*;

import com.globalmentor.collections.*;
import com.globalmentor.net.*;
import com.globalmentor.text.StringTemplate;

import io.clogr.Clogged;
import io.confound.config.Configuration;
import io.guise.mummy.*;
import io.guise.mummy.deploy.DeployTarget;
import io.urf.vocab.content.Content;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.*;
import software.amazon.awssdk.services.s3.*;
import software.amazon.awssdk.services.s3.model.*;

/**
 * Deploys a site to <a href="https://aws.amazon.com/s3/">AWS S3</a>.
 * @author Garret Wilson
 */
public class S3 implements DeployTarget, Clogged {

	/** The section relative key for the indication of bucket region in the configuration. */
	public static final String CONFIG_KEY_REGION = "region";
	/** The section relative key for the bucket name in the configuration; defaults to {@link GuiseMummy#CONFIG_KEY_SITE_DOMAIN} in the global configuration. */
	public static final String CONFIG_KEY_BUCKET = "bucket";
	/**
	 * The section relative key for the alternative buckets in the configuration; defaults to {@link GuiseMummy#CONFIG_KEY_SITE_ALT_DOMAINS} in the global
	 * configuration.
	 */
	public static final String CONFIG_KEY_ALT_BUCKETS = "altBuckets";

	/**
	 * The regions that use a dash (<code>-</code>) instead of a dot (<code>.</code>) to separate <code>s3-website</code> from the region in the endpoint domain
	 * string.
	 */
	private static final Set<Region> WEBSITE_ENDPOINT_DASH_REGIONS = Set.of(Region.US_EAST_1, Region.US_WEST_1, Region.US_WEST_2, Region.AP_SOUTHEAST_1,
			Region.AP_SOUTHEAST_2, Region.AP_NORTHEAST_1, Region.EU_WEST_1, Region.SA_EAST_1);

	private static final String ENDPOINT_S3_WEBSITE_REGION_DELIMITER_DASH = String.valueOf('-');
	private static final String ENDPOINT_S3_WEBSITE_REGION_DELIMITER_DOT = String.valueOf('.');

	/**
	 * String template for a bucket web site URL, with the following string parameters:
	 * <ol>
	 * <li>bucket name</li>
	 * <li>S3 website region delimiter: dot (<code>.</code>) or dash (<code>-</code>)</li>
	 * <li>region ID</li>
	 * <li>region domain</li>
	 * <li>
	 * </ol>
	 * @implNote The <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/WebsiteEndpoints.html">Website Endpoints</a> documentation indicates that there are
	 *           two forms based upon region, one using a dot (<code>.</code>) character and the other using a dash (<code>-</code>) character, but the dot form
	 *           seems to work as well, as some have <a href=
	 *           "https://stackoverflow.com/questions/46627060/how-to-resolve-aws-s3-url-says-west-bucket-says-east#comment80219601_46627148">indicated</a> that
	 *           AWS may be standardizing on the dot form. Nevertheless this implementation distinguishes between the two forms to provide the most correct values
	 *           as per the documentation.
	 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/WebsiteEndpoints.html">Website Endpoints</a>
	 * @see <a href="https://docs.aws.amazon.com/general/latest/gr/rande.html#s3_website_region_endpoints">Amazon Simple Storage Service Website Endpoints</a>
	 * @see <a href="https://stackoverflow.com/q/57480708/421049">Get AWS S3 bucket web static site URL programmatically using Java SDK v2</a>
	 */
	private final static StringTemplate BUCKET_WEBSITE_ENDPOINT_TEMPLATE = StringTemplate.builder().parameter(StringTemplate.STRING_PARAMETER).text(".s3-website")
			.parameter(StringTemplate.STRING_PARAMETER).parameter(StringTemplate.STRING_PARAMETER).text(".").parameter(StringTemplate.STRING_PARAMETER).build();

	/**
	 * Returns the endpoint domain for static web site hosting for the given bucket in the indicated region.
	 * @param bucket The name of the bucket.
	 * @param region The region in which the bucket is located.
	 * @return A endpoint domain for accessing the static web site hosted in the identified bucket.
	 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/WebsiteEndpoints.html">Website Endpoints</a>
	 * @see <a href="https://docs.aws.amazon.com/general/latest/gr/rande.html#s3_website_region_endpoints">Amazon Simple Storage Service Website Endpoints</a>
	 * @see PartitionMetadata#hostname()
	 */
	public static String getBucketWebsiteEndpoint(@Nonnull final String bucket, @Nonnull final Region region) {
		final String s3WebsiteRegionDelimiter = WEBSITE_ENDPOINT_DASH_REGIONS.contains(region) ? ENDPOINT_S3_WEBSITE_REGION_DELIMITER_DASH
				: ENDPOINT_S3_WEBSITE_REGION_DELIMITER_DOT;
		return BUCKET_WEBSITE_ENDPOINT_TEMPLATE.apply(bucket, s3WebsiteRegionDelimiter, region.id(), region.metadata().domain());
	}

	/**
	 * Returns the URL for static web site hosting for the given bucket in the indicated region.
	 * @param bucket The name of the bucket.
	 * @param region The region in which the bucket is located.
	 * @return A URL for accessing the static web site hosted in the identified bucket.
	 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/WebsiteEndpoints.html">Website Endpoints</a>
	 * @see <a href="https://docs.aws.amazon.com/general/latest/gr/rande.html#s3_website_region_endpoints">Amazon Simple Storage Service Website Endpoints</a>
	 * @see PartitionMetadata#hostname()
	 */
	public static URI getBucketWebsiteUrl(@Nonnull final String bucket, @Nonnull final Region region) {
		return createURI(HTTP_URI_SCHEME, null, getBucketWebsiteEndpoint(bucket, region), -1, URIs.ROOT_PATH, null, null);
	}

	/**
	 * The policy template for setting a bucket to public read access. There is one parameter:
	 * <ol>
	 * <li>bucket</li>
	 * </ol>
	 */
	protected static final StringTemplate POLICY_TEMPLATE_PUBLIC_READ_GET_OBJECT = StringTemplate.builder().text( //@formatter:off
			"{" + 
			"	\"Version\": \"2012-10-17\"," + 
			"	\"Statement\": [{" + 
			"		\"Sid\": \"PublicReadGetObject\"," + 
			"		\"Effect\": \"Allow\"," + 
			"		\"Principal\": \"*\"," + 
			"		\"Action\": [\"s3:GetObject\"]," + 
			"		\"Resource\": [\"arn:aws:s3:::").parameter(StringTemplate.STRING_PARAMETER).text("/*\"]" + 
			"	}]" + 
			"}").build();	//@formatter:on

	private final String profile;

	/** @return The AWS profile if one was set explicitly. */
	public final Optional<String> getProfile() {
		return Optional.ofNullable(profile);
	}

	private final Region region;

	/** @return The specified region for deployment. */
	public Region getRegion() {
		return region;
	}

	private final String bucket;

	/** @return The destination S3 bucket for deployment. */
	public String getBucket() {
		return bucket;
	}

	private final Set<String> altBuckets;

	/** @return The S3 buckets, if any, to serve as alternatives and redirect to the primary bucket. */
	public Set<String> getAltBuckets() {
		return altBuckets;
	}

	/**
	 * @return A stream of all bucket names, starting with the primary bucket {@link #getBucket()}, followed by the alternatives {@link #getAltBuckets()} in no
	 *         guaranteed order.
	 */
	public Stream<String> buckets() {
		return concat(Stream.of(getBucket()), getAltBuckets().stream());
	}

	private final S3Client s3Client;

	/** @return The client for connecting to S3. */
	protected S3Client getS3Client() {
		return s3Client;
	}

	/** The map of S3 object keys for each artifact. */
	private final ReverseMap<Artifact, String> artifactKeys = new DecoratorReverseMap<>(new LinkedHashMap<>(), new HashMap<>());

	/** The map of alternative (redirect) keys for artifacts that have redirects. */
	private final Map<Artifact, String> artifactAltKeys = new LinkedHashMap<>();

	/**
	 * Configuration constructor.
	 * <p>
	 * The region is retrieved from {@value #CONFIG_KEY_REGION} in the local configuration. The bucket name is retrieved from {@value #CONFIG_KEY_BUCKET} in the
	 * local configuration, falling back to {@value GuiseMummy#CONFIG_KEY_SITE_DOMAIN} in the context configuration if not specified. The bucket alternatives are
	 * retrieved from {@value #CONFIG_KEY_ALT_BUCKETS}, falling back to {@value GuiseMummy#CONFIG_KEY_SITE_ALT_DOMAINS} in the context configuration if not
	 * specified.
	 * </p>
	 * @param context The context of static site generation.
	 * @param localConfiguration The local configuration for this deployment target, which may be a section of the project configuration.
	 * @see AWS#CONFIG_KEY_DEPLOY_AWS_PROFILE
	 * @see #CONFIG_KEY_REGION
	 * @see #CONFIG_KEY_BUCKET
	 * @see #CONFIG_KEY_ALT_BUCKETS
	 * @see GuiseMummy#CONFIG_KEY_SITE_DOMAIN
	 * @see GuiseMummy#CONFIG_KEY_SITE_ALT_DOMAINS
	 */
	public S3(@Nonnull final MummyContext context, @Nonnull final Configuration localConfiguration) {
		this(context.getConfiguration().findString(CONFIG_KEY_DEPLOY_AWS_PROFILE).orElse(null), Region.of(localConfiguration.getString(CONFIG_KEY_REGION)),
				localConfiguration.findString(CONFIG_KEY_BUCKET).orElseGet(() -> context.getConfiguration().getString(CONFIG_KEY_SITE_DOMAIN)),
				localConfiguration.findCollection(CONFIG_KEY_ALT_BUCKETS, String.class)
						.or(() -> context.getConfiguration().findCollection(CONFIG_KEY_SITE_ALT_DOMAINS, String.class)).orElse(emptyList()));
	}

	/**
	 * Region, bucket, and alternative buckets constructor. The alternative buckets will be stored as a set.
	 * @param profile The name of the AWS profile to use for retrieving credentials, or <code>null</code> if the default credential provider should be used.
	 * @param region The AWS region of deployment.
	 * @param bucket The bucket into which the site should be deployed.
	 * @param altBuckets The bucket alternatives, if any, to redirect to the primary bucket.
	 */
	public S3(@Nullable String profile, @Nonnull final Region region, @Nonnull String bucket, @Nonnull final Collection<String> altBuckets) {
		this.profile = profile;
		this.region = requireNonNull(region);
		this.bucket = requireNonNull(bucket);
		this.altBuckets = new LinkedHashSet<>(altBuckets); //maintain order to help with reporting and debugging
		final S3ClientBuilder s3ClientBuilder = S3Client.builder().region(region);
		if(profile != null) {
			s3ClientBuilder.credentialsProvider(ProfileCredentialsProvider.create(profile));
		}
		s3Client = s3ClientBuilder.build();
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation creates and configures the specified buckets as needed.
	 */
	@Override
	public void prepare(final MummyContext context) throws IOException {
		try {
			final S3Client s3Client = getS3Client();

			//prepare bucket
			final String bucket = getBucket();
			final Region region = getRegion();
			getProfile().ifPresent(profile -> getLogger().info("Using AWS S3 credentials profile `{}`.", profile));
			getLogger().info("Preparing AWS S3 bucket `{}` in region `{}` for deployment.", bucket, region);
			final boolean bucketExists = bucketExists(bucket);
			getLogger().debug("Bucket `{}` exists? {}.", bucket, bucketExists);
			final boolean bucketHasPolicy;
			if(bucketExists) { //if the bucket exists, see if it has a policy
				bucketHasPolicy = bucketHasPolicy(bucket);
			} else { //create the bucket if it doesn't exist
				getLogger().info("Creating S3 bucket `{}` in AWS region `{}`.", bucket, region);
				s3Client.createBucket(request -> request.bucket(bucket).createBucketConfiguration(config -> config.locationConstraint(region.id())));
				bucketHasPolicy = false; //the bucket doesn't have a policy yet, as we just created it
			}

			//set bucket policy
			if(!bucketHasPolicy) { //if the bucket doesn't already have a policy (leave any existing policy alone)
				getLogger().info("Setting policy of S3 bucket `{}` to public.", bucket);
				s3Client.putBucketPolicy(request -> request.bucket(bucket).policy(POLICY_TEMPLATE_PUBLIC_READ_GET_OBJECT.apply(bucket)));
			}

			//prepare alternative buckets
			for(final String altBucket : getAltBuckets()) {
				getLogger().info("Preparing AWS S3 bucket `{}` in region `{}` to serve as an alternative.", altBucket, region);
				final boolean altBucketExists = bucketExists(altBucket);
				getLogger().debug("Alternative bucket `{}` exists? {}.", altBucket, altBucketExists);
				if(!altBucketExists) { //create the bucket if it doesn't exist
					getLogger().info("Creating S3 alternative bucket `{}` in AWS region `{}`.", altBucket, region);
					s3Client.createBucket(request -> request.bucket(altBucket).createBucketConfiguration(config -> config.locationConstraint(region.id())));
				}

			}
		} catch(final SdkException sdkException) {
			throw new IOException(sdkException);
		}
	}

	@Override
	public Optional<URI> deploy(@Nonnull final MummyContext context, @Nonnull Artifact rootArtifact) throws IOException {
		getLogger().info("Deploying to AWS region `{}` S3 bucket `{}`.", getRegion(), bucket);

		//#plan
		plan(context, rootArtifact);

		//#put
		put(context);

		//#prune
		prune(context);

		//configure bucket for web site with appropriate redirects
		try {
			getLogger().info("Configuring S3 bucket `{}` for web site access.", bucket);
			//TODO use some separate configuration access for the "content name"
			final boolean isNameBare = context.getConfiguration().findBoolean(CONFIG_KEY_MUMMY_PAGE_NAMES_BARE).orElse(false);
			final String indexDocumentSuffix = isNameBare ? "index" : "index.html"; //TODO get from configuration
			final IndexDocument indexDocument = IndexDocument.builder().suffix(indexDocumentSuffix).build();
			final Set<RoutingRule> routingRules = artifactAltKeys.entrySet().stream().map(artifactAltKeyEntry -> {
				final Artifact artifact = artifactAltKeyEntry.getKey();
				final String altKey = artifactAltKeyEntry.getValue();
				final String artifactKey = artifactKeys.get(artifact);
				checkState(artifactKey != null, "An S3 object key should have been determined for artifact %s during planning.", artifact);
				return RoutingRule.builder().condition(condition -> condition.keyPrefixEquals(altKey)).redirect(redirect -> {
					//If the artifact (e.g. a directory) can contain other artifacts, we'll replace the entire key prefix
					//to allow redirects for contained artifacts as well. Otherwise replace the entire key in case an artifact
					//key matches the prefix of another non-redirected path (not expected to occur frequently in practice).
					//(This applies to a collection `foo/` redirecting to a non-collection `bar` as well.) 
					if(artifact instanceof CollectionArtifact) {
						redirect.replaceKeyPrefixWith(artifactKey);
					} else {
						redirect.replaceKeyWith(artifactKey);
					}
				}).build();
			}).collect(toCollection(LinkedHashSet::new)); //maintain order to help with debugging
			final WebsiteConfiguration.Builder websiteConfigurationBuilder = WebsiteConfiguration.builder();
			websiteConfigurationBuilder.indexDocument(indexDocument);
			if(!routingRules.isEmpty()) {
				websiteConfigurationBuilder.routingRules(routingRules.toArray(RoutingRule[]::new));
			}
			s3Client.putBucketWebsite(request -> request.bucket(bucket).websiteConfiguration(websiteConfigurationBuilder.build()));

			//configure alternative buckets for web sites
			for(final String altBucket : getAltBuckets()) {
				getLogger().info("Configuring S3 alternative bucket `{}` for web site redirection.", altBucket);
				s3Client.putBucketWebsite(
						builder -> builder.bucket(altBucket).websiteConfiguration(config -> config.redirectAllRequestsTo(redirect -> redirect.hostName(bucket))));
			}
		} catch(final SdkException sdkException) {
			throw new IOException(sdkException);
		}

		return Optional.of(getBucketWebsiteUrl(getBucket(), getRegion()));
	}

	/**
	 * Plans deployment of a site.
	 * @param context The context of static site generation.
	 * @param rootArtifact The root artifact of the site being deployed.
	 * @throws IOException if there is an I/O error during site deployment planning.
	 */
	protected void plan(@Nonnull final MummyContext context, @Nonnull Artifact rootArtifact) throws IOException {
		plan(context, rootArtifact, rootArtifact);
	}

	/**
	 * Plans deployment of a site for an artifact and its comprised artifacts.
	 * @param context The context of static site generation.
	 * @param rootArtifact The root artifact of the site being deployed.
	 * @param artifact The current artifact for which deployment is being planned.
	 * @throws IOException if there is an I/O error during site deployment planning.
	 */
	protected void plan(@Nonnull final MummyContext context, @Nonnull Artifact rootArtifact, @Nonnull Artifact artifact) throws IOException {
		try {
			final URIPath resourceReference = context.relativizeTargetReference(rootArtifact, artifact);
			final String key = resourceReference.toString();
			getLogger().debug("Planning deployment for artifact {}, S3 key `{}`.", artifact, key);
			artifactKeys.put(artifact, key);
			artifact.getResourceDescription().findPropertyValue(PROPERTY_TAG_MUMMY_ALT_LOCATION).filter(CharSequence.class::isInstance).map(Object::toString)
					.map(URIPath::of).map(altLocationReference -> resolve(artifact.getTargetPath().toUri(), altLocationReference)) //convert to absolute file system URI
					.map(altLocationUri -> URIPath.relativize(rootArtifact.getTargetPath().toUri(), altLocationUri)) //relativize to the site root
					.ifPresent(throwingConsumer(altLocationReference -> {
						if(!altLocationReference.isSubPath()) {
							throw new IOException(String.format("Artifact for resource %s specifies an alternative location %s which is outside the site boundaries.",
									resourceReference, altLocationReference));
						}
						final String altKey = altLocationReference.toString();
						getLogger().debug("Planning deployment redirect for artifact {} from S3 key `{}` to S3 key `{}`.", artifact, altKey, key);
						artifactAltKeys.put(artifact, altKey);
					}));
			if(artifact instanceof CompositeArtifact) {
				for(final Artifact comprisedArtifact : (Iterable<Artifact>)((CompositeArtifact)artifact).comprisedArtifacts()::iterator) {
					plan(context, rootArtifact, comprisedArtifact);
				}
			}
		} catch(final SdkException sdkException) {
			throw new IOException(sdkException);
		}
	}

	/**
	 * Transfers content for deployment.
	 * @implSpec This implementation skips directories.
	 * @apiNote This is the main deployment method, which actually deploys content.
	 * @param context The context of static site generation.
	 * @throws IOException if there is an I/O error during putting.
	 */
	protected void put(@Nonnull final MummyContext context) throws IOException {
		try {
			final S3Client s3Client = getS3Client();
			final String bucket = getBucket();
			for(final Map.Entry<Artifact, String> artifactKeyEntry : artifactKeys.entrySet()) {
				final Artifact artifact = artifactKeyEntry.getKey();
				if(!(artifact instanceof DirectoryArtifact)) { //don't deploy anything for directories TODO improve semantics, especially after the root artifact type changes; maybe use CollectionArtifact
					final String key = artifactKeyEntry.getValue();
					getLogger().info("Deploying artifact to S3 key `{}`.", key);
					final PutObjectRequest.Builder putBuilder = PutObjectRequest.builder().bucket(bucket).key(key);
					//set content-type if found
					Content.findContentType(artifact.getResourceDescription()).map(ContentType::toString).ifPresent(putBuilder::contentType);
					s3Client.putObject(putBuilder.build(), RequestBody.fromFile(artifact.getTargetPath()));
				}
			}
		} catch(final SdkException sdkException) {
			throw new IOException(sdkException);
		}
	}

	/**
	 * Prunes any objects that don't exist in the site.
	 * @apiNote This process can occur even when actual putting is being performed concurrently, as existing objects that are in the site are left undisturbed.
	 *          There is no need to determine if the existing object is out of date, as it will be replaced if it hasn't been already. Only files no longer in the
	 *          site are removed.
	 * @param context The context of static site generation.
	 * @throws IOException if there is an I/O error during pruning.
	 */
	protected void prune(@Nonnull final MummyContext context) throws IOException {
		try {
			final S3Client s3Client = getS3Client();
			final String bucket = getBucket();
			ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder().bucket(bucket).build();
			ListObjectsV2Response listObjectsResponse;
			do {
				listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);
				for(final S3Object s3Object : listObjectsResponse.contents()) {
					final String key = s3Object.key();
					if(!artifactKeys.containsValue(key)) { //if this object isn't in our site, delete it
						getLogger().info("Pruning S3 object `{}`.", key);
						s3Client.deleteObject(builder -> builder.bucket(bucket).key(key));
					}
				}
				listObjectsRequest = ListObjectsV2Request.builder().bucket(bucket).continuationToken(listObjectsResponse.nextContinuationToken()).build();
			} while(listObjectsResponse.isTruncated());
		} catch(final SdkException sdkException) {
			throw new IOException(sdkException);
		}
	}

	//# S3 utility methods; could be removed to separate library

	//## hosted zones

	/**
	 * Determines whether the bucket exists.
	 * @param bucket The bucket to check.
	 * @return <code>true</code> if the bucket exists; otherwise <code>false</code>.
	 * @throws SdkException if some error occurred, such as insufficient permissions.
	 */
	protected boolean bucketExists(@Nonnull final String bucket) throws SdkException {
		try {
			getS3Client().headBucket(builder -> builder.bucket(bucket));
			return true;
		} catch(final NoSuchBucketException noSuchBucketException) {
			return false;
		}
	}

	/**
	 * Determines whether a bucket has a policy.
	 * @param bucket The bucket to check.
	 * @return <code>true</code> if the bucket has a policy; otherwise <code>false</code>.
	 * @throws SdkException if some error occurred, such as insufficient permissions.
	 */
	protected boolean bucketHasPolicy(@Nonnull final String bucket) throws SdkException {
		try {
			getS3Client().getBucketPolicy(builder -> builder.bucket(bucket));
			return true;
		} catch(final S3Exception s3Exception) {
			if(s3Exception.statusCode() == HTTP.SC_NOT_FOUND) {
				return false;
			}
			throw s3Exception;
		}
	}

	/**
	 * Determines whether a bucket has a web site configuration.
	 * @param bucket The bucket to check.
	 * @return <code>true</code> if the bucket has a web site configuration; otherwise <code>false</code>.
	 * @throws SdkException if some error occurred, such as insufficient permissions.
	 */
	protected boolean bucketHasWebsiteConfiguration(@Nonnull final String bucket) throws SdkException {
		try {
			getS3Client().getBucketWebsite(builder -> builder.bucket(bucket));
			return true;
		} catch(final S3Exception s3Exception) {
			if(s3Exception.statusCode() == HTTP.SC_NOT_FOUND) {
				return false;
			}
			throw s3Exception;
		}
	}

}
