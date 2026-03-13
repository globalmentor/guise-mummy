# Plan: Flange Deploy Target

Implement `FlangeWebSite` as a `DeployTarget` within Guise Mummy's existing deployment plugin architecture, enabling site deployment to a Flange-managed AWS environment (S3 + CloudFront OAC + CloudFront KVS).

## Overview

**Chunk 1: Foundation** (independent — compiles and tests in isolation)

- Step 1: Add Flange Maven dependencies to BOM and `mummy` module
- Step 2: Site manifest — `FlangeWebSite.Manifest`
- Step 3: MetadataStrategy adapter — `FlangeWebSite.ArtifactMetadataStrategy`
- Step 4: Collection content resource name derivation — `FlangeWebSite.deriveCollectionContentResourceName()`
- Step 5: Logging-based synchronization monitor

**Chunk 2: Integration** (requires Chunk 1)

- Step 6: `FlangeWebSite` deploy target class — constructor, `prepare()`, `deploy()` (with `analyze` / `apply` subphases)
- Step 7: Register `FlangeWebSite` in the deploy target factory switch in `GuiseMummy.mummify()`
- Step 8: Unit tests for `FlangeWebSite`

**Notable decisions:**

- `FlangeWebSite` is a standard `DeployTarget`, configured as `* FlangeWebSite:` in `deploy.targets`. The name follows the Flange convention of "WebSite" (two words), consistent with `FlangePlatformAws.Templates.Exports.WEB_SITE_URL`. Coexists naturally with legacy targets — no special-case orchestration.
- The artifact tree analysis is reified as a `FlangeWebSite.Manifest` record — a single tree walk producing both the redirect map and the artifact index. This eliminates the duplication of separate `collectRedirects()` and `buildArtifactIndex()` methods, provides a named concept for the aggregate ("what does the deployer need from this artifact tree?"), and shifts the test surface to the manifest composition rather than individual extractions. Parallels `PlanDescriber.describeTo()`, which also performs one walk with multiple accumulators.
- `ArtifactMetadataStrategy` receives the pre-built `Map<Path, Artifact>` from the manifest rather than the root artifact — it no longer performs its own tree traversal.
- Content types are stored as `MediaType` objects in the resource description (not strings). MetadataStrategy is a proper class, not a factory-returning-lambda.
- Synchronization monitor uses SLF4J logging (equivalent to existing `S3` deploy target reporting). `CliStatus`-based progress is deferred to a future user-feedback overhaul.
- Deploy URL is derived from the `WebSiteUrl` environment output via `FlangePlatformAws.Templates.Exports.WEB_SITE_URL` (transitively available from `flange-env-aws` → `flange-platform-aws`).
- The artifact tree walk uses the "comprised − subsumed" pattern per the refactored `S3.plan()`, not the old full `comprisedArtifacts()` walk. This was informed by the double-redirect bug fix (`plans/2026-03-10-s3-collection-artifact-semantics.md`) and the architecture refinements around `DirectoryArtifact.findContentArtifact()`. Content-finding uses `DirectoryArtifact.findContentArtifact()`, not `CollectionArtifact.getSubsumedArtifacts()` — subsumption does not imply content designation.
- `deploy()` is structured as two explicit subphases: **analyze** (walk the artifact tree, produce a `Manifest`) and **apply** (delegate to `AwsFlangeDeployer.deploySite()`). This aligns with a common deployment pattern observed across all deploy targets: `S3` has `plan()` (analyze) → `put()` + `prune()` (apply); `S3Website` extends both; `CloudFront` and `Route53` follow the same shape implicitly. TODO: converge existing deploy targets toward a common analyze/apply subphase vocabulary.
- See also `artifact-tree-walking.md` for an analysis of all current tree walks and potential future unification.

---

## Step 1: Add Flange Maven Dependencies

### BOM (`pom.xml`, root)

Add version-managed dependency for `flange-deploy-aws` in `<dependencyManagement>`:

```xml
<dependency>
    <groupId>dev.flange</groupId>
    <artifactId>flange-deploy-aws</artifactId>
    <version>0.2.0-SNAPSHOT</version>
</dependency>
```

Also add `flange-env-aws` (needed for `AwsFlangeEnvironmentManager`):

