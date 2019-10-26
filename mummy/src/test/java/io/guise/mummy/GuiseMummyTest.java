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

package io.guise.mummy;

import static com.github.npathai.hamcrestopt.OptionalMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;

import com.globalmentor.net.DomainName;

import io.confound.config.*;
import io.guise.mummy.GuiseMummy;

/**
 * Tests of {@link GuiseMummy}.
 * @author Garret Wilson
 */
public class GuiseMummyTest {

	//# configuration

	//## `domain`

	/** @see GuiseMummy#findConfiguredDomain(Configuration) */
	@Test
	public void testFindConfiguredDomainEmpty() {
		assertThat(GuiseMummy.findConfiguredDomain(Configuration.empty()), isEmpty());
	}

	/** @see GuiseMummy#findConfiguredDomain(Configuration) */
	@Test
	public void testFindConfiguredDomain() {
		final Configuration configuration = new ObjectMapConfiguration(Map.of(GuiseMummy.CONFIG_KEY_DOMAIN, "example.com."));
		assertThat(GuiseMummy.findConfiguredDomain(configuration), isPresentAndIs(DomainName.of("example.com.")));
	}

	/** @see GuiseMummy#findConfiguredDomain(Configuration) */
	@Test
	public void testFindConfiguredDomainNotAbsoluteThrowsException() {
		final Configuration configuration = new ObjectMapConfiguration(Map.of(GuiseMummy.CONFIG_KEY_DOMAIN, "example.com"));
		assertThrows(ConfigurationException.class, () -> GuiseMummy.findConfiguredDomain(configuration));
	}

	/** @see GuiseMummy#findConfiguredDomain(Configuration) */
	@Test
	public void testFindConfiguredDomainRootThrowsException() {
		final Configuration configuration = new ObjectMapConfiguration(Map.of(GuiseMummy.CONFIG_KEY_DOMAIN, "."));
		assertThrows(ConfigurationException.class, () -> GuiseMummy.findConfiguredDomain(configuration));
	}

	//## `site.domain`

	/** @see GuiseMummy#findConfiguredSiteDomain(Configuration) */
	@Test
	public void testFindConfiguredSiteDomainEmpty() {
		assertThat(GuiseMummy.findConfiguredSiteDomain(Configuration.empty()), isEmpty());
	}

	/** @see GuiseMummy#findConfiguredSiteDomain(Configuration) */
	@Test
	public void testFindConfiguredSiteDomain() {
		final Configuration configuration = new ObjectMapConfiguration(
				Map.of(GuiseMummy.CONFIG_KEY_DOMAIN, "example.com.", GuiseMummy.CONFIG_KEY_SITE_DOMAIN, "www.example.com."));
		assertThat(GuiseMummy.findConfiguredSiteDomain(configuration), isPresentAndIs(DomainName.of("www.example.com.")));
	}

	/** @see GuiseMummy#findConfiguredSiteDomain(Configuration) */
	@Test
	public void testFindConfiguredSiteDomainResolvesToDomain() {
		final Configuration configuration = new ObjectMapConfiguration(
				Map.of(GuiseMummy.CONFIG_KEY_DOMAIN, "example.com.", GuiseMummy.CONFIG_KEY_SITE_DOMAIN, "www"));
		assertThat(GuiseMummy.findConfiguredSiteDomain(configuration), isPresentAndIs(DomainName.of("www.example.com.")));
	}

	/** @see GuiseMummy#findConfiguredSiteDomain(Configuration) */
	@Test
	public void testFindConfiguredSiteDomainDefaultsToDomain() {
		final Configuration configuration = new ObjectMapConfiguration(Map.of(GuiseMummy.CONFIG_KEY_DOMAIN, "www.example.com."));
		assertThat(GuiseMummy.findConfiguredSiteDomain(configuration), isPresentAndIs(DomainName.of("www.example.com.")));
	}

