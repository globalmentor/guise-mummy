# Aspect Description Loading — Design Rationale

## Problem Summary

Aspect artifacts (e.g. image previews) carry the **main artifact's** fingerprint instead of their own. This is because aspect descriptions are never loaded from their persisted `.description.turf` files; instead, the `DefaultAspectualSourceFileArtifact` constructor copies all properties from the main artifact's description. The description *save* path writes correct per-aspect descriptions, but the *load* path never reads them back.

## Responsibility Analysis

Three responsibilities are relevant:

| Responsibility | Current owner | Correct owner |
|---|---|---|
| Loading main artifact description | Mummifier (`AbstractFileMummifier.plan()`) | Same |
| Saving descriptions (main + aspects) | Mummifier framework (`AbstractFileMummifier.mummify()`) | Same |
| Determining which aspects exist | Mummifier (`DefaultImageMummifier.createArtifact()`) | Same |
| **Loading aspect descriptions** | **Nobody** (constructor copies parent description) | **Mummifier** |
| Deriving aspect file paths | Artifact constructor | Mummifier |
| Creating aspect artifacts | Artifact constructor | Mummifier |

The artifact is a passive data holder throughout the system. The mummifier is the decision-maker. The gap is that the mummifier doesn't load aspect descriptions or create aspect artifacts — the artifact constructor does both, without access to the description-loading infrastructure.

## Design Decisions

### D1: Mummifier creates aspect artifacts before the main artifact

The mummifier (`DefaultImageMummifier.createArtifact()`) builds each aspect artifact fully — with its own loaded description — and passes the finished collection to the main artifact's builder. This is consistent with how `DirectoryMummifier.plan()` creates all child artifacts before creating the `DirectoryArtifact`.

**Rejected alternative: Pass aspect specs (paths + descriptions) through the builder.** This overloads the builder with complex structured data. The builder pattern exists for collecting simple configuration, not for relaying pre-computed artifacts.

**Rejected alternative: Provide a callback interface for description loading.** Adds indirection without benefit. The mummifier already has `loadArtifactDescription()` available and knows the aspect identity at this point.

### D2: Reuse `loadArtifactDescription(context, sourceFile, targetFile)` for aspects

The existing method works correctly for aspects when called with the main image as `sourceFile` and the aspect's target path as `targetFile`:

- **Cached description exists, source unchanged:** Loads the aspect's `.description.turf` with correct fingerprint. The `mummy/sourceContentModifiedAt` check compares against the main image's timestamp, which is semantically correct — the main image *is* the source.
- **Cached description exists, source changed:** Discards stale description. Falls through to create a new description from the main image's metadata. Metadata (e.g. dimensions) will reflect the main image, not the aspect — but this is no worse than the current copy-all-properties behavior, and mummification will set the correct fingerprint and timestamp.
- **No cached description:** Creates a new description from source metadata. Same quality as current code.

**Key semantic note:** The aspect artifact's `getSourcePath()` returns a synthetic path (e.g. `example-preview.jpg`) that doesn't exist. The description is loaded using the main image as the source file. The `mummy/sourceContentModifiedAt` in the loaded description reflects the main image's timestamp. This is correct because the main image is the true source of the aspect, but it means the description's source reference and the artifact's source path don't correspond to the same file. This is an existing semantic gap (the synthetic source path was never a real file), not one introduced by this change.

### D3: Interface-level `invariably` parameter instead of `removeProperty()` workaround

When the main image is being mummified, its aspects must also be mummified — their content is derived from the main image. The current code removes `Content.MODIFIED_AT_PROPERTY_TAG` from the aspect description to force dirtiness. This is a proxy mechanism: the absence of the timestamp causes `AbstractFileMummifier.mummify()` to consider the content dirty.

With correctly loaded descriptions, aspect descriptions will have their *own* `modifiedAt` timestamp, which may match the aspect target file on disk — causing the dirtiness check to conclude "clean" even though the source image changed.

The solution is a new `boolean invariably` parameter on `Mummifier.mummify()`. The concept of incremental mummification is well established in Guise Mummy and has standard industry semantics (analogous to Make, Gradle, Maven). The `invariably` parameter allows a caller to indicate that mummification must be performed regardless of incremental optimizations. Setting `invariably` to `true` overrides `MummyContext.isIncremental()`; setting it to `false` does not imply incremental behavior — it simply means the mummifier proceeds with its normal behavior as determined by the context.

The parameter is on the `Mummifier` interface (not just `AbstractFileMummifier`) because incremental mummification is a framework-level concept, not an implementation detail. The existing `mummify(MummyContext, Artifact)` becomes a `default` method delegating with `invariably = false`.

The term "invariably" mirrors the existing documentation in `AbstractFileMummifier.mummifyFile()`: "Invariably mummifies a resource to a file." It describes the desired behavior without prescribing a mechanism.

**Rejected alternative: `isDirty` naming.** "Dirty" is specific to the `AbstractFileMummifier` timestamp-checking model and implies information about state rather than a behavioral directive. "Invariably" is mechanism-neutral.

**Rejected alternative: `protected` overload on `AbstractFileMummifier` only.** Keeps the interface clean but prevents other mummifier types from participating in the pattern. Since the concept is framework-level, the parameter belongs on the interface.

### D4: Aspect filename derivation moves to the mummifier

The `FILENAME_ASPECT_DELIMITER` constant and the `appendFilenameBase()` derivation logic move from `DefaultAspectualSourceFileArtifact` to `DefaultImageMummifier`. The mummifier needs aspect target paths *before* the artifact exists (to load descriptions), and the mummifier is already the entity that decides which aspects exist. Having it also determine their filenames is consistent.

### D5: Builder receives finished aspect artifacts, not aspect IDs

The builder's `withAspects(Set<String>)` methods are replaced by `withAspectArtifacts(Map<String, Artifact>)`. The `build()` method dispatches on whether the map is non-empty. The `DefaultAspectualSourceFileArtifact` constructor takes only the builder (canonical Bloch pattern) and pulls the pre-built map from it.

## Invariants

After this change, the following invariants hold:

1. Every aspect artifact's in-memory description contains `Content.FINGERPRINT_PROPERTY_TAG` reflecting the aspect's own target file, not the parent's.
2. Every aspect artifact's description contains `PROPERTY_TAG_MUMMY_ASPECT` set to its aspect ID.
3. When the main image is dirty (being mummified), all aspects are force-mummified.
4. When the main image is clean (incremental, unchanged), aspect descriptions loaded from cached `.description.turf` files carry the correct fingerprint from the prior mummification.
5. Description save and load paths are symmetric for aspects: what `saveTargetDescription()` writes, `loadArtifactDescription()` reads back.
