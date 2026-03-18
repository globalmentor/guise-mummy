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

package dev.guise.mummy.deploy.flange;

import static com.github.npathai.hamcrestopt.OptionalMatchers.*;
import static com.globalmentor.java.OperatingSystem.*;
import static com.globalmentor.security.MessageDigests.*;
import static dev.guise.mummy.Artifact.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.nio.file.Path;
import java.util.*;

import org.junit.jupiter.api.*;

import com.globalmentor.net.MediaType;
import com.globalmentor.net.UriPath;
import com.globalmentor.security.Hash;

import dev.flange.aws.s3.support.S3Synchronizer;
import dev.guise.mummy.*;
import dev.guise.mummy.deploy.flange.FlangeWebSite.*;
import dev.guise.mummy.mummify.Mummifier;
import dev.guise.mummy.mummify.collection.DirectoryArtifact;
import dev.guise.mummy.mummify.page.PageMummifier;
import dev.guise.mummy.plan.*;
import dev.guise.mummy.plan.PlanSummary.*;
import io.urf.model.UrfObject;
import io.urf.vocab.content.Content;

/// Tests of [FlangeWebSite] foundation components.
class FlangeWebSiteTest {

	private static final Path PROJECT_DIRECTORY = getTempDirectory().resolve("project");
	private static final Path SOURCE_DIRECTORY = PROJECT_DIRECTORY.resolve("src").resolve("site");
	private static final Path TARGET_DIRECTORY = PROJECT_DIRECTORY.resolve("target").resolve("site");

	//## `Manifest.Builder`

	/// Tests that [Manifest.Builder#build] extracts non-warning redirects from [PlanSummary] and builds the artifact index.
	@Test
	void testManifestBuilderExtractsRedirectsAndBuildsIndex() {
		final var builder = new Manifest.Builder();
		final Mummifier mummifier = mock(Mummifier.class);
		final Artifact page = new DummyArtifact(mummifier, SOURCE_DIRECTORY.resolve("page.html"), TARGET_DIRECTORY.resolve("page.html"));
		builder.addArtifact(page);
		final var okRedirect = new RedirectEntry(UriPath.parse("old.html"), URI.create("page.html"), Optional.empty());
		final var warnedRedirect = new RedirectEntry(UriPath.parse("external.html"), URI.create("https://example.com/"),
				Optional.of(PlanWarning.REDIRECT_OUTSIDE_SITE));
		final var summary = new PlanSummary(1, 0, 0, 0, 0, List.of(okRedirect, warnedRedirect));
		final Manifest manifest = builder.build(summary);
		assertThat("non-warning redirect extracted", manifest.redirects(), hasEntry(UriPath.parse("old.html"), URI.create("page.html")));
		assertThat("warning redirect filtered out", manifest.redirects(), not(hasKey(UriPath.parse("external.html"))));
		assertThat("redirect count excludes warned entries", manifest.redirects(), aMapWithSize(1));
		assertThat("artifact indexed by target path", manifest.artifactsByTargetPath().get(TARGET_DIRECTORY.resolve("page.html")), is(page));
	}

	/// Tests that [Manifest.Builder#build] produces an empty redirect map when all entries carry warnings.
	@Test
	void testManifestBuilderFilterAllWarnings() {
		final var builder = new Manifest.Builder();
		final var warned = new RedirectEntry(UriPath.parse("bad.html"), URI.create("https://elsewhere.com/"), Optional.of(PlanWarning.REDIRECT_OUTSIDE_SITE));
		final var summary = new PlanSummary(0, 0, 0, 0, 0, List.of(warned));
		final Manifest manifest = builder.build(summary);
		assertThat("all warned → empty redirects", manifest.redirects(), anEmptyMap());
	}

	//## manifest construction via `PlanDescriber.summarize(Visitor)`

