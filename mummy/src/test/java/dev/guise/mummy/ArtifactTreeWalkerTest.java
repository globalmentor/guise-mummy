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
import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;

import dev.guise.mummy.mummify.Mummifier;
import dev.guise.mummy.mummify.collection.DirectoryArtifact;

/// Tests for [ArtifactTreeWalker].
public class ArtifactTreeWalkerTest {

	private static final Path PROJECT_DIRECTORY = getTempDirectory().resolve("project");
	private static final Path SOURCE_DIRECTORY = PROJECT_DIRECTORY.resolve("src").resolve("site");
	private static final Path TARGET_DIRECTORY = PROJECT_DIRECTORY.resolve("target").resolve("site");

	/// Local record for capturing visit order and subsumption status.
	record VisitRecord(Artifact artifact, boolean subsumed) {
	}

	/// Tests that [ArtifactTreeWalker#walk(Artifact, ArtifactTreeWalker.Visitor)] visits a single non-composite artifact
	/// exactly once as non-subsumed.
	@Test
	void testWalkSingleArtifact() {
		final Mummifier mummifier = mock(Mummifier.class);
		final Artifact leaf = new DummyArtifact(mummifier, SOURCE_DIRECTORY.resolve("page.html"), TARGET_DIRECTORY.resolve("page.html"));
		final List<VisitRecord> visits = new ArrayList<>();
		ArtifactTreeWalker.walk(leaf, (artifact, subsumed) -> visits.add(new VisitRecord(artifact, subsumed)));
		assertThat("single artifact visit count", visits, hasSize(1));
		assertThat("single artifact is not subsumed", visits.getFirst().subsumed(), is(false));
		assertThat("visited artifact identity", visits.getFirst().artifact(), is(sameInstance(leaf)));
	}

	/// Tests that [ArtifactTreeWalker#walk(Artifact, ArtifactTreeWalker.Visitor)] handles a directory with no children
	/// and no content artifact.
	@Test
	void testWalkEmptyDirectory() {
		final Mummifier mummifier = mock(Mummifier.class);
		final DirectoryArtifact emptyDir = new DirectoryArtifact(mummifier, SOURCE_DIRECTORY, TARGET_DIRECTORY, null, Set.of());
		final List<VisitRecord> visits = new ArrayList<>();
		ArtifactTreeWalker.walk(emptyDir, (artifact, subsumed) -> visits.add(new VisitRecord(artifact, subsumed)));
		assertThat("empty directory visit count", visits, hasSize(1));
		assertThat("empty directory is not subsumed", visits.getFirst().subsumed(), is(false));
	}

	/// Tests that [ArtifactTreeWalker#walk(Artifact, ArtifactTreeWalker.Visitor)] traverses children in depth-first
	/// pre-order, all as non-subsumed.
	@Test
	void testWalkDirectoryWithChildren() {
		final Mummifier mummifier = mock(Mummifier.class);
		final Artifact childA = new DummyArtifact(mummifier, SOURCE_DIRECTORY.resolve("a.html"), TARGET_DIRECTORY.resolve("a.html"));
		final Artifact childB = new DummyArtifact(mummifier, SOURCE_DIRECTORY.resolve("b.html"), TARGET_DIRECTORY.resolve("b.html"));
		final DirectoryArtifact root = new DirectoryArtifact(mummifier, SOURCE_DIRECTORY, TARGET_DIRECTORY, null, Set.of(childA, childB));
		final List<VisitRecord> visits = new ArrayList<>();
		ArtifactTreeWalker.walk(root, (artifact, subsumed) -> visits.add(new VisitRecord(artifact, subsumed)));
		assertThat("directory + two children visit count", visits, hasSize(3));
		assertThat("directory visited first (pre-order)", visits.getFirst().artifact(), is(sameInstance(root)));
		assertThat("directory is not subsumed", visits.getFirst().subsumed(), is(false));
		assertThat("all children are not subsumed", visits.stream().skip(1).allMatch(v -> !v.subsumed()), is(true));
		assertThat("both children visited", visits.stream().skip(1).map(VisitRecord::artifact).toList(), containsInAnyOrder(childA, childB));
	}

	/// Tests that [ArtifactTreeWalker#walk(Artifact, ArtifactTreeWalker.Visitor)] visits the content artifact as subsumed.
	@Test
	void testWalkDirectoryWithContentArtifact() {
		final Mummifier mummifier = mock(Mummifier.class);
		final Artifact content = new DummyArtifact(mummifier, SOURCE_DIRECTORY.resolve("index.html"), TARGET_DIRECTORY.resolve("index.html"));
		final DirectoryArtifact root = new DirectoryArtifact(mummifier, SOURCE_DIRECTORY, TARGET_DIRECTORY, content, Set.of());
		final List<VisitRecord> visits = new ArrayList<>();
		ArtifactTreeWalker.walk(root, (artifact, subsumed) -> visits.add(new VisitRecord(artifact, subsumed)));
		assertThat("directory + content artifact visit count", visits, hasSize(2));
		assertThat("directory is not subsumed", visits.get(0).subsumed(), is(false));
		assertThat("content artifact is subsumed", visits.get(1).subsumed(), is(true));
		assertThat("content artifact identity", visits.get(1).artifact(), is(sameInstance(content)));
	}

