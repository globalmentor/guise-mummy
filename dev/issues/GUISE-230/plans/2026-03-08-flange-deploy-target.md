# Plan: Flange Deploy Target

Implement `Flange` as a `DeployTarget` within Guise Mummy's existing deployment plugin architecture, enabling site deployment to a Flange-managed AWS environment (S3 + CloudFront OAC + CloudFront KVS).

## Overview

**Chunk 1: Foundation** (independent — compiles and tests in isolation)

- Step 1: Add Flange Maven dependencies to BOM and `mummy` module
- Step 2: Redirect extraction utility — `FlangeDeployment.collectRedirects()`
- Step 3: MetadataStrategy adapter — `FlangeDeployment.ArtifactMetadataStrategy`
- Step 4: Collection content resource name derivation — `FlangeDeployment.deriveCollectionContentResourceName()`
- Step 5: Logging-based synchronization monitor

**Chunk 2: Integration** (requires Chunk 1)

- Step 6: `FlangeDeployment` deploy target class — constructor, `prepare()`, `deploy()`
- Step 7: Register `Flange` in the deploy target factory switch in `GuiseMummy.mummify()`
- Step 8: Unit tests for `FlangeDeployment`

**Notable decisions:**

- `Flange` is a standard `DeployTarget`, configured as `* Flange:` in `deploy.targets`. Coexists naturally with legacy targets — no special-case orchestration.
- MetadataStrategy is a proper class (`ArtifactMetadataStrategy`) wrapping a `Map<Path, Artifact>` lookup from the in-memory plan, not sidecar I/O. Content types are stored as `MediaType` objects in the resource description (not strings).
- Redirect extraction is factored as a static pure method for direct testing, parallel to `PlanDescriber.collectRedirect()`.
- Synchronization monitor uses SLF4J logging (equivalent to existing `S3` deploy target reporting). `CliStatus`-based progress is deferred to a future user-feedback overhaul.
- Deploy URL is derived from `WebSiteUrl` environment output if available.

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

`flange-deploy-aws` transitively brings `flange-aws-s3-support` (containing `S3Synchronizer`, `MetadataStrategy`, `Metadata`), `flange-aws-def` (containing `AwsProfile`), `flange-aws-cloudfront-support`, etc. `flange-env-aws` brings `flange-env` (containing `FlangeEnvironment.Name`), `AwsFlangeEnvironment`, and `AwsFlangeEnvironmentManager`.

### `cli/pom.xml`

_No changes needed._ The CLI already depends on `guise-mummy`, so the Flange classes are available transitively.

---

## Step 2: Redirect Extraction — `collectRedirects()`

### Location

Static method in `FlangeDeployment` (new class in `dev.guise.mummy.deploy.aws`).

### Signature

```java
static Map<URIPath, URI> collectRedirects(final Artifact rootArtifact, final URI rootTargetPathUri)
```

### Logic

Walk the artifact tree recursively (same pattern as `S3.plan()`). For each artifact with a `mummy/altLocation` property:

1. Parse the value as `URIPath`.
2. Resolve against the artifact's target path URI (applying `toCollectionURI` for `CollectionArtifact`).
3. Relativize against the site root URI to get the site-relative alt location `URIPath`.
4. Compute the artifact's own site-relative resource reference via `Artifact.relativizeResourceReference()`.
5. Store as `altLocationReference → resourceReference.toURI()` in the result map.

This parallels the logic in `PlanDescriber.collectRedirect()` and `S3Website.planResource()`, adapted to produce the `Map<URIPath, URI>` that `AwsFlangeDeployer.deploySite()` expects.

### Testability

Pure function — takes an artifact tree and a URI, returns a map. Testable with mock artifacts. The recursive tree walk is structural, but the per-artifact URI manipulation is the interesting part. Test cases:

- Artifact with no `altLocation` → empty map.
- File artifact with `altLocation` → correct site-relative key and value.
- Collection artifact with `altLocation` → collection URI forcing applied correctly.
- `altLocation` outside site boundary → include but log warning (Flange deployer will handle validation).

### Design note

The Flange deployer's `assembleSiteSettings()` validates that redirect sources are relative and within KVS quota. We don't need to duplicate that validation. However, we should skip `altLocation` values that resolve outside the site boundary (not a sub-path of the root), logging a warning, consistent with what `PlanDescriber` does. The Flange deployer's `checkArgument(source.checkRelative())` would reject these anyway.