	/// Tests that piggybacking a manifest visitor on [PlanDescriber#summarize] indexes all artifacts
	/// by their target path, including both file artifacts and collection artifacts.
	@Test
	void testManifestVisitorIndexesAllArtifacts() {
		final Mummifier directoryMummifier = mock(Mummifier.class);
		final PageMummifier pageMummifier = mock(PageMummifier.class);
		final Artifact page = new DummyArtifact(pageMummifier, SOURCE_DIRECTORY.resolve("about.html"), TARGET_DIRECTORY.resolve("about.html"));
		final DirectoryArtifact root = new DirectoryArtifact(directoryMummifier, SOURCE_DIRECTORY, TARGET_DIRECTORY, null, Set.of(page));
		final MummyPlan plan = new DefaultMummyPlan(root);
		final var manifestBuilder = new Manifest.Builder();
		final PlanSummary summary = new PlanDescriber(plan).summarize((artifact, _) -> manifestBuilder.addArtifact(artifact));
		final Manifest manifest = manifestBuilder.build(summary);
		assertThat("file artifact indexed", manifest.artifactsByTargetPath().get(TARGET_DIRECTORY.resolve("about.html")), is(page));
		assertThat("root directory indexed", manifest.artifactsByTargetPath().get(TARGET_DIRECTORY), is(root));
	}

	/// Tests that the manifest visitor indexes subsumed content artifacts by their own target path,
	/// alongside the directory artifact indexed by the directory's target path.
	@Test
	void testManifestVisitorIndexesSubsumedContentArtifact() {
		final Mummifier directoryMummifier = mock(Mummifier.class);
		final PageMummifier pageMummifier = mock(PageMummifier.class);
		final Artifact contentArtifact = new DummyArtifact(pageMummifier, SOURCE_DIRECTORY.resolve("sub").resolve("index.html"),
				TARGET_DIRECTORY.resolve("sub").resolve("index.html"));
		final DirectoryArtifact subDir = new DirectoryArtifact(directoryMummifier, SOURCE_DIRECTORY.resolve("sub"), TARGET_DIRECTORY.resolve("sub"),
				contentArtifact, Set.of());
		final DirectoryArtifact root = new DirectoryArtifact(directoryMummifier, SOURCE_DIRECTORY, TARGET_DIRECTORY, null, Set.of(subDir));
		final MummyPlan plan = new DefaultMummyPlan(root);
		final var manifestBuilder = new Manifest.Builder();
		new PlanDescriber(plan).summarize((artifact, _) -> manifestBuilder.addArtifact(artifact));
		final Manifest manifest = manifestBuilder.build(new PlanSummary(0, 2, 0, 0, 0, List.of()));
		assertThat("content artifact indexed by its own target path", manifest.artifactsByTargetPath().get(TARGET_DIRECTORY.resolve("sub").resolve("index.html")),
				is(contentArtifact));
		assertThat("directory indexed by its target path", manifest.artifactsByTargetPath().get(TARGET_DIRECTORY.resolve("sub")), is(subDir));
	}

