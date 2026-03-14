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

import static com.globalmentor.net.URIs.*;
import static dev.guise.mummy.Artifact.*;
import static java.util.Objects.*;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import org.jspecify.annotations.*;

import com.globalmentor.net.URIPath;

import dev.guise.mummy.*;
import dev.guise.mummy.mummify.image.ImageMummifier;
import dev.guise.mummy.mummify.page.PageMummifier;
import dev.guise.mummy.plan.PlanSummary.*;

/// Summarizes a [MummyPlan] into a [PlanSummary] and writes a human-readable description to an [Appendable].
///
/// This class has two roles:
/// 1. **Summarization** — [summarize()] walks the artifact tree via [MummyPlan#walk] and produces a [PlanSummary]
///    containing artifact counts by category and the redirect inventory.
/// 2. **Description-writing** — [writeTo] formats a [PlanSummary] (together with display inputs such as the
///    site source directory) into human-readable text.
///
/// @see MummyPlan
/// @see PlanSummary
public class PlanDescriber {

	private final MummyPlan plan;
	private final URI rootTargetPathUri;

	/// Constructor.
	/// @param plan The mummy plan to summarize and describe.
	public PlanDescriber(@NonNull final MummyPlan plan) {
		this.plan = requireNonNull(plan);
		// Deploy targets can rely on `Path.toUri()` producing a trailing slash because the target directory
		// exists on disk at deploy time. `PlanDescriber` may run during the plan phase before the target
		// directory is created, so `toCollectionURI()` is needed to ensure proper relativization.
		this.rootTargetPathUri = toCollectionURI(plan.getRootArtifact().getTargetPath().toUri());
	}

	/// Walks the plan's artifact tree and produces a [PlanSummary] with artifact counts and redirect entries.
	/// @implSpec Delegates to [#summarize(ArtifactTreeWalker.Visitor)] with a no-op additional visitor.
	/// @return An immutable summary of the plan.
	public PlanSummary summarize() {
		return summarize((_, _) -> {});
	}

	/// Walks the plan's artifact tree and produces a [PlanSummary], invoking an additional visitor alongside
	/// the summarization logic via [ArtifactTreeWalker.Visitor#andThen].
	///
	/// @implSpec Subsumed artifacts are skipped for both counting and redirect extraction, maintaining behavioral
	///           equivalence with the pre-walker traversal that used [CollectionArtifact#getChildArtifacts()].
	///           The `additionalVisitor` receives every `(artifact, subsumed)` event independently — it can
	///           decide on its own whether to skip subsumed artifacts.
	/// @param additionalVisitor An additional visitor to invoke for each artifact alongside summarization.
	/// @return An immutable summary of the plan.
	public PlanSummary summarize(final ArtifactTreeWalker.Visitor additionalVisitor) {
		final var builder = PlanSummary.builder();
		plan.walk(((ArtifactTreeWalker.Visitor)(artifact, subsumed) -> {
			if(subsumed) {
				return;
			}
			if(artifact instanceof CollectionArtifact) {
				builder.incrementCollectionCount();
			} else if(artifact.getMummifier() instanceof PageMummifier) {
				builder.incrementPageCount();
			} else if(artifact.getMummifier() instanceof ImageMummifier) {
				builder.incrementImageCount();
			} else {
				builder.incrementOtherCount();
			}
			if(artifact.isPost()) {
				builder.incrementPostCount();
			}
			findRedirect(rootTargetPathUri, artifact).ifPresent(builder::addRedirect);
		}).andThen(additionalVisitor));
		return builder.build();
	}

	/// Writes the formatted plan description to the given appendable.
	/// @param appendable The target for the description output.
	/// @param summary The plan summary containing counts and redirect data.
	/// @param verbose Whether to include per-item detail listings such as individual redirect mappings.
	/// @throws IOException if an I/O error occurs while writing.
	public void writeTo(@NonNull final Appendable appendable, @NonNull final PlanSummary summary, final boolean verbose) throws IOException {
		requireNonNull(appendable);
		requireNonNull(summary);
		final List<RedirectEntry> sortedRedirects = summary.sortedRedirects();
		appendable.append("Site Plan%n".formatted());
		appendable.append("  %-14s%s%n".formatted("Source:", plan.getRootArtifact().getSourcePath()));
		appendable.append("  %-14s%d%n".formatted("Artifacts:", summary.totalCount()));
		appendable.append("    %-14s%d%n".formatted("Pages:", summary.pageCount()));
		appendable.append("    %-14s%d%n".formatted("Collections:", summary.collectionCount()));
		appendable.append("    %-14s%d%n".formatted("Images:", summary.imageCount()));
		appendable.append("    %-14s%d%n".formatted("Other:", summary.otherCount()));
		appendable.append("    %-14s%d%n".formatted("Posts:", summary.postCount()));
		appendable.append("  %-14s%d%n".formatted("Redirects:", sortedRedirects.size()));
		if(summary.warningCount() > 0) {
			appendable.append("    %-22s%d [!]%n".formatted("Warnings:", summary.warningCount()));
		}
		if(verbose && !sortedRedirects.isEmpty()) {
			appendable.append("%n".formatted());
			appendable.append("  Redirect Details:%n".formatted());
			for(final RedirectEntry redirect : sortedRedirects) {
				final String warningMarker = redirect.optionalWarning().map(w -> " " + w.marker()).orElse("");
				appendable.append("    %s -> %s%s%n".formatted(redirect.sourcePath().toString(), redirect.targetUri().toASCIIString(), warningMarker));
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

	/// Convenience method that summarizes the plan and writes the description in one call.
	/// @param appendable The target for the description output.
	/// @param verbose Whether to include per-item detail listings such as individual redirect mappings.
	/// @throws IOException if an I/O error occurs while writing.
	public void describeTo(@NonNull final Appendable appendable, final boolean verbose) throws IOException {
		writeTo(appendable, summarize(), verbose);
	}

	/// Examines an artifact for a `mummy/altLocation` property and returns the corresponding redirect entry, if any.
	/// @implSpec This follows the same URI processing chain as `S3Website#planResource`:
	///           parse as [URIPath], resolve against the artifact's target path URI (in collection form for collection
	///           artifacts), relativize against the site root, and check for site-boundary violations.
	/// @param rootTargetPathUri The URI of the site root target path for relativization.
	/// @param artifact The artifact to check.
	/// @return The redirect entry, or empty if the artifact has no `altLocation`.
	static Optional<RedirectEntry> findRedirect(@NonNull final URI rootTargetPathUri, @NonNull final Artifact artifact) {
		requireNonNull(rootTargetPathUri);
		return artifact.getResourceDescription().findPropertyValue(PROPERTY_TAG_MUMMY_ALT_LOCATION).filter(CharSequence.class::isInstance).map(Object::toString)
				.map(URIPath::of).map(altLocationReference -> {
					final URI artifactTargetUri = artifact.getTargetPath().toUri();
					return resolve(artifact instanceof CollectionArtifact ? toCollectionURI(artifactTargetUri) : artifactTargetUri, altLocationReference);
				}).map(altLocationUri -> URIPath.relativize(rootTargetPathUri, altLocationUri)).map(altLocationReference -> {
					final URIPath targetReference = Artifact.relativizeResourceReference(rootTargetPathUri, artifact);
					final Optional<PlanWarning> optionalWarning = altLocationReference.isSubPath() ? Optional.empty() : Optional.of(PlanWarning.REDIRECT_OUTSIDE_SITE);
					return RedirectEntry.of(altLocationReference, targetReference, optionalWarning);
				});
	}

}
