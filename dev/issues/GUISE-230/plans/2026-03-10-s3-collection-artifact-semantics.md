# Plan: S3 Collection Artifact Deployment Semantics

Fix a bug in `S3Website` redirect generation where collection artifacts produce incorrect redirect targets, and refactor `S3.plan()` to walk the artifact tree by model identity rather than by implementation detail. This aligns the S3 deployer with the Guise Mummy site model documented in [architecture.md](../../mummy/architecture.md) (see "Choosing the Correct Walk" and "Mapping Collection Artifacts to Deployment Platforms").

## Overview

**Single chunk** — all steps modify tightly coupled code in `S3.java`, `S3ArtifactDeployObject.java`, and `S3Website.java`. The changes are not independently compilable because `S3ArtifactDeployObject`'s constructor signature changes (Step 1) and the walk restructuring (Step 2) must coordinate. Tests (Step 4) validate the new behavior.

- **Step 1**: Add `Path contentFile` to `S3ArtifactDeployObject` — decouple content I/O from `getArtifact().getTargetPath()`
- **Step 2**: Restructure `S3.plan()` — find content via `CollectionArtifact` ternary, recurse with subsumed filter
- **Step 3**: Update `S3.planResource()` and `S3Website.planResource()` — accept content artifact, use collection resource reference for redirects
- **Step 4**: Unit tests for the restructured walk and redirect correctness

**Notable decisions:**

- No convenience constructor on `S3ArtifactDeployObject` — the content path is always explicit. Every caller must state where content comes from.
- The walk restructuring is in `S3.plan(MummyContext, URI, Artifact)`, which controls both content-finding and the `planResource()` call. Content-finding uses a `CollectionArtifact`-specific ternary with `Streams.toFindOnly()`; recursion uses a single `CompositeArtifact` block that filters out subsumed artifacts uniformly. `planResource()` gains `Path contentFile` and `String key` parameters.
- `S3Website.planResource()` changes only in what it receives — its internal redirect logic is unchanged; the fix comes from receiving the correct artifact and resource reference.
- Tests use `DirectoryArtifact` with `DummyArtifact` content artifacts, not mocks of `CollectionArtifact`.

---

## Bug Description

`S3.plan()` walks `comprisedArtifacts()`, which includes subsumed content artifacts. For a `DirectoryArtifact` at `foo/` with content artifact `foo/index`:

1. `planResource()` is called for the `DirectoryArtifact` — `S3.planResource()` skips it (`instanceof CollectionArtifact` guard), but `S3Website.planResource()` sees `altLocation` (via description delegation) and creates redirect `foo/bar` → `foo/` (correct).
2. `planResource()` is called for the content artifact `foo/index` — `S3.planResource()` creates a deploy object, and `S3Website.planResource()` sees the same `altLocation` and creates redirect `foo/bar` → `foo/index` (incorrect), overwriting the correct redirect.

The surviving redirect targets `foo/index` instead of the canonical `foo/`.

## Step 1: Add `Path contentFile` to `S3ArtifactDeployObject`

**File:** `mummy/src/main/java/dev/guise/mummy/deploy/aws/S3ArtifactDeployObject.java`

Currently `S3ArtifactDeployObject` uses `getArtifact().getTargetPath()` for three I/O operations: `getContentLength()` (line 67), `getContentType()` fallback (line 79), and `createInputStream()` (line 87). It also uses `getArtifact().getResourceDescription()` for metadata (lines 59, 78).

After refactoring, the artifact may be a `DirectoryArtifact` whose `getTargetPath()` returns a directory — `Files.size()` and `Files.newInputStream()` would fail. The metadata methods still work because `DirectoryArtifact.getResourceDescription()` delegates to the content artifact's description.

**Changes:**

Add a `Path contentFile` field. Replace all `getArtifact().getTargetPath()` references with `contentFile`. Update the constructor:

```java
private final Path contentFile;

/// Returns the filesystem path to the content to be deployed.
/// @apiNote For a collection artifact, this is the subsumed content artifact's target path
///          (e.g. `foo/index.html`), not the collection directory path.
/// @return The path to the deployable content file.
public Path getContentFile() {
    return contentFile;
}

/// Constructor.
/// @param key The S3 key representing the deployment path of the object in the bucket.
/// @param artifact The artifact whose description provides metadata for the deployed object.
/// @param contentFile The filesystem path to the content file to be deployed.
public S3ArtifactDeployObject(@NonNull final String key, @NonNull final Artifact artifact, @NonNull final Path contentFile) {
    super(key);
    this.artifact = requireNonNull(artifact);
    this.contentFile = requireNonNull(contentFile);
}
```

