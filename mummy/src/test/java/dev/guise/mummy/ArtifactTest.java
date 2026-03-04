/*
 * Copyright © 2026 GlobalMentor, Inc. <https://www.globalmentor.com/>
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

package dev.guise.mummy;

import static com.globalmentor.java.OperatingSystem.*;
import static com.globalmentor.net.URIs.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.*;

import dev.guise.mummy.mummify.Mummifier;
import dev.guise.mummy.mummify.collection.DirectoryArtifact;
import dev.guise.mummy.mummify.page.PageMummifier;

/// Tests for static methods on [Artifact].
class ArtifactTest {

	private static final Path PROJECT_DIRECTORY = getTempDirectory().resolve("project");
	private static final Path SOURCE_DIRECTORY = PROJECT_DIRECTORY.resolve("src").resolve("site");
	private static final Path TARGET_DIRECTORY = PROJECT_DIRECTORY.resolve("target").resolve("site");
	private static final URI ROOT_TARGET_PATH_URI = toCollectionURI(TARGET_DIRECTORY.toUri());

	//## `relativizeResourceReference(URI, URI, boolean)`

	/// Tests for [Artifact#relativizeResourceReference(URI, URI, boolean)].
	@Test
	void testRelativizeResourceReferenceUriCore() {
		final URI fileUri = TARGET_DIRECTORY.resolve("about.html").toUri();
		final URI dirUri = TARGET_DIRECTORY.resolve("sub").toUri();
		assertThat("non-collection without forceCollection", Artifact.relativizeResourceReference(ROOT_TARGET_PATH_URI, fileUri, false).toString(),
				is("about.html"));
		assertThat("directory with forceCollection adds trailing slash", Artifact.relativizeResourceReference(ROOT_TARGET_PATH_URI, dirUri, true).toString(),
				is("sub/"));
		assertThat("directory without forceCollection has no trailing slash", Artifact.relativizeResourceReference(ROOT_TARGET_PATH_URI, dirUri, false).toString(),
				is("sub"));
		assertThat("nested path",
				Artifact.relativizeResourceReference(ROOT_TARGET_PATH_URI, TARGET_DIRECTORY.resolve("sub").resolve("deep.html").toUri(), false).toString(),
				is("sub/deep.html"));
		assertThat("root relativizes to empty path", Artifact.relativizeResourceReference(ROOT_TARGET_PATH_URI, ROOT_TARGET_PATH_URI, false).toString(), is(""));
	}

	//## `relativizeResourceReference(URI, Artifact)`

	/// Tests for [Artifact#relativizeResourceReference(URI, Artifact)].
	@Test
	void testRelativizeResourceReferenceArtifact() {
		final Mummifier directoryMummifier = mock(Mummifier.class);
		final PageMummifier pageMummifier = mock(PageMummifier.class);
		final Artifact page = new DummyArtifact(pageMummifier, SOURCE_DIRECTORY.resolve("about.html"), TARGET_DIRECTORY.resolve("about.html"));
		final DirectoryArtifact subDir = new DirectoryArtifact(directoryMummifier, SOURCE_DIRECTORY.resolve("sub"), TARGET_DIRECTORY.resolve("sub"), null,
				Set.of());
		final Artifact nested = new DummyArtifact(pageMummifier, SOURCE_DIRECTORY.resolve("sub").resolve("deep.html"),
				TARGET_DIRECTORY.resolve("sub").resolve("deep.html"));
		final DirectoryArtifact root = new DirectoryArtifact(directoryMummifier, SOURCE_DIRECTORY, TARGET_DIRECTORY, null, Set.of());
		assertThat("non-collection produces path without trailing slash", Artifact.relativizeResourceReference(ROOT_TARGET_PATH_URI, page).toString(),
				is("about.html"));
		assertThat("collection produces path with trailing slash via toCollectionURI",
				Artifact.relativizeResourceReference(ROOT_TARGET_PATH_URI, subDir).toString(), is("sub/"));
		assertThat("nested artifact produces multi-segment path", Artifact.relativizeResourceReference(ROOT_TARGET_PATH_URI, nested).toString(),
				is("sub/deep.html"));
		assertThat("root artifact relativizes to empty path", Artifact.relativizeResourceReference(ROOT_TARGET_PATH_URI, root).toString(), is(""));
	}

}
