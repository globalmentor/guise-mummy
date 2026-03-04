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
import static dev.guise.mummy.Artifact.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;

import org.junit.jupiter.api.*;

import com.globalmentor.net.URIPath;
import dev.guise.mummy.mummify.Mummifier;
import dev.guise.mummy.mummify.collection.DirectoryArtifact;
import dev.guise.mummy.mummify.image.ImageMummifier;
import dev.guise.mummy.mummify.page.PageMummifier;
import io.urf.model.UrfObject;

/// Tests of [PlanDescriber].
public class PlanDescriberTest {

	private static final Path PROJECT_DIRECTORY = getTempDirectory().resolve("project");
	private static final Path SOURCE_DIRECTORY = PROJECT_DIRECTORY.resolve("src").resolve("site");
	private static final Path TARGET_DIRECTORY = PROJECT_DIRECTORY.resolve("target").resolve("site");
	private static final URI ROOT_TARGET_PATH_URI = toCollectionURI(TARGET_DIRECTORY.toUri());
	private static final String NL = System.lineSeparator();

	/// Creates a [PlanDescriber] with the standard test root target URI and source directory.
	/// @param plan The plan to describe.
	/// @return A new plan describer configured for testing.
	private static PlanDescriber planDescriber(final MummyPlan plan) {
		return new PlanDescriber(plan, ROOT_TARGET_PATH_URI, SOURCE_DIRECTORY);
	}

	//## artifact classification and counting

	/// Tests that [PlanDescriber#describeTo(Appendable, boolean)] produces correct counts for a tree
	/// with pages, images, collections, and opaque files.
	@Test
	void testDescribeToClassifiesArtifactsByType() throws IOException {
		final Mummifier directoryMummifier = mock(Mummifier.class);
		final PageMummifier pageMummifier = mock(PageMummifier.class);
		final ImageMummifier imageMummifier = mock(ImageMummifier.class);
		final Mummifier opaqueMummifier = mock(Mummifier.class);
		final Artifact page = new DummyArtifact(pageMummifier, SOURCE_DIRECTORY.resolve("about.html"), TARGET_DIRECTORY.resolve("about.html"));
		final Artifact image = new DummyArtifact(imageMummifier, SOURCE_DIRECTORY.resolve("photo.jpg"), TARGET_DIRECTORY.resolve("photo.jpg"));
		final Artifact opaque = new DummyArtifact(opaqueMummifier, SOURCE_DIRECTORY.resolve("data.json"), TARGET_DIRECTORY.resolve("data.json"));
		final DirectoryArtifact root = new DirectoryArtifact(directoryMummifier, SOURCE_DIRECTORY, TARGET_DIRECTORY, null, Set.of(page, image, opaque));
		final MummyPlan plan = new DefaultMummyPlan(root);

		final StringBuilder output = new StringBuilder();
		planDescriber(plan).describeTo(output, false);
		final String result = output.toString();
		assertThat("total artifact count", result, containsString("Artifacts:    4"));
		assertThat("page count", result, containsString("Pages:        1"));
		assertThat("collection count", result, containsString("Collections:  1"));
		assertThat("image count", result, containsString("Images:       1"));
		assertThat("other count", result, containsString("Other:        1"));
		assertThat("post count is zero for non-post artifacts", result, containsString("Posts:        0"));
	}

	/// Tests that nested collection directories are counted correctly.
	@Test
	void testDescribeToCountsNestedCollections() throws IOException {
		final Mummifier directoryMummifier = mock(Mummifier.class);
		final PageMummifier pageMummifier = mock(PageMummifier.class);
		final Path subSourceDir = SOURCE_DIRECTORY.resolve("sub");
		final Path subTargetDir = TARGET_DIRECTORY.resolve("sub");
		final Artifact subPage = new DummyArtifact(pageMummifier, subSourceDir.resolve("page.html"), subTargetDir.resolve("page.html"));
		final DirectoryArtifact subDir = new DirectoryArtifact(directoryMummifier, subSourceDir, subTargetDir, null, Set.of(subPage));
		final DirectoryArtifact root = new DirectoryArtifact(directoryMummifier, SOURCE_DIRECTORY, TARGET_DIRECTORY, null, Set.of(subDir));
		final MummyPlan plan = new DefaultMummyPlan(root);

		final StringBuilder output = new StringBuilder();
		planDescriber(plan).describeTo(output, false);
		final String result = output.toString();
		assertThat("total artifact count (root + sub + page)", result, containsString("Artifacts:    3"));
		assertThat("collection count (root + sub)", result, containsString("Collections:  2"));
		assertThat("page count", result, containsString("Pages:        1"));
	}

