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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;

import io.confound.config.*;
import io.guise.mummy.GuiseMummy;

/**
 * Tests of {@link S3}.
 * @author Garret Wilson
 */
public class S3Test {

	//# configuration

	//## `….bucket`

	/*** @see S3#getConfiguredBucket(Configuration, Configuration) */
	@Test
	public void testGetConfiguredBucket() {
		final Configuration globalConfiguration = new ObjectMapConfiguration(Map.of(GuiseMummy.CONFIG_KEY_SITE_DOMAIN, "test.example.com.",
				GuiseMummy.CONFIG_KEY_SITE_ALT_DOMAINS, List.of("foo.example.com.", "bar.example.com.")));
		final Configuration localConfiguration = new StringMapConfiguration(Map.of(S3.CONFIG_KEY_BUCKET, "example.net"));
		assertThat(S3.getConfiguredBucket(globalConfiguration, localConfiguration), is("example.net"));
	}

	/*** @see S3#getConfiguredBucket(Configuration, Configuration) */
	@Test
	public void testGetConfiguredBucketMissingThrowsException() {
		assertThrows(ConfigurationException.class, () -> S3.getConfiguredBucket(Configuration.empty(), Configuration.empty()));
	}

	/*** @see S3#getConfiguredBucket(Configuration, Configuration) */
	@Test
	public void testGetConfiguredBucketDefaultsToSiteDomain() {
		final Configuration globalConfiguration = new ObjectMapConfiguration(Map.of(GuiseMummy.CONFIG_KEY_SITE_DOMAIN, "test.example.com.",
				GuiseMummy.CONFIG_KEY_SITE_ALT_DOMAINS, List.of("foo.example.com.", "bar.example.com.")));
		final Configuration localConfiguration = Configuration.empty();
		assertThat(S3.getConfiguredBucket(globalConfiguration, localConfiguration), is("test.example.com"));
	}

}
