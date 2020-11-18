/*
 * Copyright © 2020 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.guise.mummy.mummify.image;

import static com.github.npathai.hamcrestopt.OptionalMatchers.*;
import static com.globalmentor.io.Images.*;
import static com.globalmentor.java.OperatingSystem.*;
import static org.hamcrest.MatcherAssert.*;

import java.io.IOException;
import java.nio.file.Paths;

import org.junit.jupiter.api.*;

import io.confound.config.Configuration;
import io.guise.mummy.*;

/**
 * Tests of {@link DefaultImageMummifier}.
 * @author Garret Wilson
 */
public class DefaultImageMummifierTest {

	private MummyContext fixtureContext;

	@BeforeEach
	protected void setupFixture() {
		final GuiseProject project = new DefaultGuiseProject(getWorkingDirectory(), Configuration.empty());
		fixtureContext = new StubMummyContext(project);
	}

	private BaseImageMummifier testMummifier;

	@BeforeEach
	private void setupTestMummifier() throws IOException {
		testMummifier = new DefaultImageMummifier();
	}

	@Test
	public void testGetArtifactMediaType() throws IOException {
		assertThat(testMummifier.getArtifactMediaType(fixtureContext, Paths.get("test.bmp")), isEmpty());
		assertThat(testMummifier.getArtifactMediaType(fixtureContext, Paths.get("test.BMP")), isEmpty());
		assertThat(testMummifier.getArtifactMediaType(fixtureContext, Paths.get("test.gif")), isPresentAndIs(GIF_MEDIA_TYPE));
		assertThat(testMummifier.getArtifactMediaType(fixtureContext, Paths.get("test.GIF")), isPresentAndIs(GIF_MEDIA_TYPE));
		assertThat(testMummifier.getArtifactMediaType(fixtureContext, Paths.get("test.jpg")), isPresentAndIs(JPEG_MEDIA_TYPE));
		assertThat(testMummifier.getArtifactMediaType(fixtureContext, Paths.get("test.JPG")), isPresentAndIs(JPEG_MEDIA_TYPE));
		assertThat(testMummifier.getArtifactMediaType(fixtureContext, Paths.get("test.jpeg")), isPresentAndIs(JPEG_MEDIA_TYPE));
		assertThat(testMummifier.getArtifactMediaType(fixtureContext, Paths.get("test.JPEG")), isPresentAndIs(JPEG_MEDIA_TYPE));
		assertThat(testMummifier.getArtifactMediaType(fixtureContext, Paths.get("test.png")), isPresentAndIs(PNG_MEDIA_TYPE));
		assertThat(testMummifier.getArtifactMediaType(fixtureContext, Paths.get("test.PNG")), isPresentAndIs(PNG_MEDIA_TYPE));
	}

}
