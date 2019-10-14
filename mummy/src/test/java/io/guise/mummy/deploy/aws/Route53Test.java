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

import static com.github.npathai.hamcrestopt.OptionalMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.util.*;

import org.junit.jupiter.api.*;

import io.confound.config.*;
import io.guise.mummy.GuiseMummy;

/**
 * Tests of {@link Route53}.
 * @author Garret Wilson
 */
public class Route53Test {

	/*** @see Route53#getConfiguredHostedZoneName(Configuration, Configuration) */
	@Test
	public void testHostedZoneUsesDomainConfiguredLocally() {
		final Configuration globalConfiguration = new ObjectMapConfiguration(
				Map.of(GuiseMummy.CONFIG_KEY_SITE_DOMAIN, "test.example.com", GuiseMummy.CONFIG_KEY_SITE_ALT_DOMAINS, List.of("foo.example.com", "bar.example.com")));
		final Configuration localConfiguration = new StringMapConfiguration(Map.of(Route53.CONFIG_KEY_HOSTED_ZONE_NAME, "example.net"));
		assertThat(Route53.getConfiguredHostedZoneName(globalConfiguration, localConfiguration), isPresentAndIs("example.net"));
	}

	/*** @see Route53#getConfiguredHostedZoneName(Configuration, Configuration) */
	@Test
	public void testHostedZoneDefaultsToSiteBaseDomain() {
		final Configuration globalConfiguration = new ObjectMapConfiguration(
				Map.of(GuiseMummy.CONFIG_KEY_SITE_DOMAIN, "test.example.com", GuiseMummy.CONFIG_KEY_SITE_ALT_DOMAINS, List.of("foo.example.com", "bar.example.com")));
		final Configuration localConfiguration = Configuration.empty();
		assertThat(Route53.getConfiguredHostedZoneName(globalConfiguration, localConfiguration), isPresentAndIs("example.com."));
	}

}
