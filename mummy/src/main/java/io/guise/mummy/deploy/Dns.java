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

package io.guise.mummy.deploy;

import static io.guise.mummy.GuiseMummy.*;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static org.zalando.fauxpas.FauxPas.*;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.annotation.*;

import com.globalmentor.java.Conditions;
import com.globalmentor.java.Enums;
import com.globalmentor.net.*;

import io.confound.config.*;
import io.guise.mummy.*;

/**
 * Access to a Domain Name System (DNS).
 * @author Garret Wilson
 */
public interface Dns extends DeployTarget {

	/** The default TTL value if none is configured. */
	public static final long DEFAULT_TTL = TimeUnit.HOURS.toSeconds(1);

	/**
	 * The DNS zone section relative key for the origin, that is, base domain name. Must be a valid domain name in absolute (FQDN) form, ending with the
	 * <code>.</code> domain delimiter. Defaults to the project domain {@value GuiseMummy#CONFIG_KEY_DOMAIN}, or if not present the common domain suffix of
	 * {@value GuiseMummy#CONFIG_KEY_SITE_DOMAIN} and {@value GuiseMummy#CONFIG_KEY_SITE_ALT_DOMAINS}.
	 */
	public static final String CONFIG_KEY_ORIGIN = "origin";

	/** The DNS section relative key for the records list. */
	public static final String CONFIG_KEY_RECORDS = "records";

	/** The resource record section relative key for the record type. */
	public static final String RECORD_CONFIG_KEY_TYPE = "type";

	/** The resource record section relative key for the record name. */
	public static final String RECORD_CONFIG_KEY_NAME = "name";

	/** The resource record section relative key for the record value. */
	public static final String RECORD_CONFIG_KEY_VALUE = "value";

	/** The resource record section relative key for the record TTL. */
	public static final String RECORD_CONFIG_KEY_TTL = "ttl";

	/**
	 * Returns the origin, or base domain name, for this DNS zone in absolute form.
	 * @return The fully-qualified domain name serving as the base for this zone.
	 */
	public DomainName getOrigin();

	/**
	 * {@inheritDoc} This may include creating any accounts or record zones, for example.
	 */
	@Override
	public void prepare(MummyContext context) throws IOException;

	/**
	 * {@inheritDoc} This includes actually creating any defined resource records.
	 * @see #CONFIG_KEY_RECORDS
	 */
	@Override
	public Optional<URI> deploy(MummyContext context, Artifact rootArtifact) throws IOException;

	/**
	 * Sets a resource record. If a resource record with the same type and name does not already exists, it will be added. If a resource record already exists
	 * with the same type and name, it will be replaced. (This is commonly referred to as <dfn>upsert</dfn>.)
	 * @implSpec The default implementation delegates to {@link #setResourceRecords(ResourceRecord.Type, DomainName, Stream, long)}.
	 * @implNote Implementations should normally only override {@link #setResourceRecords(String, DomainName, Stream, long)}.
	 * @param type The type of resource record to set.
	 * @param name The fully-qualified domain name of the resource record.
	 * @param value The value to store in the resource record.
	 * @param ttl The resource record cache time to live, in seconds.
	 * @throws IllegalArgumentException if the given name is not absolute.
	 * @throws IllegalArgumentException if the given TTL is negative.
	 * @throws IOException If there was an error setting the resource record.
	 */
	public default void setResourceRecord(@Nonnull final ResourceRecord.Type type, @Nonnull final DomainName name, @Nonnull final String value,
			@Nonnegative final long ttl) throws IOException {
		setResourceRecords(type, name, Stream.of(value), ttl);
	}

	/**
	 * Sets a resource record. If a resource record with the same type and name does not already exists, it will be added. If a resource record already exists
	 * with the same type and name, it will be replaced. (This is commonly referred to as <dfn>upsert</dfn>.)
	 * @apiNote Use {@link #setResourceRecord(ResourceRecord.Type, DomainName, String, long)} for known resource record types is preferred for type and value
	 *          safety.
	 * @implSpec The default implementation delegates to {@link #setResourceRecords(String, DomainName, Stream, long)}.
	 * @implNote Implementations should normally only override {@link #setResourceRecords(String, DomainName, Stream, long)}.
	 * @param type The type of resource record to set.
	 * @param name The fully-qualified domain name of the resource record.
	 * @param value The value to store in the resource record.
	 * @param ttl The resource record cache time to live, in seconds.
	 * @throws IllegalArgumentException if the given name is not absolute.
	 * @throws IllegalArgumentException if the given TTL is negative.
	 * @throws IOException If there was an error setting the resource record.
	 */
	public default void setResourceRecord(@Nonnull final String type, @Nonnull final DomainName name, @Nonnull final String value, @Nonnegative final long ttl)
			throws IOException {
		setResourceRecords(type, name, Stream.of(value), ttl);
	}

