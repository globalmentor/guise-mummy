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

package io.guise.mummy.mummify.page;

import static com.globalmentor.java.OperatingSystem.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.*;
import java.net.URI;
import java.time.LocalDate;
import java.util.*;

import org.junit.jupiter.api.*;

import io.confound.config.Configuration;
import io.guise.mummy.*;
import io.guise.mummy.mummify.page.XhtmlPageMummifier;
import io.urf.URF.Handle;

/**
 * Tests of {@link XhtmlPageMummifier}.
 * @author Garret Wilson
 */
public class XhtmlPageMummifierTest {

	public static final String SIMPLE_METADATA_XHTML_RESOURCE_NAME = "simple-metadata.xhtml";

	private MummyContext mummyContext;

	@BeforeEach
	protected void setupContext() {
		final GuiseProject project = new DefaultGuiseProject(getWorkingDirectory(), Configuration.empty());
		mummyContext = new StubMummyContext(project);
	}

	/**
	 * @see XhtmlPageMummifier#loadSourceMetadata(MummyContext, InputStream, String)
	 * @see #SIMPLE_METADATA_XHTML_RESOURCE_NAME
	 */
	@Test
	public void testSimpleXhtmlMetadata() throws IOException {
		final XhtmlPageMummifier mummifier = new XhtmlPageMummifier();
		try (final InputStream inputStream = getClass().getResourceAsStream(SIMPLE_METADATA_XHTML_RESOURCE_NAME)) {
			assertThat(mummifier.loadSourceMetadata(mummyContext, inputStream, SIMPLE_METADATA_XHTML_RESOURCE_NAME),
					containsInAnyOrder(Map.entry(Handle.toTag("title"), "Simple Page with Other Metadata"), Map.entry(Handle.toTag("label"), "Simplicity"),
							Map.entry(Handle.toTag("fooBar"), "This is a test."),
							//property defined in `property` attribute, inferred to be a local date from its name pattern
							Map.entry(Handle.toTag(Artifact.PROPERTY_HANDLE_PUBLISHED_ON), LocalDate.of(2001, 2, 3)),
							//property defined in `property` attribute with namespace declared on ancestor node
							Map.entry(Artifact.PROPERTY_TAG_MUMMY_ORDER, "3"),
							//property defined in `property` attribute with undeclared yet predefined vocabulary prefix
							Map.entry(URI.create("http://ogp.me/ns#type"), "website"),
							//two defined in both `name` and `property` attribute on same element
							Map.entry(Handle.toTag("photo"), "https://example.com/picture.jpg"),
							Map.entry(URI.create("http://ogp.me/ns#image"), "https://example.com/picture.jpg"),
							//multiple properties indicated in the same property attribute
							Map.entry(GuiseMummy.NAMESPACE.resolve("foo"), "foobar test"), Map.entry(GuiseMummy.NAMESPACE.resolve("bar"), "foobar test"),
							Map.entry(GuiseMummy.NAMESPACE.resolve("test"), "foobar test")));
		}
	}

}
