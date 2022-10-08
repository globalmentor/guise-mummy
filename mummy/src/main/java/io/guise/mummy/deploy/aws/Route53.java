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

package io.guise.mummy.deploy.aws;

import static com.globalmentor.collections.iterables.Iterables.*;
import static com.globalmentor.java.Conditions.*;
import static com.globalmentor.util.stream.Streams.*;
import static io.guise.mummy.GuiseMummy.*;
import static io.guise.mummy.deploy.aws.AWS.*;
import static java.util.Objects.*;
import static java.util.stream.Collectors.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import javax.annotation.*;

import org.slf4j.Logger;

import com.globalmentor.java.Enums;
import com.globalmentor.net.DomainName;
import com.globalmentor.net.ResourceRecord;

import io.confound.config.Configuration;
import io.guise.mummy.GuiseMummy;
import io.guise.mummy.MummyContext;
import io.guise.mummy.deploy.*;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
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
public class Route53 extends AbstractDns {

	/** The DNS section relative key for the hosted zone ID, if a hosted zone already exists. */
	public static final String CONFIG_KEY_HOSTED_ZONE_ID = "hostedZoneId";

	/**
	 * The predefined hosted zone ID for an alias with a CloudFront target.
	 * @see <a href="https://docs.aws.amazon.com/Route53/latest/APIReference/API_AliasTarget.html">AliasTarget</a>
	 */
	public static final String CLOUDFRONT_ALIAS_HOSTED_ZONE_ID = "Z2FDTNDATAQYW2";

	/**
	 * The predefined hosted zone ID for an alias with an AWS Global Accelerator target.
	 * @see <a href="https://docs.aws.amazon.com/Route53/latest/APIReference/API_AliasTarget.html">AliasTarget</a>
	 */
	public static final String AWS_GLOBAL_ACCELERATOR_ALIAS_HOSTED_ZONE_ID = "Z2BJ6XQ5FK7U4H";

	private final String profile;

	/** @return The AWS profile if one was set explicitly. */
	public final Optional<String> getProfile() {
		return Optional.of(profile);
	}

