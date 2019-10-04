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
import static com.globalmentor.util.stream.Streams.*;
import static io.guise.mummy.GuiseMummy.*;
import static java.util.Collections.*;
import static java.util.Objects.*;
import static java.util.stream.Collectors.*;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

import javax.annotation.*;

import org.slf4j.Logger;

import io.clogr.Clogged;
import io.confound.config.Configuration;
import io.guise.mummy.GuiseMummy;
import io.guise.mummy.MummyContext;
import io.guise.mummy.deploy.Dns;
import software.amazon.awssdk.core.exception.SdkException;
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

	private final String configuredHostedZoneId; //may be null until deployment preparation

	private final String configuredHostedZoneName; //may be null until deployment preparation 

	private HostedZone hostedZone;

	/** @return The hosted zone; only available after {@link #prepare(MummyContext)} has been called. */
	protected Optional<HostedZone> getHostedZone() {
		return Optional.ofNullable(hostedZone);
	}

	private final Route53Client route53Client;

	/** @return The client for connecting to Route 53. */
	protected Route53Client getRoute53Client() {
		return route53Client;
	}

	/**
	 * Configuration constructor.
	 * <p>
	 * The hosted zone ID is retrieved from {@value #CONFIG_KEY_HOSTED_ZONE_ID} in the local configuration, if present. The domain is retrieved from
	 * {@value #CONFIG_KEY_HOSTED_ZONE_NAME} in the local configuration; if not specified it falls back to the greatest common DNS suffix of
	 * {@value GuiseMummy#CONFIG_KEY_SITE_DOMAIN} and {@value GuiseMummy#CONFIG_KEY_SITE_ALIASES} of the context configuration, with a domain delimiter
	 * <code>.</code> appended.
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
		this.configuredHostedZoneId = hostedZoneId;
		this.configuredHostedZoneName = domain;
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
		final Logger logger = getLogger();
		final Route53Client client = getRoute53Client();
		if(configuredHostedZoneId != null) {
			final Set<HostedZone> existingHostedZones = getHostedZonesById(client, configuredHostedZoneId);
			for(final HostedZone existingHostedZone : existingHostedZones) {
				logger.debug("Hosted zone with ID `{}` exists with name `{}`.", existingHostedZone.id(), existingHostedZone.name());
			}
			if(existingHostedZones.isEmpty()) {
				throw new IOException(String.format("No hosted zone exists with ID `%s`. Please check the ID or provide a hosted zone name so that it can be created.",
						configuredHostedZoneId));
			}
			if(existingHostedZones.size() > 1) { //we don't expect this on AWS
				throw new IOException(String.format("Multiple hosted zones encountered with the ID `%s`.", configuredHostedZoneId));
			}
			hostedZone = getOnly(existingHostedZones);
		} else {
			assert configuredHostedZoneName != null : "Either a hosted zone name or a hosted zone ID should have been configured.";
			final Set<HostedZone> existingHostedZones = getHostedZonesByName(client, configuredHostedZoneName);
			for(final HostedZone existingHostedZone : existingHostedZones) {
				logger.debug("Hosted zone with ID `{}` already exists for name `{}`.", existingHostedZone.id(), existingHostedZone.name());
			}
			if(!existingHostedZones.isEmpty()) {
				if(existingHostedZones.size() > 1) {
					throw new IOException(
							String.format("Multiple hosted zones already exist with the name `%s`. Please identify the hosted zone by ID or remove the other hosted zones.",
									configuredHostedZoneName));
				}
				hostedZone = getOnly(existingHostedZones);
			} else { //create a named hosted zone, using a random UUID as the temporary caller reference (required)
				logger.info("Creating Route 53 public hosted zone for name `{}`.", configuredHostedZoneName);
				final StringBuilder commentBuilder = new StringBuilder();
				commentBuilder.append("Created by ").append(context.getMummifierIdentification()); //i18n
				commentBuilder.append(" on ").append(ZonedDateTime.now()); //i18n
				context.getConfiguration().findString(CONFIG_KEY_SITE_DOMAIN).ifPresent(siteDomain -> commentBuilder.append(" for site domain ").append(siteDomain)); //i18n
				context.getConfiguration().findCollection(CONFIG_KEY_SITE_ALIASES, String.class)
						.ifPresent(siteAliases -> commentBuilder.append(" with aliases ").append(siteAliases)); //i18n
				commentBuilder.append("."); //i18n
				hostedZone = client.createHostedZone(request -> request.name(configuredHostedZoneName).callerReference(UUID.randomUUID().toString())
						.hostedZoneConfig(config -> config.comment(commentBuilder.toString()))).hostedZone();
				logger.debug("Created Route 53 public hosted zone with ID `{}` for name `{}`.", hostedZone.id(), hostedZone.name());
			}
		}

		assert hostedZone != null;

		//log name servers
		getNsRecords(client, hostedZone).forEach(nsRecord -> {
			logger.info("Name server: {}", nsRecord.value());

		});
	}

	@Override
	public void setResourceRecord(final String type, final String name, final String value, final long ttl) throws IOException {
		requireNonNull(type);
		requireNonNull(name);
		requireNonNull(value);
		final Route53Client client = getRoute53Client();
		final HostedZone hostedZone = getHostedZone().orElseThrow(IllegalStateException::new);
		try {
			final ResourceRecord resourceRecord = ResourceRecord.builder().value(value).build();
			final ResourceRecordSet resourceRecordSet = ResourceRecordSet.builder().type(type).name(name).resourceRecords(resourceRecord).ttl(ttl).build();
			final Change change = Change.builder().resourceRecordSet(resourceRecordSet).action(ChangeAction.UPSERT).build();
			client.changeResourceRecordSets(request -> request.hostedZoneId(hostedZone.id()).changeBatch(batch -> batch.changes(change)));
		} catch(final SdkException sdkException) {
			throw new IOException(sdkException);
		}
	}

	//# Route 53 utility methods; could be removed to separate library

	//## hosted zones

	/**
	 * Retrieves all hosted zones with a given ID.
	 * @implSpec This implementation delegates to {@link #hostedZones(Route53Client)}.
	 * @param client The client to use for retrieving the zones.
	 * @param id The ID of the hosted zone to retrieve; <em>needs to end with a domain delimiter <code>.</code></em>.
	 * @return A set of all hosted zones with the given ID.
	 * @see HostedZone#id()
	 */
	protected static Set<HostedZone> getHostedZonesById(@Nonnull final Route53Client client, @Nonnull final String id) {
		requireNonNull(id);
		try (final Stream<HostedZone> hostedZonesByName = hostedZones(client).filter(hostedZone -> hostedZone.id().equals(id))) {
			return hostedZonesByName.collect(toSet());
		}
	}

	/**
	 * Retrieves all hosted zones with a given name.
	 * @implSpec This implementation delegates to {@link #hostedZones(Route53Client)}.
	 * @param client The client to use for retrieving the zones.
	 * @param name The name of the hosted zone to retrieve; <em>needs to end with a domain delimiter <code>.</code></em>.
	 * @return A set of all hosted zones with the given name.
	 * @see HostedZone#name()
	 */
	protected static Set<HostedZone> getHostedZonesByName(@Nonnull final Route53Client client, @Nonnull final String name) {
		requireNonNull(name);
		try (final Stream<HostedZone> hostedZonesByName = hostedZones(client).filter(hostedZone -> hostedZone.name().equals(name))) {
			return hostedZonesByName.collect(toSet());
		}
	}

	/**
	 * Retrieves a stream of all hosted zones.
	 * @param client The client to use for retrieving the zones.
	 * @return A stream of all hosted zones the client knows about.
	 */
	protected static Stream<HostedZone> hostedZones(@Nonnull final Route53Client client) {
		return client.listHostedZonesPaginator().stream().flatMap(response -> response.hostedZones().stream());
	}

	//## record sets

	/**
	 * Retrieves the NS records for a hosted zone
	 * @param client The client to use for retrieving the record sets.
	 * @param hostedZone The hosted zone for which to retrieve the NS records.
	 * @return The NS records for the hosted zone.
	 * @throws IOException if no NS record set was encountered.
	 * @throws IOException if more than one NS record set was encountered.
	 * @see RRType#NS
	 */
	protected static List<ResourceRecord> getNsRecords(@Nonnull final Route53Client client, @Nonnull final HostedZone hostedZone) throws IOException {
		//start listing the record sets at the NS record for efficiency, but we still have to filter the result because there are likely subsequent record sets
		try (final Stream<ResourceRecordSet> nsRecordSets = resourceRecordSets(client, hostedZone.id(), hostedZone.name(), RRType.NS)
				.filter(recordSet -> recordSet.name().equals(hostedZone.name()) && recordSet.type().equals(RRType.NS))) {
			return nsRecordSets
					.collect(toOnly(
							() -> new IllegalStateException("Multiple NS record sets encountered for hosted zone `" + hostedZone.name() + "` (`" + hostedZone.id() + "`).")))
					.resourceRecords();
		} catch(final NoSuchElementException | IllegalStateException exception) {
			throw new IOException(exception);
		}
	}

	/**
	 * Retrieves a stream of all record sets for a hosted zone, optionally starting at a resource record name and optionally a type.
	 * @param client The client to use for retrieving the record sets.
	 * @param hostedZoneId The ID of the hosted zone for which to retrieve the record sets.
	 * @param startRecordName The name of the resource record set to begin the record listing from, if any.
	 * @param startRecordType The type of resource record set to begin the record listing from, if any; if present a name must be indicated as well.
	 * @return A stream of all record sets for the identified hosted zone.
	 */
	protected static Stream<ResourceRecordSet> resourceRecordSets(@Nonnull final Route53Client client, @Nonnull final String hostedZoneId,
			@Nullable final String startRecordName, @Nullable final RRType startRecordType) {
		requireNonNull(hostedZoneId);
		return client.listResourceRecordSetsPaginator(request -> {
			request.hostedZoneId(hostedZoneId);
			if(startRecordName != null) {
				request.startRecordName(startRecordName);
			}
			if(startRecordType != null) { //AWS will check if a type is given with no name
				request.startRecordType(startRecordType);
			}
		}).stream().flatMap(response -> response.resourceRecordSets().stream());
	}

}
