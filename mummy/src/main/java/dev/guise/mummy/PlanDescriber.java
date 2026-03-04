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

import static com.globalmentor.net.URIs.*;
import static dev.guise.mummy.Artifact.*;
import static java.util.Objects.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;

import org.jspecify.annotations.*;

import com.globalmentor.net.URIPath;
import dev.guise.mummy.mummify.image.ImageMummifier;
import dev.guise.mummy.mummify.page.PageMummifier;

/// Writes a human-readable description of a [MummyPlan] to an [Appendable].
///
/// The description includes artifact counts by category and a redirect inventory. The artifact tree is walked in a single
/// pass with local counters — no intermediate data structure is produced.
///
/// @see MummyPlan
public class PlanDescriber {

	private final MummyPlan plan;
	private final URI rootTargetPathUri;
	private final Path siteSourceDirectory;

	/// Constructor.
	/// @param plan The mummy plan to describe.
	/// @param rootTargetPathUri The URI form of the root artifact target path, used for relativizing artifact paths into
	///        site-relative resource references.
	/// @param siteSourceDirectory The site source directory, displayed in the summary header.
	public PlanDescriber(@NonNull final MummyPlan plan, @NonNull final URI rootTargetPathUri, @NonNull final Path siteSourceDirectory) {
		this.plan = requireNonNull(plan);
		this.rootTargetPathUri = requireNonNull(rootTargetPathUri);
		this.siteSourceDirectory = requireNonNull(siteSourceDirectory);
	}

	/// Writes the plan description to the given appendable.
	/// @param appendable The target for the description output.
	/// @param verbose Whether to include per-item detail listings such as individual redirect mappings.
	/// @throws IOException if an I/O error occurs while writing.
	public void describeTo(@NonNull final Appendable appendable, final boolean verbose) throws IOException {
		requireNonNull(appendable);
		final Artifact rootArtifact = plan.getRootArtifact();

		//## walk the artifact tree
		long totalCount = 0;
		long pageCount = 0;
		long collectionCount = 0;
		long imageCount = 0;
		long otherCount = 0;
		long postCount = 0;
		final List<RedirectEntry> redirects = new ArrayList<>();

		final Deque<Artifact> stack = new ArrayDeque<>();
		stack.push(rootArtifact);
		while(!stack.isEmpty()) {
			final Artifact artifact = stack.pop();
			totalCount++;
			if(artifact instanceof CollectionArtifact collectionArtifact) {
				collectionCount++;
				for(final Artifact child : collectionArtifact.getChildArtifacts()) {
					stack.push(child);
				}
			} else if(artifact.getMummifier() instanceof PageMummifier) {
				pageCount++;
			} else if(artifact.getMummifier() instanceof ImageMummifier) {
				imageCount++;
			} else {
				otherCount++;
			}
			if(artifact.isPost()) {
				postCount++;
			}
			collectRedirect(rootTargetPathUri, artifact, redirects);
		}

		final List<RedirectEntry> sortedRedirects = redirects.stream().sorted().toList();
		final long redirectCollectionCount = sortedRedirects.stream().filter(RedirectEntry::collection).count();
		final long redirectPageCount = sortedRedirects.size() - redirectCollectionCount;
		final long warningCount = sortedRedirects.stream().filter(r -> r.optionalWarning().isPresent()).count();

		//## format output
		appendable.append("Site Plan%n".formatted());
		appendable.append("  %-14s%s%n".formatted("Source:", siteSourceDirectory));
		appendable.append("  %-14s%d%n".formatted("Artifacts:", totalCount));
		appendable.append("    %-14s%d%n".formatted("Pages:", pageCount));
		appendable.append("    %-14s%d%n".formatted("Collections:", collectionCount));
		appendable.append("    %-14s%d%n".formatted("Images:", imageCount));
		appendable.append("    %-14s%d%n".formatted("Other:", otherCount));
		appendable.append("    %-14s%d%n".formatted("Posts:", postCount));
		appendable.append("  %-14s%d%n".formatted("Redirects:", sortedRedirects.size()));
		appendable.append("    %-14s%d%n".formatted("Collection:", redirectCollectionCount));
		appendable.append("    %-14s%d%n".formatted("Page:", redirectPageCount));
		if(warningCount > 0) {
			appendable.append("    %-14s%d [!]%n".formatted("Warnings:", warningCount));
		}

		if(verbose && !sortedRedirects.isEmpty()) {
			appendable.append("%n".formatted());
			appendable.append("  Redirect Details:%n".formatted());
			for(final RedirectEntry redirect : sortedRedirects) {
				final String warningMarker = redirect.optionalWarning().map(w -> " " + w.marker()).orElse("");
				appendable.append("    /%s -> /%s%s%n".formatted(
						redirect.altLocationReference(), redirect.resourceReference(), warningMarker));
			}
		}

		//## legend
		final Set<PlanWarning> warnings = EnumSet.noneOf(PlanWarning.class);
		sortedRedirects.stream().flatMap(r -> r.optionalWarning().stream()).forEach(warnings::add);
		if(!warnings.isEmpty()) {
			appendable.append("%n".formatted());
			for(final PlanWarning warning : warnings) {
				appendable.append("  %s %s%n".formatted(warning.marker(), warning.description()));
			}
		}
	}

