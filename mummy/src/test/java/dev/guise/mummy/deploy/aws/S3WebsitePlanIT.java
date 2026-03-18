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

package dev.guise.mummy.deploy.aws;

import static com.globalmentor.net.URIs.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.*;

import org.jspecify.annotations.NonNull;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import dev.guise.mummy.*;
import dev.guise.mummy.mummify.Mummifier;
import dev.guise.mummy.mummify.collection.DirectoryArtifact;
import io.urf.model.UrfObject;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/// Integration tests for [S3Website] deployment planning walk and redirect correctness.
class S3WebsitePlanIT {

	@TempDir
	Path tempDir;

	private Path sourceDir;
	private Path targetDir;
	private URI rootTargetPathUri;
	private S3Website s3Website;
	private MummyContext context;
	private Mummifier mummifier;

	@BeforeEach
	void setUp() throws IOException {
		sourceDir = Files.createDirectories(tempDir.resolve("src").resolve("site"));
		targetDir = Files.createDirectories(tempDir.resolve("target").resolve("site"));
		rootTargetPathUri = toCollectionURI(targetDir.toUri());
		s3Website = new S3Website(Region.US_EAST_1, "test-bucket", mock(S3Client.class));
		context = mock(MummyContext.class);
		mummifier = mock(Mummifier.class);
	}

	/// Tests that [S3Website#plan(MummyContext, URI, Artifact)] produces a deploy object keyed by the content artifact's
	/// resource reference (e.g. `section/index`), with the collection artifact as the metadata source and the content
	/// artifact's target path as the content path.
	@Test
	void testCollectionArtifactDeployObjectAtContentKey() throws IOException {
		final Path sectionSourceDir = Files.createDirectories(sourceDir.resolve("section"));
		final Path sectionTargetDir = Files.createDirectories(targetDir.resolve("section"));
		final Path contentSourceFile = Files.createFile(sectionSourceDir.resolve("index"));
		final Path contentTargetFile = Files.createFile(sectionTargetDir.resolve("index"));
		final DummyArtifact contentArtifact = new DummyArtifact(mummifier, contentSourceFile, contentTargetFile);
		final DirectoryArtifact directory = new DirectoryArtifact(mummifier, sectionSourceDir, sectionTargetDir, contentArtifact, Set.of());
		s3Website.plan(context, rootTargetPathUri, directory);
		final Map<String, S3DeployObject> deployObjects = s3Website.getDeployObjectsByKey();
		assertThat("deploy object at content key", deployObjects, hasKey("section/index"));
		assertThat("no deploy object at collection key", deployObjects, not(hasKey("section/")));
		final S3ArtifactDeployObject deployObject = (S3ArtifactDeployObject)deployObjects.get("section/index");
		assertThat("artifact is the directory", deployObject.getArtifact(), is(directory));
		assertThat("content path from content artifact", deployObject.getContentFile(), is(contentTargetFile));
	}

	/// Tests that [S3Website#plan(MummyContext, URI, Artifact)] produces a redirect whose target key is the collection
	/// resource reference (e.g. `section/`), not the content artifact's key (e.g. `section/index`).
	@Test
	void testCollectionRedirectTargetsCollectionReference() throws IOException {
		final Path sectionSourceDir = Files.createDirectories(sourceDir.resolve("section"));
		final Path sectionTargetDir = Files.createDirectories(targetDir.resolve("section"));
		final Path contentSourceFile = Files.createFile(sectionSourceDir.resolve("index"));
		final Path contentTargetFile = Files.createFile(sectionTargetDir.resolve("index"));
		final UrfObject description = new UrfObject();
		description.setPropertyValue(Artifact.PROPERTY_TAG_MUMMY_ALT_LOCATION, "old-name");
		final DummyArtifact contentArtifact = new DummyArtifact(mummifier, contentSourceFile, contentTargetFile, description);
		final DirectoryArtifact directory = new DirectoryArtifact(mummifier, sectionSourceDir, sectionTargetDir, contentArtifact, Set.of());
		s3Website.plan(context, rootTargetPathUri, directory);
		// Find the redirect deploy object — it should target `section/`, not `section/index`.
		final Optional<S3ArtifactRedirectDeployObject> foundRedirect = s3Website.getDeployObjectsByKey().values().stream()
				.filter(S3ArtifactRedirectDeployObject.class::isInstance).map(S3ArtifactRedirectDeployObject.class::cast).findFirst();
		// The routing rules may also contain the redirect if it requires a routing rule.
		final S3ArtifactRedirectDeployObject redirect = foundRedirect.or(() -> s3Website.getRoutingRuleRedirectObjects().stream().findFirst())
				.orElseThrow(() -> new AssertionError("no redirect found"));
		assertThat("redirect target is collection reference", redirect.getRedirectTargetKey(), is("section/"));
	}

