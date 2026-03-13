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

package dev.guise.mummy.plan;

import static com.github.npathai.hamcrestopt.OptionalMatchers.*;
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

import dev.guise.mummy.*;
import dev.guise.mummy.mummify.Mummifier;
import dev.guise.mummy.mummify.collection.DirectoryArtifact;
import dev.guise.mummy.mummify.image.ImageMummifier;
import dev.guise.mummy.mummify.page.PageMummifier;
import dev.guise.mummy.plan.PlanSummary.*;
import io.urf.model.UrfObject;

/// Tests of [PlanDescriber].
public class PlanDescriberTest {

	private static final Path PROJECT_DIRECTORY = getTempDirectory().resolve("project");
	private static final Path SOURCE_DIRECTORY = PROJECT_DIRECTORY.resolve("src").resolve("site");
	private static final Path TARGET_DIRECTORY = PROJECT_DIRECTORY.resolve("target").resolve("site");
	private static final URI ROOT_TARGET_PATH_URI = toCollectionURI(TARGET_DIRECTORY.toUri());
	private static final String NL = System.lineSeparator();

	/// Creates a [PlanDescriber] with the standard test root target URI.
	/// @param plan The plan to describe.
	/// @return A new plan describer configured for testing.
	private static PlanDescriber planDescriber(final MummyPlan plan) {
		return new PlanDescriber(plan, ROOT_TARGET_PATH_URI);
	}

	//## `summarize()`

	/// Tests that [PlanDescriber#summarize()] classifies artifacts by mummifier type and collects redirects.
	@Test
	void testSummarizeClassifiesArtifactsAndCollectsRedirects() {
		final Mummifier directoryMummifier = mock(Mummifier.class);
		final PageMummifier pageMummifier = mock(PageMummifier.class);
		final ImageMummifier imageMummifier = mock(ImageMummifier.class);
		final Mummifier opaqueMummifier = mock(Mummifier.class);
		final UrfObject redirectDescription = new UrfObject();
		redirectDescription.setPropertyValue(PROPERTY_TAG_MUMMY_ALT_LOCATION, "old-page.html");
		final Artifact page = new DummyArtifact(pageMummifier, SOURCE_DIRECTORY.resolve("about.html"), TARGET_DIRECTORY.resolve("about.html"));
		final Artifact pageWithRedirect = new DummyArtifact(pageMummifier, SOURCE_DIRECTORY.resolve("new-page.html"), TARGET_DIRECTORY.resolve("new-page.html"),
				redirectDescription);
		final Artifact image = new DummyArtifact(imageMummifier, SOURCE_DIRECTORY.resolve("photo.jpg"), TARGET_DIRECTORY.resolve("photo.jpg"));
		final Artifact opaque = new DummyArtifact(opaqueMummifier, SOURCE_DIRECTORY.resolve("data.json"), TARGET_DIRECTORY.resolve("data.json"));
		final Artifact post = new DummyArtifact(pageMummifier, SOURCE_DIRECTORY.resolve("@2026-01-15-hello.html"),
				TARGET_DIRECTORY.resolve("@2026-01-15-hello.html"));
		final DirectoryArtifact root = new DirectoryArtifact(directoryMummifier, SOURCE_DIRECTORY, TARGET_DIRECTORY, null,
				Set.of(page, pageWithRedirect, image, opaque, post));
		final MummyPlan plan = new DefaultMummyPlan(root);
		final PlanSummary summary = planDescriber(plan).summarize();
		assertThat("page count includes pages and posts", summary.pageCount(), is(3L));
		assertThat("collection count (root only)", summary.collectionCount(), is(1L));
		assertThat("image count", summary.imageCount(), is(1L));
		assertThat("other count", summary.otherCount(), is(1L));
		assertThat("post count", summary.postCount(), is(1L));
		assertThat("total is sum of categories", summary.totalCount(), is(6L));
		assertThat("one redirect collected", summary.sortedRedirects(), hasSize(1));
		assertThat("redirect alt location", summary.sortedRedirects().getFirst().altLocationReference().toString(), is("old-page.html"));
	}