	/**
	 * Sets potentially several resource records with the given type and name, removing all other resource records with the same type and name.
	 * @implSpec The default implementation delegates to {@link #setResourceRecords(ResourceRecord.Type, DomainName, Stream, long)}.
	 * @implNote Implementations should normally only override {@link #setResourceRecords(String, DomainName, Stream, long)}.
	 * @param type The type of resource record to set.
	 * @param name The fully-qualified domain name of the resource record.
	 * @param values The values to store in the resource records; there must be at least one value.
	 * @param ttl The resource record cache time to live, in seconds.
	 * @throws IllegalArgumentException if the given name is not absolute.
	 * @throws IllegalArgumentException if no values are given.
	 * @throws IllegalArgumentException if the given TTL is negative.
	 * @throws IOException If there was an error setting the resource record values.
	 */
	public default void setResourceRecords(@Nonnull final ResourceRecord.Type type, @Nonnull final DomainName name, @Nonnull final Collection<String> values,
			@Nonnegative final long ttl) throws IOException {
		setResourceRecords(type, name, values.stream(), ttl);
	}

	/**
	 * Sets potentially several resource records with the given type and name, removing all other resource records with the same type and name.
	 * @implSpec The default implementation delegates to {@link #setResourceRecords(String, DomainName, Stream, long)}.
	 * @implNote Implementations should normally only override {@link #setResourceRecords(String, DomainName, Stream, long)}.
	 * @param type The type of resource record to set.
	 * @param name The fully-qualified domain name of the resource record.
	 * @param values The values to store in the resource records; there must be at least one value.
	 * @param ttl The resource record cache time to live, in seconds.
	 * @throws IllegalArgumentException if the given name is not absolute.
	 * @throws IllegalArgumentException if no values are given.
	 * @throws IllegalArgumentException if the given TTL is negative.
	 * @throws IOException If there was an error setting the resource record values.
	 */
	public default void setResourceRecords(@Nonnull final ResourceRecord.Type type, @Nonnull final DomainName name, @Nonnull final Stream<String> values,
			@Nonnegative final long ttl) throws IOException {
		setResourceRecords(type.name(), name, values, ttl);
	}

	/**
	 * Sets potentially several resource records with the given type and name, removing all other resource records with the same type and name.
	 * @implSpec The default implementation delegates to {@link #setResourceRecords(String, DomainName, Stream, long)}.
	 * @implNote Implementations should normally only override {@link #setResourceRecords(String, DomainName, Stream, long)}.
	 * @param type The type of resource record to set.
	 * @param name The fully-qualified domain name of the resource record.
	 * @param values The values to store in the resource records; there must be at least one value.
	 * @param ttl The resource record cache time to live, in seconds.
	 * @throws IllegalArgumentException if the given name is not absolute.
	 * @throws IllegalArgumentException if no values are given.
	 * @throws IllegalArgumentException if the given TTL is negative.
	 * @throws IOException If there was an error setting the resource record values.
	 */
	public default void setResourceRecords(@Nonnull final String type, @Nonnull final DomainName name, @Nonnull final Collection<String> values,
			@Nonnegative final long ttl) throws IOException {
		setResourceRecords(type, name, values.stream(), ttl);
	}

	/**
	 * Sets potentially several resource records with the given type and name, removing all other resource records with the same type and name.
	 * @apiNote Use {@link #setResourceRecords(ResourceRecord.Type, DomainName, Stream, long)} for known resource record types is preferred for type and value
	 *          safety.
	 * @param type The type of resource record to set.
	 * @param name The fully-qualified domain name of the resource record.
	 * @param values The values to store in the resource records; there must be at least one value.
	 * @param ttl The resource record cache time to live, in seconds.
	 * @throws IllegalArgumentException if the given name is not absolute.
	 * @throws IllegalArgumentException if no values are given.
	 * @throws IllegalArgumentException if the given TTL is negative.
	 * @throws IOException If there was an error setting the resource record values.
	 */
	public void setResourceRecords(@Nonnull final String type, @Nonnull final DomainName name, @Nonnull final Stream<String> values, @Nonnegative final long ttl)
			throws IOException;