	/// Tests that [S3Website#plan(MummyContext, URI, Artifact)] produces no deploy object for a collection artifact
	/// with no content artifact (empty intermediate directory), while still recursing into child artifacts.
	@Test
	void testEmptyCollectionProducesNoDeployObject() throws IOException {
		final Path sectionSourceDir = Files.createDirectories(sourceDir.resolve("section"));
		final Path sectionTargetDir = Files.createDirectories(targetDir.resolve("section"));
		final Path childSourceFile = Files.createFile(sectionSourceDir.resolve("page.html"));
		final Path childTargetFile = Files.createFile(sectionTargetDir.resolve("page.html"));
		final DummyArtifact child = new DummyArtifact(mummifier, childSourceFile, childTargetFile);
		final DirectoryArtifact directory = new DirectoryArtifact(mummifier, sectionSourceDir, sectionTargetDir, null, Set.of(child));
		s3Website.plan(context, rootTargetPathUri, directory);
		final Map<String, S3DeployObject> deployObjects = s3Website.getDeployObjectsByKey();
		assertThat("no deploy object for empty collection", deployObjects, not(hasKey("section/")));
		assertThat("child artifact deployed", deployObjects, hasKey("section/page.html"));
	}

	/// Tests that [S3Website#plan(MummyContext, URI, Artifact)] produces a deploy object for a non-collection artifact
	/// at its own resource reference, with its own target path as the content path.
	@Test
	void testNonCollectionArtifactDeployObject() throws IOException {
		final Path pageSourceFile = Files.createFile(sourceDir.resolve("page.html"));
		final Path pageTargetFile = Files.createFile(targetDir.resolve("page.html"));
		final DummyArtifact page = new DummyArtifact(mummifier, pageSourceFile, pageTargetFile);
		s3Website.plan(context, rootTargetPathUri, page);
		final Map<String, S3DeployObject> deployObjects = s3Website.getDeployObjectsByKey();
		assertThat("deploy object at own key", deployObjects, hasKey("page.html"));
		final S3ArtifactDeployObject deployObject = (S3ArtifactDeployObject)deployObjects.get("page.html");
		assertThat("content path is own target", deployObject.getContentFile(), is(pageTargetFile));
	}