Update the three I/O methods to use `contentFile`:

```java
@Override
public long getContentLength() throws IOException {
    return Files.size(contentFile);
}

@Override
public String getContentType() {
    return getArtifact().getResourceDescription().findPropertyValue(Content.TYPE_PROPERTY_TAG).map(Object::toString)
            .orElseGet(() -> Mimetype.getInstance().getMimetype(contentFile));
}

@Override
protected InputStream createInputStream() throws IOException {
    return Files.newInputStream(contentFile);
}
```

Update Javadoc on `getArtifact()` to clarify it provides the model artifact (for metadata/description), not a content path source:

```java
/// Returns the artifact whose description provides metadata for the deployed object.
/// @apiNote For collection artifacts, this is the collection artifact itself (e.g. the `DirectoryArtifact`),
///          not the subsumed content artifact. Use [#getContentFile()] for the deployable content file.
/// @return The artifact providing metadata for this deploy object.
```

## Step 2: Restructure `S3.plan(MummyContext, URI, Artifact)`

**File:** `mummy/src/main/java/dev/guise/mummy/deploy/aws/S3.java` (lines 347–355)

Currently:

```java
protected void plan(final MummyContext context, final URI rootTargetPathUri, Artifact artifact) throws IOException {
    final URIPath resourceReference = Artifact.relativizeResourceReference(rootTargetPathUri, artifact);
    planResource(context, rootTargetPathUri, artifact, resourceReference);
    if(artifact instanceof CompositeArtifact compositeArtifact) {
        for(final Artifact comprisedArtifact : (Iterable<Artifact>)compositeArtifact.comprisedArtifacts()::iterator) {
            plan(context, rootTargetPathUri, comprisedArtifact);
        }
    }
}
```

After:

```java
protected void plan(@NonNull final MummyContext context, @NonNull final URI rootTargetPathUri,
        @NonNull Artifact artifact) throws IOException {
    final URIPath resourceReference = Artifact.relativizeResourceReference(rootTargetPathUri, artifact);
    // A collection has separate content via a subsumed artifact; other artifacts are their own content.
    final Optional<Artifact> foundContentArtifact = artifact instanceof CollectionArtifact collectionArtifact
            ? collectionArtifact.getSubsumedArtifacts().stream().reduce(toFindOnly())
            : Optional.of(artifact);
    foundContentArtifact.ifPresent(throwingConsumer(contentArtifact -> { // on S3, only artifacts with content can be uploaded
        final String s3Key = Artifact.relativizeResourceReference(rootTargetPathUri, contentArtifact).toDecodedString(); // canonical resource name, not URI-encoded
        planResource(context, rootTargetPathUri, artifact, resourceReference, contentArtifact.getTargetPath(), s3Key);
    }));
    if(artifact instanceof CompositeArtifact compositeArtifact) { // recurse into non-subsumed comprised artifacts
        final Set<Artifact> subsumedArtifacts = Set.copyOf(compositeArtifact.getSubsumedArtifacts());
        for(final Artifact comprisedArtifact : (Iterable<Artifact>)compositeArtifact.comprisedArtifacts()::iterator) {
            if(!subsumedArtifacts.contains(comprisedArtifact)) {
                plan(context, rootTargetPathUri, comprisedArtifact);
            }
        }
    }
}
```

**New imports required:**

```java
import static com.globalmentor.util.stream.Streams.*;
import static org.zalando.fauxpas.FauxPas.*;
```

(`FauxPas` is already imported in `S3Website.java`; `Streams` is a new dependency for `S3.java`.)

**Key behavioral changes:**