	/// Tests that [PlanDescriber#summarize()] skips subsumed content artifacts.
	@Test
	void testSummarizeSkipsSubsumedArtifacts() {
		final Mummifier directoryMummifier = mock(Mummifier.class);
		final PageMummifier pageMummifier = mock(PageMummifier.class);
		final Artifact contentArtifact = new DummyArtifact(pageMummifier, SOURCE_DIRECTORY.resolve("sub").resolve("index.html"),
				TARGET_DIRECTORY.resolve("sub").resolve("index.html"));
		final DirectoryArtifact subDir = new DirectoryArtifact(directoryMummifier, SOURCE_DIRECTORY.resolve("sub"), TARGET_DIRECTORY.resolve("sub"),
				contentArtifact, Set.of());
		final DirectoryArtifact root = new DirectoryArtifact(directoryMummifier, SOURCE_DIRECTORY, TARGET_DIRECTORY, null, Set.of(subDir));
		final MummyPlan plan = new DefaultMummyPlan(root);
		final PlanSummary summary = planDescriber(plan).summarize();
		assertThat("content artifact is not counted as a page", summary.pageCount(), is(0L));
		assertThat("two collections: root + sub", summary.collectionCount(), is(2L));
		assertThat("total excludes subsumed content artifact", summary.totalCount(), is(2L));
	}

	//## `writeTo()`

	/// Tests that [PlanDescriber#writeTo] formats a pre-constructed [PlanSummary] correctly,
	/// independent of `summarize()`.
	@Test
	void testWriteToFormatsPreConstructedSummary() throws IOException {
		final Mummifier mummifier = mock(Mummifier.class);
		final DirectoryArtifact root = new DirectoryArtifact(mummifier, SOURCE_DIRECTORY, TARGET_DIRECTORY, null, Set.of());
		final MummyPlan plan = new DefaultMummyPlan(root);
		final var redirect = new RedirectEntry(URIPath.of("old.html"), URIPath.of("new.html"), false, Optional.of(PlanWarning.REDIRECT_OUTSIDE_SITE));
		final PlanSummary summary = new PlanSummary(5, 2, 3, 1, 1, List.of(redirect));
		final StringBuilder output = new StringBuilder();
		planDescriber(plan).writeTo(output, SOURCE_DIRECTORY, summary, true);
		final String result = output.toString();
		assertThat("artifacts from summary, not from walk", result, containsString("Artifacts:    11"));
		assertThat("pages", result, containsString("Pages:        5"));
		assertThat("collections", result, containsString("Collections:  2"));
		assertThat("images", result, containsString("Images:       3"));
		assertThat("other", result, containsString("Other:        1"));
		assertThat("posts", result, containsString("Posts:        1"));
		assertThat("redirect count", result, containsString("Redirects:    1"));
		assertThat("verbose shows redirect detail", result, containsString("    old.html -> new.html [!]" + NL));
		assertThat("legend present", result, containsString("[!] redirect target is outside the site boundary"));
	}

	//## `describeTo()` (end-to-end via convenience method)