```xml
<dependency>
    <groupId>dev.flange</groupId>
    <artifactId>flange-env-aws</artifactId>
    <version>0.2.0-SNAPSHOT</version>
</dependency>
```

### `mummy/pom.xml`

Add dependencies (version managed by BOM):

```xml
<dependency>
    <groupId>dev.flange</groupId>
    <artifactId>flange-deploy-aws</artifactId>
</dependency>

<dependency>
    <groupId>dev.flange</groupId>
    <artifactId>flange-env-aws</artifactId>
</dependency>
```

`flange-deploy-aws` transitively brings `flange-aws-s3-support` (containing `S3Synchronizer`, `MetadataStrategy`, `Metadata`), `flange-aws-def` (containing `AwsProfile`), `flange-aws-cloudfront-support`, etc. `flange-env-aws` brings `flange-env` (containing `FlangeEnvironment.Name`), `AwsFlangeEnvironment`, `AwsFlangeEnvironmentManager`, and transitively `flange-platform-aws` (containing `FlangePlatformAws.Templates.Exports` with the `WEB_SITE_URL` constant).

### `cli/pom.xml`

_No changes needed._ The CLI already depends on `guise-mummy`, so the Flange classes are available transitively.

---

## Step 2: Site Manifest — `FlangeWebSite.Manifest`

### Location

Package-private static nested record inside `FlangeWebSite` (new class in `dev.guise.mummy.deploy.aws`).

### Design

The manifest reifies the artifact tree analysis as a single, immutable record — encapsulating everything the deployer needs from the artifact tree in one traversal:

```java
record Manifest(Map<URIPath, URI> redirects, Map<Path, Artifact> artifactsByContentPath) {
    static Manifest of(final Artifact rootArtifact, final URI rootTargetPathUri) { ... }
}
```

`Manifest.of()` performs **one** walk of the artifact tree using the "comprised − subsumed" pattern, populating both maps simultaneously. This replaces the previously separate `collectRedirects()` and `buildArtifactIndex()` methods.

### Walk algorithm

Walk the artifact tree recursively, visiting only non-subsumed comprised artifacts (same walk as the refactored `S3.plan()`; see architecture.md § "Choosing the Correct Walk"). For each artifact:

**Redirect extraction** — If the artifact has a `mummy/altLocation` property (accessed via the artifact's `UrfResourceDescription`):

1. Parse the `altLocation` value as `URIPath`.
2. Resolve against `artifact.getTargetPath().toUri()` to produce an absolute filesystem URI. No `toCollectionURI` forcing is needed here because `Manifest.of()` runs during DEPLOY, after MUMMIFY has created the target tree — `Path.toUri()` already produces trailing-slash URIs for directories. (See the architecture document's "Lifecycle Phase and `toCollectionURI`" section.)
3. Relativize against `rootTargetPathUri` to get the site-relative alt location `URIPath`.
4. Check `isSubPath()` — skip if outside the site boundary (log a warning, don't throw).
5. Compute the artifact's own site-relative resource reference via `Artifact.relativizeResourceReference(rootTargetPathUri, artifact)`. This method applies `toCollectionURI` defensively for `CollectionArtifact` instances — a no-op during DEPLOY since the trailing slash is already present, but it makes the API safe to call in any lifecycle phase.
6. Store as `altLocationReference → resourceReference.toURI()` in the redirects map.

**Artifact index** — For each non-collection artifact, map `artifact.getTargetPath()` → `artifact`. For a `DirectoryArtifact`, map the content artifact's target path (via `DirectoryArtifact.findContentArtifact()`) → the directory artifact itself — because the artifact provides the metadata (via description delegation) while the content artifact provides the storage path. For a non-directory `CollectionArtifact` (if one ever appears), no entry is created — same as the `S3.plan()` behavior. An empty `DirectoryArtifact` (no content artifact) produces no index entry.

If the artifact is a `CompositeArtifact`, recurse into its non-subsumed comprised artifacts.

### Double-redirect avoidance

Because the walk skips subsumed artifacts (e.g. a directory's `index.xhtml`), each `altLocation` is processed exactly once — at the collection artifact level, where `Artifact.relativizeResourceReference()` produces the canonical collection reference (e.g. `foo/`). This avoids the double-redirect bug that was fixed in `S3.plan()` (see `plans/2026-03-10-s3-collection-artifact-semantics.md` § Bug Description): if the walk visited subsumed content artifacts, their `altLocation` (inherited via `DirectoryArtifact` description delegation) would produce a redirect to the content path (e.g. `foo/index`) instead of the canonical collection path.

### Path matching

Guise Mummy guarantees that artifact target paths are real (canonical) paths, so no `toRealPath()` normalization is needed during index construction. `S3Synchronizer` canonicalizes the root directory via `toRealPath()` at entry, and `Files.list()` under that root produces canonical paths, so the paths will match.

### Redirect validation

The Flange deployer's `assembleSiteSettings()` validates that redirect sources are relative and within KVS quota. We don't need to duplicate that validation. We skip `altLocation` values that resolve outside the site boundary (not a sub-path of the root), logging a warning. The Flange deployer's `checkArgument(source.checkRelative())` would reject these anyway.

This parallels the logic in `S3Website.planResource()`, adapted to produce the `Map<URIPath, URI>` that `AwsFlangeDeployer.deploySite()` expects. Both run during DEPLOY, so the same lifecycle assumptions apply. The existing code in `S3Website` throws an `IOException` for out-of-boundary alt locations, but since the Flange deployer's `assembleSiteSettings()` performs its own validation, we log a warning and skip instead.

### Testability

Pure function — takes an artifact tree and a URI, returns a record with two maps. Testable with mock artifacts. The walk is structural, but the per-artifact logic (URI manipulation for redirects, path mapping for the index) is the interesting part. Test cases are consolidated in Step 8.

---

## Step 3: MetadataStrategy Adapter — `FlangeWebSite.ArtifactMetadataStrategy`

### Location

Static nested class within `FlangeWebSite`, implementing `S3Synchronizer.MetadataStrategy`.

### Design

A proper class rather than a factory-returning-lambda. This makes the strategy directly constructable and testable. The constructor receives the pre-built artifact index from the manifest — it does **not** perform its own tree traversal.

```java
static class ArtifactMetadataStrategy implements S3Synchronizer.MetadataStrategy {

    private final Map<Path, Artifact> artifactsByContentPath;

    ArtifactMetadataStrategy(final Map<Path, Artifact> artifactsByContentPath) {
        this.artifactsByContentPath = requireNonNull(artifactsByContentPath);
    }

    @Override
    public Optional<S3Synchronizer.Metadata> findMetadata(
            final Path file, final SequencedSet<Algorithm> preferredHashAlgorithms) {
        return Optional.ofNullable(artifactsByContentPath.get(file))
                .map(artifact -> toMetadata(artifact, preferredHashAlgorithms));
    }
}
```

### `toMetadata()`

Static method — the per-artifact conversion, separated as a distinct concern:

```java
static S3Synchronizer.Metadata toMetadata(
    final Artifact artifact, final SequencedSet<Algorithm> preferredHashAlgorithms)
```

1. Read `Content.TYPE_PROPERTY_TAG` from the artifact's resource description. The value is stored as a `MediaType` object (not a string); cast via `asInstance(MediaType.class)`.
2. Read `Content.FINGERPRINT_PROPERTY_TAG` → if present, wrap as `Hash.of(bytes)` keyed by `MessageDigests.SHA_256` (same algorithm as `Mummifier.FINGERPRINT_ALGORITHM`).
3. Return `new S3Synchronizer.Metadata(optionalContentType, checksumMap)`.

Pure function — no I/O, no filesystem access.

### Thread safety

The `Map` is built once (in `Manifest.of()`) and never modified. The artifact descriptions are read-only at this point in the lifecycle. `MetadataStrategy` implementations must be thread-safe per the interface contract; this implementation satisfies that.

---

## Step 4: Collection Content Resource Name Derivation

### Location

Static method in `FlangeWebSite`.

### Signature

```java
static Optional<String> deriveCollectionContentResourceName(final Configuration configuration)
```

### Logic

Replicates the logic from `S3Website.deploy()` [lines 394-400](mummy/src/main/java/dev/guise/mummy/deploy/aws/S3Website.java#L394):

1. Read `CONFIG_KEY_MUMMY_COLLECTION_CONTENT_BASE_NAMES` → take the first entry (e.g., `"index"`).
2. Read `CONFIG_KEY_MUMMY_PAGE_NAMES_BARE` → if `true`, the resource name is the base name alone (e.g., `"index"`); if `false`, append `.html` (e.g., `"index.html"`).
3. Return `Optional.of(resourceName)` if a base name exists, `Optional.empty()` otherwise.

### Testability

Pure function of configuration values. Testable with a mock `Configuration`.

---

## Step 5: Logging-Based Synchronization Monitor

### Location

Static nested class within `FlangeWebSite`: `FlangeWebSite.LoggingSynchronizationMonitor`.

### Approach

Implement `AwsFlangeDeployer.SiteSynchronizationMonitor` with SLF4J logging, providing equivalent reporting to the existing `S3` deploy target. No `CliStatus` dependency — `globalmentor-application` is not in the `mummy` module's dependency graph.

### Implementation

```java
static class LoggingSynchronizationMonitor implements AwsFlangeDeployer.SiteSynchronizationMonitor, Clogged {

    @Override public void onFileDiscovered(final Path file) { }
    @Override public void onEnterDirectory(final Path directory) { }
    @Override public void onDirectoryCompleted(final Path directory) { }

    @Override public void onSkipUnreadablePath(final Path path) {
        getLogger().atWarn().log("Skipping unreadable path `{}`.", path);
    }

    @Override public void onSkipExcludedPath(final Path path) { }
    @Override public void beforeGenerateFileContentHash(final Path file) { }
    @Override public void afterGenerateFileContentHash(final Path file) { }

    @Override public void beforeFileUpload(final Path file, final String s3Key) { }

    @Override public void afterFileUpload(final Path file, final String s3Key) {
        getLogger().atInfo().log("Deployed object to S3 key `{}`.", s3Key);
    }

    @Override public void beforeFilesDelete(final Collection<String> s3Keys) { }

    @Override public void afterFilesDelete(final Collection<String> s3Keys) {
        for(final String s3Key : s3Keys) {
            getLogger().atInfo().log("Pruned S3 object `{}`.", s3Key);
        }
    }

    @Override public void close() { }
}
```

This matches the reporting granularity of `S3.put()` (INFO per upload) and `S3.prune()` (INFO per deletion).

---

## Step 6: `FlangeWebSite` Deploy Target Class

### Location

New class: `mummy/src/main/java/dev/guise/mummy/deploy/aws/FlangeWebSite.java`

### Structure

```java
public class FlangeWebSite implements DeployTarget, Clogged {

    // configuration key for the Flange environment name (section-local)
    public static final String CONFIG_KEY_ENVIRONMENT = "environment";

    private final Optional<AwsProfile> optionalAwsProfile; // from AWS.CONFIG_KEY_DEPLOY_AWS_PROFILE (global)
    private final FlangeEnvironment.Name envName;           // from local config section

    // resolved during prepare(), following the CloudFront.acmCertificateArn precedent
    // (see DeployTarget state model in architecture.md)
    private AwsFlangeEnvironment flangeEnv;
}
```

### Constructor

```java
public FlangeWebSite(final MummyContext context, final Configuration localConfiguration) {
    this.optionalAwsProfile = context.getConfiguration()
            .findString(AWS.CONFIG_KEY_DEPLOY_AWS_PROFILE)
            .map(AwsProfile::new);
    this.envName = new FlangeEnvironment.Name(localConfiguration.getString(CONFIG_KEY_ENVIRONMENT));
}
```

Reads the `environment` property from the target section and the AWS profile from the global config, mapping the profile string to `AwsProfile` from `flange-aws-def`. Configuration values are not cached — they are read from `context.getConfiguration()` at each point of use (following the established `DeployTarget` state model; see `S3Website.deploy()` for the pattern). The existing legacy deploy targets (`S3`, `S3Website`, `CloudFront`) store the profile as a raw `String`; a TODO should be added to those classes to adopt `AwsProfile` in a future pass.

### `getSupportedProtocols()`

Returns `Set.of("https")` — Flange environments use CloudFront with HTTPS.

### `prepare(MummyContext context)`

1. Log the environment name and AWS profile.
2. Create `AwsFlangeEnvironmentManager.forProfile(optionalAwsProfile)` within a try-with-resources.
3. Resolve the environment: `envManager.resolve(envName)`. Throw `ConfiguredStateException` if not found — the Confound configuration is valid (the environment name string was read successfully), but the named environment doesn't exist in AWS infrastructure. This matches `AwsFlangeDeployer.deploySite()`, which throws `ConfiguredStateException` for missing environment outputs.
4. Validate the environment has site infrastructure: `findSiteBucketName()`, `findSiteDistributionId()`, `findSiteKeyValueStoreArn()` — throw `ConfiguredStateException` if missing (same rationale: infrastructure state mismatch, not a configuration read failure).
5. Emit warnings for incompatible features:
   - If `deploy.dns` is configured → warn that DNS management is not supported with Flange deployment.
   - If `site.altDomains` is configured → warn that alternative domain redirects are not supported with Flange deployment.

### `deploy(MummyContext context, Artifact rootArtifact)`

Structured as two subphases — **analyze** and **apply** — following the common deployment pattern (see Notable Decisions).

**Analyze:**

1. Derive `collectionContentResourceName` from `context.getConfiguration()` (Step 4).
2. Build the manifest: `analyzeArtifacts(rootArtifact, rootArtifact.getTargetPath().toUri())` (Step 2).
3. Create the metadata strategy: `new ArtifactMetadataStrategy(manifest.artifactsByContentPath())` (Step 3).

**Apply:**

4. Get `siteTargetDirectory` from `context.getSiteTargetDirectory()`.
5. Create `AwsFlangeDeployer.forProfile(optionalAwsProfile)` within a try-with-resources.
6. Call `deployer.deploySite(siteTargetDirectory, manifest.redirects(), collectionContentResourceName, flangeEnv, metadataStrategy, LoggingSynchronizationMonitor::new)`.

**Return:** the deploy URL — `flangeEnv.findOutput(FlangePlatformAws.Templates.Exports.WEB_SITE_URL).map(URI::create)`, or `Optional.empty()` if not available.

The analyze/apply split is currently just structural clarity within `deploy()` (comments and ordering), not separate methods. This positions the code for a future refactoring where `DeployTarget` might formalize these as subphase hooks, paralleling how `S3` already has `plan()` / `put()` / `prune()` as protected template methods.

### `AutoCloseable` consideration

`AwsFlangeDeployer` and `AwsFlangeEnvironmentManager` are `AutoCloseable`. The `FlangeWebSite` target is created in `PREPARE_DEPLOY` and used in `DEPLOY`, then the orchestrator moves on. Currently `DeployTarget` does not extend `AutoCloseable`.

**Chosen approach:** Create resources locally within try-with-resources in each method. The env manager is only needed during `prepare()` and can be closed there. The deployer is only needed during `deploy()` and can be created and closed there. This keeps resource management tight and doesn't require `DeployTarget` to extend `AutoCloseable`.

Structure:
- `prepare()`: create env manager in try-with-resources, resolve environment, store `flangeEnv`, close manager.
- `deploy()`: create deployer in try-with-resources, call `deploySite()`, close deployer.

### Testing

`prepare()` and `deploy()` make AWS API calls and aren't directly unit-testable. The unit-testable parts are extracted in Steps 2–4. An integration test could verify the full flow against a real Flange environment, but that's out of scope for this ticket.

---

## Step 7: Register `FlangeWebSite` in Deploy Target Factory

### Location

`GuiseMummy.mummify()`, in the deploy target type switch at [line ~341](mummy/src/main/java/dev/guise/mummy/GuiseMummy.java#L341).

### Change

Add an `else if` branch:

```java
} else if(targetType.equals("FlangeWebSite")) {
    target = new FlangeWebSite(context, targetSection);
}
```

This uses the string `"FlangeWebSite"` as the target type name, matching the configuration `* FlangeWebSite:` in TURF. This is the same pattern used for `CloudFront`, `S3`, and `S3Website`.

### Configuration example

```turf
deploy:
  aws:
    profile = "myprofile"
  ;
  targets = [
    * FlangeWebSite:
      environment = "prod"
  ]
;
```

### Coexistence with legacy targets

Users can configure both legacy and Flange targets simultaneously:

```turf
deploy:
  targets = [
    * S3Website:
      region = "us-east-1"
    * FlangeWebSite:
      environment = "prod"
  ]
;
```

Both will be instantiated, prepared, and deployed in sequence. This is the "natural" behavior of the existing plugin architecture—no special handling required. The user can comment out either section to test one path at a time.

### Warning about DNS/altDomains

The warning logic in `FlangeWebSite.prepare()` checks the global configuration for `deploy.dns` and `site.altDomains`. If `deploy.dns` is configured, it means DNS management is _also_ configured (possibly for the legacy target), but the Flange target doesn't handle DNS. The warning is advisory — it doesn't prevent deployment.

---

## Step 8: Unit Tests

### Test class

`mummy/src/test/java/dev/guise/mummy/deploy/aws/FlangeWebSiteTest.java`

### Tests

#### `Manifest.of()` (primary test surface)

The manifest is the single point of artifact tree analysis. Testing the manifest tests both redirect extraction and artifact indexing as a composition.

**Redirect behavior:**
- **No alt locations**: artifact tree with no `mummy/altLocation` properties → `manifest.redirects()` is empty.
- **Single file redirect**: artifact with `altLocation = "old-name.html"` → `manifest.redirects()` has correct site-relative URIPath key and URI value.
- **Collection redirect**: directory artifact with `altLocation = "old-dir/"` → redirect target has collection form (trailing slash), not the content artifact path (e.g. `foo/` not `foo/index`).
- **Alt location outside site boundary**: `altLocation` resolving outside root → skipped with no entry.
- **Multiple redirects**: multiple artifacts with `altLocation` → all collected.
- **Content artifact altLocation not duplicated**: directory artifact with content artifact sharing `altLocation` via description delegation → exactly one redirect entry at the collection level, not two.

**Index behavior:**
- **File artifact**: indexed by its target path in `manifest.artifactsByContentPath()`.
- **Directory artifact with content**: content artifact's target path maps to the directory artifact in the index. The directory path itself is not a key.
- **Directory artifact without content (empty directory)**: no index entry.
- **Multiple artifacts**: each path maps to the correct artifact.

**Composition behavior:**
- **Directory with both content and altLocation**: a `DirectoryArtifact` with a content artifact and an `altLocation` → the manifest contains *both* the correct redirect (keyed by the collection reference) *and* the correct index entry (keyed by the content artifact's target path).

#### `ArtifactMetadataStrategy.toMetadata()` (per-artifact conversion)
- **Content type and fingerprint present**: artifact with both `Content.TYPE_PROPERTY_TAG` (`MediaType`) and `Content.FINGERPRINT_PROPERTY_TAG` → `Metadata` with content type and SHA-256 checksum.
- **Content type only**: artifact with `Content.TYPE_PROPERTY_TAG` but no fingerprint → `Metadata` with content type and empty checksums.
- **No description properties**: artifact with empty description → `Metadata` with empty content type and empty checksums.

#### `ArtifactMetadataStrategy.findMetadata()` (lookup via pre-built index)
- **Known path**: construct `ArtifactMetadataStrategy` with a pre-built map, call `findMetadata()` with a matching path → returns `Metadata`.
- **Unknown path**: path not in the map → returns `Optional.empty()`.

#### `deriveCollectionContentResourceName()`
- **Default index with HTML extension**: default config → `Optional.of("index.html")`.
- **Bare names**: `mummy.page.namesBare = true` → `Optional.of("index")`.
- **Empty collection content base names**: → `Optional.empty()`.
- **Custom base name**: `collectionContentBaseNames = ["default"]`, not bare → `Optional.of("default.html")`.

### Not tested directly

- `prepare()` and `deploy()` — these make AWS API calls. The components they delegate to (`AwsFlangeDeployer`, `AwsFlangeEnvironmentManager`) are already tested in the Flange project.
- `LoggingSynchronizationMonitor` — trivial SLF4J delegation; equivalent to existing `S3` logging.

---

## Files Modified

| File | Change |
|---|---|
| `pom.xml` (root BOM) | Add `flange-deploy-aws` and `flange-env-aws` to `<dependencyManagement>` |
| `mummy/pom.xml` | Add `flange-deploy-aws` and `flange-env-aws` dependencies |
| `mummy/src/main/java/dev/guise/mummy/deploy/aws/FlangeWebSite.java` | **New** — deploy target implementation (includes nested `Manifest` record, `ArtifactMetadataStrategy`, `LoggingSynchronizationMonitor`) |
| `mummy/src/main/java/dev/guise/mummy/GuiseMummy.java` | Add `"FlangeWebSite"` branch in deploy target factory switch |
| `mummy/src/main/java/dev/guise/mummy/deploy/aws/S3.java` | Add TODO to adopt `AwsProfile` from `flange-aws-def` (constructor delegation and encoding fix already applied in prior commit) |
| `mummy/src/main/java/dev/guise/mummy/deploy/aws/S3Website.java` | Add TODO to adopt `AwsProfile` from `flange-aws-def` (constructor delegation and encoding fix already applied in prior commit) |
| `mummy/src/main/java/dev/guise/mummy/deploy/aws/CloudFront.java` | Add TODO to adopt `AwsProfile` from `flange-aws-def` |
| `mummy/src/test/java/dev/guise/mummy/deploy/aws/FlangeWebSiteTest.java` | **New** — unit tests |

---

## Alternatives Considered

### Top-level Flange orchestration vs. DeployTarget plugin

**Rejected:** Adding a top-level `if flangeEnvironment then ... else existing targets` in `GuiseMummy.mummify()`. This would bypass the plugin architecture, create a special-case fork in the orchestrator, and prevent coexistence of legacy and Flange targets. The `DeployTarget` approach is the natural fit — it's what the plugin system was designed for.

### Flange environment as a project-level config vs. deploy target section-local config

**Rejected:** Putting the Flange environment name at the project level (e.g., `flangeEnvironment = "prod"`). This would create a second, parallel mechanism for deployment configuration alongside `deploy.targets`. Keeping it section-local within `* FlangeWebSite:` is consistent with how `S3Website` stores `region`, `bucket`, etc.

### Sidecar I/O metadata strategy vs. in-memory plan-based strategy

**Rejected:** Reading `.-.tupr` sidecar files from disk for each metadata query. The in-memory plan already has the artifact descriptions loaded. Disk I/O is slower, has thread-safety concerns with file reading, and adds unnecessary complexity. The plan-based approach is simpler and faster.

### Factory-returning-lambda vs. proper `MetadataStrategy` class

**Rejected:** A static factory method `createMetadataStrategy()` that builds the path-to-artifact map and returns a lambda implementing `MetadataStrategy`. This obscures what is a straightforward adapter class with state (the lookup map). A proper class is more explicit, directly constructable in tests, and follows the natural factoring of index construction + per-artifact conversion as two distinct concerns.

### `CliStatus`-based progress monitor vs. logging monitor

**Deferred:** The Flange CLI's `SynchronizeStatus` extends `CliStatus<Path>` for a live progress bar with elapsed time, counters, and work-in-progress labels. However, `globalmentor-application` (which provides `CliStatus`) is not in the `mummy` module's dependency graph — it's only available in the `cli` module. Adding it as a dependency would couple the library to CLI infrastructure. A comprehensive overhaul of user feedback, logging, and ANSI styles is planned for the future; the `CliStatus` monitor can be revisited then. For now, the logging monitor provides equivalent reporting to the existing `S3` deploy target.

### Separate `collectRedirects()` and `buildArtifactIndex()` methods vs. unified `Manifest`

**Rejected:** The original plan factored redirect extraction and artifact indexing as separate static methods with their own tree walks, optimizing for single-responsibility testability. Both walk the same tree with the same "comprised − subsumed" pattern — the only difference is the per-artifact action. A single walk producing a reified `Manifest` record eliminates the duplication, provides a named concept for the aggregate ("what does the deployer need from this artifact tree?"), and shifts the test surface to the composition (the manifest) rather than the individual extractions. The `PlanDescriber.describeTo()` method already demonstrates this pattern — one walk, multiple accumulators.