	/** @see GuiseMummy#findConfiguredSiteDomain(Configuration) */
	@Test
	public void testFindConfiguredSiteDomainNotAbsoluteThrowsException() {
		final Configuration configuration = new ObjectMapConfiguration(Map.of(GuiseMummy.CONFIG_KEY_SITE_DOMAIN, "example.com"));
		assertThrows(ConfigurationException.class, () -> GuiseMummy.findConfiguredSiteDomain(configuration));
	}

	/** @see GuiseMummy#findConfiguredSiteDomain(Configuration) */
	@Test
	public void testFindConfiguredSiteDomainRootThrowsException() {
		final Configuration configuration = new ObjectMapConfiguration(Map.of(GuiseMummy.CONFIG_KEY_SITE_DOMAIN, "."));
		assertThrows(ConfigurationException.class, () -> GuiseMummy.findConfiguredSiteDomain(configuration));
	}

	//## `site.altDomains`

	/** @see GuiseMummy#findConfiguredSiteAltDomains(Configuration) */
	@Test
	public void testGetConfiguredSiteAltDomainsEmpty() {
		assertThat(GuiseMummy.findConfiguredSiteAltDomains(Configuration.empty()), isEmpty());
	}

	/** @see GuiseMummy#findConfiguredSiteAltDomains(Configuration) */
	@Test
	public void testGetConfiguredSiteAltDomains() {
		final Configuration configuration = new ObjectMapConfiguration(Map.of(GuiseMummy.CONFIG_KEY_SITE_ALT_DOMAINS,
				List.of("example.com.", "www.example.com.", "foo.example.com.", "bar.example.net.", "foo.bar.example.com.")));
		assertThat(GuiseMummy.findConfiguredSiteAltDomains(configuration), isPresentAnd(containsInAnyOrder(DomainName.of("example.com."),
				DomainName.of("www.example.com."), DomainName.of("foo.example.com."), DomainName.of("bar.example.net."), DomainName.of("foo.bar.example.com."))));
	}

	/** @see GuiseMummy#findConfiguredSiteAltDomains(Configuration) */
	@Test
	public void testGetConfiguredSiteAltDomainsResolveToDomain() {
		final Configuration configuration = new ObjectMapConfiguration(Map.of(GuiseMummy.CONFIG_KEY_DOMAIN, "example.com.", GuiseMummy.CONFIG_KEY_SITE_ALT_DOMAINS,
				List.of("", "www", "foo.example.com.", "bar.example.net.", "foo.bar")));
		assertThat(GuiseMummy.findConfiguredSiteAltDomains(configuration), isPresentAnd(containsInAnyOrder(DomainName.of("example.com."),
				DomainName.of("www.example.com."), DomainName.of("foo.example.com."), DomainName.of("bar.example.net."), DomainName.of("foo.bar.example.com."))));
	}

	/** @see GuiseMummy#findConfiguredSiteAltDomains(Configuration) */
	@Test
	public void testGetConfiguredSiteAltDomainsNotAbsoluteThrowsException() {
		final Configuration configuration = new ObjectMapConfiguration(
				Map.of(GuiseMummy.CONFIG_KEY_SITE_ALT_DOMAINS, List.of("example.com.", "www.example.com.", "relative.example.com")));
		assertThrows(ConfigurationException.class, () -> GuiseMummy.findConfiguredSiteAltDomains(configuration));
	}

	/** @see GuiseMummy#findConfiguredSiteAltDomains(Configuration) */
	@Test
	public void testGetConfiguredSiteAltDomainsRootThrowsException() {
		final Configuration configuration = new ObjectMapConfiguration(
				Map.of(GuiseMummy.CONFIG_KEY_SITE_ALT_DOMAINS, List.of("example.com.", "www.example.com.", ".")));
		assertThrows(ConfigurationException.class, () -> GuiseMummy.findConfiguredSiteAltDomains(configuration));
	}

}
