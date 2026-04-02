# Plan: Fix Aspect Fingerprint Bug

**Ticket:** [GUISE-231]  
**Date:** 2026-04-01  
**Design:** [aspect-description-loading.md](../designs/aspect-description-loading.md)

## Overview

All steps modify shared interfaces and span multiple compilation units. The changes are tightly coupled — the interface change, the builder API change, the artifact constructor change, and the mummifier changes must happen together for the code to compile. This is one irreducible chunk.

- **Step 1:** Add `mummify(context, artifact, invariably)` to `Mummifier` interface; update all implementations (~4 files)
- **Step 2:** Replace builder's aspect ID API with aspect artifact API in `DefaultSourceFileArtifact.Builder` (~20 lines changed)
- **Step 3:** Rewrite `DefaultAspectualSourceFileArtifact` constructor and move constant (~40 lines removed/rewritten)
- **Step 4:** Update `DefaultImageMummifier.createArtifact()` to load descriptions and build aspects (~20 lines)
- **Step 5:** Update `DefaultImageMummifier.mummifyFile()` aspect loop (~5 lines)
- **Step 6:** Update existing tests if affected

## Step 1: Add `invariably` parameter to `Mummifier` interface and update implementations

The concept of incremental mummification is well established in Guise Mummy and has standard industry semantics: skip regenerating output when inputs haven't changed. Sometimes a caller knows that regeneration must occur even if the mummifier's own staleness checks would not detect it — for example, when a parent artifact has changed and its derived aspects must be regenerated. The new `invariably` parameter communicates this to the mummifier.

### 1a. `Mummifier` interface

**File:** `mummy/src/main/java/dev/guise/mummy/mummify/Mummifier.java`

The existing `mummify(MummyContext, Artifact)` becomes a `default` method delegating to the new primary method. The new method adds the `invariably` parameter.

```java
/// Mummifies a resource in the presence of a context artifact, which may or may not be the same as the artifact itself.
/// @implSpec The default implementation delegates to [#mummify(MummyContext, Artifact, boolean)] with `invariably` set to `false`.
/// @param context The context of static site generation.
/// @param artifact The artifact being generated.
/// @throws IOException if there is an I/O error during static site generation.
public default void mummify(@NonNull final MummyContext context, @NonNull Artifact artifact) throws IOException {
    mummify(context, artifact, false);
}

/// Mummifies a resource in the presence of a context artifact, which may or may not be the same as the artifact itself.
/// Mummification is invariably performed if `invariably` is `true`, regardless of whether
/// [MummyContext#isIncremental()] is enabled. If `invariably` is `false`, the mummifier may still perform
/// mummification unconditionally or incrementally according to its own implementation and the context; this
/// parameter only guarantees mummification when set to `true`, and does not imply incremental behavior when
/// set to `false`.
/// @param context The context of static site generation.
/// @param artifact The artifact being generated.
/// @param invariably `true` if mummification must invariably be performed regardless of incremental
///        optimizations, or `false` for normal behavior as determined by the mummifier and context.
/// @throws IOException if there is an I/O error during static site generation.
/// @see MummyContext#isIncremental()
public void mummify(@NonNull final MummyContext context, @NonNull Artifact artifact, boolean invariably) throws IOException;
```

### 1b. `AbstractFileMummifier`

**File:** `mummy/src/main/java/dev/guise/mummy/mummify/AbstractFileMummifier.java`

Remove the existing `mummify(MummyContext, Artifact)` override (the 2-arg `@Override` is no longer needed since the interface default handles it). Rename/update the method to the 3-arg signature:

```java
@Override
public final void mummify(@NonNull final MummyContext context, @NonNull Artifact artifact, final boolean invariably) throws IOException {
```

Update the body — hoist `invariably` into the incremental guard so that invariable mummification skips the incremental block entirely, avoiding unnecessary filesystem checks:

```java
// Before:
if(context.isIncremental()) {

// After:
if(!invariably && context.isIncremental()) {
```

The timestamp comparison and `oldTargetModifiedAt` computation inside the incremental block remain unchanged. When `invariably` is `true`, the code falls through to the `else` branch where `targetContentDirty = true` — which is the desired behavior.

Update the Javadoc on this method to reflect the new parameter. Preserve the existing `@implSpec` documentation about incremental timestamp checking, and add documentation for the `invariably` parameter's effect.

### 1c. `DirectoryMummifier`

**File:** `mummy/src/main/java/dev/guise/mummy/mummify/collection/DirectoryMummifier.java`

Update the method signature to the 3-arg form. The `invariably` parameter is accepted but has no effect on directory mummification — directories are always created if absent, and children are always delegated to. Add a brief `@implSpec` noting this.

