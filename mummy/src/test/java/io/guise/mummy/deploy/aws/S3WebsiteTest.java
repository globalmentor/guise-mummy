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

import java.net.URI;
import java.util.*;

import org.junit.jupiter.api.*;

import io.confound.config.*;
import io.guise.mummy.GuiseMummy;
import software.amazon.awssdk.regions.Region;

/**
 * Tests of {@link S3}.
 * @author Garret Wilson
 */
public class S3Test {

	//# configuration

	//# `….bucket`

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

	//# `….altBuckets`

	/*** @see S3#getConfiguredAltBuckets(Configuration, Configuration) */
	@Test
	public void testGetConfiguredAltBuckets() {
		final Configuration globalConfiguration = new ObjectMapConfiguration(Map.of(GuiseMummy.CONFIG_KEY_SITE_DOMAIN, "test.example.com.",
				GuiseMummy.CONFIG_KEY_SITE_ALT_DOMAINS, List.of("foo.example.com.", "bar.example.com.")));
		final Configuration localConfiguration = new ObjectMapConfiguration(Map.of(S3.CONFIG_KEY_ALT_BUCKETS, List.of("foo", "example.net", "bar")));
		assertThat(S3.getConfiguredAltBuckets(globalConfiguration, localConfiguration), contains("foo", "example.net", "bar"));
	}

	/*** @see S3#getConfiguredAltBuckets(Configuration, Configuration) */
	@Test
	public void testGetConfiguredBucketMissingIsEmptyCollection() {
		assertThat(S3.getConfiguredAltBuckets(Configuration.empty(), Configuration.empty()), is(empty()));
	}

	/*** @see S3#getConfiguredAltBuckets(Configuration, Configuration) */
	@Test
	public void testGetConfiguredAltBucketsDefaultToSiteAltDomains() {
		final Configuration globalConfiguration = new ObjectMapConfiguration(Map.of(GuiseMummy.CONFIG_KEY_SITE_DOMAIN, "test.example.com.",
				GuiseMummy.CONFIG_KEY_SITE_ALT_DOMAINS, List.of("foo.example.com.", "bar.example.com.")));
		final Configuration localConfiguration = Configuration.empty();
		assertThat(S3.getConfiguredAltBuckets(globalConfiguration, localConfiguration), contains("foo.example.com", "bar.example.com"));
	}

	/** @see S3#getBucketWebsiteEndpoint(String, Region) */
	@Test
	public void testGetBucketWebsiteEndpoint() {
		assertThat(S3.getBucketWebsiteEndpoint("foobar", Region.US_EAST_1), is("foobar.s3-website-us-east-1.amazonaws.com"));
		assertThat(S3.getBucketWebsiteEndpoint("foobar", Region.US_EAST_2), is("foobar.s3-website.us-east-2.amazonaws.com"));
		assertThat(S3.getBucketWebsiteEndpoint("foobar", Region.US_WEST_1), is("foobar.s3-website-us-west-1.amazonaws.com"));
		assertThat(S3.getBucketWebsiteEndpoint("foobar", Region.US_WEST_2), is("foobar.s3-website-us-west-2.amazonaws.com"));
		assertThat(S3.getBucketWebsiteEndpoint("foobar", Region.AP_SOUTH_1), is("foobar.s3-website.ap-south-1.amazonaws.com"));
		assertThat(S3.getBucketWebsiteEndpoint("foobar", Region.SA_EAST_1), is("foobar.s3-website-sa-east-1.amazonaws.com"));
	}

	/** @see S3#getBucketWebsiteUrl(String, Region) */
	@Test
	public void testGetBucketWebsiteUrl() {
		assertThat(S3.getBucketWebsiteUrl("foobar", Region.US_EAST_1), is(URI.create("http://foobar.s3-website-us-east-1.amazonaws.com/")));
		assertThat(S3.getBucketWebsiteUrl("foobar", Region.US_EAST_2), is(URI.create("http://foobar.s3-website.us-east-2.amazonaws.com/")));
		assertThat(S3.getBucketWebsiteUrl("foobar", Region.US_WEST_1), is(URI.create("http://foobar.s3-website-us-west-1.amazonaws.com/")));
		assertThat(S3.getBucketWebsiteUrl("foobar", Region.US_WEST_2), is(URI.create("http://foobar.s3-website-us-west-2.amazonaws.com/")));
		assertThat(S3.getBucketWebsiteUrl("foobar", Region.AP_SOUTH_1), is(URI.create("http://foobar.s3-website.ap-south-1.amazonaws.com/")));
		assertThat(S3.getBucketWebsiteUrl("foobar", Region.SA_EAST_1), is(URI.create("http://foobar.s3-website-sa-east-1.amazonaws.com/")));
	}

}
