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

package io.guise.mummy.deploy;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;

import com.globalmentor.net.DomainName;

import io.confound.config.*;
import io.guise.mummy.GuiseMummy;

/**
 * Tests of {@link Dns}.
 * @author Garret Wilson
 */
public class DnsTest {

	/**
	 * @see Dns#getConfiguredOrigin(Configuration, Configuration)
	 * @see Dns#CONFIG_KEY_ORIGIN
	 */
	@Test
	public void testGetConfiguredHostedZoneUsesDomainConfiguredLocally() {
		final Configuration globalConfiguration = new ObjectMapConfiguration(Map.of(GuiseMummy.CONFIG_KEY_SITE_DOMAIN, "test.example.com.",
				GuiseMummy.CONFIG_KEY_SITE_ALT_DOMAINS, List.of("foo.example.com.", "bar.example.com.")));
		final Configuration localConfiguration = new StringMapConfiguration(Map.of(Dns.CONFIG_KEY_ORIGIN, "example.net."));
		assertThat(Dns.getConfiguredOrigin(globalConfiguration, localConfiguration), is(DomainName.of("example.net.")));
	}

	/**
	 * @see Dns#getConfiguredOrigin(Configuration, Configuration)
	 * @see Dns#CONFIG_KEY_ORIGIN
	 */
	@Test
	public void testGetConfiguredHostedZoneNotAbsoluteThrowsException() {
		final Configuration localConfiguration = new StringMapConfiguration(Map.of(Dns.CONFIG_KEY_ORIGIN, "example.net"));
		assertThrows(ConfigurationException.class, () -> Dns.getConfiguredOrigin(Configuration.empty(), localConfiguration));
	}

	/**
	 * @see Dns#getConfiguredOrigin(Configuration, Configuration)
	 * @see Dns#CONFIG_KEY_ORIGIN
	 */
	@Test
	public void testGetConfiguredHostedZoneRootThrowsException() {
		final Configuration localConfiguration = new StringMapConfiguration(Map.of(Dns.CONFIG_KEY_ORIGIN, "."));
		assertThrows(ConfigurationException.class, () -> Dns.getConfiguredOrigin(Configuration.empty(), localConfiguration));
	}

	/*** @see Dns#getConfiguredOrigin(Configuration, Configuration) */
	@Test
	public void testGetConfiguredHostedZoneDefaultsToProjectDomain() {
		final Configuration globalConfiguration = new ObjectMapConfiguration(Map.of(GuiseMummy.CONFIG_KEY_DOMAIN, "example.net.", GuiseMummy.CONFIG_KEY_SITE_DOMAIN,
				"test.example.com.", GuiseMummy.CONFIG_KEY_SITE_ALT_DOMAINS, List.of("foo.example.com.", "bar.example.com.")));
		final Configuration localConfiguration = Configuration.empty();
		assertThat(Dns.getConfiguredOrigin(globalConfiguration, localConfiguration), is(DomainName.of("example.net.")));
	}

	/*** @see Dns#getConfiguredOrigin(Configuration, Configuration) */
	@Test
	public void testGetConfiguredHostedZoneFallsBackToSiteBaseDomain() {
		final Configuration globalConfiguration = new ObjectMapConfiguration(Map.of(GuiseMummy.CONFIG_KEY_SITE_DOMAIN, "test.example.com.",
				GuiseMummy.CONFIG_KEY_SITE_ALT_DOMAINS, List.of("foo.example.com.", "bar.example.com.")));
		final Configuration localConfiguration = Configuration.empty();
		assertThat(Dns.getConfiguredOrigin(globalConfiguration, localConfiguration), is(DomainName.of("example.com.")));
	}

}
