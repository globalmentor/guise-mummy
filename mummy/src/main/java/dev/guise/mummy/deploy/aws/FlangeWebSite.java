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

import static com.globalmentor.util.Optionals.*;
import static java.util.Objects.*;
import static java.util.stream.Collectors.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;

import org.jspecify.annotations.*;

import com.globalmentor.net.*;
import com.globalmentor.security.*;
import com.globalmentor.security.MessageDigests.Algorithm;

import dev.flange.aws.s3.support.S3Synchronizer;
import dev.flange.deploy.aws.AwsFlangeDeployer;
import dev.guise.mummy.*;
import dev.guise.mummy.deploy.DeployTarget;
import dev.guise.mummy.mummify.Mummifier;
import dev.guise.mummy.mummify.collection.DirectoryArtifact;
import dev.guise.mummy.plan.PlanSummary;

import io.clogr.Clogged;
import io.urf.vocab.content.Content;

/// Deploys a site to a [Flange](https://flange.dev/)-managed AWS environment (S3 + CloudFront OAC + CloudFront KVS).
///
/// # Configuration
///
/// Configured as a deploy target section in the project TURF configuration:
///
/// ```turf
/// deploy:
///   aws:
///     profile = "myprofile"
///   ;
///   targets = [
///     * FlangeWebSite:
///       environment = "prod"
///   ]
/// ;
/// ```
///
/// - `environment` — the Flange environment name (required; section-local).
/// - `deploy.aws.profile` — the AWS profile (global, shared with other AWS deploy targets; optional).
///
/// # Lifecycle
///
/// The deploy target follows the standard [DeployTarget] state model:
/// 1. **Constructor** — reads configuration (environment name, AWS profile).
/// 2. **[#prepare]** — resolves the Flange environment and validates site infrastructure.
/// 3. **[#deploy]** — analyzes the artifact tree via [dev.guise.mummy.plan.PlanDescriber], builds a
///    [Manifest], and delegates to [AwsFlangeDeployer#deploySite].
///
/// @see DeployTarget
/// @see AwsFlangeDeployer
public class FlangeWebSite implements DeployTarget, Clogged {

	/// The section-relative configuration key for the Flange environment name.
	public static final String CONFIG_KEY_ENVIRONMENT = "environment";

	/// Placeholder no-args constructor; Chunk 2 will replace with the real constructor.
	FlangeWebSite() {
	}

	//## `Manifest`

	/// Reification of the deploy target's artifact tree analysis — encapsulates everything the deployer needs.
	///
	/// The manifest does not perform its own tree walk. It is assembled by the deploy target's analyze phase,
	/// which reuses [dev.guise.mummy.plan.PlanDescriber#summarize(ArtifactTreeWalker.Visitor)] to piggyback a
	/// manifest-building visitor alongside the [PlanSummary] accumulation.
	///
	/// @param redirects The redirect mappings (site-relative percent-encoded source path → target URI),
	///        extracted from [PlanSummary#sortedRedirects] with warning entries filtered out.
	/// @param artifactsByContentPath The artifact content path index: maps each artifact's filesystem content
	///        path to the artifact that provides its metadata (via resource description delegation).
	/// @implNote The index is keyed by **content file** path, not by the owning artifact's target path. The
	///           [S3Synchronizer] iterates the target directory's files and queries the [ArtifactMetadataStrategy]
	///           with each file path it encounters. For a [DirectoryArtifact], the file on disk is the content
	///           artifact (e.g. `sub/index.html`), not the directory itself (`sub/`). The index maps that content
	///           file path back to the owning directory artifact, which provides the authoritative metadata via
	///           description delegation. This reverse lookup ensures the metadata returned to the synchronizer
	///           reflects the collection artifact's properties, not those of the subsumed content artifact.
	record Manifest(Map<URIPath, URI> redirects, Map<Path, Artifact> artifactsByContentPath) {
		/// Validation constructor.
		/// @implSpec Defensively copies both maps to ensure immutability.
		Manifest {
			redirects = Map.copyOf(redirects);
			artifactsByContentPath = Map.copyOf(artifactsByContentPath);
		}

		/// Mutable accumulator for building a [Manifest] during the artifact tree walk.
		static final class Builder {
			private final Map<Path, Artifact> artifactsByContentPath = new HashMap<>();

			/// Adds an artifact to the content path index.
			/// @apiNote For a [DirectoryArtifact], `contentPath` is the target path of the directory's
			///          content artifact (e.g. `sub/index.html`), while `artifact` is the directory artifact
			///          itself — because the directory provides the authoritative metadata via description
			///          delegation. Non-collection artifacts are their own content, so both paths coincide.
			/// @param contentPath The filesystem path to the artifact's content file.
			/// @param artifact The artifact that provides metadata for this content path.
			void addArtifact(final Path contentPath, final Artifact artifact) {
				artifactsByContentPath.put(requireNonNull(contentPath), requireNonNull(artifact));
			}

			/// Builds the [Manifest] from the accumulated artifact index and the redirect data in the given [PlanSummary].
			/// @implSpec Warning entries are filtered out of the redirect map — they represent out-of-site-boundary
			///           redirects that cannot be deployed.
			/// @param summary The plan summary from which redirects are extracted.
			/// @return A new immutable manifest.
			Manifest build(final PlanSummary summary) {
				final Map<URIPath, URI> redirects = summary.sortedRedirects().stream().filter(entry -> entry.optionalWarning().isEmpty())
						.collect(toMap(PlanSummary.RedirectEntry::sourcePath, PlanSummary.RedirectEntry::targetUri));
				return new Manifest(redirects, artifactsByContentPath);
			}
		}
	}

