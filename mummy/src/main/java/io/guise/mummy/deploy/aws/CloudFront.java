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
import static io.guise.mummy.GuiseMummy.*;
import static java.util.Collections.*;
import static java.util.Objects.*;
import static java.util.stream.Collectors.*;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Stream;

import javax.annotation.*;

import org.slf4j.Logger;

import io.clogr.Clogged;
import io.confound.config.Configuration;
import io.guise.mummy.*;
import io.guise.mummy.deploy.DeployTarget;
import io.guise.mummy.deploy.Dns;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.acm.AcmClient;
import software.amazon.awssdk.services.acm.model.*;

/**
 * Sets up a <a href="https://aws.amazon.com/cloudfront/">CloudFront</a> distribution for the site.
 * @implSpec This implementation requires an {@link S3} deployment to be specified in the configuration before this deployment.
 * @implSpec This implementation expects an existing certificate, if any, to indicate the main site domain as specified in
 *           {@value GuiseMummy#CONFIG_KEY_SITE_DOMAIN} as the primary domain; otherwise a new certificate will be requested. This implementation does not
 *           support multiple certificates to be specified for the same site domain; all but one must be removed.
 * @implSpec This implementation requires a {@link Dns} to be specified in the configuration if a certificate needs to be requested.
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

	private final String domain;

	/** @return The domain for CloudFront distribution. */
	public String getDomain() {
		return domain;
	}

	private final Set<String> aliases;

	/** @return The domain aliases, if any. */
	public Set<String> getAliases() {
		return aliases;
	}

	private final AcmClient acmClient;

	/** @return The client for connecting to ACM. */
	protected AcmClient getAcmClient() {
		return acmClient;
	}

	/**
	 * Configuration constructor.
	 * <p>
	 * The site domain is retrieved from {@value GuiseMummy#CONFIG_KEY_SITE_DOMAIN} and the site domain aliases from {@value GuiseMummy#CONFIG_KEY_SITE_ALIASES}
	 * in the context configuration.
	 * </p>
	 * @param context The context of static site generation.
	 * @param localConfiguration The local configuration for this deployment target, which may be a section of the project configuration.
	 * @see GuiseMummy#CONFIG_KEY_SITE_DOMAIN
	 * @see GuiseMummy#CONFIG_KEY_SITE_ALIASES
	 */
	public CloudFront(@Nonnull final MummyContext context, @Nonnull final Configuration localConfiguration) {
		this(context.getConfiguration().getString(CONFIG_KEY_SITE_DOMAIN),
				context.getConfiguration().findCollection(CONFIG_KEY_SITE_ALIASES, String.class).orElse(emptyList()));
	}

	/**
	 * Domain and aliases constructor. The aliases will be stored as a set.
	 * @param domain The site domain.
	 * @param aliases The site domain aliases, if any.
	 */
	public CloudFront(@Nonnull String domain, @Nonnull final Collection<String> aliases) {
		this.domain = requireNonNull(domain);
		this.aliases = new LinkedHashSet<>(aliases); //maintain order to help with reporting and debugging
		acmClient = AcmClient.builder().region(ACM_REGION).build();
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation requests a public certificate with ACM if one does not already exist.
	 */
	@Override
	public void prepare(final MummyContext context) throws IOException {
		final AcmClient acmClient = getAcmClient();
		final Logger logger = getLogger();
		final String domain = getDomain();
		final Set<String> aliases = getAliases();

		//request a certificate if needed
		final Set<CertificateSummary> existingCertificateSummaries = getCertificateSummariesByDomainName(acmClient, domain);
		for(final CertificateSummary existingCertificateSummary : existingCertificateSummaries) {
			logger.debug("Certificate with ARN `{}` exists for domain name `{}`.", existingCertificateSummary.certificateArn(),
					existingCertificateSummary.domainName());
		}
		if(existingCertificateSummaries.isEmpty()) {
			//TODO check to ensure we have a DNS specified so that we can use it as the validation method
			logger.info("Requesting certificate for domain name `{}` and alternative names `{}`.", domain, aliases);
			//TODO fix to use SHA-256 32-character limited hash final String idempotencyToken = domain.replaceAll("[^\\w]", "_"); //in case we request a certificate for the same domain before the other one registers TODO use constants
			acmClient.requestCertificate(request -> request.domainName(domain).subjectAlternativeNames(aliases)
					.validationMethod(ValidationMethod.DNS)/*TODO fix .idempotencyToken(idempotencyToken)*/);
			//TODO save the ARN?
		} else {
			if(existingCertificateSummaries.size() > 1) {
				throw new IOException(String.format("Multiple certificates per domain not supported; please remove all but one certificates for domain `%s`.", domain));
			}
			final CertificateSummary certificateSummary = getOnly(existingCertificateSummaries);
			final CertificateDetail certificate = acmClient.describeCertificate(request -> request.certificateArn(certificateSummary.certificateArn())).certificate();
			if(!Set.copyOf(certificate.subjectAlternativeNames()).equals(aliases)) {
				logger.warn("Certificate `{}` alternate names `{}` do not match site domain aliases `{}`.", certificate.certificateArn(),
						certificate.subjectAlternativeNames(), aliases);
			}
			//TODO save the ARN?
		}
	}

	@Override
	public Optional<URI> deploy(final MummyContext context, final Artifact rootArtifact) throws IOException {
		return Optional.empty(); //TODO implement
	}

	//ACM utility methods; could be removed to separate library

	//## certificates

	/**
	 * Retrieves summaries of all certificates for a given domain name.
	 * @implSpec This implementation delegates to {@link #certificateSummaries(AcmClient)}.
	 * @param client The client to use for retrieving the certificate summaries.
	 * @param domainName The main domain name of the certificate.
	 * @return A set of summaries all certificates for the given domain name.
	 * @see CertificateSummary#domainName()
	 */
	protected static Set<CertificateSummary> getCertificateSummariesByDomainName(@Nonnull final AcmClient client, @Nonnull final String domainName) {
		requireNonNull(domainName);
		try (final Stream<CertificateSummary> certificatesByDomainName = certificateSummaries(client)
				.filter(certificate -> certificate.domainName().equals(domainName))) {
			return certificatesByDomainName.collect(toSet());
		}
	}

	/**
	 * Retrieves a stream of summaries all certificates.
	 * @param client The client to use for retrieving the certificate summaries.
	 * @return A stream of summaries of all certificate.
	 */
	protected static Stream<CertificateSummary> certificateSummaries(@Nonnull final AcmClient client) {
		return client.listCertificatesPaginator().stream().flatMap(response -> response.certificateSummaryList().stream());
	}

}
