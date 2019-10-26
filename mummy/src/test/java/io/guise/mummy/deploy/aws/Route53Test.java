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

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.*;

import com.globalmentor.net.DomainName;
import com.globalmentor.net.ResourceRecord;

/**
 * Tests of {@link Route53}.
 * @author Garret Wilson
 */
public class Route53Test {

	/** @see Route53#normalizeValueForType(String, String) */
	@Test
	public void testNormalizeValueForType() {
		assertThat(Route53.normalizeValueForType("CNAME", "example.com."), is("example.com."));
		assertThat(Route53.normalizeValueForType("MX", "10 smtp.example.com."), is("10 smtp.example.com."));
		assertThat(Route53.normalizeValueForType("TXT", "example.com."), is("\"example.com.\"")); //TXT gets quoted no matter what
		assertThat(Route53.normalizeValueForType("TXT", "foobar"), is("\"foobar\"")); //TXT gets quoted no matter what
		assertThat(Route53.normalizeValueForType("TXT", "foo bar"), is("\"foo bar\"")); //TXT gets quoted no matter what
	}

	/** @see Route53#transformResourceRecordForSet(DomainName, Collection) */
	@Test
	public void transformResourceRecordForSet() {
		final DomainName origin = DomainName.of("example.com.");
		assertThat(
				Route53.transformResourceRecordForSet(origin, List.of(new ResourceRecord("CNAME", DomainName.of("www.example.com."), "other.example.com.", 300L))),
				containsInAnyOrder(new ResourceRecord("CNAME", DomainName.of("www.example.com."), "other.example.com.", 300L)));
		assertThat(Route53.transformResourceRecordForSet(origin, List.of(new ResourceRecord("MX", DomainName.EMPTY, "10 smtp1.example.com.", -1))),
				containsInAnyOrder(new ResourceRecord("MX", DomainName.of("example.com."), "10 smtp1.example.com.", -1)));
		assertThat(
				Route53.transformResourceRecordForSet(origin,
						List.of(new ResourceRecord("CNAME", DomainName.of("www"), "other.example.com.", 300L),
								new ResourceRecord("MX", DomainName.EMPTY, "10 smtp1.example.com.", -1),
								new ResourceRecord("TXT", DomainName.of("example.com."), "\"foo bar\"", 600L), new ResourceRecord("TXT", DomainName.EMPTY, "abcdefg", -1),
								new ResourceRecord("TXT", DomainName.of("example.com."), "this=that; other/thing", 600L),
								new ResourceRecord("MX", DomainName.of("example.com."), "20 smtp2.example.com.", 1200L))),
				containsInAnyOrder(new ResourceRecord("CNAME", DomainName.of("www.example.com."), "other.example.com.", 300L),
						new ResourceRecord("MX", DomainName.of("example.com."), "10 smtp1.example.com.\n20 smtp2.example.com.", 1200L),
						new ResourceRecord("TXT", DomainName.of("example.com."), "\"foo bar\"\n\"abcdefg\"\n\"this=that; other/thing\"", 600L)));
	}

}
