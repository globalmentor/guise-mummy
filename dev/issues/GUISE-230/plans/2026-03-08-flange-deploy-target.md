# Plan: Flange Deploy Target

Implement `FlangeWebSite` as a `DeployTarget` within Guise Mummy's existing deployment plugin architecture, enabling site deployment to a Flange-managed AWS environment (S3 + CloudFront OAC + CloudFront KVS).

## Overview

**Chunk 0: Prerequisite** (prepares existing code for reuse by the deploy target)

- Step 0a: Add `Visitor.andThen()` to `ArtifactTreeWalker.Visitor`
- Step 0b: Refactor `PlanSummary.RedirectEntry` — rename fields to `sourcePath`/`targetUri`, change target type to `URI`, drop `collection` flag, add factory method
- Step 0c: Update `PlanSummary` — drop `redirectCollectionCount()`/`redirectPageCount()`
- Step 0d: Update `PlanDescriber` — add `summarize(Visitor)` overload, update `findRedirect()` to produce `URI` target, update `writeTo()` display

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
- **Redirect reuse via `PlanDescriber`.** Instead of `Manifest` performing its own tree walk for redirect extraction, the deploy target reuses `PlanDescriber.summarize(Visitor)`, piggybacking on the same walk that produces `PlanSummary`. The `Visitor.andThen()` composition (added to `ArtifactTreeWalker.Visitor`) lets the deploy target's visitor run alongside the summarizer without modifying `PlanDescriber`'s internal logic. The `Manifest` receives the `PlanSummary` at build time and extracts the `Map<URIPath, URI>` redirect map from it (filtering out warning entries), alongside the artifact content path index collected by the piggybacked visitor. This eliminates redundant tree walks and keeps redirect extraction logic in one place (`PlanDescriber.findRedirect()`).
- **`RedirectEntry` aligned with HTTP redirect semantics.** Renamed from `altLocationReference`/`resourceReference` (Guise Mummy implementation terms) to `sourcePath`/`targetUri` (matching Flange's "source → target" vocabulary). The target type is `URI` rather than `URIPath`, supporting future redirect targets outside the site (e.g. `https://example.com/new-page`). The `collection` flag is dropped — collection-ness is inherent in the target URI path's trailing slash when relevant, and the redirect sub-categorization ("Collection Targets" / "Page Targets") is removed from the plan display as it was misleading and not actionable.
- `ArtifactMetadataStrategy` receives the pre-built `Map<Path, Artifact>` from the manifest rather than the root artifact — it no longer performs its own tree traversal.
- Content types are stored as `MediaType` objects in the resource description (not strings). MetadataStrategy is a proper class, not a factory-returning-lambda.
- Synchronization monitor uses SLF4J logging (equivalent to existing `S3` deploy target reporting). `CliStatus`-based progress is deferred to a future user-feedback overhaul.
- Deploy URL is derived from the `WebSiteUrl` environment output via `FlangePlatformAws.Templates.Exports.WEB_SITE_URL` (transitively available from `flange-env-aws` → `flange-platform-aws`).
- The artifact tree walk uses `ArtifactTreeWalker` (via `MummyPlan.walk()`), which visits all comprised artifacts including subsumed ones, passing the subsumption status to the visitor. This was informed by the double-redirect bug fix (`plans/2026-03-10-s3-collection-artifact-semantics.md`) and the architecture refinements around `DirectoryArtifact.findContentArtifact()`. Content-finding uses `DirectoryArtifact.findContentArtifact()`, not `CollectionArtifact.getSubsumedArtifacts()` — subsumption does not imply content designation.
- `deploy()` is structured as two explicit subphases: **analyze** (walk the artifact tree via `PlanDescriber`, produce a `Manifest`) and **apply** (delegate to `AwsFlangeDeployer.deploySite()`). This aligns with a common deployment pattern observed across all deploy targets: `S3` has `plan()` (analyze) → `put()` + `prune()` (apply); `S3Website` extends both; `CloudFront` and `Route53` follow the same shape implicitly. TODO: converge existing deploy targets toward a common analyze/apply subphase vocabulary.
- **Redirect display.** In `PlanDescriber.writeTo()`, redirect source paths are shown via `toString()` (the raw/encoded form) and redirect targets via `URI.toASCIIString()` (ensuring fully encoded form). This is consistent regardless of whether the target is a site-relative path or a future full URL.

---

## Step 0a: Add `Visitor.andThen()` to `ArtifactTreeWalker.Visitor`

### Location

`mummy/src/main/java/dev/guise/mummy/ArtifactTreeWalker.java`, inside the `Visitor` interface.

### Change

Add a default method for functional composition, following `Consumer.andThen()`:

```java
default Visitor andThen(final Visitor after) {
    return (artifact, subsumed) -> {
        this.visit(artifact, subsumed);
        after.visit(artifact, subsumed);
    };
}
```

Each visitor receives the raw `(artifact, subsumed)` event independently. If the primary visitor short-circuits (e.g. `if(subsumed) { return; }`), that only exits its own lambda — the `after` visitor still receives the event.

---

## Step 0b: Refactor `PlanSummary.RedirectEntry`

### Location

`mummy/src/main/java/dev/guise/mummy/plan/PlanSummary.java`, the `RedirectEntry` nested record.

### Changes

1. **Rename fields** to align with HTTP redirect semantics and Flange's vocabulary:
   - `altLocationReference` → `sourcePath` (the site-relative path that triggers the redirect)
   - `resourceReference` → `targetUri` (the redirect destination)

2. **Change target type** from `URIPath` to `URI`, supporting future redirect targets that may be full URLs.

3. **Drop `collection` flag.** Collection-ness is inherent in the target URI path when relevant. The redirect sub-categorization in plan display is removed.

4. **Add factory method** for convenient construction from a `URIPath` destination:

   ```java
   public static RedirectEntry of(final URIPath sourcePath, final URIPath targetReference,
           final Optional<PlanWarning> optionalWarning) {
       return new RedirectEntry(sourcePath, targetReference.toURI(), optionalWarning);
   }
   ```

5. **Update `compareTo()`** — same logic (case-insensitive on decoded source path), but references `sourcePath` instead of `altLocationReference`.

### Resulting record

```java
public record RedirectEntry(URIPath sourcePath, URI targetUri,
        Optional<PlanWarning> optionalWarning) implements Comparable<RedirectEntry> {
    public RedirectEntry {
        requireNonNull(sourcePath);
        requireNonNull(targetUri);
        requireNonNull(optionalWarning);
    }
    public static RedirectEntry of(final URIPath sourcePath, final URIPath targetReference,
            final Optional<PlanWarning> optionalWarning) {
        return new RedirectEntry(sourcePath, targetReference.toURI(), optionalWarning);
    }
    @Override
    public int compareTo(@NonNull final RedirectEntry other) {
        return String.CASE_INSENSITIVE_ORDER.compare(
                this.sourcePath.toDecodedString(),
                other.sourcePath.toDecodedString());
    }
}
```

---

## Step 0c: Update `PlanSummary`

### Location

`mummy/src/main/java/dev/guise/mummy/plan/PlanSummary.java`.

### Changes

1. **Remove `redirectCollectionCount()` and `redirectPageCount()`.** The redirect sub-categorization is dropped.
2. The `sortedRedirects` field, `warningCount()`, `Builder`, and `PlanWarning` remain unchanged.

---

## Step 0d: Update `PlanDescriber`

### Location

`mummy/src/main/java/dev/guise/mummy/plan/PlanDescriber.java`.

### Changes

1. **Add `summarize(Visitor)` overload:**

   ```java
   public PlanSummary summarize(final ArtifactTreeWalker.Visitor additionalVisitor) {
       final var builder = PlanSummary.builder();
       plan.walk(((ArtifactTreeWalker.Visitor) (artifact, subsumed) -> {
           // ... existing summarization logic, unchanged
       }).andThen(additionalVisitor));
       return builder.build();
   }
   ```

   The existing no-arg `summarize()` delegates: `return summarize((artifact, subsumed) -> { });`.

2. **Update `findRedirect()`** to produce a `URI` target:

   ```java
   return RedirectEntry.of(altLocationReference, targetReference, optionalWarning);
   ```

   The `artifact instanceof CollectionArtifact` argument is removed (no more `collection` flag).

3. **Update `writeTo()`:**
   - Remove the "Collection Targets:" and "Page Targets:" lines.
   - Update the verbose redirect detail line to use `redirect.sourcePath().toString()` and `redirect.targetUri().toASCIIString()`.

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

The manifest reifies the deploy target's artifact tree analysis — encapsulating everything the deployer needs from the artifact tree:

```java
record Manifest(Map<URIPath, URI> redirects, Map<Path, Artifact> artifactsByContentPath) {
}
```

The manifest does **not** perform its own tree walk. Instead, it is assembled by the deploy target's analyze phase (Step 6), which reuses `PlanDescriber.summarize(Visitor)` to piggyback a manifest-building visitor alongside the `PlanSummary` accumulation. After the walk completes:

1. The `PlanSummary` is received from `PlanDescriber.summarize()`.
2. The redirect map (`Map<URIPath, URI>`) is extracted from the `PlanSummary`'s `sortedRedirects`, filtering out entries with warnings (out-of-site-boundary redirects) and mapping `RedirectEntry.sourcePath()` → `RedirectEntry.targetUri()`.
3. The artifact content path index (`Map<Path, Artifact>`) is built by the piggybacked visitor during the walk.
4. Both maps are passed to the `Manifest` constructor.

### Builder

A `Manifest.Builder` accumulates the artifact index entries during the walk:

```java
static final class Builder {
    private final Map<Path, Artifact> artifactsByContentPath = new HashMap<>();

    void addArtifact(final Path contentPath, final Artifact artifact) {
        artifactsByContentPath.put(requireNonNull(contentPath), requireNonNull(artifact));
    }

    Manifest build(final PlanSummary summary) {
        final Map<URIPath, URI> redirects = summary.sortedRedirects().stream()
                .filter(entry -> entry.optionalWarning().isEmpty())
                .collect(toUnmodifiableMap(RedirectEntry::sourcePath, RedirectEntry::targetUri));
        return new Manifest(redirects, Map.copyOf(artifactsByContentPath));
    }
}
```

### Artifact index construction (piggybacked visitor logic)

The visitor passed to `PlanDescriber.summarize(Visitor)` collects the artifact content path index. For each artifact visited:

- **Subsumed artifacts** are skipped (the visitor independently checks the `subsumed` flag).
- The type dispatch follows `S3.plan()`'s collection-first pattern — check `CollectionArtifact` first, then narrow to `DirectoryArtifact` inside that branch:
  - **`CollectionArtifact`** → **`DirectoryArtifact`**: map the content artifact's target path (via `DirectoryArtifact.findContentArtifact()`) → the directory artifact itself — because the artifact provides the metadata (via description delegation) while the content artifact provides the storage path. An empty `DirectoryArtifact` (no content artifact) produces no index entry.
  - **`CollectionArtifact`** → other: log a warning and produce no index entry (same as `S3.plan()`).
  - **else** (non-collection artifact): map `artifact.getTargetPath()` → `artifact`.

### Double-redirect avoidance

Because the walker visits subsumed artifacts with `subsumed=true` and both the `PlanDescriber` visitor and the manifest visitor skip them, each `altLocation` is processed exactly once — at the collection artifact level, where `Artifact.relativizeResourceReference()` produces the canonical collection reference (e.g. `foo/`). This avoids the double-redirect bug fixed in `S3.plan()` (see `plans/2026-03-10-s3-collection-artifact-semantics.md`).

### Path matching

Guise Mummy guarantees that artifact target paths are real (canonical) paths, so no `toRealPath()` normalization is needed during index construction. `S3Synchronizer` canonicalizes the root directory via `toRealPath()` at entry, and `Files.list()` under that root produces canonical paths, so the paths will match.

### Testability

The manifest construction is testable through two surfaces:
1. The piggybacked visitor logic — directly testable by calling `PlanDescriber.summarize(visitor)` with mock artifacts and inspecting the builder's accumulated state.
2. `Builder.build(PlanSummary)` — testable with a pre-built `PlanSummary` to verify redirect extraction and filtering.

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

1. Read `Content.TYPE_PROPERTY_TAG` from the artifact's resource description via `asInstance(MediaType.class)`. (Verified: all mummifiers store the value as a `MediaType` object — `AbstractFileMummifier`, `DirectoryMummifier`, `DefaultImageMummifier` all confirm this.)
2. Read `Content.FINGERPRINT_PROPERTY_TAG` → if present, wrap as `Hash.of(bytes)` keyed by `MessageDigests.SHA_256` (same algorithm as `Mummifier.FINGERPRINT_ALGORITHM`).
3. Return `new S3Synchronizer.Metadata(optionalContentType, checksumMap)`.

Pure function — no I/O, no filesystem access.

### Thread safety

The `Map` is built once (in `Manifest.Builder.build()`) and never modified. The artifact descriptions are read-only at this point in the lifecycle. `MetadataStrategy` implementations must be thread-safe per the interface contract; this implementation satisfies that.

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
2. Create a `PlanDescriber` with the plan from `context.getPlan()` and `toCollectionURI(rootArtifact.getTargetPath().toUri())`.
3. Create a `Manifest.Builder`.
4. Call `planDescriber.summarize(manifestVisitor)` — the manifest visitor (a lambda that populates the `Manifest.Builder` with artifact content path entries per Step 2) runs alongside `PlanDescriber`'s summarization logic via `Visitor.andThen()`.
5. Build the manifest: `manifestBuilder.build(planSummary)` — this extracts `Map<URIPath, URI>` redirects from the `PlanSummary` (filtering out warning entries) and combines them with the accumulated artifact index.
6. Create the metadata strategy: `new ArtifactMetadataStrategy(manifest.artifactsByContentPath())` (Step 3).

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

#### Manifest construction (via `PlanDescriber.summarize(Visitor)` + `Manifest.Builder`)

The manifest is assembled by piggybacking on `PlanDescriber.summarize()`. Testing verifies the visitor accumulation and the builder's extraction from `PlanSummary`.

**Redirect extraction (via `PlanSummary`):**
- **No alt locations**: artifact tree with no `mummy/altLocation` properties → `manifest.redirects()` is empty.
- **Single file redirect**: artifact with `altLocation = "old-name.html"` → `manifest.redirects()` has correct site-relative URIPath key and URI value.
- **Collection redirect**: directory artifact with `altLocation = "old-dir/"` → redirect target has collection form (trailing slash), not the content artifact path (e.g. `foo/` not `foo/index`).
- **Alt location outside site boundary**: `altLocation` resolving outside root → entry has warning in `PlanSummary`, filtered out of `manifest.redirects()`.
- **Multiple redirects**: multiple artifacts with `altLocation` → all non-warned entries collected.
- **Content artifact altLocation not duplicated**: directory artifact with content artifact sharing `altLocation` via description delegation → exactly one redirect entry at the collection level, not two.

**Index behavior (via piggybacked visitor):**
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
| `mummy/src/main/java/dev/guise/mummy/ArtifactTreeWalker.java` | Add `Visitor.andThen()` default method |
| `mummy/src/main/java/dev/guise/mummy/plan/PlanSummary.java` | Refactor `RedirectEntry` (rename fields, `URI` target, drop `collection`), drop redirect sub-categorization methods |
| `mummy/src/main/java/dev/guise/mummy/plan/PlanDescriber.java` | Add `summarize(Visitor)` overload, update `findRedirect()` for `URI` target, update `writeTo()` display |
| `mummy/src/test/java/dev/guise/mummy/plan/PlanSummaryTest.java` | Update for `RedirectEntry` changes |
| `mummy/src/test/java/dev/guise/mummy/plan/PlanDescriberTest.java` | Update for display output changes, add `summarize(Visitor)` test |
| `mummy/src/test/java/dev/guise/mummy/ArtifactTreeWalkerTest.java` | Add `andThen()` test |
| `pom.xml` (root BOM) | Add `flange-deploy-aws` and `flange-env-aws` to `<dependencyManagement>` |
| `mummy/pom.xml` | Add `flange-deploy-aws` and `flange-env-aws` dependencies |
| `mummy/src/main/java/dev/guise/mummy/deploy/aws/FlangeWebSite.java` | **New** — deploy target implementation (includes nested `Manifest` record + `Builder`, `ArtifactMetadataStrategy`, `LoggingSynchronizationMonitor`) |
| `mummy/src/main/java/dev/guise/mummy/GuiseMummy.java` | Add `"FlangeWebSite"` branch in deploy target factory switch |
| `mummy/src/main/java/dev/guise/mummy/deploy/aws/S3.java` | Add TODO to adopt `AwsProfile` from `flange-aws-def` |
| `mummy/src/main/java/dev/guise/mummy/deploy/aws/S3Website.java` | Add TODO to adopt `AwsProfile` from `flange-aws-def` |
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

**Rejected:** The original plan factored redirect extraction and artifact indexing as separate static methods with their own tree walks, optimizing for single-responsibility testability. Both walk the same tree with the same "comprised − subsumed" pattern — the only difference is the per-artifact action. A single walk producing a reified `Manifest` record eliminates the duplication, provides a named concept for the aggregate ("what does the deployer need from this artifact tree?"), and shifts the test surface to the composition (the manifest) rather than the individual extractions.

### Manifest performs its own tree walk vs. piggybacking on `PlanDescriber`

**Rejected:** The original plan had `Manifest.of()` perform its own `ArtifactTreeWalker.walk()` to extract both redirects and the artifact index simultaneously. This duplicated the redirect extraction logic already present in `PlanDescriber.findRedirect()` — the same URI resolution chain, the same `toCollectionURI` compensation, the same site-boundary check. The revised approach reuses `PlanDescriber.summarize(Visitor)` with `Visitor.andThen()` composition: the summarizer handles redirect extraction (producing `PlanSummary`), while the piggybacked visitor handles the artifact index. The `Manifest.Builder` receives the `PlanSummary` at build time and extracts the redirect map from it. This keeps redirect logic in one place and eliminates a redundant tree walk.

### `RedirectEntry` with `altLocationReference`/`resourceReference` and `collection` flag

**Rejected:** The original `RedirectEntry` was named in terms of the Guise Mummy input that produced it (`altLocation` property → artifact resource reference) rather than the resulting redirect. The `collection` flag existed solely to support a "Collection Targets" / "Page Targets" breakdown in the plan display — a categorization that was misleading (collection content resources blur the line) and not actionable. Renamed to `sourcePath`/`targetUri` to align with HTTP redirect and Flange terminology, changed the target type to `URI` to support future non-site redirect destinations, and dropped the `collection` flag entirely.
