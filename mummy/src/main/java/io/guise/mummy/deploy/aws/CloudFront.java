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

import static com.globalmentor.collections.iterables.Iterables.*;
import static com.globalmentor.net.HTTP.*;
import static com.globalmentor.net.URIs.*;
import static io.guise.mummy.deploy.aws.AWS.*;
import static java.util.Objects.*;
import static java.util.stream.Collectors.*;
import static org.zalando.fauxpas.FauxPas.*;

import java.io.IOException;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.annotation.*;

import org.slf4j.Logger;

import com.globalmentor.collections.Sets;
import com.globalmentor.net.*;

import io.clogr.Clogged;
import io.confound.config.*;
import io.guise.mummy.*;
import io.guise.mummy.deploy.*;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.acm.*;
import software.amazon.awssdk.services.acm.model.*;
import software.amazon.awssdk.services.cloudfront.*;
import software.amazon.awssdk.services.cloudfront.model.*;

/**
 * Sets up a <a href="https://aws.amazon.com/cloudfront/">CloudFront</a> distribution for the site.
 * @implSpec This implementation requires an {@link S3} deployment to be specified in the configuration before this deployment. The S3 bucket and aliases (which
 *           may or may not have been originally determined from the site domain and aliases) will be used as the certificate domain name and alternative names,
 *           respectively. If there is an existing certificate indicating the main S3 bucket, it will be used. This implementation does not support multiple
 *           certificates to be specified for the same S3 bucket; all but one must be removed.
 * @implSpec To use this implementation one should specify a {@link Dns} in the configuration for requesting certificates and for updating the DNS after the
 *           CloudFront distribution is in place.
 * @implSpec To use this implementation one should specify a {@link Dns} of type {@link Route53} in the configuration in order to create an alias to the
 *           CloudFront distribution.
 * @author Garret Wilson
 */
public class CloudFront implements DeployTarget, Clogged {

	/**
	 * The region to use with ACM to work with CloudFront.
	 * @see <a href="https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/cnames-and-https-requirements.html#https-requirements-aws-region">AWS
	 *      Region that You Request a Certificate In (for AWS Certificate Manager)</a>
	 * @see <a href="https://aws.amazon.com/certificate-manager/faqs/">AWS Certificate Manager FAQs</a>
	 */
	public static final Region ACM_REGION = Region.US_EAST_1;

	private final String profile;

	/** @return The AWS profile if one was set explicitly. */
	public final Optional<String> getProfile() {
		return Optional.of(profile);
	}

	private final AcmClient acmClient;

	/** @return The client for connecting to ACM. */
	protected AcmClient getAcmClient() {
		return acmClient;
	}

	private final CloudFrontClient cloudFrontClient;

	/** @return The client for connecting to CloudFront. */
	protected CloudFrontClient getCloudFrontClient() {
		return cloudFrontClient;
	}

	private String acmCertificateArn;

	/**
	 * Returns the ACM-managed certificate to use with the CloudFront distribution. The ACM certificate ARN is guaranteed to be available after successful
	 * preparation, but will not be available before this.
	 * @return The ARN of the ACM-managed SSL/TLS certificate
	 */
	public Optional<String> getAcmCertificateArn() {
		return Optional.of(acmCertificateArn);
	}

	/**
	 * Configuration constructor.
	 * <p>
	 * The site domain and aliases will be determined later.
	 * </p>
	 * @param context The context of static site generation.
	 * @param localConfiguration The local configuration for this deployment target, which may be a section of the project configuration.
	 * @see AWS#CONFIG_KEY_DEPLOY_AWS_PROFILE
	 */
	public CloudFront(@Nonnull final MummyContext context, @Nonnull final Configuration localConfiguration) {
		this(context.getConfiguration().findString(CONFIG_KEY_DEPLOY_AWS_PROFILE).orElse(null));
	}

	/**
	 * Constructor.
	 * @param profile The name of the AWS profile to use for retrieving credentials, or <code>null</code> if the default credential provider should be used.
	 */
	public CloudFront(@Nullable String profile) {
		this.profile = profile;
		final AcmClientBuilder acmClientBuilder = AcmClient.builder().region(ACM_REGION);
		if(profile != null) {
			acmClientBuilder.credentialsProvider(ProfileCredentialsProvider.create(profile));
		}
		acmClient = acmClientBuilder.build();
		final CloudFrontClientBuilder cloudFrontClientBuilder = CloudFrontClient.builder().region(Region.AWS_GLOBAL);
		if(profile != null) {
			cloudFrontClientBuilder.credentialsProvider(ProfileCredentialsProvider.create(profile));
		}
		cloudFrontClient = cloudFrontClientBuilder.build();
	}

