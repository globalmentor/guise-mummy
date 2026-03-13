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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.util.*;

import org.junit.jupiter.api.*;

import com.globalmentor.net.URIPath;

import dev.guise.mummy.plan.PlanSummary.*;

/// Tests of [PlanSummary].
public class PlanSummaryTest {

	//## builder accumulation

	/// Tests that [PlanSummary.Builder] accumulates counts correctly and that [PlanSummary#totalCount()] is the sum.
	@Test
	void testBuilderAccumulatesCounts() {
		final var builder = PlanSummary.builder();
		builder.incrementPageCount();
		builder.incrementPageCount();
		builder.incrementPageCount();
		builder.incrementCollectionCount();
		builder.incrementCollectionCount();
		builder.incrementImageCount();
		builder.incrementOtherCount();
		builder.incrementPostCount();
		final PlanSummary summary = builder.build();
		assertThat("page count", summary.pageCount(), is(3L));
		assertThat("collection count", summary.collectionCount(), is(2L));
		assertThat("image count", summary.imageCount(), is(1L));
		assertThat("other count", summary.otherCount(), is(1L));
		assertThat("post count", summary.postCount(), is(1L));
		assertThat("total is sum of page + collection + image + other", summary.totalCount(), is(7L));
	}

	/// Tests that an empty builder produces zero counts and empty redirects.
	@Test
	void testBuilderDefaultsToZeroCounts() {
		final PlanSummary summary = PlanSummary.builder().build();
		assertThat("page count", summary.pageCount(), is(0L));
		assertThat("collection count", summary.collectionCount(), is(0L));
		assertThat("image count", summary.imageCount(), is(0L));
		assertThat("other count", summary.otherCount(), is(0L));
		assertThat("post count", summary.postCount(), is(0L));
		assertThat("total count", summary.totalCount(), is(0L));
		assertThat("no redirects", summary.sortedRedirects(), is(empty()));
	}

	/// Tests that the builder sorts redirect entries by [RedirectEntry#compareTo].
	@Test
	void testBuilderSortsRedirects() {
		final var builder = PlanSummary.builder();
		final var redirectB = new RedirectEntry(URIPath.of("beta.html"), URI.create("new-beta.html"), Optional.empty());
		final var redirectA = new RedirectEntry(URIPath.of("alpha.html"), URI.create("new-alpha.html"), Optional.empty());
		builder.addRedirect(redirectB);
		builder.addRedirect(redirectA);
		final PlanSummary summary = builder.build();
		assertThat("redirects sorted by alt location", summary.sortedRedirects(), contains(redirectA, redirectB));
	}

	//## derived accessors

	/// Tests that [PlanSummary#warningCount()] counts entries with a present warning.
	@Test
	void testWarningCount() {
		final var builder = PlanSummary.builder();
		builder.addRedirect(new RedirectEntry(URIPath.of("ok.html"), URI.create("new-ok.html"), Optional.empty()));
		builder.addRedirect(new RedirectEntry(URIPath.of("../../outside.html"), URI.create("page.html"), Optional.of(PlanWarning.REDIRECT_OUTSIDE_SITE)));
		final PlanSummary summary = builder.build();
		assertThat("warning count", summary.warningCount(), is(1L));
	}

	//## validation

	/// Tests that the validation constructor rejects negative counts.
	@Test
	void testValidationRejectsNegativeCounts() {
		assertThrows(IllegalArgumentException.class, () -> new PlanSummary(-1, 0, 0, 0, 0, List.of()), "negative pageCount");
		assertThrows(IllegalArgumentException.class, () -> new PlanSummary(0, -1, 0, 0, 0, List.of()), "negative collectionCount");
		assertThrows(IllegalArgumentException.class, () -> new PlanSummary(0, 0, -1, 0, 0, List.of()), "negative imageCount");
		assertThrows(IllegalArgumentException.class, () -> new PlanSummary(0, 0, 0, -1, 0, List.of()), "negative otherCount");
		assertThrows(IllegalArgumentException.class, () -> new PlanSummary(0, 0, 0, 0, -1, List.of()), "negative postCount");
	}

	/// Tests that the sorted redirects list is defensively copied and immutable.
	@Test
	void testSortedRedirectsDefensivelyCopied() {
		final var redirect = new RedirectEntry(URIPath.of("old.html"), URI.create("new.html"), Optional.empty());
		final var mutableList = new ArrayList<>(List.of(redirect));
		final PlanSummary summary = new PlanSummary(0, 0, 0, 0, 0, mutableList);
		mutableList.clear(); // mutate the source list
		assertThat("record retains its own copy", summary.sortedRedirects(), hasSize(1));
		assertThrows(UnsupportedOperationException.class, () -> summary.sortedRedirects().add(redirect), "returned list is immutable");
	}

}