---

## Step 3: MetadataStrategy Adapter — `ArtifactMetadataStrategy`

### Location

Static nested class within `FlangeDeployment`, implementing `S3Synchronizer.MetadataStrategy`.

### Design

A proper class rather than a factory-returning-lambda. This makes the strategy directly constructable and testable.

```java
static class ArtifactMetadataStrategy implements S3Synchronizer.MetadataStrategy {

    private final Map<Path, Artifact> artifactsByTargetPath;

    ArtifactMetadataStrategy(final MummyPlan plan) {
        this.artifactsByTargetPath = buildArtifactIndex(plan.getRootArtifact());
    }

    @Override
    public Optional<S3Synchronizer.Metadata> findMetadata(
            final Path file, final SequencedSet<Algorithm> preferredHashAlgorithms) {
        return Optional.ofNullable(artifactsByTargetPath.get(file))
                .map(artifact -> toMetadata(artifact, preferredHashAlgorithms));
    }
}
```

### `buildArtifactIndex()`

Static method that walks the artifact tree recursively. For each non-collection artifact, maps `artifact.getTargetPath()` → `artifact`. Returns an unmodifiable `Map<Path, Artifact>`.

Guise Mummy guarantees that artifact target paths are real (canonical) paths, so no `toRealPath()` normalization is needed during index construction. `S3Synchronizer` canonicalizes the root directory via `toRealPath()` at entry, and `Files.list()` under that root produces canonical paths, so the paths will match.

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

The `Map` is built once and never modified. The artifact descriptions are read-only at this point in the lifecycle. `MetadataStrategy` implementations must be thread-safe per the interface contract; this implementation satisfies that.

### Testability

Both levels are directly unit-testable without a filesystem:

- **`ArtifactMetadataStrategy`**: Construct with a `MummyPlan` containing mock artifacts with known target paths. Call `findMetadata()` with matching and non-matching paths. Verify lookup behavior.
- **`toMetadata()`**: Construct a mock artifact with known resource description properties. Verify the `MediaType` cast, fingerprint extraction, and `Metadata` record construction.

---

## Step 4: Collection Content Resource Name Derivation

### Location

Static method in `FlangeDeployment`.

### Signature

```java
static Optional<String> deriveCollectionContentResourceName(final Configuration configuration)
```

### Logic