	//## post counting

	/// Tests that post artifacts are counted independently of their mummifier type.
	@Test
	void testDescribeToCountsPosts() throws IOException {
		final Mummifier directoryMummifier = mock(Mummifier.class);
		final PageMummifier pageMummifier = mock(PageMummifier.class);
		final Artifact post = new DummyArtifact(pageMummifier, SOURCE_DIRECTORY.resolve("@2026-01-15-hello-world.html"),
				TARGET_DIRECTORY.resolve("@2026-01-15-hello-world.html"));
		final Artifact nonPost = new DummyArtifact(pageMummifier, SOURCE_DIRECTORY.resolve("about.html"), TARGET_DIRECTORY.resolve("about.html"));
		final DirectoryArtifact root = new DirectoryArtifact(directoryMummifier, SOURCE_DIRECTORY, TARGET_DIRECTORY, null, Set.of(post, nonPost));
		final MummyPlan plan = new DefaultMummyPlan(root);

		final StringBuilder output = new StringBuilder();
		planDescriber(plan).describeTo(output, false);
		final String result = output.toString();
		assertThat("post count includes only post-filename artifacts", result, containsString("Posts:        1"));
		assertThat("page count includes both post and non-post pages", result, containsString("Pages:        2"));
	}

	//## source display

	/// Tests that the source directory is displayed as a platform-native filesystem path.
	@Test
	void testDescribeToShowsSourceDirectory() throws IOException {
		final Mummifier mummifier = mock(Mummifier.class);
		final DirectoryArtifact root = new DirectoryArtifact(mummifier, SOURCE_DIRECTORY, TARGET_DIRECTORY, null, Set.of());
		final MummyPlan plan = new DefaultMummyPlan(root);

		final StringBuilder output = new StringBuilder();
		planDescriber(plan).describeTo(output, false);
		assertThat("source path is the site source directory", output.toString(), containsString("Source:       " + SOURCE_DIRECTORY));
	}

	//## redirect extraction

	/// Tests that page redirects are counted correctly.
	@Test
	void testDescribeToExtractsPageRedirects() throws IOException {
		final Mummifier directoryMummifier = mock(Mummifier.class);
		final PageMummifier pageMummifier = mock(PageMummifier.class);
		final UrfObject description = new UrfObject();
		description.setPropertyValue(PROPERTY_TAG_MUMMY_ALT_LOCATION, "old-page.html");
		final Artifact pageWithRedirect = new DummyArtifact(pageMummifier, SOURCE_DIRECTORY.resolve("new-page.html"), TARGET_DIRECTORY.resolve("new-page.html"),
				description);
		final DirectoryArtifact root = new DirectoryArtifact(directoryMummifier, SOURCE_DIRECTORY, TARGET_DIRECTORY, null, Set.of(pageWithRedirect));
		final MummyPlan plan = new DefaultMummyPlan(root);

		final StringBuilder output = new StringBuilder();
		planDescriber(plan).describeTo(output, false);
		final String result = output.toString();
		assertThat("redirect count", result, containsString("Redirects:    1"));
		assertThat("page redirect count", result, containsString("Page:         1"));
		assertThat("collection redirect count", result, containsString("Collection:   0"));
	}