	/**
	 * Sets a resource record. If a resource record with the same type and name does not already exists, it will be added. If a resource record already exists
	 * with the same type and name, it will be replaced. (This is commonly referred to as <dfn>upsert</dfn>.)
	 * <p>
	 * Resource record names will be resolved against the origin domain name, if any. {@link #DEFAULT_TTL} will be used if no TTL is indicated.
	 * </p>
	 * @implSpec The default implementation delegates to {@link #setResourceRecord(String, DomainName, String, long)}.
	 * @param resourceRecord The resource record to set.
	 * @throws IOException If there was an error setting the resource record.
	 * @see #getOrigin()
	 * @see #DEFAULT_TTL
	 */
	public default void setResourceRecord(@Nonnull final ResourceRecord resourceRecord) throws IOException {
		final DomainName origin = getOrigin();
		final DomainName name = origin.resolve(resourceRecord.getName().orElse(DomainName.EMPTY));
		final long ttl = resourceRecord.getTtl().orElse(DEFAULT_TTL);
		setResourceRecord(resourceRecord.getType(), name, resourceRecord.getValue(), ttl);
	}

	/**
	 * Sets the given resource records in this DNS zone. For each unit type and name in the given group of resource records, all other values not given will be
	 * removed.
	 * <p>
	 * Resource record names will be resolved against the origin domain name, if any. {@link #DEFAULT_TTL} will be used if no TTL is indicated. If a set of
	 * records have different TTL values, it is undefined which TTL value will be used, although if one of the combined records has a TTL value is guaranteed that
	 * the combined record will have a TTL value.
	 * </p>
	 * @implSpec The default implementation delegates to {@link #setResourceRecords(Stream)}.
	 * @param resourceRecords The resource records to set.
	 * @throws IOException If there was an error setting a resource record.
	 * @see #getOrigin()
	 * @see #DEFAULT_TTL
	 */
	public default void setResourceRecords(@Nonnull final Collection<ResourceRecord> resourceRecords) throws IOException {
		setResourceRecords(resourceRecords.stream());
	}

	/**
	 * Sets the given resource records in this DNS zone. For each unit type and name in the given group of resource records, all other values not given will be
	 * removed.
	 * <p>
	 * Resource record names will be resolved against the origin domain name, if any. {@link #DEFAULT_TTL} will be used if no TTL is indicated. If a set of
	 * records have different TTL values, it is undefined which TTL value will be used, although if one of the combined records has a TTL value is guaranteed that
	 * the combined record will have a TTL value.
	 * </p>
	 * @param resourceRecords The resource records to set.
	 * @implSpec The default implementation groups together the values of records with the same type and resolved name and delegates to
	 *           {@link #setResourceRecords(String, DomainName, Stream, long)}.
	 * @throws IOException If there was an error setting a resource record.
	 * @see #getOrigin()
	 * @see #DEFAULT_TTL
	 */
	public default void setResourceRecords(@Nonnull final Stream<ResourceRecord> resourceRecords) throws IOException {
		final DomainName origin = getOrigin();
		resourceRecords //group by type+name
				.collect(groupingBy(resourceRecord -> Map.entry(resourceRecord.getType(), origin.resolve(resourceRecord.getName().orElse(DomainName.EMPTY)))))
				.forEach(throwingBiConsumer((key, resourceRecordGroup) -> {
					assert !resourceRecordGroup.isEmpty() : "Grouping should not have produced an empty group.";
					setResourceRecords(key.getKey(), key.getValue(), resourceRecordGroup.stream().map(ResourceRecord::getValue),
							resourceRecordGroup.stream().filter(resourceRecord -> resourceRecord.getTtl().isPresent()).findAny().map(ResourceRecord::getTtl)
									.map(OptionalLong::getAsLong).orElse(DEFAULT_TTL));

				}));
	}