	/// Tests that [PlanDescriber#describeTo(Appendable, Path, boolean)] produces correct counts for a tree
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
		planDescriber(plan).describeTo(output, SOURCE_DIRECTORY, false);
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
		planDescriber(plan).describeTo(output, SOURCE_DIRECTORY, false);
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
		planDescriber(plan).describeTo(output, SOURCE_DIRECTORY, false);
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
		planDescriber(plan).describeTo(output, SOURCE_DIRECTORY, false);
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
		planDescriber(plan).describeTo(output, SOURCE_DIRECTORY, false);
		final String result = output.toString();
		assertThat("redirect count", result, containsString("Redirects:    1"));
		assertThat("page redirect count", result, containsString("Page Targets:         1"));
		assertThat("collection redirect count", result, containsString("Collection Targets:   0"));
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
		planDescriber(plan).describeTo(output, SOURCE_DIRECTORY, false);
		final String result = output.toString();
		assertThat("redirect count", result, containsString("Redirects:    1"));
		assertThat("collection redirect count", result, containsString("Collection Targets:   1"));
		assertThat("page redirect count", result, containsString("Page Targets:         0"));
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
		planDescriber(plan).describeTo(output, SOURCE_DIRECTORY, true);
		final String result = output.toString();
		assertThat("verbose output includes redirect details header", result, containsString("Redirect Details:"));
		assertThat("verbose output includes redirect mapping with -> arrow", result, containsString("    old-page.html -> new-page.html" + NL));
	}

	/// Tests that verbose output decodes percent-encoded paths at three UTF-8 encoding boundaries:
	/// 2-byte (`café.html`), 3-byte (`日記.html`, Japanese for "diary"), and 4-byte (`𝄞.html`, musical G clef U+1D11E).
	@Test
	void testDescribeToVerboseDecodesNonAsciiPaths() throws IOException {
		final Mummifier directoryMummifier = mock(Mummifier.class);
		final PageMummifier pageMummifier = mock(PageMummifier.class);
		final UrfObject desc2byte = new UrfObject();
		desc2byte.setPropertyValue(PROPERTY_TAG_MUMMY_ALT_LOCATION, "caf%C3%A9.html"); // café.html
		final Artifact page2byte = new DummyArtifact(pageMummifier, SOURCE_DIRECTORY.resolve("new-café.html"), TARGET_DIRECTORY.resolve("new-café.html"),
				desc2byte);
		final UrfObject desc3byte = new UrfObject();
		desc3byte.setPropertyValue(PROPERTY_TAG_MUMMY_ALT_LOCATION, "%E6%97%A5%E8%A8%98.html"); // 日記.html
		final Artifact page3byte = new DummyArtifact(pageMummifier, SOURCE_DIRECTORY.resolve("新日記.html"), TARGET_DIRECTORY.resolve("新日記.html"), desc3byte);
		final UrfObject desc4byte = new UrfObject();
		desc4byte.setPropertyValue(PROPERTY_TAG_MUMMY_ALT_LOCATION, "%F0%9D%84%9E.html"); // 𝄞.html (U+1D11E, musical G clef)
		final Artifact page4byte = new DummyArtifact(pageMummifier, SOURCE_DIRECTORY.resolve("new-𝄞.html"), TARGET_DIRECTORY.resolve("new-𝄞.html"), desc4byte);
		final DirectoryArtifact root = new DirectoryArtifact(directoryMummifier, SOURCE_DIRECTORY, TARGET_DIRECTORY, null, Set.of(page2byte, page3byte, page4byte));
		final MummyPlan plan = new DefaultMummyPlan(root);

		final StringBuilder output = new StringBuilder();
		planDescriber(plan).describeTo(output, SOURCE_DIRECTORY, true);
		final String result = output.toString();
		assertThat("2-byte UTF-8 decoded (Latin accented)", result, containsString("    caf\u00e9.html -> new-caf\u00e9.html" + NL));
		assertThat("3-byte UTF-8 decoded (CJK)", result, containsString("    \u65e5\u8a18.html -> \u65b0\u65e5\u8a18.html" + NL));
		assertThat("4-byte UTF-8 decoded (supplementary, outside BMP)", result, containsString("    \ud834\udd1e.html -> new-\ud834\udd1e.html" + NL));
	}

	/// Tests that verbose output omits redirect details when there are no redirects.
	@Test
	void testDescribeToVerboseOmitsRedirectDetailsWhenEmpty() throws IOException {
		final Mummifier mummifier = mock(Mummifier.class);
		final DirectoryArtifact root = new DirectoryArtifact(mummifier, SOURCE_DIRECTORY, TARGET_DIRECTORY, null, Set.of());
		final MummyPlan plan = new DefaultMummyPlan(root);

		final StringBuilder output = new StringBuilder();
		planDescriber(plan).describeTo(output, SOURCE_DIRECTORY, true);
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
		planDescriber(plan).describeTo(output, SOURCE_DIRECTORY, false);
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
		planDescriber(plan).describeTo(output, SOURCE_DIRECTORY, true);
		final String result = output.toString();
		assertThat("warning count shown in summary", result, containsString("Warnings:             1 [!]"));
		assertThat("redirect detail shows out-of-site path with warning marker", result, containsString("    ../../outside.html -> page.html [!]" + NL));
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
		planDescriber(plan).describeTo(output, SOURCE_DIRECTORY, true);
		final String result = output.toString();
		assertThat("no warning count line for in-site redirects", result, not(containsString("Warnings:")));
		assertThat("no [!] marker for in-site redirects", result, not(containsString("[!]")));
	}

	//## `findRedirect()`

	/// Tests that [PlanDescriber#findRedirect(URI, Artifact)] extracts a page redirect entry.
	@Test
	void testFindRedirectExtractsPageRedirect() {
		final PageMummifier pageMummifier = mock(PageMummifier.class);
		final UrfObject description = new UrfObject();
		description.setPropertyValue(PROPERTY_TAG_MUMMY_ALT_LOCATION, "old-page.html");
		final Artifact page = new DummyArtifact(pageMummifier, SOURCE_DIRECTORY.resolve("new-page.html"), TARGET_DIRECTORY.resolve("new-page.html"), description);
		final Optional<RedirectEntry> foundRedirect = PlanDescriber.findRedirect(ROOT_TARGET_PATH_URI, page);
		assertThat("redirect found", foundRedirect, isPresentAnd(notNullValue()));
		final var entry = foundRedirect.orElseThrow();
		assertThat("alt location reference", entry.altLocationReference().toString(), is("old-page.html"));
		assertThat("resource reference", entry.resourceReference().toString(), is("new-page.html"));
		assertThat("not a collection redirect", entry.collection(), is(false));
		assertThat("in-site redirect has no warning", entry.optionalWarning(), is(Optional.empty()));
	}

	/// Tests that [PlanDescriber#findRedirect(URI, Artifact)] extracts a collection redirect
	/// with trailing slash via [URIs#toCollectionURI(URI)].
	@Test
	void testFindRedirectExtractsCollectionRedirect() {
		final Mummifier directoryMummifier = mock(Mummifier.class);
		final PageMummifier pageMummifier = mock(PageMummifier.class);
		final UrfObject contentDescription = new UrfObject();
		contentDescription.setPropertyValue(PROPERTY_TAG_MUMMY_ALT_LOCATION, "../old-section");
		final Artifact contentArtifact = new DummyArtifact(pageMummifier, SOURCE_DIRECTORY.resolve("new-section").resolve("index.html"),
				TARGET_DIRECTORY.resolve("new-section").resolve("index.html"), contentDescription);
		final DirectoryArtifact dirWithRedirect = new DirectoryArtifact(directoryMummifier, SOURCE_DIRECTORY.resolve("new-section"),
				TARGET_DIRECTORY.resolve("new-section"), contentArtifact, Set.of());
		final Optional<RedirectEntry> foundRedirect = PlanDescriber.findRedirect(ROOT_TARGET_PATH_URI, dirWithRedirect);
		assertThat("redirect found", foundRedirect, isPresentAnd(notNullValue()));
		final var entry = foundRedirect.orElseThrow();
		assertThat("alt location reference for collection", entry.altLocationReference().toString(), is("old-section"));
		assertThat("resource reference has trailing slash", entry.resourceReference().toString(), is("new-section/"));
		assertThat("is a collection redirect", entry.collection(), is(true));
		assertThat("in-site redirect has no warning", entry.optionalWarning(), is(Optional.empty()));
	}

	/// Tests that [PlanDescriber#findRedirect(URI, Artifact)] flags out-of-site redirects.
	@Test
	void testFindRedirectWarnsOnOutOfSiteRedirect() {
		final PageMummifier pageMummifier = mock(PageMummifier.class);
		final UrfObject description = new UrfObject();
		description.setPropertyValue(PROPERTY_TAG_MUMMY_ALT_LOCATION, "../../outside.html");
		final Artifact page = new DummyArtifact(pageMummifier, SOURCE_DIRECTORY.resolve("page.html"), TARGET_DIRECTORY.resolve("page.html"), description);
		final Optional<RedirectEntry> foundRedirect = PlanDescriber.findRedirect(ROOT_TARGET_PATH_URI, page);
		assertThat("redirect found", foundRedirect, isPresentAnd(notNullValue()));
		assertThat("out-of-site redirect has warning", foundRedirect.orElseThrow().optionalWarning().isPresent(), is(true));
	}

	/// Tests that [PlanDescriber#findRedirect(URI, Artifact)] returns empty when no `altLocation` is declared.
	@Test
	void testFindRedirectReturnsEmptyWithoutAltLocation() {
		final PageMummifier pageMummifier = mock(PageMummifier.class);
		final Artifact page = new DummyArtifact(pageMummifier, SOURCE_DIRECTORY.resolve("page.html"), TARGET_DIRECTORY.resolve("page.html"));
		final Optional<RedirectEntry> foundRedirect = PlanDescriber.findRedirect(ROOT_TARGET_PATH_URI, page);
		assertThat("no redirect for artifact without altLocation", foundRedirect, isEmpty());
	}

	//## `RedirectEntry` sorting

	/// Tests that [PlanSummary.RedirectEntry] sorts flat by decoded source path, case-insensitive.
	@Test
	void testRedirectEntrySorting() {
		final var collectionB = new RedirectEntry(URIPath.of("beta/"), URIPath.of("new-beta/"), true, Optional.empty());
		final var collectionA = new RedirectEntry(URIPath.of("alpha/"), URIPath.of("new-alpha/"), true, Optional.empty());
		final var pageB = new RedirectEntry(URIPath.of("b-page.html"), URIPath.of("new-b.html"), false, Optional.empty());
		final var pageA = new RedirectEntry(URIPath.of("a-page.html"), URIPath.of("new-a.html"), false, Optional.empty());
		final var sorted = new ArrayList<>(List.of(pageB, collectionB, pageA, collectionA));
		Collections.sort(sorted);
		assertThat("sorted flat by decoded source path, case-insensitive", sorted, contains(pageA, collectionA, pageB, collectionB));
	}

}