	/// Tests that collection (directory) redirects are counted correctly.
	@Test
	void testDescribeToExtractsCollectionRedirects() throws IOException {
		final Mummifier directoryMummifier = mock(Mummifier.class);
		final PageMummifier pageMummifier = mock(PageMummifier.class);
		final UrfObject contentDescription = new UrfObject();
		contentDescription.setPropertyValue(PROPERTY_TAG_MUMMY_ALT_LOCATION, "../old-section");
		final Artifact contentArtifact = new DummyArtifact(pageMummifier, SOURCE_DIRECTORY.resolve("new-section").resolve("index.html"),
				TARGET_DIRECTORY.resolve("new-section").resolve("index.html"), contentDescription);
		final DirectoryArtifact dirWithRedirect = new DirectoryArtifact(directoryMummifier, SOURCE_DIRECTORY.resolve("new-section"),
				TARGET_DIRECTORY.resolve("new-section"), contentArtifact, Set.of());
		final DirectoryArtifact root = new DirectoryArtifact(directoryMummifier, SOURCE_DIRECTORY, TARGET_DIRECTORY, null, Set.of(dirWithRedirect));
		final MummyPlan plan = new DefaultMummyPlan(root);

		final StringBuilder output = new StringBuilder();
		planDescriber(plan).describeTo(output, false);
		final String result = output.toString();
		assertThat("redirect count", result, containsString("Redirects:    1"));
		assertThat("collection redirect count", result, containsString("Collection:   1"));
		assertThat("page redirect count", result, containsString("Page:         0"));
	}

	//## verbose output

	/// Tests that verbose output includes redirect detail lines with `-\>` arrow.
	@Test
	void testDescribeToVerboseShowsRedirectDetails() throws IOException {
		final Mummifier directoryMummifier = mock(Mummifier.class);
		final PageMummifier pageMummifier = mock(PageMummifier.class);
		final UrfObject description = new UrfObject();
		description.setPropertyValue(PROPERTY_TAG_MUMMY_ALT_LOCATION, "old-page.html");
		final Artifact pageWithRedirect = new DummyArtifact(pageMummifier, SOURCE_DIRECTORY.resolve("new-page.html"), TARGET_DIRECTORY.resolve("new-page.html"),
				description);
		final DirectoryArtifact root = new DirectoryArtifact(directoryMummifier, SOURCE_DIRECTORY, TARGET_DIRECTORY, null, Set.of(pageWithRedirect));
		final MummyPlan plan = new DefaultMummyPlan(root);

		final StringBuilder output = new StringBuilder();
		planDescriber(plan).describeTo(output, true);
		final String result = output.toString();
		assertThat("verbose output includes redirect details header", result, containsString("Redirect Details:"));
		assertThat("verbose output includes redirect mapping with -> arrow", result, containsString("    /old-page.html -> /new-page.html" + NL));
	}

	/// Tests that verbose output omits redirect details when there are no redirects.
	@Test
	void testDescribeToVerboseOmitsRedirectDetailsWhenEmpty() throws IOException {
		final Mummifier mummifier = mock(Mummifier.class);
		final DirectoryArtifact root = new DirectoryArtifact(mummifier, SOURCE_DIRECTORY, TARGET_DIRECTORY, null, Set.of());
		final MummyPlan plan = new DefaultMummyPlan(root);

		final StringBuilder output = new StringBuilder();
		planDescriber(plan).describeTo(output, true);
		assertThat("no redirect details when none exist", output.toString(), not(containsString("Redirect Details:")));
	}

	/// Tests that non-verbose output does not include redirect detail lines.
	@Test
	void testDescribeToNonVerboseOmitsRedirectDetails() throws IOException {
		final Mummifier directoryMummifier = mock(Mummifier.class);
		final PageMummifier pageMummifier = mock(PageMummifier.class);
		final UrfObject description = new UrfObject();
		description.setPropertyValue(PROPERTY_TAG_MUMMY_ALT_LOCATION, "old-page.html");
		final Artifact pageWithRedirect = new DummyArtifact(pageMummifier, SOURCE_DIRECTORY.resolve("new-page.html"), TARGET_DIRECTORY.resolve("new-page.html"),
				description);
		final DirectoryArtifact root = new DirectoryArtifact(directoryMummifier, SOURCE_DIRECTORY, TARGET_DIRECTORY, null, Set.of(pageWithRedirect));
		final MummyPlan plan = new DefaultMummyPlan(root);

		final StringBuilder output = new StringBuilder();
		planDescriber(plan).describeTo(output, false);
		assertThat("non-verbose output omits redirect details", output.toString(), not(containsString("Redirect Details:")));
	}

	//## out-of-site redirect warnings