	/**
	 * Determines the domain name to use as the base for the hosted zone. This method determines the domain in the following order:
	 * <ol>
	 * <li>The key {@value #CONFIG_KEY_ORIGIN} relative to the DNS local configuration.</li>
	 * <li>The key {@value GuiseMummy#CONFIG_KEY_DOMAIN} retrieved from the global configuration.</li>
	 * <li>The site base domain determined by the longest common domain segment suffix from the site domain {@value GuiseMummy#CONFIG_KEY_SITE_DOMAIN} and
	 * alternative domains {@value GuiseMummy#CONFIG_KEY_SITE_ALT_DOMAINS}, retrieved from the global configuration.</li>
	 * </ol>
	 * @implSpec This method calls {@link GuiseMummy#findConfiguredDomain(Configuration)}, {@link GuiseMummy#findConfiguredSiteDomain(Configuration)}, and
	 *           {@link GuiseMummy#findConfiguredSiteAltDomains(Configuration)}.
	 * @param globalConfiguration The configuration containing all the configuration values.
	 * @param localConfiguration The local configuration for the Route 53 DNS, which may be a section of the project configuration.
	 * @return The base domain to be managed by the hosted zone.
	 * @throws ConfigurationException if no origin is configured, or the configured origin is not a full-qualified domain name.
	 * @see #CONFIG_KEY_ORIGIN
	 * @see GuiseMummy#CONFIG_KEY_DOMAIN
	 * @see GuiseMummy#CONFIG_KEY_SITE_DOMAIN
	 * @see GuiseMummy#CONFIG_KEY_SITE_ALT_DOMAINS
	 */
	static DomainName getConfiguredOrigin(@Nonnull final Configuration globalConfiguration, @Nonnull final Configuration localConfiguration)
			throws ConfigurationException {
		//local hosted zone designation
		return localConfiguration.findString(CONFIG_KEY_ORIGIN).map(DomainName::of).map(hostedZoneName -> {
			if(!hostedZoneName.isAbsolute() || hostedZoneName.isRoot()) {
				throw new ConfigurationException(
						String.format("The DNS zone `%s` configuration `%s` must be a fully-qualified, non-root domain name (FQDN), ending in a dot `%s` character.",
								CONFIG_KEY_ORIGIN, hostedZoneName, DomainName.DELIMITER)); //TODO i18n
			}
			return hostedZoneName;
		})
				//fall back to global site domain configuration
				.or(() -> findConfiguredDomain(globalConfiguration))
				//determined base domain
				.or(() -> {
					final List<DomainName> domains = Stream
							.concat(findConfiguredSiteDomain(globalConfiguration).stream(), findConfiguredSiteAltDomains(globalConfiguration).orElse(emptyList()).stream())
							.collect(toList());
					return DomainName.findGreatestCommonBase(domains);
				}).orElseThrow(() -> new ConfigurationException("No origin domain could be determined for DNS zone."));
	}

	/**
	 * Determines the resource records that have been configured use for the zone section, usually the DNS section itself, of the configuration.
	 * @implNote This implementation attempts to normalize values to their encoded form for some recognized resource record types such as
	 *           {@link ResourceRecord.Type#TXT} in case the producer has provided an encoded value, e.g. by copying from configuration documentation.
	 * @param zone The configuration section defining the zone.
	 * @return The configured resource records.
	 * @throws ConfigurationException if a resource record has invalid in incomplete information, such as a missing type or a negative TTL.
	 * @see Dns#CONFIG_KEY_RECORDS
	 * @see Dns#RECORD_CONFIG_KEY_TYPE
	 * @see Dns#RECORD_CONFIG_KEY_NAME
	 * @see Dns#RECORD_CONFIG_KEY_VALUE
	 * @see Dns#RECORD_CONFIG_KEY_TTL
	 */
	static Collection<ResourceRecord> getConfiguredResourceRecords(@Nonnull final Configuration zone) throws ConfigurationException {
		return zone.findCollection(CONFIG_KEY_RECORDS, Section.class).map(records -> {
			return records.stream().map(record -> {
				try {
					final String type = record.getString(RECORD_CONFIG_KEY_TYPE);
					final DomainName name = record.findString(RECORD_CONFIG_KEY_NAME).map(DomainName::of).orElse(null);
					final String value = ResourceRecord.decodeCharactString(record.getString(RECORD_CONFIG_KEY_VALUE));
					//normalize the configured value if possible for recognized resource record types
					final String normalizedValue = Enums.asEnum(ResourceRecord.Type.class, type).map(resourceRecordType -> {
						switch(resourceRecordType) {
							case TXT:
								return ResourceRecord.normalizeCharacterString(value);
							default:
								return value;
						}
					}).orElse(value);
					final OptionalLong configuredTtl = record.findLong(RECORD_CONFIG_KEY_TTL);
					configuredTtl.ifPresent(Conditions::checkArgumentNotNegative);
					final long ttl = configuredTtl.orElse(-1);
					return new ResourceRecord(type, name, normalizedValue, ttl);
				} catch(final IllegalArgumentException illegalArgumentException) {
					throw new ConfigurationException("Invalid DNS resource record; " + illegalArgumentException.getMessage(), illegalArgumentException);
				}
			}).collect(toList());
		}).orElse(emptyList());
	}

}
