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

package io.guise.mummy;

import static com.github.npathai.hamcrestopt.OptionalMatchers.*;
import static com.globalmentor.java.OperatingSystem.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.*;

import io.guise.mummy.mummify.*;
import io.guise.mummy.mummify.collection.DirectoryArtifact;

/**
 * Tests of {@link DefaultMummyPlan}.
 * @author Garret Wilson
 */
public class DefaultMummyPlanTest {

	/** @see MummyPlan#getPrincipalArtifact(Artifact) */
	@Test
	void testGetPrincipalArtifact() {
		final Path sourceDirectory = getTempDirectory().resolve("source"); //used only for identification; no I/O
		final Path targetDirectory = getTempDirectory().resolve("target");
		final Mummifier mummifier = mock(Mummifier.class);
		final Artifact indexArtifact = new DummyArtifact(mummifier, sourceDirectory.resolve("index.html"), targetDirectory.resolve("index.html"));
		final Artifact childArtifact = new DummyArtifact(mummifier, sourceDirectory.resolve("test.html"), targetDirectory.resolve("test.html"));
		final DirectoryArtifact directoryArtifact = new DirectoryArtifact(mummifier, sourceDirectory, targetDirectory, indexArtifact, Set.of(childArtifact));
		final MummyPlan plan = new DefaultMummyPlan(directoryArtifact);
		assertThat("Directory is its own principal artifact.", plan.getPrincipalArtifact(directoryArtifact), is(directoryArtifact));
		assertThat("Directory is principal artifact of content artifact.", plan.getPrincipalArtifact(indexArtifact), is(directoryArtifact));
		assertThat("Child artifact is its own principal artifact.", plan.getPrincipalArtifact(childArtifact), is(childArtifact));
	}

	/** @see MummyPlan#findArtifactBySourceReference(Path) */
	@Test
	void testFindArtifactBySourceReferenceForSubsumedArtifact() {
		final Path sourceDirectory = getTempDirectory().resolve("source"); //used only for identification; no I/O
		final Path sourceSubdirectory = sourceDirectory.resolve("sub");
		final Path targetDirectory = getTempDirectory().resolve("target");
		final Path targetSubdirectory = targetDirectory.resolve("sub");
		final Mummifier mummifier = mock(Mummifier.class);
		final Artifact subdirectoryIndexArtifact = new DummyArtifact(mummifier, sourceSubdirectory.resolve("index.html"), targetSubdirectory.resolve("index.html"));
		final Artifact subdirectoryChildArtifact = new DummyArtifact(mummifier, sourceSubdirectory.resolve("test.html"), sourceSubdirectory.resolve("test.html"));
		final DirectoryArtifact subdirectoryArtifact = new DirectoryArtifact(mummifier, sourceSubdirectory, targetSubdirectory, subdirectoryIndexArtifact,
				Set.of(subdirectoryChildArtifact));
		final DirectoryArtifact rootDirectoryArtifact = new DirectoryArtifact(mummifier, sourceDirectory, targetDirectory, null, Set.of(subdirectoryArtifact));
		final MummyPlan plan = new DefaultMummyPlan(rootDirectoryArtifact);
		assertThat("Subdirectory path retrieves subdirectory artifact.", plan.findArtifactBySourceReference(sourceSubdirectory),
				isPresentAndIs(subdirectoryArtifact));
		assertThat("Subdirectory child path retrieves subdirectory child artifact.", plan.findArtifactBySourceReference(sourceSubdirectory.resolve("test.html")),
				isPresentAndIs(subdirectoryChildArtifact));
		assertThat("Subdirectory index (subsumed artifact) path retrieves subdirectory (principal) artifact.",
				plan.findArtifactBySourceReference(sourceSubdirectory.resolve("index.html")), isPresentAndIs(subdirectoryArtifact));
	}

	/** @see MummyPlan#findParentArtifact(Artifact) */
	@Test
	void testFindParentArtifact() {
		final Path sourceDirectory = getTempDirectory().resolve("source"); //used only for identification; no I/O
		final Path targetDirectory = getTempDirectory().resolve("target");
		final Path fooDirectory = sourceDirectory.resolve("foo");
		final Mummifier mummifier = mock(Mummifier.class);
		final Artifact indexArtifact = new DummyArtifact(mummifier, sourceDirectory.resolve("index.html"), targetDirectory.resolve("index.html")); //index.html
		final Artifact childArtifact = new DummyArtifact(mummifier, sourceDirectory.resolve("test.html"), targetDirectory.resolve("test.html")); //test.html
		final Artifact fooIndexArtifact = new DummyArtifact(mummifier, fooDirectory.resolve("index.html"), targetDirectory.resolve("foo").resolve("index.html")); ///foo/index.html
		final Artifact fooBarArtifact = new DummyArtifact(mummifier, fooDirectory.resolve("bar.html"), targetDirectory.resolve("foo").resolve("test.html")); //foo/bar.html
		final DirectoryArtifact fooDirectoryArtifact = new DirectoryArtifact(mummifier, fooDirectory, targetDirectory.resolve("foo"), fooIndexArtifact,
				Set.of(fooBarArtifact)); //foo/
		final DirectoryArtifact rootDirectoryArtifact = new DirectoryArtifact(mummifier, sourceDirectory, targetDirectory, indexArtifact,
				Set.of(childArtifact, fooDirectoryArtifact));
		final MummyPlan plan = new DefaultMummyPlan(rootDirectoryArtifact);
		assertThat("Root artifact has no parent artifact.", plan.findParentArtifact(rootDirectoryArtifact), isEmpty());
		assertThat("Root directory content artifact has no parent artifact.", plan.findParentArtifact(indexArtifact), isEmpty());
		assertThat("Root directory artifact is parent artifact of child artifact.", plan.findParentArtifact(childArtifact), isPresentAndIs(rootDirectoryArtifact));
		assertThat("Root directory artifact is parent artifact of child `foo` directory.", plan.findParentArtifact(fooDirectoryArtifact),
				isPresentAndIs(rootDirectoryArtifact));
		assertThat("Root directory artifact is parent artifact of child `foo` directory content artifact.", plan.findParentArtifact(fooIndexArtifact),
				isPresentAndIs(rootDirectoryArtifact));
		assertThat("`foo` directory is parent artifact `foo/bar.html` child artifact.", plan.findParentArtifact(fooBarArtifact),
				isPresentAndIs(fooDirectoryArtifact));
	}

}