	/// Tests that a redirect whose `altLocation` escapes the site boundary is flagged with `[!]` and a legend.
	@Test
	void testDescribeToWarnsOnOutOfSiteRedirect() throws IOException {
		final Mummifier directoryMummifier = mock(Mummifier.class);
		final PageMummifier pageMummifier = mock(PageMummifier.class);
		final UrfObject description = new UrfObject();
		description.setPropertyValue(PROPERTY_TAG_MUMMY_ALT_LOCATION, "../../outside.html"); // escapes site root
		final Artifact pageWithRedirect = new DummyArtifact(pageMummifier, SOURCE_DIRECTORY.resolve("page.html"), TARGET_DIRECTORY.resolve("page.html"),
				description);
		final DirectoryArtifact root = new DirectoryArtifact(directoryMummifier, SOURCE_DIRECTORY, TARGET_DIRECTORY, null, Set.of(pageWithRedirect));
		final MummyPlan plan = new DefaultMummyPlan(root);

		final StringBuilder output = new StringBuilder();
		planDescriber(plan).describeTo(output, true);
		final String result = output.toString();
		assertThat("warning count shown in summary", result, containsString("Warnings:     1 [!]"));
		assertThat("redirect detail shows out-of-site path with warning marker", result, containsString("    /../../outside.html -> /page.html [!]" + NL));
		assertThat("legend explains [!]", result, containsString("[!] redirect target is outside the site boundary"));
	}

	/// Tests that in-site redirects have no warning marker or legend.
	@Test
	void testDescribeToNoWarningForInSiteRedirect() throws IOException {
		final Mummifier directoryMummifier = mock(Mummifier.class);
		final PageMummifier pageMummifier = mock(PageMummifier.class);
		final UrfObject description = new UrfObject();
		description.setPropertyValue(PROPERTY_TAG_MUMMY_ALT_LOCATION, "old-page.html"); // stays within site
		final Artifact pageWithRedirect = new DummyArtifact(pageMummifier, SOURCE_DIRECTORY.resolve("new-page.html"), TARGET_DIRECTORY.resolve("new-page.html"),
				description);
		final DirectoryArtifact root = new DirectoryArtifact(directoryMummifier, SOURCE_DIRECTORY, TARGET_DIRECTORY, null, Set.of(pageWithRedirect));
		final MummyPlan plan = new DefaultMummyPlan(root);

		final StringBuilder output = new StringBuilder();
		planDescriber(plan).describeTo(output, true);
		final String result = output.toString();
		assertThat("no warning count line for in-site redirects", result, not(containsString("Warnings:")));
		assertThat("no [!] marker for in-site redirects", result, not(containsString("[!]")));
	}

	//## `collectRedirect()`

	/// Tests that [PlanDescriber#collectRedirect(URI, Artifact, List)] extracts a page redirect entry.
	@Test
	void testCollectRedirectExtractsPageRedirect() {
		final PageMummifier pageMummifier = mock(PageMummifier.class);
		final UrfObject description = new UrfObject();
		description.setPropertyValue(PROPERTY_TAG_MUMMY_ALT_LOCATION, "old-page.html");
		final Artifact page = new DummyArtifact(pageMummifier, SOURCE_DIRECTORY.resolve("new-page.html"), TARGET_DIRECTORY.resolve("new-page.html"), description);
		final List<PlanDescriber.RedirectEntry> redirects = new ArrayList<>();
		PlanDescriber.collectRedirect(ROOT_TARGET_PATH_URI, page, redirects);
		assertThat("one redirect extracted", redirects, hasSize(1));
		final var entry = redirects.getFirst();
		assertThat("alt location reference", entry.altLocationReference().toString(), is("old-page.html"));
		assertThat("resource reference", entry.resourceReference().toString(), is("new-page.html"));
		assertThat("not a collection redirect", entry.collection(), is(false));
		assertThat("in-site redirect has no warning", entry.optionalWarning(), is(Optional.empty()));
	}

