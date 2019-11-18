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

import static com.globalmentor.java.OperatingSystem.*;
import static java.util.stream.Collectors.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;

import org.junit.jupiter.api.*;

import io.confound.config.Configuration;
import io.guise.mummy.deploy.*;
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
		final GuiseProject project = new DefaultGuiseProject(getWorkingDirectory(), Configuration.empty()); //TODO improve mock project
		mummyContext = new BaseMummyContext(project) { //TODO move to separate mock context

			@Override
			public Configuration getConfiguration() {
				return getProject().getConfiguration();
			}

			@Override
			public SourcePathMummifier getDefaultSourceFileMummifier() {
				throw new UnsupportedOperationException();
			}

			@Override
			public SourcePathMummifier getDefaultSourceDirectoryMummifier() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Optional<SourcePathMummifier> findRegisteredMummifierForSourceFile(Path sourceFile) {
				return Optional.empty();
			}

			@Override
			public Optional<SourcePathMummifier> findRegisteredMummifierForSourceDirectory(Path sourceDirectory) {
				return Optional.empty();
			}

			@Override
			public Optional<Dns> getDeployDns() {
				return Optional.empty();
			}

			@Override
			public Optional<List<DeployTarget>> getDeployTargets() {
				return Optional.empty();
			}

		};
	}

	/**
	 * @see XhtmlPageMummifier#sourceMetadata(MummyContext, InputStream, String)
	 * @see #SIMPLE_METADATA_XHTML_RESOURCE_NAME
	 */
	@Test
	public void testSimpleXhtmlMetadata() throws IOException {
		final XhtmlPageMummifier mummifier = new XhtmlPageMummifier();
		try (final InputStream inputStream = getClass().getResourceAsStream(SIMPLE_METADATA_XHTML_RESOURCE_NAME)) {
			assertThat(mummifier.sourceMetadata(mummyContext, inputStream, SIMPLE_METADATA_XHTML_RESOURCE_NAME).collect(toList()),
					containsInAnyOrder(Map.entry(Handle.toTag("title"), "Simple Page with Other Metadata"), Map.entry(Handle.toTag("label"), "Simplicity"),
							Map.entry(Handle.toTag("fooBar"), "This is a test."),
							//namespace declared on ancestor node
							Map.entry(Artifact.PROPERTY_TAG_MUMMY_ORDER, "3"),
							//undeclared yet predefined vocabulary prefix
							Map.entry(URI.create("http://ogp.me/ns#type"), "website")));
		}
	}

}