```java
/// {@inheritDoc}
/// @implSpec This implementation ignores the `invariably` parameter, as directory mummification is always
///           performed unconditionally. Child artifacts are mummified via the normal (non-invariable) path.
/// @implSpec This implementation saves the description description if modified by calling [#saveTargetDescription(MummyContext, Artifact)].
@Override
public void mummify(final MummyContext context, final Artifact artifact, final boolean invariably) throws IOException {
    // ... existing body unchanged
}
```

The internal calls to `contentArtifact.getMummifier().mummify(context, contentArtifact)` and `childArtifact.getMummifier().mummify(context, childArtifact)` remain as 2-arg calls, which now go through the interface default (`invariably = false`). This is correct — the directory doesn't force its children to regenerate.

**Rejected alternative: `protected` overload on `AbstractFileMummifier` only.** Keeps the interface clean but prevents other mummifier types from participating in the incremental override pattern. Since incremental mummification is a framework-level concept referenced across the codebase (not just in `AbstractFileMummifier`), the parameter belongs on the interface.

**Rejected alternative: `isDirty` naming.** "Dirty" is specific to the `AbstractFileMummifier` timestamp-checking model. "Invariably" mirrors the existing terminology in the `mummifyFile()` docs ("Invariably mummifies a resource…") and describes the desired *behavior* without prescribing a mechanism.

## Step 2: Replace builder aspect API

**File:** `mummy/src/main/java/dev/guise/mummy/DefaultSourceFileArtifact.java`  
**Lines:** 125–158 (current `aspectIds` field, `withAspects()` methods, and `build()`)

Remove:
- `private Set<String> aspectIds`
- `public B withAspects(String... aspectIds)`
- `public B withAspects(Collection<String> aspectIds)`

Add:
- `private Map<String, Artifact> aspectArtifacts`
- Package-private getter `Map<String, Artifact> getAspectArtifacts()`
- `public B withAspectArtifacts(Map<String, Artifact> aspectArtifacts)`

Update `build()`:

```java
@Override
public DefaultSourceFileArtifact build() {
    validate();
    return aspectArtifacts != null && !aspectArtifacts.isEmpty()
        ? new DefaultAspectualSourceFileArtifact(this)
        : new DefaultSourceFileArtifact(this);
}
```

## Step 3: Rewrite `DefaultAspectualSourceFileArtifact`

**File:** `mummy/src/main/java/dev/guise/mummy/DefaultAspectualSourceFileArtifact.java`

### Remove

- `FILENAME_ASPECT_DELIMITER` constant (moves to `DefaultImageMummifier` in Step 4)
- All aspect-creation logic from the constructor (path derivation, description copying, child artifact building)
- Stale `@implSpec` and `@implNote` docs about description copying and `MODIFIED_AT` removal workaround
- Remove unused imports (`java.net.URI`, `java.util.stream.Stream`, `io.urf.model.*`, `static java.util.function.Function.*`, `static java.util.stream.Collectors.*`)

### Rewrite constructor

The constructor becomes a canonical single-argument builder constructor:

```java
/// Builder constructor.
/// @param builder The builder specifying the construction parameters.
/// @throws IllegalArgumentException if the corporeal source file does not exist or is not a regular file.
protected DefaultAspectualSourceFileArtifact(@NonNull final Builder<?> builder) {
    super(builder);
    this.aspectsById = builder.getAspectArtifacts(); // already an unmodifiable map from the builder
}
```

### Update class docs

Replace the `@implSpec`/`@implNote` about description copying with a clean description stating that aspects are pre-built by the mummifier and provided via the builder. Add an `@implNote` about the synthetic source path for aspects:

> The aspect artifacts' source paths are synthetic (e.g. `example-preview.jpg`) and do not correspond to real files. The aspect descriptions are loaded using the main image as the source file; the `mummy/sourceContentModifiedAt` property in each aspect description reflects the main image's modification timestamp.

## Step 4: Update `DefaultImageMummifier.createArtifact()`

**File:** `mummy/src/main/java/dev/guise/mummy/mummify/image/DefaultImageMummifier.java`  
**Lines:** 91–99

### Add constant

```java
/// The delimiter for appending an aspect ID to a filename.
private static final char ASPECT_FILENAME_DELIMITER = '-';
```

### Add import

`import static com.globalmentor.io.Paths.*;` — already available transitively through `BaseImageMummifier`, but `DefaultImageMummifier` doesn't have it. Add the static import.

Also add `import static java.util.function.Function.*;` for `identity()`.

### Rewrite `createArtifact()`

Uses FauxPas `throwingFunction()` for the stream lambda, consistent with `DirectoryMummifier` and other Guise Mummy code that calls checked-exception methods inside functional pipelines.