	/// If the artifact declares a `mummy/altLocation`, adds a [RedirectEntry] to the list.
	/// @implSpec This follows the same URI processing chain as [S3Website#planResource]:
	///           parse as [URIPath], resolve against the artifact's target path URI (in collection form for collection
	///           artifacts), relativize against the site root, and check for site-boundary violations.
	/// @param rootTargetPathUri The URI of the site root target path for relativization.
	/// @param artifact The artifact to check.
	/// @param redirects The list to which any redirect entry is added.
	static void collectRedirect(@NonNull final URI rootTargetPathUri, @NonNull final Artifact artifact, @NonNull final List<RedirectEntry> redirects) {
		requireNonNull(rootTargetPathUri);
		requireNonNull(redirects);
		artifact.getResourceDescription().findPropertyValue(PROPERTY_TAG_MUMMY_ALT_LOCATION)
				.filter(CharSequence.class::isInstance)
				.map(Object::toString)
				.map(URIPath::of)
				.map(altLocationReference -> {
					final URI artifactTargetUri = artifact.getTargetPath().toUri();
					return resolve(artifact instanceof CollectionArtifact ? toCollectionURI(artifactTargetUri) : artifactTargetUri,
							altLocationReference);
				})
				.map(altLocationUri -> URIPath.relativize(rootTargetPathUri, altLocationUri))
				.ifPresent(altLocationReference -> {
					final URIPath targetReference = Artifact.relativizeResourceReference(rootTargetPathUri, artifact);
					final Optional<PlanWarning> optionalWarning = altLocationReference.isSubPath()
							? Optional.empty() : Optional.of(PlanWarning.REDIRECT_OUTSIDE_SITE);
					redirects.add(new RedirectEntry(altLocationReference, targetReference,
							artifact instanceof CollectionArtifact, optionalWarning));
				});
	}

	/// A diagnostic warning that can be attached to plan entries.
	/// @param marker The short marker displayed inline (e.g. `[!]`).
	/// @param description The legend description displayed at the bottom of the output.
	enum PlanWarning {
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
	record RedirectEntry(URIPath altLocationReference, URIPath resourceReference, boolean collection,
			Optional<PlanWarning> optionalWarning) implements Comparable<RedirectEntry> {
		/// Validation constructor.
		RedirectEntry {
			requireNonNull(altLocationReference);
			requireNonNull(resourceReference);
			requireNonNull(optionalWarning);
		}
		/// @implSpec Collections sort before non-collections; within each group, entries are sorted alphabetically
		///           by alternate location reference.
		@Override
		public int compareTo(@NonNull final RedirectEntry other) {
			if(this.collection != other.collection) {
				return this.collection ? -1 : 1;
			}
			return this.altLocationReference.toString().compareTo(other.altLocationReference.toString());
		}
	}

}
