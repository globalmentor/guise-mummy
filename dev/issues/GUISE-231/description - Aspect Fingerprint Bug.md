# [GUISE-231] Aspect artifacts carry the main artifact's fingerprint, causing S3 upload rejection.

## Objective

Ensure that aspect artifacts (e.g., image previews and thumbnails) carry their own correct SHA-256 fingerprint in their in-memory description at deploy time, rather than inheriting the main artifact's fingerprint.

## Problem

When deploying a site via `guise deploy` as a separate invocation after `guise mummify`, S3 rejects the upload of aspect artifacts (e.g., `example-preview.jpg`) with:

> The SHA256 you specified did not match the calculated checksum.

The aspect artifact's in-memory description carries the **main** image's fingerprint (`fingerprint_M`) instead of its own (`fingerprint_A`). Flange's `S3Synchronizer` sends this fingerprint as a real S3 `checksumSHA256` header, which S3 validates server-side against the uploaded bytes. Since the preview image's bytes differ from the main image's, the checksum fails.

This bug is latent — it existed before Flange integration. The old `S3`/`S3Website` deploy targets stored the fingerprint as opaque custom S3 metadata (`x-amz-meta-content-fingerprint`), which S3 never validated. The incorrect fingerprint caused unnecessary re-uploads on incremental deploys (the S3-side metadata never matched the artifact's description) but never a hard failure. Flange's use of S3's native checksum validation (`PutObjectRequest.checksumSHA256()`) surfaced the bug as a fatal error.

### Workaround

`guise clean && guise deploy` (single invocation) avoids the bug because MUMMIFY and DEPLOY share the same in-memory artifacts, and full mummification always corrects the fingerprint. However, any subsequent incremental `guise deploy` will fail again.

## History

### Root cause: description-copying in `DefaultAspectualSourceFileArtifact`

The aspect construction in `DefaultAspectualSourceFileArtifact`'s [constructor](mummy/src/main/java/dev/guise/mummy/DefaultAspectualSourceFileArtifact.java) copies **all** properties from the main artifact's in-memory description to each aspect's description — including `Content.FINGERPRINT_PROPERTY_TAG`. This is annotated with a TODO acknowledging the problem:

```java
//TODO fix description caching for artifacts somehow; the current logic will set wrong fingerprints, for example
```

The class-level `@implNote` documents awareness of the stale-description issue and describes a partial workaround: removing `Content.MODIFIED_AT_PROPERTY_TAG` from the aspect to force it dirty. `DefaultImageMummifier.mummifyFile()` implements this workaround at [line 149](mummy/src/main/java/dev/guise/mummy/mummify/image/DefaultImageMummifier.java), but the workaround only fires when the main image's `mummifyFile()` is called — i.e., when the main image itself is dirty.

### The structural disconnect

The underlying issue is an asymmetry between description loading and description saving:

- **Description saving** follows the artifact tree. During mummification, `AbstractFileMummifier.mummify()` is called on each aspect individually (orchestrated by the loop in `DefaultImageMummifier.mummifyFile()`). Each aspect's `.description.turf` is written with its correct fingerprint.
- **Description loading** follows the source directory walk. During planning, the `DirectoryMummifier` walks the source tree, encounters `example.jpg` (aspects like `example-preview.jpg` don't exist as source files), and delegates to `AbstractFileMummifier.plan()`. That method calls `loadArtifactDescription()` — loading `example.jpg.description.turf` — **then** calls `createArtifact()`, which constructs the `DefaultAspectualSourceFileArtifact`. Aspect artifacts are synthesized inside this constructor, not planned through the normal path, so the aspect description loading infrastructure is never invoked for them.

The net effect is that aspect `.description.turf` files are written but never read back. The description-write path and the description-read path are structurally different for aspects.

### Why the sequencing prevents a simple fix at the current abstraction level

`AbstractFileMummifier.plan()` calls `loadArtifactDescription()` **before** `createArtifact()`. At description-loading time, the system does not yet know whether the artifact will have aspects or which aspects it will have — that decision is made in `createArtifact()`, based on configuration and source file size. The aspect's `.description.turf` files exist on disk (from a prior mummification), but nobody loads them because aspect identity isn't known until after description loading is complete.

### Mummification gating

Aspect mummification is gated behind the main artifact's dirtiness. The aspect mummification loop in `DefaultImageMummifier.mummifyFile()` lives inside `mummifyFile()`, which `AbstractFileMummifier.mummify()` only calls when the **main** artifact is dirty. If the main image hasn't changed, `mummifyFile()` is never called, the aspect loop never runs, and the aspects' `mummify()` is never invoked at all. The aspects sit with their copied (incorrect) descriptions, and these flow directly into the deploy phase.

### Responsibility analysis

Currently:

- **Description loading**: The mummifier (`AbstractFileMummifier.plan()` → `loadArtifactDescription()`). The artifact is passive — it receives an already-loaded description at construction time.
- **Description saving**: The mummifier framework (`AbstractFileMummifier.mummify()` → `saveTargetDescription()`). The artifact is passive.
- **Aspect discovery**: The image mummifier (`DefaultImageMummifier.createArtifact()`), which reads aspect IDs from configuration and the file size threshold. The aspect artifact receives the decision; it doesn't make it.

In all three cases the mummifier is the decision-maker and the artifact is a passive data holder. The gap is that the mummifier takes full responsibility for loading the main artifact's description, for saving all descriptions (main + aspects), and for deciding what aspects exist — but does not take responsibility for loading aspect descriptions.

## Desired Behavior

After any `guise deploy` (whether in the same invocation as `guise mummify` or a separate one), each aspect artifact's in-memory description contains the correct fingerprint for that aspect's target file, not the main artifact's fingerprint. S3 uploads of aspect files succeed with verified checksums. Incremental deploys of unchanged aspect artifacts are correctly skipped (fingerprints match S3-stored checksums).

## Constraints

- Aspect identity (which aspects an image will have) is determined by configuration (`mummy.image.withAspects`) and source file size threshold (`mummy.image.processThresholdFileSize`) — both evaluated during `createArtifact()`, after description loading.
- Aspect source files don't exist on disk. The `corporealSourceFile` of an aspect is the main image's source file. The aspect's source path (e.g., `example-preview.jpg`) is synthetic.
- Aspect `.description.turf` files are keyed by target path (e.g., `example-preview.jpg.description.turf`), which is derivable from the aspect ID and the main artifact's target path.
- The `DefaultAspectualSourceFileArtifact` constructor currently has no access to `MummyContext` or the description-loading machinery.
- There is an existing `TODO generalize within framework` on the aspect mummification loop in [DefaultImageMummifier.mummifyFile()](mummy/src/main/java/dev/guise/mummy/mummify/image/DefaultImageMummifier.java), indicating intent to move aspect mummification orchestration out of individual mummifiers.

## Acceptance Criteria

- Deploying a site with image aspects via `guise mummify` followed by a separate `guise deploy` succeeds without checksum errors.
- Aspect artifacts carry their own correct SHA-256 fingerprint in their in-memory description, not the main artifact's fingerprint.
- Incremental `guise deploy` of unchanged aspect artifacts is correctly skipped (no unnecessary re-uploads).
- Existing tests continue to pass; new tests verify aspect fingerprint correctness across separate plan/mummify/deploy invocations.

## Non-Goals

- Generalizing the aspect mummification framework (the `TODO generalize within framework`). This ticket fixes the fingerprint bug; framework generalization is a separate concern.
- Redesigning the description caching architecture. The fix should address the specific asymmetry for aspect artifacts without requiring a full overhaul of description loading/saving.
- Changing how the old `S3`/`S3Website` deploy targets handle fingerprints. The bug is in the artifact model, not in any specific deploy target.

## Guidance

### Orientation pointers

- **Aspect construction**: `DefaultAspectualSourceFileArtifact` constructor — [mummy/src/main/java/dev/guise/mummy/DefaultAspectualSourceFileArtifact.java](mummy/src/main/java/dev/guise/mummy/DefaultAspectualSourceFileArtifact.java).
- **Description loading**: `AbstractFileMummifier.plan()` and `loadArtifactDescription()` — [mummy/src/main/java/dev/guise/mummy/mummify/AbstractFileMummifier.java](mummy/src/main/java/dev/guise/mummy/mummify/AbstractFileMummifier.java).
- **Artifact creation (image)**: `DefaultImageMummifier.createArtifact()` — [mummy/src/main/java/dev/guise/mummy/mummify/image/DefaultImageMummifier.java](mummy/src/main/java/dev/guise/mummy/mummify/image/DefaultImageMummifier.java).
- **Aspect mummification loop**: `DefaultImageMummifier.mummifyFile()` at the `AspectualArtifact` check — same file.
- **Mummification dirty check and description save**: `AbstractFileMummifier.mummify()` — same as description loading file above.
- **Description saving**: `AbstractMummifier.saveTargetDescription()` — [mummy/src/main/java/dev/guise/mummy/mummify/AbstractMummifier.java](mummy/src/main/java/dev/guise/mummy/mummify/AbstractMummifier.java).
- **Cached description loading from target**: `AbstractMummifier.loadArtifactTargetDescription()` — same file.
- **Fingerprint used in Flange deployment**: `FlangeWebSite.ArtifactMetadataStrategy.toMetadata()` — [mummy/src/main/java/dev/guise/mummy/deploy/flange/FlangeWebSite.java](mummy/src/main/java/dev/guise/mummy/deploy/flange/FlangeWebSite.java).
- **Fingerprint used in legacy deployment**: `S3.put()` and `S3ArtifactDeployObject.findFingerprint()` — [mummy/src/main/java/dev/guise/mummy/deploy/aws/S3.java](mummy/src/main/java/dev/guise/mummy/deploy/aws/S3.java) and [mummy/src/main/java/dev/guise/mummy/deploy/aws/S3ArtifactDeployObject.java](mummy/src/main/java/dev/guise/mummy/deploy/aws/S3ArtifactDeployObject.java).
- **Flange S3 upload with checksum**: `S3Synchronizer.uploadFile()` — [flange/aws-s3-support/src/main/java/dev/flange/aws/s3/support/S3Synchronizer.java](flange/aws-s3-support/src/main/java/dev/flange/aws/s3/support/S3Synchronizer.java) (in the Flange workspace root).
- **Image aspect configuration**: `ImageMummifier.CONFIG_KEY_MUMMY_IMAGE_WITH_ASPECTS` — [mummy/src/main/java/dev/guise/mummy/mummify/image/ImageMummifier.java](mummy/src/main/java/dev/guise/mummy/mummify/image/ImageMummifier.java).
- **Default aspect settings** (preview: 600px max, thumbnail: 300px max): `GuiseMummy` default settings — [mummy/src/main/java/dev/guise/mummy/GuiseMummy.java](mummy/src/main/java/dev/guise/mummy/GuiseMummy.java).

### Discovery context from [GUISE-230]

This bug was discovered during [GUISE-230] Flange deployment integration. The error manifested when deploying a website containing large JPEG images with configured `"preview"` aspects. The sequence `guise clean && guise mummify && guise deploy` (separate invocations) triggered the S3 checksum rejection on every aspect artifact. The old `S3`/`S3Website` deploy targets masked the bug because they stored fingerprints as unvalidated custom S3 metadata.

[GUISE-230]: ../GUISE-230/