- The content-finding ternary uses `CollectionArtifact`-specific semantics (subsumed content artifact discovery) for collections, while non-collection artifacts are their own content. This aligns with the refinement that only `CollectionArtifact` has the "separate content via subsumed artifact" pattern — other composite types like `AspectualArtifact` do not.
- `Streams.toFindOnly()` replaces a manual size check, throwing `IllegalArgumentException` if a collection has more than one subsumed artifact. This is semantically correct — multiple subsumed artifacts violates a model constraint, not an I/O condition.
- Recursion uses a single `CompositeArtifact` block with a subsumed filter, uniform across all composite types. Subsumption is a `CompositeArtifact`-level invariant (subsumed artifacts are implementation details not independently deployable), so the filter applies equally to `CollectionArtifact` (where `getChildArtifacts()` is equivalent) and `AspectualArtifact` (where the subsumed set is empty, making the filter a no-op). See architecture.md § "Subsumption and Composite Walks".
- For a `CollectionArtifact` with no content artifact (empty intermediate directory): `foundContentArtifact` is empty, so no `planResource()` call and no deploy object. Recursion still visits children. Any `altLocation` on such a collection is silently not processed, since redirect handling occurs inside `planResource()` — correct, because there is no resource to redirect to.
- For a `CollectionArtifact` with one content artifact: `planResource()` is called once with the _collection_ artifact, its _collection_ resource reference (`foo/`), the content artifact's target path, and the content-derived S3 key (`foo/index`).

**Design notes:**

- The `Set.copyOf(compositeArtifact.getSubsumedArtifacts())` in the recursion block creates an immutable snapshot for the `contains()` check. `getSubsumedArtifacts()` already returns a `Collection`, but `Set.copyOf()` guarantees O(1) lookup and defensive immutability.
- For non-collection artifacts, `foundContentArtifact` is `Optional.of(artifact)`, so `s3Key == resourceReference.toDecodedString()` — the canonical filesystem name (see `designs/s3-key-encoding.md` §S3 Key Identity Principle).

**Alternatives considered:**

- _Passing `Optional<Artifact> foundContentArtifact` to `planResource()`_: Initially discussed as an approach to keep key derivation consolidated in `planResource()`. Rejected because `planResource()` is a lower-level method that should not need collection semantics, and because `planResource()` is not called at all when there is no content artifact (empty intermediate directory), making the `Optional` always present at the call site — unnecessary optionality. Computing the concrete `key` and `contentFile` in `plan()` and passing them directly is cleaner.
- _Keeping a single `planResource(context, rootTargetPathUri, artifact, resourceReference)` signature and adding content path lookup inside_: This pushes collection-awareness into `planResource()`, which is a lower-level method that should not need to know about collection semantics.
- _Splitting recursion into two branches_ (`getChildArtifacts()` for collections, `comprisedArtifacts()` for non-collections): Rejected because subsumption is a `CompositeArtifact`-level concept, not collection-specific. A single unified filter is more semantically correct and avoids code duplication. The "comprised − subsumed" pattern is documented as a general recursive strategy in the architecture document.
- _Throwing checked `IOException` for multiple subsumed artifacts_: Replaced with `IllegalArgumentException` via `toFindOnly()`. This is a model constraint violation (the artifact tree is malformed), not an I/O problem.

## Step 3: Update `S3.planResource()` and `S3Website.planResource()`

### `S3.planResource()`

**File:** `mummy/src/main/java/dev/guise/mummy/deploy/aws/S3.java` (lines 365–372)

The signature gains two parameters: `Path contentFile` and `String key`. The `CollectionArtifact` guard is removed — `plan()` now controls when `planResource()` is called for collections.

Before:

```java
protected void planResource(final MummyContext context, final URI rootTargetPathUri, Artifact artifact,
        final URIPath resourceReference) throws IOException {
    final String key = resourceReference.toString();
    getLogger().debug("Planning deployment for artifact {}, S3 key `{}`.", artifact, key);
    if(!(artifact instanceof CollectionArtifact)) {
        getDeployObjectsByKey().put(key, new S3ArtifactDeployObject(key, artifact));
    }
}
```

After:

```java
/// Plans deployment of a single resource.
/// @implSpec This version stores an S3 key and deploy object for the resource in [#getDeployObjectsByKey()].
/// @param context The context of static site generation.
/// @param rootTargetPathUri The URI form of the root artifact target path of the site being deployed.
/// @param artifact The artifact for which deployment is being planned; provides metadata via its resource description.
/// @param resourceReference A URI reference to the resource, relative to the site root.
/// @param contentFile The filesystem path to the content file to be deployed.
/// @param key The S3 key at which the object (content and metadata) will be stored.
/// @throws IOException if there is an I/O error during site deployment planning.
protected void planResource(@NonNull final MummyContext context, @NonNull final URI rootTargetPathUri, @NonNull Artifact artifact,
        @NonNull final URIPath resourceReference, @NonNull final Path contentFile, @NonNull final String key) throws IOException {
    getLogger().debug("Planning deployment for artifact {}, S3 key `{}`.", artifact, key);
    getDeployObjectsByKey().put(key, new S3ArtifactDeployObject(key, artifact, contentFile));
}
```

Note: the S3 key used in the deploy object is now `key` (e.g. `foo/index`), derived in `plan()` from the content artifact's resource reference via `.toDecodedString()`, rather than from `resourceReference.toString()` (e.g. `foo/`). Two changes from the old code: (1) the key is derived from the _content artifact's_ resource reference (the storage path), not the _collection's_ resource reference; and (2) `.toDecodedString()` produces the canonical filesystem name per the S3 Key Identity Principle (see `designs/s3-key-encoding.md`), avoiding percent-encoding artifacts that `.toString()` would leak for spaces, `#`, `?`, and `%`.

### `S3Website.planResource()`

**File:** `mummy/src/main/java/dev/guise/mummy/deploy/aws/S3Website.java` (lines 460–482)

The signature gains the same two parameters. The redirect logic is unchanged internally, but now receives the _collection_ artifact and its _collection_ `resourceReference` — which produces the correct redirect target.

Before:

```java
@Override
protected void planResource(final MummyContext context, final URI rootTargetPathUri, final Artifact artifact, final URIPath resourceReference)
        throws IOException {
    super.planResource(context, rootTargetPathUri, artifact, resourceReference);
    artifact.getResourceDescription().findPropertyValue(PROPERTY_TAG_MUMMY_ALT_LOCATION)...
            .ifPresent(throwingConsumer(altLocationReference -> {
                ...
                final String artifactKey = resourceReference.toString();
                ...
                final S3ArtifactRedirectDeployObject redirectDeployObject = new S3ArtifactRedirectDeployObject(altKey, artifactKey, artifact);
                ...
            }));
}
```

After:

```java
@Override
protected void planResource(final MummyContext context, final URI rootTargetPathUri, final Artifact artifact,
        final URIPath resourceReference, final Path contentFile, final String key) throws IOException {
    super.planResource(context, rootTargetPathUri, artifact, resourceReference, contentFile, key);
    // redirect logic is unchanged — the fix comes from receiving the collection artifact and
    // its collection `resourceReference`, so `artifactKey` resolves to e.g. `foo/` rather than `foo/index`
    ...
}
```

The internal logic is identical — the fix comes from the caller. Previously, `planResource()` was called twice for a collection path: once with the `DirectoryArtifact` (which skipped the deploy object but created the correct redirect) and once with the content artifact (which created the deploy object _and_ an incorrect redirect that overwrote the first). Now it is called once with the `DirectoryArtifact`, which creates both the deploy object (with the correct content key) and the correct redirect (with the collection resource reference as the target).

**What changes for redirects:**

| Scenario | Before | After |
|---|---|---|
| `altLocation = "bar"` on `foo/index` | Two redirects: `foo/bar` → `foo/` (overwritten), `foo/bar` → `foo/index` (survives) | One redirect: `foo/bar` → `foo/` |
| `altLocation = "bar"` on non-collection `page.html` | One redirect: `bar` → `page.html` | One redirect: `bar` → `page.html` (unchanged) |

**Routing rule `instanceof` check:**

At `S3Website.java` line 360, the routing rule generation checks `redirectObject.getTargetArtifact() instanceof CollectionArtifact` to decide between `replaceKeyPrefixWith` and `replaceKeyWith`. Previously, when the content artifact was the target artifact, this check returned `false` even for collection-to-collection redirects (because the content artifact is not a `CollectionArtifact`). Now the target artifact is the `DirectoryArtifact` itself, so the check correctly returns `true` for collection-to-collection redirects.

## Step 4: Unit Tests

### Test strategy