	//## `ArtifactMetadataStrategy`

	/// Adapts Guise Mummy artifact metadata (content type and fingerprint from [io.urf.model.UrfResourceDescription])
	/// to the [S3Synchronizer.MetadataStrategy] contract required by [AwsFlangeDeployer#deploySite].
	///
	/// The strategy looks up artifacts by their filesystem content path in a pre-built index from the [Manifest].
	/// It does not perform its own tree traversal.
	///
	/// @implNote Thread-safe: the backing map is built once (in [Manifest.Builder#build]) and never modified.
	///           Artifact descriptions are read-only at this point in the lifecycle.
	static class ArtifactMetadataStrategy implements S3Synchronizer.MetadataStrategy {

		private final Map<Path, Artifact> artifactsByContentPath;

		/// Constructor.
		/// @param artifactsByContentPath The pre-built artifact content path index from a [Manifest].
		ArtifactMetadataStrategy(final Map<Path, Artifact> artifactsByContentPath) {
			this.artifactsByContentPath = requireNonNull(artifactsByContentPath);
		}

		@Override
		public Optional<S3Synchronizer.Metadata> findMetadata(final Path file, final SequencedSet<Algorithm> preferredHashAlgorithms) {
			return Optional.ofNullable(artifactsByContentPath.get(file)).map(artifact -> toMetadata(artifact, preferredHashAlgorithms));
		}

		/// Converts an artifact's resource description properties into [S3Synchronizer.Metadata].
		///
		/// @implSpec Reads [Content#TYPE_PROPERTY_TAG] as a [MediaType] and [Content#FINGERPRINT_PROPERTY_TAG] as a
		///           `byte[]` keyed by [Mummifier#FINGERPRINT_ALGORITHM] (SHA-256).
		/// @param artifact The artifact whose resource description provides metadata.
		/// @param preferredHashAlgorithms The hash algorithms preferred by the synchronizer (not used in this
		///        implementation, which always provides the mummifier's fingerprint algorithm if available).
		/// @return The metadata for the given artifact.
		static S3Synchronizer.Metadata toMetadata(final Artifact artifact, final SequencedSet<Algorithm> preferredHashAlgorithms) {
			final var description = artifact.getResourceDescription();
			final Optional<MediaType> optionalContentType = filterAsInstance(description.findPropertyValue(Content.TYPE_PROPERTY_TAG), MediaType.class);
			final Map<Algorithm, Hash> checksums = filterAsInstance(description.findPropertyValue(Content.FINGERPRINT_PROPERTY_TAG), byte[].class)
					.map(bytes -> Map.of(Mummifier.FINGERPRINT_ALGORITHM, Hash.of(bytes))).orElse(Map.of());
			return new S3Synchronizer.Metadata(optionalContentType, checksums);
		}
	}

	//## synchronization monitor

	/// Logging-based synchronization monitor that reports S3 deployment activity via SLF4J.
	///
	/// @implSpec Equivalent in reporting granularity to the existing [S3] deploy target: INFO per upload and per deletion.
	///           Directory traversal, file discovery, hash generation, and exclusion events are silently ignored.
	///           Unreadable paths produce a WARN.
	static class LoggingSynchronizationMonitor implements AwsFlangeDeployer.SiteSynchronizationMonitor, Clogged {
		@Override
		public void onFileDiscovered(final Path file) {
		}

		@Override
		public void onEnterDirectory(final Path directory) {
		}

		@Override
		public void onDirectoryCompleted(final Path directory) {
		}

		@Override
		public void onSkipUnreadablePath(final Path path) {
			getLogger().atWarn().log("Skipping unreadable path `{}`.", path);
		}

		@Override
		public void onSkipExcludedPath(final Path path) {
		}

		@Override
		public void beforeGenerateFileContentHash(final Path file) {
		}

		@Override
		public void afterGenerateFileContentHash(final Path file) {
		}

		@Override
		public void beforeFileUpload(final Path file, final String s3Key) {
		}

		@Override
		public void afterFileUpload(final Path file, final String s3Key) {
			getLogger().atInfo().log("Deployed object to S3 key `{}`.", s3Key);
		}

		@Override
		public void beforeFilesDelete(final Collection<String> s3Keys) {
		}

		@Override
		public void afterFilesDelete(final Collection<String> s3Keys) {
			for(final String s3Key : s3Keys) {
				getLogger().atInfo().log("Pruned S3 object `{}`.", s3Key);
			}
		}

		@Override
		public void close() {
		}
	}

	//## `DeployTarget` implementation stubs — Chunk 2 will provide the full implementation.

	@Override
	public Set<String> getSupportedProtocols() {
		return Set.of("https");
	}

	@Override
	public void prepare(@NonNull final MummyContext context) throws IOException {
		throw new UnsupportedOperationException("FlangeWebSite.prepare() is not yet implemented (Chunk 2).");
	}

	@Override
	public Optional<URI> deploy(@NonNull final MummyContext context, @NonNull final Artifact rootArtifact) throws IOException {
		throw new UnsupportedOperationException("FlangeWebSite.deploy() is not yet implemented (Chunk 2).");
	}

}
