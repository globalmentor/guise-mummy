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

import static com.globalmentor.net.DomainName.*;
import static com.globalmentor.util.Optionals.*;
import static dev.flange.platform.aws.FlangePlatformAws.Templates.Exports.*;
import static dev.guise.mummy.GuiseMummy.*;
import static java.util.Objects.*;
import static java.util.stream.Collectors.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;

import org.jspecify.annotations.*;

import com.globalmentor.model.ConfiguredStateException;
import com.globalmentor.net.*;
import com.globalmentor.security.*;
import com.globalmentor.security.MessageDigests.Algorithm;

import dev.flange.aws.def.AwsProfile;
import dev.flange.aws.s3.support.S3Synchronizer;
import dev.flange.deploy.aws.AwsFlangeDeployer;
import dev.flange.env.FlangeEnvironment;
import dev.flange.env.aws.AwsFlangeEnvironment;
import dev.flange.env.aws.AwsFlangeEnvironmentManager;
import dev.guise.mummy.*;
import dev.guise.mummy.deploy.DeployTarget;
import dev.guise.mummy.deploy.aws.AWS;
import dev.guise.mummy.mummify.Mummifier;
import dev.guise.mummy.mummify.collection.*;
import dev.guise.mummy.plan.*;

import io.clogr.Clogged;
import io.confound.config.Configuration;
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
/// @implSpec This deploy target does not manage DNS. A separately configured [dev.guise.mummy.deploy.Dns] such as
///           [dev.guise.mummy.deploy.aws.Route53] can coexist: because [dev.guise.mummy.GuiseMummy] runs the
///           [dev.guise.mummy.deploy.Dns] lifecycle independently, a [dev.guise.mummy.deploy.aws.Route53] instance
///           will discover the Flange-created hosted zone and upsert its own resource records without interfering with
///           this deploy target. Such records are not tracked by CloudFormation, so they survive environment updates but
///           are also not cleaned up if later removed from the site configuration. If Flange itself adds support for
///           additional DNS records in the future, the two sets of records could conflict.
/// @implNote Unlike [dev.guise.mummy.deploy.aws.S3], which skips unsupported subsumed artifacts entirely during
///           planning, this deploy target delegates file traversal to the
///           [S3Synchronizer][dev.flange.aws.s3.support.S3Synchronizer], which uploads **all** files it encounters.
///           Unsupported subsumed artifacts are therefore still uploaded, but with a default `application/octet-stream`
///           content type. The synchronizer logs a missing-content-type warning on the initial upload only; subsequent
///           syncs fall back to the content type already stored in S3.
/// @see DeployTarget
/// @see AwsFlangeDeployer
public class FlangeWebSite implements DeployTarget, Clogged {

	/// The section-relative configuration key for the Flange environment name.
	public static final String CONFIG_KEY_ENVIRONMENT = "environment";

	private final Optional<AwsProfile> optionalAwsProfile;
	private final FlangeEnvironment.Name envName;

	/// Resolved during [#prepare]; used in [#deploy].
	private AwsFlangeEnvironment flangeEnv;

