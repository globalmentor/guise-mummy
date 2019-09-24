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
import static com.globalmentor.java.CharSequences.*;
import static com.globalmentor.java.Conditions.*;
import static io.guise.mummy.GuiseMummy.*;
import static java.util.Collections.*;
import static java.util.Objects.*;
import static java.util.stream.Collectors.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import javax.annotation.*;

import org.slf4j.Logger;

import io.clogr.Clogged;
import io.confound.config.Configuration;
import io.guise.mummy.GuiseMummy;
import io.guise.mummy.MummyContext;
import io.guise.mummy.deploy.Dns;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.*;
import software.amazon.awssdk.services.route53.model.*;

/**
 * DNS using <a href="https://aws.amazon.com/route53/">AWS Route 53</a>.
 * <p>
 * Route 53 stores records in a <em>hosted zone</em> for a domain. Though not typical, there can be several hosted zones for the same domain. To configure Route
 * 53, if a hosted zone ID is indicated, it is assumed to exist already and is retrieved. Otherwise a domain name is indicated and the first existing hosted
 * zone for that domain is used if one exists; otherwise one is created for that domain name.
 * </p>
 * @author Garret Wilson
 */
public class Route53 implements Dns, Clogged {

	/** The DNS section relative key for the hosted zone ID, if a hosted zone already exists. */
	public static final String CONFIG_KEY_HOSTED_ZONE_ID = "hostedZoneId";

	/**
	 * The DNS section relative key for the hosted zone name; must end with the <code>.</code> domain delimiter; defaults to the common domain suffix of
	 * {@link GuiseMummy#CONFIG_KEY_SITE_DOMAIN} and {@link GuiseMummy#CONFIG_KEY_SITE_ALIASES}, with a domain delimiter <code>.</code> appended.
	 */
	public static final String CONFIG_KEY_HOSTED_ZONE_NAME = "hostedZoneName";

	private String hostedZoneId; //may be null until deployment preparation

	/** @return The ID of the DNS hosted zone. */
	protected String getHostedZoneId() {
		//TODO add state exception if not yet initialized
		return hostedZoneId;
	}

	private String hostedZoneName; //may be null until deployment preparation 

	/** @return The domain managed by the hosted zone. */
	protected String getDomain() {
		//TODO add state exception if not yet initialized
		return hostedZoneName;
	}

	private final Route53Client route53Client;

	/** @return The client for connecting to Route 53. */
	protected Route53Client getRoute53Client() {
		return route53Client;
	}

	/**
	 * Configuration constructor.
	 * <p>
	 * The hosted zone ID is retrieved from {@value #CONFIG_KEY_HOSTED_ZONE_ID}, if present. The domain is retrieved from {@value #CONFIG_KEY_HOSTED_ZONE_NAME} in
	 * the local configuration; if not specified it falls back to the greatest common DNS suffix of {@value GuiseMummy#CONFIG_KEY_SITE_DOMAIN} and
	 * {@value GuiseMummy#CONFIG_KEY_SITE_ALIASES} of the context configuration, with a domain delimiter <code>.</code> appended.
	 * </p>
	 * @param context The context of static site generation.
	 * @param localConfiguration The local configuration for the Route 53 DNS, which may be a section of the project configuration.
	 * @see #CONFIG_KEY_HOSTED_ZONE_ID
	 * @see #CONFIG_KEY_HOSTED_ZONE_NAME
	 * @see GuiseMummy#CONFIG_KEY_SITE_DOMAIN
	 * @see GuiseMummy#CONFIG_KEY_SITE_ALIASES
	 */
	public Route53(@Nonnull final MummyContext context, @Nonnull final Configuration localConfiguration) {
		this(localConfiguration.findString(CONFIG_KEY_HOSTED_ZONE_ID).orElse(null),
				getConfiguredHostedZoneName(context.getConfiguration(), localConfiguration).orElse(null));
	}

	/**
	 * Hosted zone ID and domain constructor. Either a hosted zone ID or a domain must be given. If both are given, the domain must match that of the identified
	 * hosted zone, checked with the DNS is later queried.
	 * @param hostedZoneId The ID of the hosted zone, or <code>null</code> if not known until created.
	 * @param domain The domain managed by the hosted zone, or <code>null</code> if a hosted zone already exists for the domain.
	 * @throws IllegalArgumentException if neither a hosted zone ID nor a domain is specified.
	 */
	public Route53(@Nullable final String hostedZoneId, @Nullable final String domain) {
		checkArgument(hostedZoneId != null || domain != null, "Either a hosted zone ID or a domain must be configured for %s.", getClass().getSimpleName());
		this.hostedZoneId = hostedZoneId;
		this.hostedZoneName = domain;
		route53Client = Route53Client.builder().region(Region.AWS_GLOBAL).build();
	}