	/// Tests that [ArtifactTreeWalker#walk(Artifact, ArtifactTreeWalker.Visitor)] correctly distinguishes content
	/// (subsumed) from children (non-subsumed).
	@Test
	void testWalkDirectoryWithContentAndChildren() {
		final Mummifier mummifier = mock(Mummifier.class);
		final Artifact content = new DummyArtifact(mummifier, SOURCE_DIRECTORY.resolve("index.html"), TARGET_DIRECTORY.resolve("index.html"));
		final Artifact childA = new DummyArtifact(mummifier, SOURCE_DIRECTORY.resolve("a.html"), TARGET_DIRECTORY.resolve("a.html"));
		final Artifact childB = new DummyArtifact(mummifier, SOURCE_DIRECTORY.resolve("b.html"), TARGET_DIRECTORY.resolve("b.html"));
		final DirectoryArtifact root = new DirectoryArtifact(mummifier, SOURCE_DIRECTORY, TARGET_DIRECTORY, content, Set.of(childA, childB));
		final List<VisitRecord> visits = new ArrayList<>();
		ArtifactTreeWalker.walk(root, (artifact, subsumed) -> visits.add(new VisitRecord(artifact, subsumed)));
		assertThat("directory + content + two children visit count", visits, hasSize(4));
		assertThat("directory visited first (pre-order)", visits.getFirst().artifact(), is(sameInstance(root)));
		assertThat("directory is not subsumed", visits.getFirst().subsumed(), is(false));
		final var contentVisit = visits.stream().filter(v -> v.artifact() == content).findFirst().orElseThrow();
		assertThat("content artifact is subsumed", contentVisit.subsumed(), is(true));
		assertThat("child A is not subsumed", visits.stream().filter(v -> v.artifact() == childA).findFirst().orElseThrow().subsumed(), is(false));
		assertThat("child B is not subsumed", visits.stream().filter(v -> v.artifact() == childB).findFirst().orElseThrow().subsumed(), is(false));
	}

	/// Tests that [ArtifactTreeWalker#walk(Artifact, ArtifactTreeWalker.Visitor)] handles nested directory trees with
	/// content artifacts at multiple levels.
	@Test
	void testWalkNestedDirectories() {
		final Mummifier mummifier = mock(Mummifier.class);
		final Path subSourceDir = SOURCE_DIRECTORY.resolve("sub");
		final Path subTargetDir = TARGET_DIRECTORY.resolve("sub");
		final Artifact subContent = new DummyArtifact(mummifier, subSourceDir.resolve("index.html"), subTargetDir.resolve("index.html"));
		final Artifact subLeaf = new DummyArtifact(mummifier, subSourceDir.resolve("leaf.html"), subTargetDir.resolve("leaf.html"));
		final DirectoryArtifact subDir = new DirectoryArtifact(mummifier, subSourceDir, subTargetDir, subContent, Set.of(subLeaf));
		final DirectoryArtifact root = new DirectoryArtifact(mummifier, SOURCE_DIRECTORY, TARGET_DIRECTORY, null, Set.of(subDir));
		final List<VisitRecord> visits = new ArrayList<>();
		ArtifactTreeWalker.walk(root, (artifact, subsumed) -> visits.add(new VisitRecord(artifact, subsumed)));
		assertThat("root + subDir + subLeaf + subContent visit count", visits, hasSize(4));
		assertThat("root visited first", visits.get(0).artifact(), is(sameInstance(root)));
		assertThat("root is not subsumed", visits.get(0).subsumed(), is(false));
		assertThat("subdirectory visited before its children (pre-order)", visits.stream().map(VisitRecord::artifact).toList().indexOf(subDir),
				is(lessThan(visits.stream().map(VisitRecord::artifact).toList().indexOf(subLeaf))));
		assertThat("subdirectory is not subsumed", visits.stream().filter(v -> v.artifact() == subDir).findFirst().orElseThrow().subsumed(), is(false));
		assertThat("sub leaf is not subsumed", visits.stream().filter(v -> v.artifact() == subLeaf).findFirst().orElseThrow().subsumed(), is(false));
		assertThat("sub content is subsumed", visits.stream().filter(v -> v.artifact() == subContent).findFirst().orElseThrow().subsumed(), is(true));
	}

	/// Tests that [ArtifactTreeWalker#walk(Artifact, ArtifactTreeWalker.Visitor)] visits aspects of a composite artifact
	/// with empty subsumption as non-subsumed. This validates the [AspectualArtifact] invariant that aspects are
	/// non-subsumed comprised artifacts.
	@Test
	void testWalkAspectualArtifact() {
		final Mummifier mummifier = mock(Mummifier.class);
		final Artifact aspectA = new DummyArtifact(mummifier, SOURCE_DIRECTORY.resolve("preview.jpg"), TARGET_DIRECTORY.resolve("preview.jpg"));
		final Artifact aspectB = new DummyArtifact(mummifier, SOURCE_DIRECTORY.resolve("excerpt.html"), TARGET_DIRECTORY.resolve("excerpt.html"));
		final CompositeArtifact composite = mock(CompositeArtifact.class);
		when(composite.comprisedArtifacts()).thenReturn(Stream.of(aspectA, aspectB));
		when(composite.getSubsumedArtifacts()).thenReturn(Collections.emptySet());
		final List<VisitRecord> visits = new ArrayList<>();
		ArtifactTreeWalker.walk(composite, (artifact, subsumed) -> visits.add(new VisitRecord(artifact, subsumed)));
		assertThat("composite + two aspects visit count", visits, hasSize(3));
		assertThat("composite visited first", visits.get(0).artifact(), is(sameInstance(composite)));
		assertThat("composite is not subsumed", visits.get(0).subsumed(), is(false));
		assertThat("all aspects are not subsumed", visits.stream().skip(1).allMatch(v -> !v.subsumed()), is(true));
		assertThat("both aspects visited", visits.stream().skip(1).map(VisitRecord::artifact).toList(), containsInAnyOrder(aspectA, aspectB));
	}

}