	/// Tests that [PlanDescriber#collectRedirect(URI, Artifact, List)] extracts a collection redirect
	/// with trailing slash via [URIs#toCollectionURI(URI)].
	@Test
	void testCollectRedirectExtractsCollectionRedirect() {
		final Mummifier directoryMummifier = mock(Mummifier.class);
		final PageMummifier pageMummifier = mock(PageMummifier.class);
		final UrfObject contentDescription = new UrfObject();
		contentDescription.setPropertyValue(PROPERTY_TAG_MUMMY_ALT_LOCATION, "../old-section");
		final Artifact contentArtifact = new DummyArtifact(pageMummifier, SOURCE_DIRECTORY.resolve("new-section").resolve("index.html"),
				TARGET_DIRECTORY.resolve("new-section").resolve("index.html"), contentDescription);
		final DirectoryArtifact dirWithRedirect = new DirectoryArtifact(directoryMummifier, SOURCE_DIRECTORY.resolve("new-section"),
				TARGET_DIRECTORY.resolve("new-section"), contentArtifact, Set.of());
		final List<PlanDescriber.RedirectEntry> redirects = new ArrayList<>();
		PlanDescriber.collectRedirect(ROOT_TARGET_PATH_URI, dirWithRedirect, redirects);
		assertThat("one redirect extracted", redirects, hasSize(1));
		final var entry = redirects.getFirst();
		assertThat("alt location reference for collection", entry.altLocationReference().toString(), is("old-section"));
		assertThat("resource reference has trailing slash", entry.resourceReference().toString(), is("new-section/"));
		assertThat("is a collection redirect", entry.collection(), is(true));
		assertThat("in-site redirect has no warning", entry.optionalWarning(), is(Optional.empty()));
	}

	/// Tests that [PlanDescriber#collectRedirect(URI, Artifact, List)] flags out-of-site redirects.
	@Test
	void testCollectRedirectWarnsOnOutOfSiteRedirect() {
		final PageMummifier pageMummifier = mock(PageMummifier.class);
		final UrfObject description = new UrfObject();
		description.setPropertyValue(PROPERTY_TAG_MUMMY_ALT_LOCATION, "../../outside.html");
		final Artifact page = new DummyArtifact(pageMummifier, SOURCE_DIRECTORY.resolve("page.html"), TARGET_DIRECTORY.resolve("page.html"), description);
		final List<PlanDescriber.RedirectEntry> redirects = new ArrayList<>();
		PlanDescriber.collectRedirect(ROOT_TARGET_PATH_URI, page, redirects);
		assertThat("one redirect extracted", redirects, hasSize(1));
		assertThat("out-of-site redirect has warning", redirects.getFirst().optionalWarning().isPresent(), is(true));
	}

	/// Tests that [PlanDescriber#collectRedirect(URI, Artifact, List)] does nothing when no `altLocation` is declared.
	@Test
	void testCollectRedirectIgnoresArtifactWithoutAltLocation() {
		final PageMummifier pageMummifier = mock(PageMummifier.class);
		final Artifact page = new DummyArtifact(pageMummifier, SOURCE_DIRECTORY.resolve("page.html"), TARGET_DIRECTORY.resolve("page.html"));
		final List<PlanDescriber.RedirectEntry> redirects = new ArrayList<>();
		PlanDescriber.collectRedirect(ROOT_TARGET_PATH_URI, page, redirects);
		assertThat("no redirect for artifact without altLocation", redirects, is(empty()));
	}

	//## `RedirectEntry` sorting

	/// Tests that [PlanDescriber.RedirectEntry] sorts collections before pages, then alphabetically.
	@Test
	void testRedirectEntrySorting() {
		final var collectionB = new PlanDescriber.RedirectEntry(URIPath.of("beta/"), URIPath.of("new-beta/"), true, Optional.empty());
		final var collectionA = new PlanDescriber.RedirectEntry(URIPath.of("alpha/"), URIPath.of("new-alpha/"), true, Optional.empty());
		final var pageB = new PlanDescriber.RedirectEntry(URIPath.of("b-page.html"), URIPath.of("new-b.html"), false, Optional.empty());
		final var pageA = new PlanDescriber.RedirectEntry(URIPath.of("a-page.html"), URIPath.of("new-a.html"), false, Optional.empty());
		final var sorted = new ArrayList<>(List.of(pageB, collectionB, pageA, collectionA));
		Collections.sort(sorted);
		assertThat("sorted order", sorted, contains(collectionA, collectionB, pageA, pageB));
	}

}
