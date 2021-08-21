/*
 * Copyright © 2021 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.guise.mummy.mummify.collection;

import static java.nio.charset.StandardCharsets.*;
import static java.nio.file.Files.*;
import static java.util.stream.Collectors.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.github.npathai.hamcrestopt.OptionalMatchers;

import io.guise.mummy.*;

/**
 * Tests of {@link DirectoryMummifier}.
 * @author Garret Wilson
 */
public class DirectoryMummifierTest {

	/**
	 * Ensures that directory mummifier planning returns an artifact containing the correct comprised artifacts (e.g. child artifacts, content artifact, etc.).
	 * Specifically this chicks that the child artifacts do not include any content (e.g. <code>index.*</code>) file.
	 * @param A temporary directory to serve as the project base directory for the planning test.
	 * @see DirectoryMummifier#plan(MummyContext, Path, Path)
	 */
	@Test
	void verifyPlannedComprisedArtifacts(@TempDir final Path tempDir) throws IOException {
		final GuiseProject project = new DefaultGuiseProject(tempDir);
		final MummyContext mummyContext = new FakeMummyContext(project);
		final Path sourceDirectory = createDirectories(tempDir.resolve("src").resolve("site"));
		final Path indexFile = writeString(sourceDirectory.resolve("index.md"), "# Index", UTF_8);
		final Path child1File = writeString(sourceDirectory.resolve("child1.md"), "# Child 1", UTF_8);
		final Path child2File = writeString(sourceDirectory.resolve("child2.md"), "# Child 2", UTF_8);
		final Path child3File = writeString(sourceDirectory.resolve("child3.md"), "# Child 3", UTF_8);
		final Path targetDirectory = createDirectory(tempDir.resolve("target"));
		final DirectoryMummifier directoryMummifier = new DirectoryMummifier();
		final DirectoryArtifact directoryArtifact = directoryMummifier.plan(mummyContext, sourceDirectory, targetDirectory);
		assertThat("Directory artifact should contain the expected comprised artifacts.",
				directoryArtifact.comprisedArtifacts().map(Artifact::getSourcePath).collect(toSet()),
				containsInAnyOrder(indexFile, child1File, child2File, child3File));
		assertThat("Directory artifact should contain the expected content artifact.", directoryArtifact.findContentArtifact().map(Artifact::getSourcePath),
				OptionalMatchers.isPresentAndIs(indexFile));
		assertThat("Directory artifact should contain the expected child artifacts.",
				directoryArtifact.getChildArtifacts().stream().map(Artifact::getSourcePath).collect(toSet()), containsInAnyOrder(child1File, child2File, child3File));
		assertThat("Directory artifact should contain the expected subsumed artifacts.",
				directoryArtifact.getSubsumedArtifacts().stream().map(Artifact::getSourcePath).collect(toSet()), containsInAnyOrder(indexFile));
	}

}