Replicates the logic from `S3Website.deploy()` [lines 378-385](mummy/src/main/java/dev/guise/mummy/deploy/aws/S3Website.java#L378):

1. Read `CONFIG_KEY_MUMMY_COLLECTION_CONTENT_BASE_NAMES` → take the first entry (e.g., `"index"`).
2. Read `CONFIG_KEY_MUMMY_PAGE_NAMES_BARE` → if `true`, the resource name is the base name alone (e.g., `"index"`); if `false`, append `.html` (e.g., `"index.html"`).
3. Return `Optional.of(resourceName)` if a base name exists, `Optional.empty()` otherwise.

### Testability

Pure function of configuration values. Testable with a mock `Configuration`.

---

## Step 5: Logging-Based Synchronization Monitor

### Location

Static nested class within `FlangeDeployment`: `FlangeDeployment.LoggingSynchronizationMonitor`.

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

## Step 6: `FlangeDeployment` Deploy Target Class

### Location

New class: `mummy/src/main/java/dev/guise/mummy/deploy/aws/FlangeDeployment.java`

### Structure

```java
public class FlangeDeployment implements DeployTarget, Clogged {

    // configuration key for the Flange environment name (section-local)
    public static final String CONFIG_KEY_ENVIRONMENT = "environment";

    private final String profile;           // from AWS.CONFIG_KEY_DEPLOY_AWS_PROFILE (global)
    private final FlangeEnvironment.Name envName;  // from local config section
    private final Configuration globalConfiguration;

    // resolved during prepare()
    private AwsFlangeEnvironment flangeEnv;
    private AwsFlangeEnvironmentManager envManager;
    private AwsFlangeDeployer deployer;
}
```

### Constructor

```java
public FlangeDeployment(final MummyContext context, final Configuration localConfiguration) {
    this.profile = context.getConfiguration().findString(AWS.CONFIG_KEY_DEPLOY_AWS_PROFILE).orElse(null);
    this.envName = new FlangeEnvironment.Name(localConfiguration.getString(CONFIG_KEY_ENVIRONMENT));
    this.globalConfiguration = context.getConfiguration();
}
```

Reads the `environment` property from the target section and the AWS profile from the global config (same pattern as `S3`, `S3Website`, `CloudFront`).

### `getSupportedProtocols()`

Returns `Set.of("https")` — Flange environments use CloudFront with HTTPS.

### `prepare(MummyContext context)`

1. Log the environment name and AWS profile.
2. Create `AwsFlangeEnvironmentManager.forProfile(optionalAwsProfile)`.
3. Resolve the environment: `envManager.resolve(envName)`. Throw `ConfigurationException` if not found.
4. Validate the environment has site infrastructure: `findSiteBucketName()`, `findSiteDistributionId()`, `findSiteKeyValueStoreArn()` — throw if missing.
5. Create `AwsFlangeDeployer.forProfile(optionalAwsProfile)`.
6. Emit warnings for incompatible features:
   - If `deploy.dns` is configured → warn that DNS management is not supported with Flange deployment.
   - If `site.altDomains` is configured → warn that alternative domain redirects are not supported with Flange deployment.

### `deploy(MummyContext context, Artifact rootArtifact)`

1. Derive `collectionContentResourceName` from configuration (Step 4).
2. Extract redirects from the artifact plan (Step 2).
3. Create the metadata strategy: `new ArtifactMetadataStrategy(context.getPlan())`.
4. Call `deployer.deploySite(siteTargetDirectory, redirects, collectionContentResourceName, flangeEnv, metadataStrategy, LoggingSynchronizationMonitor::new)`.
5. Return the deploy URL: `flangeEnv.findOutput("WebSiteUrl").map(URI::create)`, or `Optional.empty()` if not available.

### `AutoCloseable` consideration

`AwsFlangeDeployer` and `AwsFlangeEnvironmentManager` are `AutoCloseable`. The `FlangeDeployment` target is created in `PREPARE_DEPLOY` and used in `DEPLOY`, then the orchestrator moves on. Currently `DeployTarget` does not extend `AutoCloseable`.

**Approach:** Hold the deployer and env manager as fields. Close them at the end of `deploy()` (the last method called on the target). This isn't perfect (prepare/deploy could be called in different phases with the expectation the object stays alive), but looking at the existing lifecycle in `GuiseMummy.mummify()`, `prepare()` and `deploy()` are called in immediate succession with no intervening operations.

Alternatively, use try-with-resources within `deploy()` around the `deploySite()` call. Since we already have the resolved environment from `prepare()`, the only resources needing lifecycle management during `deploy()` are the deployer's AWS clients.

**Chosen approach:** Create the deployer in `deploy()` within a try-with-resources, not as a field. The env manager is only needed during `prepare()` and can be closed there. This keeps resource management tight and doesn't require `DeployTarget` to extend `AutoCloseable`.

Revised structure:
- `prepare()`: create env manager in try-with-resources, resolve environment, close manager.
- `deploy()`: create deployer in try-with-resources, call `deploySite()`, close deployer.

### Testing

`prepare()` and `deploy()` make AWS API calls and aren't directly unit-testable. The unit-testable parts are extracted in Steps 2–4. An integration test could verify the full flow against a real Flange environment, but that's out of scope for this ticket.

---

## Step 7: Register `Flange` in Deploy Target Factory

### Location

`GuiseMummy.mummify()`, in the deploy target type switch at [line ~335](mummy/src/main/java/dev/guise/mummy/GuiseMummy.java#L335).

### Change

Add an `else if` branch:

```java
} else if(targetType.equals("Flange")) {
    target = new FlangeDeployment(context, targetSection);
}
```

This uses the string `"Flange"` as the target type name, matching the configuration `* Flange:` in TURF. This is the same pattern used for `CloudFront`, `S3`, and `S3Website`.

### Configuration example

```turf
deploy:
  aws:
    profile = "myprofile"
  ;
  targets = [
    * Flange:
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
    * Flange:
      environment = "prod"
  ]
;
```

Both will be instantiated, prepared, and deployed in sequence. This is the "natural" behavior of the existing plugin architecture—no special handling required. The user can comment out either section to test one path at a time.

### Warning about DNS/altDomains

The warning logic in `FlangeDeployment.prepare()` checks the global configuration for `deploy.dns` and `site.altDomains`. If `deploy.dns` is configured, it means DNS management is _also_ configured (possibly for the legacy target), but the Flange target doesn't handle DNS. The warning is advisory — it doesn't prevent deployment.

---

## Step 8: Unit Tests

### Test class

`mummy/src/test/java/dev/guise/mummy/deploy/aws/FlangeDeploymentTest.java`

### Tests

#### `collectRedirects()`
- **No alt locations**: artifact tree with no `mummy/altLocation` properties → empty map.
- **Single file redirect**: artifact with `altLocation = "old-name.html"` → map with correct site-relative URIPath key and URI value.
- **Collection redirect**: directory artifact with `altLocation = "old-dir/"` → collection URI forcing applied.
- **Alt location outside site boundary**: `altLocation` resolving outside root → skipped with no entry.
- **Multiple redirects**: multiple artifacts with `altLocation` → all collected.

#### `ArtifactMetadataStrategy` (integration of index + lookup)
- **Known path**: path matching an artifact in the plan → returns `Metadata` with correct content type and checksum.
- **Unknown path**: path not in the plan → returns `Optional.empty()`.
- **Collection artifact**: collection artifact path → not indexed (collections represent directories, not uploadable files).
- **Multiple artifacts**: plan with several artifacts → each path maps to the correct artifact's metadata.

#### `ArtifactMetadataStrategy.toMetadata()` (per-artifact conversion)
- **Content type and fingerprint present**: artifact with both `Content.TYPE_PROPERTY_TAG` (`MediaType`) and `Content.FINGERPRINT_PROPERTY_TAG` → `Metadata` with content type and SHA-256 checksum.
- **Content type only**: artifact with `Content.TYPE_PROPERTY_TAG` but no fingerprint → `Metadata` with content type and empty checksums.
- **No description properties**: artifact with empty description → `Metadata` with empty content type and empty checksums.

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
| `mummy/src/main/java/dev/guise/mummy/deploy/aws/FlangeDeployment.java` | **New** — deploy target implementation |
| `mummy/src/main/java/dev/guise/mummy/GuiseMummy.java` | Add `"Flange"` branch in deploy target factory switch |
| `mummy/src/test/java/dev/guise/mummy/deploy/aws/FlangeDeploymentTest.java` | **New** — unit tests |

---

## Alternatives Considered

### Top-level Flange orchestration vs. DeployTarget plugin

**Rejected:** Adding a top-level `if flangeEnvironment then ... else existing targets` in `GuiseMummy.mummify()`. This would bypass the plugin architecture, create a special-case fork in the orchestrator, and prevent coexistence of legacy and Flange targets. The `DeployTarget` approach is the natural fit — it's what the plugin system was designed for.

### Flange environment as a project-level config vs. deploy target section-local config

**Rejected:** Putting the Flange environment name at the project level (e.g., `flangeEnvironment = "prod"`). This would create a second, parallel mechanism for deployment configuration alongside `deploy.targets`. Keeping it section-local within `* Flange:` is consistent with how `S3Website` stores `region`, `bucket`, etc.

### Sidecar I/O metadata strategy vs. in-memory plan-based strategy

**Rejected:** Reading `.-.tupr` sidecar files from disk for each metadata query. The in-memory plan already has the artifact descriptions loaded. Disk I/O is slower, has thread-safety concerns with file reading, and adds unnecessary complexity. The plan-based approach is simpler and faster.

### Factory-returning-lambda vs. proper `MetadataStrategy` class

**Rejected:** A static factory method `createMetadataStrategy()` that builds the path-to-artifact map and returns a lambda implementing `MetadataStrategy`. This obscures what is a straightforward adapter class with state (the lookup map). A proper class is more explicit, directly constructable in tests, and follows the natural factoring of index construction + per-artifact conversion as two distinct concerns.

### `CliStatus`-based progress monitor vs. logging monitor

**Deferred:** The Flange CLI's `SynchronizeStatus` extends `CliStatus<Path>` for a live progress bar with elapsed time, counters, and work-in-progress labels. However, `globalmentor-application` (which provides `CliStatus`) is not in the `mummy` module's dependency graph — it's only available in the `cli` module. Adding it as a dependency would couple the library to CLI infrastructure. A comprehensive overhaul of user feedback, logging, and ANSI styles is planned for the future; the `CliStatus` monitor can be revisited then. For now, the logging monitor provides equivalent reporting to the existing `S3` deploy target.