```java
@Override
protected Artifact createArtifact(final MummyContext context, final Path sourceFile, final Path outputFile,
        final UrfResourceDescription description) throws IOException {
    final Configuration config = context.getConfiguration();
    if(size(sourceFile) > config.findLong(CONFIG_KEY_MUMMY_IMAGE_PROCESS_THRESHOLD_FILE_SIZE).orElse(DEFAULT_SCALE_THRESHOLD_FILE_SIZE)) {
        final Set<String> aspectIds = config.findCollection(CONFIG_KEY_MUMMY_IMAGE_WITH_ASPECTS)
            .map(ids -> ids.stream().map(Object::toString).collect(toSet()))
            .orElse(DEFAULT_ASPECT_IDS);
        if(!aspectIds.isEmpty()) {
            final Map<String, Artifact> aspectArtifacts = aspectIds.stream()
                .collect(toUnmodifiableMap(identity(), throwingFunction(aspectId -> {
                    final Path aspectSourcePath = appendFilenameBase(sourceFile, ASPECT_FILENAME_DELIMITER + aspectId);
                    final Path aspectTargetPath = appendFilenameBase(outputFile, ASPECT_FILENAME_DELIMITER + aspectId);
                    final UrfResourceDescription aspectDescription = loadArtifactDescription(context, sourceFile, aspectTargetPath);
                    aspectDescription.setPropertyValue(PROPERTY_TAG_MUMMY_ASPECT, aspectId);
                    return DefaultSourceFileArtifact.builder(this, aspectSourcePath, aspectTargetPath)
                        .setCorporealSourceFile(sourceFile).withDescription(aspectDescription).build();
                })));
            return DefaultSourceFileArtifact.builder(this, sourceFile, outputFile)
                .withDescription(description).withAspectArtifacts(aspectArtifacts).build();
        }
    }
    return super.createArtifact(context, sourceFile, outputFile, description);
}
```

**Rejected alternative: Plain loop with `HashMap`.** Simpler and avoids the `throwingFunction()` wrapper, but `throwingFunction()` is the established pattern throughout Guise Mummy (used extensively in `DirectoryMummifier`). Using the stream approach maintains codebase consistency.

## Step 5: Update `DefaultImageMummifier.mummifyFile()` aspect loop

**File:** `mummy/src/main/java/dev/guise/mummy/mummify/image/DefaultImageMummifier.java`  
**Lines:** 147–151

Replace:

```java
if(artifact instanceof AspectualArtifact aspectualArtifact) { //mummify any image aspects TODO generalize within framework
    for(final Artifact aspectArtifact : aspectualArtifact.getAspects()) {
        aspectArtifact.getResourceDescription().removeProperty(Content.MODIFIED_AT_PROPERTY_TAG);
        mummify(context, aspectArtifact);
    }
}
```

With:

```java
if(artifact instanceof AspectualArtifact aspectualArtifact) { //mummify any image aspects TODO generalize within framework
    for(final Artifact aspectArtifact : aspectualArtifact.getAspects()) {
        mummify(context, aspectArtifact, true); // parent is being mummified → aspects must be mummified invariably
    }
}
```

## Step 6: Update tests

**Files to check:**
- `mummy/src/test/java/dev/guise/mummy/ArtifactTreeWalkerTest.java` — Uses mocked `CompositeArtifact`, not `DefaultAspectualSourceFileArtifact`. No change needed.
- `mummy/src/test/java/dev/guise/mummy/plan/PlanDescriberTest.java` — Uses mocked `ImageMummifier`. No change needed.
- `mummy/src/test/java/dev/guise/mummy/mummify/image/DefaultImageMummifierTest.java` — Review for any direct use of `withAspects()`. Update if present.
- `mummy/src/test/java/dev/guise/mummy/mummify/image/DefaultImageMummifierIT.java` — Review similarly.

## Resolved TODOs

These existing TODOs are resolved by this change:

| Location | TODO text | Resolution |
|---|---|---|
| `DefaultAspectualSourceFileArtifact` constructor | `TODO create description copy constructor` | Descriptions are no longer copied; loaded individually |
| `DefaultAspectualSourceFileArtifact` constructor | `TODO fix description caching for artifacts somehow; the current logic will set wrong fingerprints, for example` | Each aspect gets its own correctly loaded description |
| `DefaultAspectualSourceFileArtifact` constructor | `TODO set aspect ID` | Mummifier sets `PROPERTY_TAG_MUMMY_ASPECT` on each aspect description |

## Not addressed (separate tickets)

- `TODO generalize within framework` on the aspect mummification loop — broader framework change, not needed for this fix.
- Aspect metadata accuracy (e.g. image dimensions in description reflecting main image, not the scaled aspect) — pre-existing, orthogonal.
- Aspect configuration change detection (aspects stale because config changed, not source image) — pre-existing, orthogonal.
- `TODO abstract the copy, here and in OpaqueFileMummifier/GenericFileMummifier` — unrelated.

[GUISE-231]: ../