Add a package-private constructor to `S3` that accepts a pre-built `S3Client`, allowing tests to pass a mock client that is never called during the planning phase. `S3Website` gets a corresponding package-private constructor that delegates to it. The planning methods (`plan()`, `planResource()`) operate only on the internal `deployObjectsByKey` map and `routingRuleRedirectObjects` set — both accessible from test code via `protected` getters.

```java
// S3 test constructor
S3(@NonNull final Region region, @NonNull String bucket, @NonNull final S3Client s3Client) {
    this.profile = null;
    this.region = requireNonNull(region);
    this.bucket = requireNonNull(bucket);
    this.s3Client = requireNonNull(s3Client);
}

// S3Website test constructor
S3Website(@NonNull final Region region, @NonNull String bucket, @NonNull final S3Client s3Client) {
    super(region, bucket, s3Client);
    this.altBuckets = Set.of();
    this.siteDomain = null;
    this.redirectMeans = RedirectMeans.OBJECT;
    this.redirectCountOptimalThreshold = DEFAULT_REDIRECT_COUNT_OPTIMAL_OPTIMAL_THRESHOLD;
}
```

Tests call `plan(context, rootTargetPathUri, artifact)` with a hand-built artifact tree, then inspect the resulting map and set.

### Tests

**File:** `mummy/src/test/java/dev/guise/mummy/deploy/aws/S3WebsiteTest.java`

The tests use `DirectoryArtifact` with `DummyArtifact` content artifacts.

1. **Collection artifact produces one deploy object at the content artifact's key.** Given a `DirectoryArtifact` at `section/` with content artifact at `section/index`, verify `deployObjectsByKey` contains one entry keyed by `section/index`, whose artifact is the `DirectoryArtifact` and whose `contentFile` is the content artifact's target path.

2. **Collection artifact redirect targets the collection resource reference.** Given a `DirectoryArtifact` at `section/` with `altLocation = "old-section"` (via description delegation from its content artifact), verify that the redirect is `old-section/` → `section/` (not `old-section/` → `section/index`).

3. **Empty intermediate directory produces no deploy object.** Given a `DirectoryArtifact` with no content artifact (`null`), verify `deployObjectsByKey` has no entry for that directory. Child artifacts should still produce deploy objects.

4. **Non-collection artifact produces deploy object at its own resource reference.** Given a `DummyArtifact` at `page.html`, verify the deploy object is keyed by `page.html` with `contentFile` equal to the artifact's own target path.

5. **Walk recurses into child artifacts for collections.** Given a `DirectoryArtifact` with two child `DummyArtifact`s, verify both produce deploy objects.

6. **S3 key is the canonical filesystem name.** Verify that S3 keys produced by `plan()` are canonical decoded filesystem names with no percent-encoding artifacts leaked from the `Path.toUri()` → `URIPath` pipeline. Test character classes: non-ASCII Latin (`café/` → `café/index`), CJK (`東京/` → `東京/index`), space (`my section/` → `my section/index`), `#` (`section#2/` → `section#2/index`), `%` (`100%/` → `100%/index`), and a mixed combination (`café #1/` → `café #1/index`). See `designs/s3-key-encoding.md` §S3 Key Identity Principle. Each uses a fresh `S3Website` instance to avoid key collisions across character classes.

---

## Verification Checklist

After implementation:

- [ ] `mvn test -pl mummy` passes
- [ ] `S3ArtifactDeployObject` has no references to `getArtifact().getTargetPath()`
- [ ] `S3.plan()`: content-finding ternary uses `CollectionArtifact` (not `CompositeArtifact`) for subsumed content discovery
- [ ] `S3.plan()`: uses `Streams.toFindOnly()` for at-most-one subsumed artifact enforcement
- [ ] `S3.plan()`: single recursion block at `CompositeArtifact` level, filtering out subsumed artifacts uniformly
- [ ] `S3.plan()`: skips `planResource()` for collection with no content artifact (empty `Optional`)
- [ ] `S3.planResource()`: no `instanceof CollectionArtifact` guard
- [ ] `S3Website.planResource()`: redirect target is `resourceReference` (collection path) not content path
- [ ] `S3.plan()`: S3 key derived via `.toDecodedString()` (canonical name), not `.toString()` (raw URI path)
- [ ] Grep for `getChildArtifacts` in `S3.java` — should not appear (recursion uses `comprisedArtifacts()` minus subsumed)
