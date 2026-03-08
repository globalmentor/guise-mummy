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
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.*;

import dev.guise.mummy.mummify.Mummifier;
import dev.guise.mummy.mummify.collection.DirectoryArtifact;

/// Tests for [AbstractMummyPlan].
class AbstractMummyPlanTest {

	private static final Path PROJECT_DIRECTORY = getTempDirectory().resolve("project");
	private static final Path SOURCE_DIRECTORY = PROJECT_DIRECTORY.resolve("src").resolve("site");
	private static final Path TARGET_DIRECTORY = PROJECT_DIRECTORY.resolve("target").resolve("site");

	/// Minimal concrete subclass for testing.
	private static class TestPlan extends AbstractMummyPlan {
		TestPlan(final Artifact rootArtifact) {
			super(rootArtifact);
		}

		@Override
		public Artifact getPrincipalArtifact(final Artifact artifact) {
			return artifact;
		}

		@Override
		public Optional<Artifact> findParentArtifact(final Artifact artifact) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Optional<Artifact> findArtifactBySourceReference(final Path referenceSourcePath) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ArtifactQuery queryArtifacts() {
			throw new UnsupportedOperationException();
		}
	}

	private final Mummifier directoryMummifier = mock(Mummifier.class);
	private final DirectoryArtifact rootArtifact = new DirectoryArtifact(directoryMummifier, SOURCE_DIRECTORY, TARGET_DIRECTORY, null, Set.of());
	private final TestPlan plan = new TestPlan(rootArtifact);

	//## happy path

	/// Tests that [AbstractMummyPlan#relativizeResourceReference(Path, Path, boolean)] correctly relativizes paths in the source tree and the target tree.
	@Test
	void testRelativizeResourceReferenceHappyPath() {
		final Path sourceBase = SOURCE_DIRECTORY.resolve("index.html"); // base is a file, as in `referenceInSource`
		final Path targetBase = TARGET_DIRECTORY.resolve("index.html");
		assertThat("file in source tree", plan.relativizeResourceReference(sourceBase, SOURCE_DIRECTORY.resolve("about.html"), false).toString(), is("about.html"));
		assertThat("directory in source tree with forceCollection", plan.relativizeResourceReference(sourceBase, SOURCE_DIRECTORY.resolve("blog"), true).toString(),
				is("blog/"));
		assertThat("file in target tree", plan.relativizeResourceReference(targetBase, TARGET_DIRECTORY.resolve("about.html"), false).toString(), is("about.html"));
		assertThat("nested file in target tree",
				plan.relativizeResourceReference(targetBase, TARGET_DIRECTORY.resolve("blog").resolve("post.html"), false).toString(), is("blog/post.html"));
	}

	//## validation

	/// Tests that [AbstractMummyPlan#relativizeResourceReference(Path, Path, boolean)] rejects invalid paths.
	@Test
	void testRelativizeResourceReferenceRejectsInvalidPaths() {
		final Path outsidePath = getTempDirectory().resolve("elsewhere").resolve("file.html");
		final Path outsideBase = getTempDirectory().resolve("elsewhere");
		assertThrows(IllegalArgumentException.class, () -> plan.relativizeResourceReference(Path.of("relative"), SOURCE_DIRECTORY.resolve("about.html"), false),
				"relative base path");
		assertThrows(IllegalArgumentException.class, () -> plan.relativizeResourceReference(SOURCE_DIRECTORY, Path.of("about.html"), false),
				"relative reference path");
		assertThrows(IllegalArgumentException.class, () -> plan.relativizeResourceReference(SOURCE_DIRECTORY.resolve("index.html"), outsidePath, false),
				"reference path outside source and target trees");
		assertThrows(IllegalArgumentException.class, () -> plan.relativizeResourceReference(outsideBase, SOURCE_DIRECTORY.resolve("about.html"), false),
				"base path outside source and target trees");
	}

}