	/// Tests that a directory artifact with both a content artifact and an `altLocation` produces
	/// **both** a redirect entry and artifact index entries for the directory and its content artifact.
	@Test
	void testManifestDirectoryWithContentAndRedirect() {
		final Mummifier directoryMummifier = mock(Mummifier.class);
		final PageMummifier pageMummifier = mock(PageMummifier.class);
		final UrfObject contentDescription = new UrfObject();
		contentDescription.setPropertyValue(PROPERTY_TAG_MUMMY_ALT_LOCATION, "old-sub/"); // delegated to `DirectoryArtifact.getResourceDescription()`
		final Artifact contentArtifact = new DummyArtifact(pageMummifier, SOURCE_DIRECTORY.resolve("sub").resolve("index.html"),
				TARGET_DIRECTORY.resolve("sub").resolve("index.html"), contentDescription);
		final DirectoryArtifact subDir = new DirectoryArtifact(directoryMummifier, SOURCE_DIRECTORY.resolve("sub"), TARGET_DIRECTORY.resolve("sub"),
				contentArtifact, Set.of());
		final DirectoryArtifact root = new DirectoryArtifact(directoryMummifier, SOURCE_DIRECTORY, TARGET_DIRECTORY, null, Set.of(subDir));
		final MummyPlan plan = new DefaultMummyPlan(root);
		final var manifestBuilder = new Manifest.Builder();
		final PlanSummary summary = new PlanDescriber(plan).summarize((artifact, _) -> manifestBuilder.addArtifact(artifact));
		final Manifest manifest = manifestBuilder.build(summary);
		assertThat("redirect extracted for collection", manifest.redirects(), hasKey(UriPath.parse("sub/old-sub/")));
		assertThat("redirect target is the collection reference", manifest.redirects().get(UriPath.parse("sub/old-sub/")), is(URI.create("sub/")));
		assertThat("content artifact indexed by its target path", manifest.artifactsByTargetPath(), hasKey(TARGET_DIRECTORY.resolve("sub").resolve("index.html")));
		assertThat("directory indexed by its target path", manifest.artifactsByTargetPath(), hasKey(TARGET_DIRECTORY.resolve("sub")));
	}

	//## `ArtifactMetadataStrategy`

	/// Tests that [ArtifactMetadataStrategy#toMetadata] extracts content type and fingerprint from an artifact description.
	@Test
	void testToMetadataExtractsContentTypeAndFingerprint() {
		final Mummifier mummifier = mock(Mummifier.class);
		final UrfObject description = new UrfObject();
		final MediaType htmlType = MediaType.of(MediaType.TEXT_PRIMARY_TYPE, "html");
		description.setPropertyValue(Content.TYPE_PROPERTY_TAG, htmlType);
		final byte[] fingerprint = {0x01, 0x02, 0x03, 0x04};
		description.setPropertyValue(Content.FINGERPRINT_PROPERTY_TAG, fingerprint);
		final Artifact artifact = new DummyArtifact(mummifier, SOURCE_DIRECTORY.resolve("page.html"), TARGET_DIRECTORY.resolve("page.html"), description);
		final S3Synchronizer.Metadata metadata = ArtifactMetadataStrategy.toMetadata(artifact, new LinkedHashSet<>(List.of(SHA_256)));
		assertThat("content type extracted", metadata.contentType(), isPresentAndIs(htmlType));
		assertThat("SHA-256 checksum present", metadata.findChecksum(SHA_256), isPresentAndIs(Hash.of(fingerprint)));
	}

	/// Tests that [ArtifactMetadataStrategy#toMetadata] handles content type only (no fingerprint).
	@Test
	void testToMetadataContentTypeOnly() {
		final Mummifier mummifier = mock(Mummifier.class);
		final UrfObject description = new UrfObject();
		final MediaType cssType = MediaType.of(MediaType.TEXT_PRIMARY_TYPE, "css");
		description.setPropertyValue(Content.TYPE_PROPERTY_TAG, cssType);
		final Artifact artifact = new DummyArtifact(mummifier, SOURCE_DIRECTORY.resolve("style.css"), TARGET_DIRECTORY.resolve("style.css"), description);
		final S3Synchronizer.Metadata metadata = ArtifactMetadataStrategy.toMetadata(artifact, new LinkedHashSet<>(List.of(SHA_256)));
		assertThat("content type extracted", metadata.contentType(), isPresentAndIs(cssType));
		assertThat("no checksum", metadata.checksums(), anEmptyMap());
	}

	/// Tests that [ArtifactMetadataStrategy#toMetadata] returns empty metadata for an artifact with no description properties.
	@Test
	void testToMetadataEmptyDescription() {
		final Mummifier mummifier = mock(Mummifier.class);
		final Artifact artifact = new DummyArtifact(mummifier, SOURCE_DIRECTORY.resolve("data.bin"), TARGET_DIRECTORY.resolve("data.bin"));
		final S3Synchronizer.Metadata metadata = ArtifactMetadataStrategy.toMetadata(artifact, new LinkedHashSet<>(List.of(SHA_256)));
		assertThat("no content type", metadata.contentType(), is(Optional.empty()));
		assertThat("no checksums", metadata.checksums(), anEmptyMap());
	}