	private final String configuredHostedZoneId;

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
	 * The hosted zone ID is retrieved from {@value #CONFIG_KEY_HOSTED_ZONE_ID} in the local configuration, if present. This method determines the domain in the
	 * following order:
	 * </p>
	 * <ol>
	 * <li>The key {@value Dns#CONFIG_KEY_ORIGIN} relative to the DNS local configuration.</li>
	 * <li>The key {@value GuiseMummy#CONFIG_KEY_DOMAIN} retrieved from the global configuration.</li>
	 * <li>The site base domain determined by the longest common domain segment suffix from the site domain {@value GuiseMummy#CONFIG_KEY_SITE_DOMAIN} and
	 * alternative domains {@value GuiseMummy#CONFIG_KEY_SITE_ALT_DOMAINS}, retrieved from the global configuration.</li>
	 * </ol>
	 * @implSpec This constructor calls {@link Dns#getConfiguredOrigin(Configuration, Configuration)} and {@link Dns#getConfiguredResourceRecords(Configuration)}.
	 * @param context The context of static site generation.
	 * @param localConfiguration The local configuration for the Route 53 DNS, which may be a section of the project configuration.
	 * @see AWS#CONFIG_KEY_DEPLOY_AWS_PROFILE
	 * @see #CONFIG_KEY_HOSTED_ZONE_ID
	 * @see Dns#CONFIG_KEY_ORIGIN
	 * @see GuiseMummy#CONFIG_KEY_DOMAIN
	 * @see GuiseMummy#CONFIG_KEY_SITE_DOMAIN
	 * @see GuiseMummy#CONFIG_KEY_SITE_ALT_DOMAINS
	 * @see Dns#getConfiguredOrigin(Configuration, Configuration)
	 * @see Dns#getConfiguredResourceRecords(Configuration)
	 */
	public Route53(@Nonnull final MummyContext context, @Nonnull final Configuration localConfiguration) {
		this(context.getConfiguration().findString(CONFIG_KEY_DEPLOY_AWS_PROFILE).orElse(null),
				localConfiguration.findString(CONFIG_KEY_HOSTED_ZONE_ID).orElse(null), Dns.getConfiguredOrigin(context.getConfiguration(), localConfiguration),
				Dns.getConfiguredResourceRecords(localConfiguration));
	}

	/**
	 * Hosted zone ID and domain constructor. Either a hosted zone ID or a domain must be given. If both are given, the domain must match that of the identified
	 * hosted zone, checked with the DNS is later queried.
	 * @param profile The name of the AWS profile to use for retrieving credentials, or <code>null</code> if the default credential provider should be used.
	 * @param hostedZoneId The ID of the hosted zone, or <code>null</code> if not known until created.
	 * @param origin The fully qualified base domain name for the DNS zone.
	 * @param resourceRecords The resource records to be created during deployment; may be empty.
	 * @throws IllegalArgumentException if the given origin is not absolute.
	 */
	public Route53(@Nullable String profile, @Nullable final String hostedZoneId, @Nonnull final DomainName origin,
			@Nonnull final Collection<ResourceRecord> resourceRecords) {
		super(origin, resourceRecords);
		this.profile = profile;
		this.configuredHostedZoneId = hostedZoneId;
		final Route53ClientBuilder route53ClientBuilder = Route53Client.builder().region(Region.AWS_GLOBAL);
		if(profile != null) {
			route53ClientBuilder.credentialsProvider(ProfileCredentialsProvider.create(profile));
		}
		route53Client = route53ClientBuilder.build();
	}

	@Override
	public void prepare(final MummyContext context) throws IOException {
		try {
			final Logger logger = getLogger();
			final Route53Client client = getRoute53Client();
			getProfile().ifPresent(profile -> getLogger().info("Using AWS Route 53 credentials profile `{}`.", profile));
			final DomainName origin = getOrigin();
			if(configuredHostedZoneId != null) {
				final Set<HostedZone> existingHostedZones = getHostedZonesById(client, configuredHostedZoneId);
				for(final HostedZone existingHostedZone : existingHostedZones) {
					logger.debug("Hosted zone with ID `{}` exists with name `{}`.", existingHostedZone.id(), existingHostedZone.name());
				}
				if(existingHostedZones.isEmpty()) {
					throw new IOException(String.format(
							"No hosted zone exists with ID `%s`. Please check the ID or provide a hosted zone name so that it can be created.", configuredHostedZoneId));
				}
				if(existingHostedZones.size() > 1) { //we don't expect this on AWS
					throw new IOException(String.format("Multiple hosted zones encountered with the ID `%s`.", configuredHostedZoneId));
				}
				hostedZone = getOnly(existingHostedZones);
				if(!DomainName.of(hostedZone.name()).equals(origin)) {
					throw new IOException(
							String.format("Hosted zone with configured ID `%s` does not match the configured DNS zone origin `%s`.", configuredHostedZoneId, origin));
				}
			} else {
				final Set<HostedZone> existingHostedZones = getHostedZonesByName(client, origin);
				for(final HostedZone existingHostedZone : existingHostedZones) {
					logger.debug("Hosted zone with ID `{}` already exists for name `{}`.", existingHostedZone.id(), existingHostedZone.name());
				}
				if(!existingHostedZones.isEmpty()) {
					if(existingHostedZones.size() > 1) {
						throw new IOException(String.format(
								"Multiple hosted zones already exist with the name `%s`. Please identify the hosted zone by ID or remove the other hosted zones.", origin));
					}
					hostedZone = getOnly(existingHostedZones);
				} else { //create a named hosted zone, using a random UUID as the temporary caller reference (required)
					logger.info("Creating Route 53 public hosted zone for name `{}`.", origin);
					final StringBuilder commentBuilder = new StringBuilder();
					commentBuilder.append("Created by ").append(context.getMummifierIdentification()); //TODO i18n
					commentBuilder.append(" on ").append(LocalDate.now()); //TODO i18n
					context.getConfiguration().findString(CONFIG_KEY_SITE_DOMAIN)
							.ifPresent(siteDomain -> commentBuilder.append(" for site domain `").append(siteDomain).append('`')); //TODO i18n
					context.getConfiguration().findCollection(CONFIG_KEY_SITE_ALT_DOMAINS, String.class).ifPresent(siteAltDomains -> commentBuilder
							.append(" with alternatives ").append(siteAltDomains.stream().map(siteAltDomain -> "`" + siteAltDomain + "`").collect(toList()))); //TODO i18n
					commentBuilder.append("."); //TODO i18n
					hostedZone = client.createHostedZone(request -> request.name(origin.toString()).callerReference(UUID.randomUUID().toString())
							.hostedZoneConfig(config -> config.comment(commentBuilder.toString()))).hostedZone();
					logger.debug("Created Route 53 public hosted zone with ID `{}` for name `{}`.", hostedZone.id(), hostedZone.name());
				}
			}

			assert hostedZone != null;

			//log name servers
			getNsRecords(client, hostedZone).forEach(nsRecord -> {
				logger.info("Name server: {}", nsRecord.value());

			});
		} catch(final SdkException sdkException) {
			throw new IOException(sdkException);
		}
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation quotes all {@link ResourceRecord.Type#TXT} record types, as the developer guide indicates this is preferred, and moreover
	 *           some string containing e.g. '=' cause Route 53 errors, even if RFC 1035 doesn't seem to require quoting such values.
	 * @see <a href="https://docs.aws.amazon.com/Route53/latest/DeveloperGuide/ResourceRecordTypes.html#TXTFormat">Supported DNS Record Types: TXT Record Type</a>
	 */
	@Override
	public void setResourceRecords(final String type, final DomainName name, final Stream<String> values, final long ttl) throws IOException {
		requireNonNull(type);
		name.checkArgumentAbsolute();
		requireNonNull(values);
		checkArgumentNotNegative(ttl);
		try {
			final Route53Client client = getRoute53Client();
			final HostedZone hostedZone = getHostedZone().orElseThrow(IllegalStateException::new);
			final List<software.amazon.awssdk.services.route53.model.ResourceRecord> resourceRecords = values
					.map(value -> software.amazon.awssdk.services.route53.model.ResourceRecord.builder().value(normalizeValueForType(type, value)).build())
					.collect(toList());
			checkArgument(!resourceRecords.isEmpty(), "No resource record values given to set for [`%s`] `%s`.", type, name);
			final ResourceRecordSet resourceRecordSet = ResourceRecordSet.builder().type(type).name(name.toString()).resourceRecords(resourceRecords).ttl(ttl)
					.build();
			final Change change = Change.builder().action(ChangeAction.UPSERT).resourceRecordSet(resourceRecordSet).build();
			client.changeResourceRecordSets(request -> request.hostedZoneId(hostedZone.id()).changeBatch(batch -> batch.changes(change)));
		} catch(final SdkException sdkException) {
			throw new IOException(sdkException);
		}
	}

	/**
	 * Normalizes a value as expected by Route 53.
	 * @implSpec This implementation quotes all {@link ResourceRecord.Type#TXT} record types, as the developer guide indicates this is preferred, and moreover
	 *           some string containing e.g. '=' cause Route 53 errors, even if RFC 1035 doesn't seem to require quoting such values.
	 * @see <a href="https://docs.aws.amazon.com/Route53/latest/DeveloperGuide/ResourceRecordTypes.html#TXTFormat">Supported DNS Record Types: TXT Record Type</a>
	 * @param type The resource record type.
	 * @param value The given resource record value.
	 * @return The value form preferred by Route 53.
	 */
	static String normalizeValueForType(@Nonnull final String type, @Nonnull final String value) {
		return Enums.asEnum(ResourceRecord.Type.class, type).map(resourceRecordType -> {
			switch(resourceRecordType) {
				case TXT: //Route 53 prefers all TXT values to be quoted
					//TODO support multiple long strings in a TXT record; see https://tools.ietf.org/html/rfc7208#section-3.3, which seems to conflict with Route 53
					return ResourceRecord.normalizeCharacterString(value, true);
				default:
					return value;
			}
		}).orElse(value);
	}

	/**
	 * Sets an alias resource record. If a resource record with the same type and name does not already exists, it will be added. If a resource record already
	 * exists with the same type and name, it will be replaced. (This is commonly referred to as <dfn>upsert</dfn>.)
	 * @implSpec This implementation delegates to {@link #setAliasResourceRecord(String, DomainName, String, String)}.
	 * @implNote Alias resource records are specific to Route 53.
	 * @param type The type of resource record to set.
	 * @param name The fully-qualified domain name of the resource record.
	 * @param aliasDnsName The DNS name for the alias, such as a domain name assigned to a CloudFront distribution.
	 * @param aliasHostZoneId An identifier for some hosted zone; may be a predefined constant for known targets such as CloudFront.
	 * @throws IllegalArgumentException if the given name is not absolute.
	 * @throws IOException If there was an error setting the resource record.
	 * @see <a href="https://docs.aws.amazon.com/Route53/latest/DeveloperGuide/resource-record-sets-choosing-alias-non-alias.html">Choosing Between Alias and
	 *      Non-Alias Records</a>
	 */
	public void setAliasResourceRecord(@Nonnull final ResourceRecord.Type type, @Nonnull final DomainName name, @Nonnull final String aliasDnsName,
			@Nonnull final String aliasHostZoneId) throws IOException {
		setAliasResourceRecord(type.toString(), name, aliasDnsName, aliasHostZoneId);
	}

	/**
	 * Sets an alias resource record. If a resource record with the same type and name does not already exists, it will be added. If a resource record already
	 * exists with the same type and name, it will be replaced. (This is commonly referred to as <dfn>upsert</dfn>.)
	 * @apiNote Using {@link #setAliasResourceRecord(ResourceRecord.Type, DomainName, String, String)} for known resource record types is preferred for type and
	 *          value safety.
	 * @implNote Alias resource records are specific to Route 53.
	 * @param type The type of resource record to set.
	 * @param name The fully-qualified domain name of the resource record.
	 * @param aliasDnsName The DNS name for the alias, such as a domain name assigned to a CloudFront distribution.
	 * @param aliasHostZoneId An identifier for some hosted zone; may be a predefined constant for known targets such as CloudFront.
	 * @throws IllegalArgumentException if the given name is not absolute.
	 * @throws IOException If there was an error setting the resource record.
	 * @see <a href="https://docs.aws.amazon.com/Route53/latest/DeveloperGuide/resource-record-sets-choosing-alias-non-alias.html">Choosing Between Alias and
	 *      Non-Alias Records</a>
	 */
	public void setAliasResourceRecord(@Nonnull final String type, @Nonnull final DomainName name, @Nonnull final String aliasDnsName,
			@Nonnull final String aliasHostZoneId) throws IOException {
		requireNonNull(type);
		name.checkArgumentAbsolute();
		requireNonNull(aliasDnsName);
		requireNonNull(aliasHostZoneId);
		try {
			final Route53Client client = getRoute53Client();
			final HostedZone hostedZone = getHostedZone().orElseThrow(IllegalStateException::new);
			final AliasTarget aliasTarget = AliasTarget.builder().dnsName(aliasDnsName).hostedZoneId(aliasHostZoneId).evaluateTargetHealth(false).build();
			final ResourceRecordSet resourceRecordSet = ResourceRecordSet.builder().type(type).name(name.toString()).aliasTarget(aliasTarget).build();
			final Change change = Change.builder().action(ChangeAction.UPSERT).resourceRecordSet(resourceRecordSet).action(ChangeAction.UPSERT).build();
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
	 * @param id The ID of the hosted zone to retrieve.
	 * @return A set of all hosted zones with the given ID.
	 * @throws SdkException if an error occurs related to AWS.
	 * @see HostedZone#id()
	 */
	protected static Set<HostedZone> getHostedZonesById(@Nonnull final Route53Client client, @Nonnull final String id) throws SdkException {
		requireNonNull(id);
		try (final Stream<HostedZone> hostedZonesByName = hostedZones(client).filter(hostedZone -> hostedZone.id().equals(id))) {
			return hostedZonesByName.collect(toSet());
		}
	}

	/**
	 * Retrieves all hosted zones with a given name.
	 * @implSpec This implementation delegates to {@link #hostedZones(Route53Client)}.
	 * @param client The client to use for retrieving the zones.
	 * @param hostedZoneName The name of the hosted zone to retrieve; must be an absolute domain name.
	 * @return A set of all hosted zones with the given name.
	 * @throws SdkException if an error occurs related to AWS.
	 * @see HostedZone#name()
	 * @throws IllegalArgumentException if the given domain name is not absolute.
	 */
	protected static Set<HostedZone> getHostedZonesByName(@Nonnull final Route53Client client, @Nonnull final DomainName hostedZoneName) throws SdkException {
		final String name = hostedZoneName.checkArgumentAbsolute().toString();
		try (final Stream<HostedZone> hostedZonesByName = hostedZones(client).filter(hostedZone -> hostedZone.name().equals(name))) {
			return hostedZonesByName.collect(toSet());
		}
	}

	/**
	 * Retrieves a stream of all hosted zones.
	 * @param client The client to use for retrieving the zones.
	 * @return A stream of all hosted zones the client knows about.
	 * @throws SdkException if an error occurs related to AWS.
	 */
	protected static Stream<HostedZone> hostedZones(@Nonnull final Route53Client client) throws SdkException {
		return client.listHostedZonesPaginator().stream().flatMap(response -> response.hostedZones().stream());
	}

	//## record sets

	/**
	 * Retrieves all resource records for a hosted zone
	 * @param client The client to use for retrieving the record sets.
	 * @param hostedZone The hosted zone for which to retrieve the records.
	 * @return The resource records for the hosted zone.
	 * @throws SdkException if an error occurs related to AWS.
	 */
	protected static Stream<ResourceRecord> resourceRecords(@Nonnull final Route53Client client, @Nonnull final HostedZone hostedZone) throws SdkException {
		try (final Stream<ResourceRecordSet> recordSets = resourceRecordSets(client, hostedZone.id())) {
			return recordSets
					.flatMap(resourceRecordSet -> resourceRecordSet.resourceRecords().stream().map(resourceRecord -> new ResourceRecord(resourceRecordSet.type().name(),
							DomainName.of(resourceRecordSet.name()), resourceRecord.value(), resourceRecordSet.ttl())));
		}
	}

	/**
	 * Retrieves the NS records for a hosted zone
	 * @param client The client to use for retrieving the record sets.
	 * @param hostedZone The hosted zone for which to retrieve the NS records.
	 * @return The NS records for the hosted zone.
	 * @throws IOException if no NS record set was encountered.
	 * @throws IOException if more than one NS record set was encountered.
	 * @throws SdkException if an error occurs related to AWS.
	 * @see RRType#NS
	 */
	protected static List<software.amazon.awssdk.services.route53.model.ResourceRecord> getNsRecords(@Nonnull final Route53Client client,
			@Nonnull final HostedZone hostedZone) throws IOException, SdkException {
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
	 * Retrieves a stream of all record sets for a hosted zone.
	 * @implSpec This implementation delegates to {@link #resourceRecordSets(Route53Client, String, String, RRType)}.
	 * @param client The client to use for retrieving the record sets.
	 * @param hostedZoneId The ID of the hosted zone for which to retrieve the record sets.
	 * @return A stream of all record sets for the identified hosted zone.
	 * @throws SdkException if an error occurs related to AWS.
	 */
	protected static Stream<ResourceRecordSet> resourceRecordSets(@Nonnull final Route53Client client, @Nonnull final String hostedZoneId) throws SdkException {
		return resourceRecordSets(client, hostedZoneId, null, null);
	}

	/**
	 * Retrieves a stream of all record sets for a hosted zone, optionally starting at a resource record name and optionally a type.
	 * @param client The client to use for retrieving the record sets.
	 * @param hostedZoneId The ID of the hosted zone for which to retrieve the record sets.
	 * @param startRecordName The name of the resource record set to begin the record listing from, if any.
	 * @param startRecordType The type of resource record set to begin the record listing from, if any; if present a name must be indicated as well.
	 * @return A stream of all record sets for the identified hosted zone.
	 * @throws SdkException if an error occurs related to AWS.
	 */
	protected static Stream<ResourceRecordSet> resourceRecordSets(@Nonnull final Route53Client client, @Nonnull final String hostedZoneId,
			@Nullable final String startRecordName, @Nullable final RRType startRecordType) throws SdkException {
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