	/** The cache time for the DNS record validating a certificate. */
	public static final long CERTIFICATE_VALIDATION_DNS_TTL = 300L;

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation requests a public certificate with ACM if one does not already exist.
	 */
	@Override
	public void prepare(final MummyContext context) throws IOException {
		try {
			final AcmClient acmClient = getAcmClient();
			final Logger logger = getLogger();

			final S3 s3 = context.getDeployTargets().orElseThrow(IllegalStateException::new).stream().filter(S3.class::isInstance).map(S3.class::cast).findFirst()
					.orElseThrow(() -> new ConfigurationException("CloudFront deployement currently requires an S3 target to be configured first."));
			final String domain = s3.getBucket();
			final Set<String> aliases = s3.getAltBuckets();

			//request a certificate if needed
			final Set<CertificateSummary> existingCertificateSummaries = getCertificateSummariesByDomainName(acmClient, domain);
			for(final CertificateSummary existingCertificateSummary : existingCertificateSummaries) {
				logger.debug("Certificate with ARN `{}` exists for domain name `{}`.", existingCertificateSummary.certificateArn(),
						existingCertificateSummary.domainName());
			}
			final String certificateArn;
			if(existingCertificateSummaries.isEmpty()) {
				logger.info("Requesting certificate for domain name `{}` and alternative names `{}`.", domain, aliases);
				certificateArn = acmClient
						.requestCertificate(request -> request.domainName(domain).subjectAlternativeNames(aliases).validationMethod(ValidationMethod.DNS)).certificateArn();
			} else {
				if(existingCertificateSummaries.size() > 1) {
					throw new IOException(
							String.format("Multiple certificates per domain not supported; please remove all but one certificates for domain `%s`.", domain));
				}
				certificateArn = getOnly(existingCertificateSummaries).certificateArn();
			}
			assert certificateArn != null;
			this.acmCertificateArn = certificateArn; //save the certificate ARN for other phases
			CertificateDetail certificateDetail = null;
			//keep polling for the certificate request to be recognized, from a one second to a one minute interval, backing off by doubling the interval
			for(int pollIntervalSeconds = 1; pollIntervalSeconds <= TimeUnit.MINUTES.toSeconds(1); pollIntervalSeconds *= 2) {
				final CertificateDetail verifyCertificateDetail = acmClient.describeCertificate(request -> request.certificateArn(certificateArn)).certificate();
				boolean recognized = true;
				if(verifyCertificateDetail.domainName() == null) {
					recognized = false; //as soon as we create a certificate, it doesn't indicate any domain
				} else { //even after indicating a domain, it may not yet indicate all the DNS validation records needed
					for(final DomainValidation domainValidation : verifyCertificateDetail.domainValidationOptions()) {
						if(domainValidation.validationMethod().equals(ValidationMethod.DNS) && domainValidation.resourceRecord() == null) {
							recognized = false;
							break;
						}
					}
				}
				if(recognized) {
					certificateDetail = verifyCertificateDetail;
					break;
				}
				try { //if the certificate isn't yet recognized, wait and try again
					logger.info("Waiting for requested certificate `{}` to be recognized...", certificateArn);
					TimeUnit.SECONDS.sleep(pollIntervalSeconds);
				} catch(final InterruptedException interruptedException) {
					//if interrupted while sleeping, just try again and keep backing off
				}
			}
			if(certificateDetail == null) {
				throw new IOException(String
						.format("Requested certificate `%s` was never recognized. Correct any problems, perhaps wait a while longer, and deploy again.", certificateArn));
			}

			//apparently the domain alternative names _includes_ the domain name itself
			if(!Set.copyOf(certificateDetail.subjectAlternativeNames()).equals(Sets.immutableSetOf(aliases, domain))) {
				logger.warn("Certificate `{}` alternate names `{}` do not match site domain {} and aliases `{}`.", certificateArn,
						List.copyOf(certificateDetail.subjectAlternativeNames()), domain, aliases);
			}

			//set up domain validation in the DNS
			final List<DomainValidation> domainValidations = certificateDetail.domainValidationOptions();
			final Set<String> pendingValidationDomainNames = new LinkedHashSet<>(domainValidations.size());
			for(final DomainValidation domainValidation : domainValidations) {
				switch(domainValidation.validationStatus()) {
					case SUCCESS: //already validated
						logger.debug("Certificate `{}` for domain name `{}` has been validated.", certificateArn, domainValidation.domainName());
						break;
					case PENDING_VALIDATION:
						if(domainValidation.validationMethod().equals(ValidationMethod.DNS)) {
							final ResourceRecord resourceRecord = domainValidation.resourceRecord();
							context.getDeployDns().ifPresentOrElse(throwingConsumer(dns -> {
								logger.info("Setting DNS entry (`{}`) `{} = {}` for certificate `{}` validation for domain name `{}`.", resourceRecord.typeAsString(),
										resourceRecord.name(), resourceRecord.value(), certificateArn, domainValidation.domainName());
								dns.setResourceRecord(resourceRecord.typeAsString(), DomainName.of(resourceRecord.name()), resourceRecord.value(),
										CERTIFICATE_VALIDATION_DNS_TTL);
							}), () -> logger.warn(
									"No DNS configured. To validate certificate `{}` for domain name `{}`, resource record (`{}`) `{} = {}` must be set in the DNS manually.",
									certificateArn, domainValidation.domainName(), resourceRecord.typeAsString(), resourceRecord.name(), resourceRecord.value()));
						} else {
							logger.warn("Certificate `{}` specifies an unknown validation method `{}` for domain name `{}` and must be performed manually.", certificateArn,
									domainValidation.validationMethod(), domainValidation.domainName());
						}
						//note this pending domain to report later, but continue examining the validations to add other DNS entries as needed before reporting the problem
						pendingValidationDomainNames.add(domainValidation.domainName());
						break;
					case FAILED:
						throw new IOException(String.format(
								"Certificate `%s` for domain name `%s` failed validation: `%s`. Please delete the certificate, correct any problems, and deploy again.",
								certificateArn, domainValidation.domainName(), certificateDetail.failureReason()));
					default:
						throw new IOException(String.format(
								"Unable to validate certificate `%s` for domain name `%s`; validation status indicates `%s`. Please delete the certificate and deploy again.",
								certificateArn, domainValidation.domainName(), domainValidation.validationStatus()));
				}
			}
			if(!pendingValidationDomainNames.isEmpty()) {
				throw new IOException(String.format("Certificate `%s` is still pending validation for domain names %s."
						+ " Make sure your domain is configured with the correct NS records for your DNS, wait for the certificate to finish validating,"
						+ " and initiate deployment again.", certificateArn, pendingValidationDomainNames));
			}
		} catch(final SdkException sdkException) {
			throw new IOException(sdkException);
		}
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation creates a CloudFront distribution if one does not exist, and adds appropriate records to the DNS if a DNS is available. Each
	 *           S3 bucket name is used as the name of a CloudFront distribution.
	 */
	@Override
	public Optional<URI> deploy(final MummyContext context, final Artifact rootArtifact) throws IOException {
		try {
			final CloudFrontClient cloudFrontClient = getCloudFrontClient();
			final Logger logger = getLogger();

			getProfile().ifPresent(profile -> getLogger().info("Using AWS CloudFront credentials profile `{}`.", profile));
			final S3 s3 = context.getDeployTargets().orElseThrow(IllegalStateException::new).stream().filter(S3.class::isInstance).map(S3.class::cast).findFirst()
					.orElseThrow(() -> new ConfigurationException("CloudFront deployement currently requires an S3 target to be configured first."));
			final Region s3BucketRegion = s3.getRegion();
			for(final String s3Bucket : (Iterable<String>)s3.buckets()::iterator) {

				//create a distribution if there is none for this bucket
				final Set<DistributionSummary> existingDistributionSummaries = getDistributionSummariesByAliases(cloudFrontClient, Set.of(s3Bucket));
				for(final DistributionSummary existingDistributionSummary : existingDistributionSummaries) {
					logger.debug("Distribution ID `{}` (ARN `{}`) exists for S3 bucket `{}`.", existingDistributionSummary.id(), existingDistributionSummary.arn(),
							s3Bucket);
				}
				final String distributionId;
				final DomainName distributionDomainName;
				if(existingDistributionSummaries.isEmpty()) {
					logger.info("Creating distribution for S3 bucket `{}`.", s3Bucket);
					final StringBuilder commentBuilder = new StringBuilder(); //CloudFront comments are limited to a little over 120 characters in length
					commentBuilder.append("Created by ").append(context.getMummifierIdentification()); //i18n
					commentBuilder.append(" on ").append(ZonedDateTime.now()); //TODO i18n
					commentBuilder.append("."); //TODO i18n
					final String s3BucketWebsiteEndpoint = S3.getBucketWebsiteEndpoint(s3Bucket, s3BucketRegion);
					final CustomOriginConfig s3BucketOriginConfig = CustomOriginConfig.builder().httpPort(HTTP.DEFAULT_PORT).httpsPort(HTTP.DEFAULT_SECURE_PORT)
							.originProtocolPolicy(OriginProtocolPolicy.HTTP_ONLY) //S3 buckets as website endpoints only support HTTP
							.build();
					final Origin origin = Origin.builder().id(s3Bucket).domainName(s3BucketWebsiteEndpoint).customOriginConfig(s3BucketOriginConfig).build(); //use the bucket as the ID as a convenience
					final Aliases aliases = Aliases.builder().items(s3Bucket).quantity(1).build(); //add a CNAME alias to the bucket because that's the domain for serving
					final String acmCertificateArn = getAcmCertificateArn().orElseThrow(IllegalStateException::new);
					final ViewerCertificate viewerCertificate = ViewerCertificate.builder() //use our ACM certificate and only allows browser that support SNI (as recommended)
							.acmCertificateArn(acmCertificateArn).sslSupportMethod(SSLSupportMethod.SNI_ONLY).build();
					final CookiePreference forwardCookies = CookiePreference.builder().forward(ItemSelection.NONE).build();
					final boolean forwardQueries = false;
					final DefaultCacheBehavior redirectToHttps = DefaultCacheBehavior.builder().viewerProtocolPolicy(ViewerProtocolPolicy.REDIRECT_TO_HTTPS)
							.forwardedValues(forwardedValues -> forwardedValues.queryString(forwardQueries).cookies(forwardCookies)).targetOriginId(origin.id())
							.trustedSigners(trustedSigners -> trustedSigners.enabled(false).quantity(0)).minTTL(0L).build();
					final String callerReference = UUID.randomUUID().toString(); //use a random UUID as the temporary caller reference (required)
					final DistributionConfig distributionConfig = DistributionConfig.builder().origins(origins -> origins.items(origin).quantity(1)).aliases(aliases)
							.defaultCacheBehavior(redirectToHttps).viewerCertificate(viewerCertificate).enabled(true).callerReference(callerReference)
							.comment(commentBuilder.toString()).build();
					final Distribution distribution = cloudFrontClient.createDistribution(request -> request.distributionConfig(distributionConfig)).distribution();
					distributionId = distribution.id();
					distributionDomainName = DomainName.of(distribution.domainName());
				} else {
					if(existingDistributionSummaries.size() > 1) {
						throw new IOException(String.format("Multiple distributions found with an alias for bucket `%s`; all but one must be removed.", s3Bucket));
					}
					final DistributionSummary distributionSummary = getOnly(existingDistributionSummaries);
					distributionId = distributionSummary.id();
					distributionDomainName = DomainName.of(distributionSummary.domainName());
					//TODO ensure that the existing distribution truly has the correct origin, i.e. to the S3 bucket
				}

				//add an alias record to the new distribution if we have a Route 53 DNS
				final DomainName domainName = DomainName.ROOT.resolve(DomainName.of(s3Bucket)); //the domain name is the S3 bucket (as a relative domain) resolved to `.`
				context.getDeployDns().filter(Route53.class::isInstance).map(Route53.class::cast).ifPresentOrElse(throwingConsumer(route53 -> {
					logger.info("Setting DNS alias `A` record for domain name `{}` to CloudFront distribution `{}` domain `{}`.", domainName, distributionId,
							distributionDomainName);
					route53.setAliasResourceRecord(Dns.ResourceRecordType.A, domainName, distributionDomainName.toString(), Route53.CLOUDFRONT_ALIAS_HOSTED_ZONE_ID);
				}), () -> logger.warn(
						"No Route 53 DNS configured; an alias for domain name `{}` to CloudFront distribution `{}` domain `{}` must be set in the Route 53 manually if desired.",
						domainName, distributionId, distributionDomainName));
			}

			return Optional.of(createURI(HTTPS_URI_SCHEME, null, s3.getBucket(), -1, URIs.ROOT_PATH, null, null)); //https://s3-bucket/
		} catch(final SdkException sdkException) {
			throw new IOException(sdkException);
		}
	}

	//# ACM utility methods; could be removed to separate library

	//## certificates

	/**
	 * Retrieves summaries of all certificates for a given domain name.
	 * @implSpec This implementation delegates to {@link #certificateSummaries(AcmClient)}.
	 * @param client The client to use for retrieving the certificate summaries.
	 * @param domainName The main domain name of the certificate.
	 * @return A set of summaries all certificates for the given domain name.
	 * @throws SdkException if an error occurs related to AWS.
	 * @see CertificateSummary#domainName()
	 */
	protected static Set<CertificateSummary> getCertificateSummariesByDomainName(@Nonnull final AcmClient client, @Nonnull final String domainName)
			throws SdkException {
		requireNonNull(domainName);
		try (final Stream<CertificateSummary> certificateSummariesByDomainName = certificateSummaries(client)
				.filter(certificate -> certificate.domainName().equals(domainName))) {
			return certificateSummariesByDomainName.collect(toSet());
		}
	}

	/**
	 * Retrieves a stream of summaries of all certificates.
	 * @param client The client to use for retrieving the certificate summaries.
	 * @return A stream of summaries of all certificate.
	 * @throws SdkException if an error occurs related to AWS.
	 */
	protected static Stream<CertificateSummary> certificateSummaries(@Nonnull final AcmClient client) throws SdkException {
		return client.listCertificatesPaginator().stream().flatMap(response -> response.certificateSummaryList().stream());
	}

	//# CloudFront utility methods; could be removed to separate library

	//## distributions

	/**
	 * Retrieves summaries of all distributions with the given aliases.
	 * @implSpec This implementation delegates to {@link #distributionSummaries(CloudFrontClient)}.
	 * @param client The client to use for retrieving the distribution summaries.
	 * @param aliases The aliases for which distributions should be returned.
	 * @return A set of summaries all distributions with the exact aliases, in any order
	 * @throws SdkException if an error occurs related to AWS.
	 * @see DistributionSummary#aliases()
	 * @see Aliases
	 */
	protected static Set<DistributionSummary> getDistributionSummariesByAliases(@Nonnull final CloudFrontClient client, @Nonnull final Set<String> aliases)
			throws SdkException {
		requireNonNull(aliases);
		try (final Stream<DistributionSummary> distributionSummariesByAliases = distributionSummaries(client)
				.filter(distribution -> Set.copyOf(distribution.aliases().items()).equals(aliases))) {
			return distributionSummariesByAliases.collect(toSet());
		}
	}

	/**
	 * Retrieves a stream of summaries of all distributions.
	 * @implNote The AWS SDK for CloudFront currently has no streaming methods, so this implementation collects all distribution summaries and returns a stream
	 *           for consistency with other methods. In the future this could be made more efficient with an iterator.
	 * @param client The client to use for retrieving the distribution summaries.
	 * @return A stream of summaries of all distributions.
	 * @throws SdkException if an error occurs related to AWS.
	 */
	protected static Stream<DistributionSummary> distributionSummaries(@Nonnull final CloudFrontClient client) throws SdkException {
		final List<DistributionSummary> distributionSummaries = new ArrayList<>();
		ListDistributionsRequest request = ListDistributionsRequest.builder().build();
		ListDistributionsResponse response;
		DistributionList distributionList;
		do {
			response = client.listDistributions(request);
			distributionList = response.distributionList();
			distributionSummaries.addAll(distributionList.items());
			request = ListDistributionsRequest.builder().marker(distributionList.nextMarker()).build();
		} while(distributionList.isTruncated());
		return distributionSummaries.stream();
	}

}