	/// Tests that [ArtifactMetadataStrategy#findMetadata] returns metadata for known paths and empty for unknown paths.
	@Test
	void testFindMetadataLookup() {
		final Mummifier mummifier = mock(Mummifier.class);
		final UrfObject description = new UrfObject();
		description.setPropertyValue(Content.TYPE_PROPERTY_TAG, MediaType.of(MediaType.TEXT_PRIMARY_TYPE, "html"));
		final Artifact page = new DummyArtifact(mummifier, SOURCE_DIRECTORY.resolve("page.html"), TARGET_DIRECTORY.resolve("page.html"), description);
		final DirectoryArtifact root = new DirectoryArtifact(mummifier, SOURCE_DIRECTORY, TARGET_DIRECTORY, null, Set.of(page));
		final MummyPlan plan = new DefaultMummyPlan(root);
		final Path knownPath = TARGET_DIRECTORY.resolve("page.html");
		final Path unknownPath = TARGET_DIRECTORY.resolve("missing.html");
		final var strategy = new ArtifactMetadataStrategy(plan, Map.of(knownPath, page));
		assertThat("known path returns metadata", strategy.findMetadata(knownPath, new LinkedHashSet<>(List.of(SHA_256))).isPresent(), is(true));
		assertThat("unknown path returns empty", strategy.findMetadata(unknownPath, new LinkedHashSet<>(List.of(SHA_256))).isPresent(), is(false));
	}

	/// Tests that [ArtifactMetadataStrategy#findMetadata] resolves a directory content artifact's path
	/// to the directory artifact's metadata via [MummyPlan#getPrincipalArtifact].
	@Test
	void testFindMetadataResolvesContentArtifactToDirectoryMetadata() {
		final Mummifier directoryMummifier = mock(Mummifier.class);
		final PageMummifier pageMummifier = mock(PageMummifier.class);
		final UrfObject contentDescription = new UrfObject();
		contentDescription.setPropertyValue(Content.TYPE_PROPERTY_TAG, MediaType.of(MediaType.TEXT_PRIMARY_TYPE, "html"));
		final Artifact contentArtifact = new DummyArtifact(pageMummifier, SOURCE_DIRECTORY.resolve("sub").resolve("index.html"),
				TARGET_DIRECTORY.resolve("sub").resolve("index.html"), contentDescription);
		final DirectoryArtifact subDir = new DirectoryArtifact(directoryMummifier, SOURCE_DIRECTORY.resolve("sub"), TARGET_DIRECTORY.resolve("sub"),
				contentArtifact, Set.of());
		final DirectoryArtifact root = new DirectoryArtifact(directoryMummifier, SOURCE_DIRECTORY, TARGET_DIRECTORY, null, Set.of(subDir));
		final MummyPlan plan = new DefaultMummyPlan(root);
		final var manifestBuilder = new Manifest.Builder();
		new PlanDescriber(plan).summarize((artifact, _) -> manifestBuilder.addArtifact(artifact));
		final Manifest manifest = manifestBuilder.build(new PlanSummary(0, 2, 0, 0, 0, List.of()));
		final var strategy = new ArtifactMetadataStrategy(plan, manifest.artifactsByTargetPath());
		final Path contentPath = TARGET_DIRECTORY.resolve("sub").resolve("index.html");
		assertThat("metadata returned for directory content artifact", strategy.findMetadata(contentPath, new LinkedHashSet<>(List.of(SHA_256))).isPresent(),
				is(true));
		assertThat("content type resolved through principal artifact",
				strategy.findMetadata(contentPath, new LinkedHashSet<>(List.of(SHA_256))).orElseThrow().contentType(),
				isPresentAndIs(MediaType.of(MediaType.TEXT_PRIMARY_TYPE, "html")));
	}

}