	/// Constructor.
	/// @param context The context of static site generation.
	/// @param localConfiguration The local configuration for this deployment target section.
	/// @see AWS#CONFIG_KEY_DEPLOY_AWS_PROFILE
	/// @see #CONFIG_KEY_ENVIRONMENT
	public FlangeWebSite(final MummyContext context, final Configuration localConfiguration) {
		this.optionalAwsProfile = context.getConfiguration().findString(AWS.CONFIG_KEY_DEPLOY_AWS_PROFILE).map(AwsProfile::new);
		this.envName = new FlangeEnvironment.Name(localConfiguration.getString(CONFIG_KEY_ENVIRONMENT));
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
	/// @param artifactsByTargetPath All artifacts encountered during the tree walk, indexed by their target path.
	record Manifest(Map<UriPath, URI> redirects, Map<Path, Artifact> artifactsByTargetPath) {
		/// Validation constructor.
		/// @implSpec Defensively copies both maps to ensure immutability.
		Manifest {
			redirects = Map.copyOf(redirects);
			artifactsByTargetPath = Map.copyOf(artifactsByTargetPath);
		}

		/// Mutable accumulator for building a [Manifest] during the artifact tree walk.
		static final class Builder {
			private final Map<Path, Artifact> artifactsByTargetPath = new HashMap<>();

			/// Adds an artifact to the target path index.
			/// @param artifact The artifact to index.
			void addArtifact(final Artifact artifact) {
				artifactsByTargetPath.put(requireNonNull(artifact).getTargetPath(), artifact);
			}

			/// Builds the [Manifest] from the accumulated artifact index and the redirect data in the given [PlanSummary].
			/// @implSpec Warning entries are filtered out of the redirect map — they represent out-of-site-boundary
			///           redirects that cannot be deployed.
			/// @param summary The plan summary from which redirects are extracted.
			/// @return A new immutable manifest.
			Manifest build(final PlanSummary summary) {
				final Map<UriPath, URI> redirects = summary.sortedRedirects().stream().filter(entry -> entry.optionalWarning().isEmpty())
						.collect(toMap(PlanSummary.RedirectEntry::sourcePath, PlanSummary.RedirectEntry::targetUri));
				return new Manifest(redirects, artifactsByTargetPath);
			}
		}
	}

	//## `ArtifactMetadataStrategy`

	/// Adapts Guise Mummy artifact metadata (content type and fingerprint from [io.urf.model.UrfResourceDescription])
	/// to the [S3Synchronizer.MetadataStrategy] contract required by [AwsFlangeDeployer#deploySite].
	///
	/// Artifacts are looked up by target path from a pre-built [Manifest] index and resolved through
	/// [MummyPlan#getPrincipalArtifact] to ensure subsumed artifacts (e.g. directory content) yield the
	/// principal artifact's metadata.
	///
	/// @implNote Thread-safe: the backing map is built once (in [Manifest.Builder#build]) and never modified.
	///           Artifact descriptions are read-only at this point in the lifecycle.
	static class ArtifactMetadataStrategy implements S3Synchronizer.MetadataStrategy, Clogged {

		private final MummyPlan plan;
		private final Map<Path, Artifact> artifactsByTargetPath;

		/// Constructor.
		/// @param plan The mummy plan, used to resolve principal artifacts.
		/// @param artifactsByTargetPath The pre-built artifact target path index from a [Manifest].
		ArtifactMetadataStrategy(final MummyPlan plan, final Map<Path, Artifact> artifactsByTargetPath) {
			this.plan = requireNonNull(plan);
			this.artifactsByTargetPath = requireNonNull(artifactsByTargetPath);
		}

		/// {@inheritDoc}
		/// @implSpec Looks up the artifact for the given file path, then resolves it to its
		///           [principal artifact][MummyPlan#getPrincipalArtifact]. If the artifact is its own principal
		///           (non-subsumed), its metadata is returned directly. If its principal is a [DirectoryArtifact]
		///           whose content artifact matches, the directory's metadata is returned. For any other subsumed
		///           artifact type, a warning is logged and empty is returned — deployment of such artifacts is
		///           not yet supported.
		@Override
		public Optional<S3Synchronizer.Metadata> findMetadata(final Path file, final SequencedSet<Algorithm> preferredHashAlgorithms) {
			return Optional.ofNullable(artifactsByTargetPath.get(file)).flatMap(artifact -> {
				final Artifact principalArtifact = plan.getPrincipalArtifact(artifact);
				if(principalArtifact.equals(artifact)) { // non-subsumed artifact — use its own metadata
					return Optional.of(artifact);
				}
				if(principalArtifact instanceof DirectoryArtifact directoryArtifact // subsumed directory content — use directory's metadata
						&& directoryArtifact.findContentArtifact().filter(artifact::equals).isPresent()) {
					return Optional.of(principalArtifact);
				}
				getLogger().atWarn().log("Skipping metadata for subsumed artifact `{}`; only directory content artifacts are supported.", file);
				return Optional.empty();
			}).map(artifact -> toMetadata(artifact, preferredHashAlgorithms));
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

	//## `DeployTarget` implementation

	@Override
	public Set<String> getSupportedProtocols() {
		return Set.of("https");
	}

	@Override
	public void prepare(@NonNull final MummyContext context) throws IOException {
		getLogger().atInfo().log("Resolving Flange environment `{}` (AWS profile: {}) ...", envName, optionalAwsProfile.map(AwsProfile::value).orElse("<default>"));
		try (final var envManager = AwsFlangeEnvironmentManager.forProfile(optionalAwsProfile)) {
			this.flangeEnv = envManager.resolve(envName)
					.orElseThrow(() -> new ConfiguredStateException("Flange environment `%s` does not exist.".formatted(envName)));
		}
		flangeEnv.findSiteBucketName().orElseThrow(() -> new ConfiguredStateException(
				"Environment `%s` is not configured for site deployment (missing output `%s`).".formatted(envName, SITE_BUCKET_NAME)));
		flangeEnv.findSiteDistributionId().orElseThrow(() -> new ConfiguredStateException(
				"Environment `%s` is not configured for site deployment (missing output `%s`).".formatted(envName, SITE_DISTRIBUTION_ID)));
		flangeEnv.findSiteKeyValueStoreArn().orElseThrow(
				() -> new ConfiguredStateException("Environment `%s` is not configured for site settings (missing output `%s`). Reprovision the environment."
						.formatted(envName, SITE_KEY_VALUE_STORE_ARN)));
		//## verify domain configuration — warn if Guise project domains don't match the Flange environment
		final var configuration = context.getConfiguration();
		final var foundGuiseDomain = findConfiguredDomain(configuration);
		foundGuiseDomain.ifPresent(guiseDomain -> {
			final var foundFlangeDomain = flangeEnv.findOutput(DOMAIN_NAME).map(DomainName::of).map(ROOT::resolve);
			if(!foundFlangeDomain.equals(foundGuiseDomain)) {
				getLogger().atWarn().log("Project `{}` is `{}`, but Flange environment domain is{}.", CONFIG_KEY_DOMAIN, guiseDomain,
						foundFlangeDomain.map(" `%s`"::formatted).orElse(" not configured"));
			}
		});
		final var foundGuiseSiteDomain = findConfiguredSiteDomain(configuration);
		foundGuiseSiteDomain.ifPresent(guiseSiteDomain -> {
			final var foundFlangeWebDomain = flangeEnv.findOutput(WEB_DOMAIN_NAME).map(DomainName::of).map(ROOT::resolve);
			if(!foundFlangeWebDomain.equals(foundGuiseSiteDomain)) {
				getLogger().atWarn().log("Project `{}` is `{}`, but Flange environment web domain is{}.", CONFIG_KEY_SITE_DOMAIN, guiseSiteDomain,
						foundFlangeWebDomain.map(" `%s`"::formatted).orElse(" not configured"));
			}
		});
		findConfiguredSiteAltDomains(configuration).ifPresent(guiseAltDomains -> { // Guise allows multiple; Flange supports one
			final var foundFlangeAltWebDomain = flangeEnv.findOutput(ALT_WEB_DOMAIN_NAME).map(DomainName::of).map(ROOT::resolve);
			if(!foundFlangeAltWebDomain.filter(guiseAltDomains::contains).isPresent()) { // absent or not among them
				getLogger().atWarn().log("Project configures `{}` ({}), but Flange environment alt web domain is{}.", CONFIG_KEY_SITE_ALT_DOMAINS,
						guiseAltDomains.stream().map("`%s`"::formatted).collect(joining(", ")),
						foundFlangeAltWebDomain.map(" `%s`, which is not among them"::formatted).orElse(" not configured"));
			} else if(guiseAltDomains.size() > 1) { // Flange domain matches, but extras won't be handled
				getLogger().atWarn().log("Project configures {} `{}` entries, but Flange supports only one alt web domain.", guiseAltDomains.size(),
						CONFIG_KEY_SITE_ALT_DOMAINS);
			}
		});
	}

	@Override
	public Optional<URI> deploy(@NonNull final MummyContext context, @NonNull final Artifact rootArtifact) throws IOException {
		//## analyze
		final Optional<String> foundCollectionContentResourceName = findCollectionContentResourceName(context.getConfiguration());
		final var planDescriber = new PlanDescriber(context.getPlan());
		final var manifestBuilder = new Manifest.Builder();
		final PlanSummary summary = planDescriber.summarize((artifact, _) -> manifestBuilder.addArtifact(artifact));
		final Manifest manifest = manifestBuilder.build(summary);
		final var metadataStrategy = new ArtifactMetadataStrategy(context.getPlan(), manifest.artifactsByTargetPath());
		//## apply
		final Path siteTargetDirectory = context.getSiteTargetDirectory();
		try (final var deployer = AwsFlangeDeployer.forProfile(optionalAwsProfile)) {
			deployer.deploySite(siteTargetDirectory, manifest.redirects(), foundCollectionContentResourceName, flangeEnv, metadataStrategy,
					LoggingSynchronizationMonitor::new);
		}
		return flangeEnv.findOutput(WEB_SITE_URL).map(URI::create);
	}

}