	/// Tests that [S3Website#plan(MummyContext, URI, Artifact)] recurses into child artifacts of a collection, deploying
	/// each child, and that the subsumed content artifact is not separately visited (its deploy object comes from the
	/// collection's own `planResource` call, not from recursion).
	@Test
	void testWalkRecursesIntoChildrenSkippingSubsumed() throws IOException {
		final Path sectionSourceDir = Files.createDirectories(sourceDir.resolve("section"));
		final Path sectionTargetDir = Files.createDirectories(targetDir.resolve("section"));
		final Path contentSourceFile = Files.createFile(sectionSourceDir.resolve("index"));
		final Path contentTargetFile = Files.createFile(sectionTargetDir.resolve("index"));
		final Path child1SourceFile = Files.createFile(sectionSourceDir.resolve("alpha.html"));
		final Path child1TargetFile = Files.createFile(sectionTargetDir.resolve("alpha.html"));
		final Path child2SourceFile = Files.createFile(sectionSourceDir.resolve("beta.html"));
		final Path child2TargetFile = Files.createFile(sectionTargetDir.resolve("beta.html"));
		final DummyArtifact contentArtifact = new DummyArtifact(mummifier, contentSourceFile, contentTargetFile);
		final DummyArtifact child1 = new DummyArtifact(mummifier, child1SourceFile, child1TargetFile);
		final DummyArtifact child2 = new DummyArtifact(mummifier, child2SourceFile, child2TargetFile);
		final DirectoryArtifact directory = new DirectoryArtifact(mummifier, sectionSourceDir, sectionTargetDir, contentArtifact, Set.of(child1, child2));
		s3Website.plan(context, rootTargetPathUri, directory);
		final Map<String, S3DeployObject> deployObjects = s3Website.getDeployObjectsByKey();
		assertThat("first child deployed", deployObjects, hasKey("section/alpha.html"));
		assertThat("second child deployed", deployObjects, hasKey("section/beta.html"));
		assertThat("content artifact at content key", deployObjects, hasKey("section/index"));
		assertThat("exactly three deploy objects", deployObjects.size(), is(3)); // content + 2 children, no duplicate from walking subsumed
	}

	/// Tests that [S3Website#plan(MummyContext, URI, Artifact)] produces S3 keys that are canonical filesystem names —
	/// decoded Unicode, with no percent-encoding artifacts leaked from the `Path.toUri()` → `UriPath` pipeline. Covers
	/// non-ASCII (2-byte Latin, 3-byte CJK), spaces, URI-significant characters (`#`, `%`), and a mixed combination.
	/// @see <a href="../../../../../../dev/issues/GUISE-230/designs/s3-key-encoding.md">S3 Key Encoding Design</a>
	@Test
	void testS3KeyIsCanonicalFilesystemName() throws IOException {
		//## non-ASCII: 2-byte Latin (`café`)
		assertCanonicalKey("caf\u00e9", "caf\u00e9/index"); // café
		//## non-ASCII: 3-byte CJK (`東京`, Japanese: "Eastern Capital")
		assertCanonicalKey("\u6771\u4EAC", "\u6771\u4EAC/index"); // 東京
		//## space — `Path.toUri()` encodes as `%20`; the key must recover the literal space
		assertCanonicalKey("my section", "my section/index");
		//## `#` — `Path.toUri()` encodes as `%23`; the key must recover the literal `#`
		assertCanonicalKey("section#2", "section#2/index");
		//## `%` — `Path.toUri()` encodes as `%25`; the key must recover the literal `%` (no double-decode)
		assertCanonicalKey("100%", "100%/index");
		//## mixed: non-ASCII + space + `#`
		assertCanonicalKey("caf\u00e9 #1", "caf\u00e9 #1/index"); // café #1
	}

	/// Creates a `DirectoryArtifact` at the given directory name under the site root, with a content artifact at
	/// `{dirName}/index`, plans deployment, and asserts that the deploy object key matches `expectedKey`.
	private void assertCanonicalKey(@NonNull final String dirName, @NonNull final String expectedKey) throws IOException {
		final Path sectionSourceDir = Files.createDirectories(sourceDir.resolve(dirName));
		final Path sectionTargetDir = Files.createDirectories(targetDir.resolve(dirName));
		final Path contentSourceFile = Files.createFile(sectionSourceDir.resolve("index"));
		final Path contentTargetFile = Files.createFile(sectionTargetDir.resolve("index"));
		final DummyArtifact contentArtifact = new DummyArtifact(mummifier, contentSourceFile, contentTargetFile);
		final DirectoryArtifact directory = new DirectoryArtifact(mummifier, sectionSourceDir, sectionTargetDir, contentArtifact, Set.of());
		final S3Website localS3Website = new S3Website(Region.US_EAST_1, "test-bucket", mock(S3Client.class));
		localS3Website.plan(context, rootTargetPathUri, directory);
		final Map<String, S3DeployObject> deployObjects = localS3Website.getDeployObjectsByKey();
		assertThat("canonical key for `%s`".formatted(dirName), deployObjects, hasKey(expectedKey));
	}

}