	/**
	 * Determines the domain name to use for the hosted zone. This method determines the domain in the following order:
	 * <ol>
	 * <li>The key {@link #CONFIG_KEY_HOSTED_ZONE_NAME} relative to the Route 53 configuration.</li>
	 * <li>The site base domain determined by the longest common domain segment suffix from the site domain {@value GuiseMummy#CONFIG_KEY_SITE_DOMAIN} and aliases
	 * {@value GuiseMummy#CONFIG_KEY_SITE_ALIASES}, retrieved from the project configuration configuration, with a domain delimiter <code>.</code> appended.</li>
	 * </ol>
	 * @param globalConfiguration The configuration containing all the configuration values.
	 * @param localConfiguration The local configuration for the Route 53 DNS, which may be a section of the project configuration.
	 * @return domain The domain to be managed by the hosted zone, which will not be present if it is not configured.
	 * @see #CONFIG_KEY_HOSTED_ZONE_NAME
	 * @see GuiseMummy#CONFIG_KEY_SITE_DOMAIN
	 * @see GuiseMummy#CONFIG_KEY_SITE_ALIASES
	 */
	static Optional<String> getConfiguredHostedZoneName(@Nonnull final Configuration globalConfiguration, @Nonnull final Configuration localConfiguration) {
		return localConfiguration.findString(CONFIG_KEY_HOSTED_ZONE_NAME).or(() -> {
			final List<String> domains = Stream.concat(globalConfiguration.findString(CONFIG_KEY_SITE_DOMAIN).stream(),
					globalConfiguration.findCollection(CONFIG_KEY_SITE_ALIASES, String.class).orElse(emptyList()).stream()).collect(toList());
			return longestCommonSegmentSuffix(domains, '.').map(domain -> domain + '.'); //append a domain delimiter as expected by hosted zone names TODO use a constant for the domain delimiter
		});
	}

	@Override
	public void prepare(final MummyContext context) throws IOException {
		//TODO query Route 53, create hosted zone if needed, and establish hosted zone ID
		final Route53Client client = getRoute53Client();
		final HostedZone hostedZone;
		if(hostedZoneId != null) {
			throw new UnsupportedOperationException(); //TODO finish
		} else {
			assert hostedZoneName != null : "Either a hosted zone name or a hosted zone ID should have been indicated.";
			final Set<HostedZone> existingHostedZones = getHostedZonesByName(client, hostedZoneName);
			final Logger logger = getLogger();
			for(final HostedZone existingHostedZone : existingHostedZones) {
				logger.debug("Hosted zone with ID `{}` already exists for name `{}`.", existingHostedZone.id(), existingHostedZone.name());
			}
			if(!existingHostedZones.isEmpty()) {
				if(existingHostedZones.size() > 1) {
					throw new IOException(
							String.format("Multiple hosted zones already exist with the name `%s`. Please identify the hosted zone by ID or remove the other hosted zones.",
									hostedZoneName));
				}
				hostedZone = getOnly(existingHostedZones);
				hostedZoneId = hostedZone.id();
			} else { //create a named hosted zone, using a random UUID as the temporary caller reference (required)
				logger.info("Creating Route 53 public hosted zone for name `{}`.", hostedZoneName);
				hostedZone = client.createHostedZone(builder -> builder.name(hostedZoneName).callerReference(UUID.randomUUID().toString())).hostedZone();
				hostedZoneId = hostedZone.id(); //save the ID of the created hosted zone
				logger.debug("Created Route 53 public hosted zone with ID `{}` for name `{}`.", hostedZoneId, hostedZoneName);
			}
		}

		//TODO retrieve and log name servers; probably store hosted zone for future queries
	}

	//TODO document
	protected static Set<HostedZone> getHostedZonesByName(@Nonnull final Route53Client client, @Nonnull final String name) {
		requireNonNull(name);
		return client.listHostedZonesPaginator().stream().flatMap(response -> response.hostedZones().stream()).filter(hostedZone -> hostedZone.name().equals(name))
				.collect(toSet());
	}

}
