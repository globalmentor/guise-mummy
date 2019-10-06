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

import java.net.URI;

import org.junit.jupiter.api.*;

import software.amazon.awssdk.regions.Region;

/**
 * Tests of {@link S3}.
 * @author Garret Wilson
 */
public class S3Test {

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
