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

import static com.globalmentor.java.Conditions.*;
import static java.util.Objects.*;

import java.util.*;

import org.jspecify.annotations.*;

import com.globalmentor.net.URIPath;

/// Immutable summary of a [dev.guise.mummy.MummyPlan] analysis: artifact counts by category and the redirect inventory.
///
/// Instances are produced by [PlanDescriber#summarize()] and consumed for display (via [PlanDescriber#writeTo])
/// or by deploy targets that need redirect and count data.
///
/// @param pageCount The number of page artifacts.
/// @param collectionCount The number of collection (directory) artifacts.
/// @param imageCount The number of image artifacts.
/// @param otherCount The number of artifacts that are not pages, collections, or images.
/// @param postCount The number of post artifacts (orthogonal to category — a post is also a page).
/// @param sortedRedirects The redirect entries, sorted by alternate location reference.
public record PlanSummary(long pageCount, long collectionCount, long imageCount, long otherCount, long postCount,
		List<RedirectEntry> sortedRedirects) {

	/// Validation constructor.
	public PlanSummary {
		checkArgumentNotNegative(pageCount);
		checkArgumentNotNegative(collectionCount);
		checkArgumentNotNegative(imageCount);
		checkArgumentNotNegative(otherCount);
		checkArgumentNotNegative(postCount);
		sortedRedirects = List.copyOf(sortedRedirects);
	}

	/// Returns the total artifact count: the sum of pages, collections, images, and other artifacts.
	/// @return The total artifact count.
	public long totalCount() {
		return pageCount + collectionCount + imageCount + otherCount;
	}

	/// Returns the number of redirects targeting collections.
	/// @return The collection redirect count.
	public long redirectCollectionCount() {
		return sortedRedirects.stream().filter(RedirectEntry::collection).count();
	}

	/// Returns the number of redirects targeting pages (non-collection redirects).
	/// @return The page redirect count.
	public long redirectPageCount() {
		return sortedRedirects.size() - redirectCollectionCount();
	}

	/// Returns the number of redirect entries that carry a diagnostic warning.
	/// @return The warning count.
	public long warningCount() {
		return sortedRedirects.stream().filter(r -> r.optionalWarning().isPresent()).count();
	}

	/// Creates a new builder for assembling a [PlanSummary].
	/// @return A new builder.
	public static Builder builder() {
		return new Builder();
	}

	/// A diagnostic warning that can be attached to plan entries.
	///
	/// Each warning carries a short `marker()` displayed inline (e.g. `[!]`) and a `description()` for the legend.
	public enum PlanWarning {
		/// The redirect's alternate location resolves outside the site boundary.
		REDIRECT_OUTSIDE_SITE("[!]", "redirect target is outside the site boundary");
		private final String marker;
		private final String description;
		PlanWarning(final String marker, final String description) {
			this.marker = marker;
			this.description = description;
		}
		String marker() { return marker; }
		String description() { return description; }
	}

	/// A redirect entry pairing the old alternate location reference with the artifact's current resource reference.
	/// @param altLocationReference The alternate (old) site-relative resource reference that triggers the redirect.
	/// @param resourceReference The artifact's current site-relative resource reference where the redirect sends the request.
	/// @param collection Whether this is a collection (directory) redirect.
	/// @param optionalWarning A diagnostic warning, if any.
	public record RedirectEntry(URIPath altLocationReference, URIPath resourceReference, boolean collection,
			Optional<PlanWarning> optionalWarning) implements Comparable<RedirectEntry> {
		/// Validation constructor.
		public RedirectEntry {
			requireNonNull(altLocationReference);
			requireNonNull(resourceReference);
			requireNonNull(optionalWarning);
		}
		/// @implSpec Entries are sorted case-insensitively by the decoded form of the alternate location reference.
		@Override
		public int compareTo(@NonNull final RedirectEntry other) {
			//TODO switch to a segment-by-segment URIPath comparator when available in `globalmentor-core`,
			// for more logical directory-level grouping (e.g. `a/b/c` before `a-suffix`)
			return String.CASE_INSENSITIVE_ORDER.compare(
					this.altLocationReference.toDecodedString(),
					other.altLocationReference.toDecodedString());
		}
	}

	/// Mutable accumulator for building a [PlanSummary].
	///
	/// @apiNote The builder accumulates counts and redirect entries. It does not perform artifact classification —
	/// the caller (typically [PlanDescriber]) decides which increment method to call based on artifact type.
	public static final class Builder {
		private long pageCount;
		private long collectionCount;
		private long imageCount;
		private long otherCount;
		private long postCount;
		private final List<RedirectEntry> redirects = new ArrayList<>();

		Builder() {
		}

		/// Increments the page count by one.
		public void incrementPageCount() { pageCount++; }
		/// Increments the collection count by one.
		public void incrementCollectionCount() { collectionCount++; }
		/// Increments the image count by one.
		public void incrementImageCount() { imageCount++; }
		/// Increments the other-artifact count by one.
		public void incrementOtherCount() { otherCount++; }
		/// Increments the post count by one.
		public void incrementPostCount() { postCount++; }

		/// Adds a redirect entry.
		/// @param entry The redirect entry to add.
		public void addRedirect(@NonNull final RedirectEntry entry) {
			redirects.add(requireNonNull(entry));
		}

		/// Builds the [PlanSummary] from the accumulated counts and redirect entries.
		/// @implSpec Redirect entries are sorted by [RedirectEntry#compareTo] before building.
		/// @return A new immutable plan summary.
		public PlanSummary build() {
			final List<RedirectEntry> sorted = redirects.stream().sorted().toList();
			return new PlanSummary(pageCount, collectionCount, imageCount, otherCount, postCount, sorted);
		}
	}

}
