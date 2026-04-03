# [GUISE-231] Summary: Fix stale fingerprints in aspect artifacts.

## Problem

Image aspect artifacts (e.g. thumbnails, previews) carried the **main** image's SHA-256 fingerprint in their in-memory descriptions instead of their own. This was latent under the old S3 deploy targets, which stored the fingerprint as opaque custom metadata that S3 never validated. Flange's use of S3's native `checksumSHA256` validation surfaced the bug as a fatal upload error. Any `guise deploy` invoked separately from `guise mummify` would fail for aspect artifacts.

## Root Cause

A structural asymmetry between description loading and description saving. During mummification, each aspect's `.description.turf` is saved with the correct fingerprint. But during planning, only the main image goes through `AbstractFileMummifier.plan()` → `loadArtifactDescription()`. Aspect artifacts were constructed inside `DefaultAspectualSourceFileArtifact`'s constructor by copying all properties from the main artifact's description — including the main artifact's fingerprint. The aspect `.description.turf` files were written but never read back.

## Fix

The fix addresses two separate concerns: loading correct descriptions for aspects, and ensuring aspects are regenerated when their parent changes.

**Aspect description loading.** Aspect artifact construction was moved from `DefaultAspectualSourceFileArtifact` into `DefaultImageMummifier.createArtifact()`, where each aspect's cached description is loaded independently via `loadArtifactDescription()` using the main image as the source file and the aspect's target path. This closes the load/save asymmetry. `DefaultSourceFileArtifact.Builder` was updated to accept pre-built aspect artifacts (`withAspectArtifacts(Map)`) instead of bare aspect IDs, and the aspectual artifact constructor was simplified to a single-arg builder pull.

**Forced aspect regeneration.** A boolean `invariably` parameter was added to `Mummifier.mummify()` at the interface level. When `true`, it bypasses incremental dirty-checking and guarantees regeneration, while still executing all orchestration (directory creation, description saving, fingerprint computation). The parameter propagates monotonically to comprised artifacts — a mummifier may escalate from `false` to `true` but must not downgrade. `DefaultImageMummifier.mummifyFile()` passes `invariably = true` when mummifying aspects, replacing the old `removeProperty(MODIFIED_AT_PROPERTY_TAG)` workaround. `DirectoryMummifier` propagates the flag to children.

## Testing and Verification

No automated regression test was added. The bug manifests in the in-memory artifact description during `plan()`, which is not observable from integration tests that inspect the file system — the on-disk `.description.turf` always has the correct fingerprint because `AbstractFileMummifier.mummify()` recomputes it from the target file. A meaningful automated test would require either a page template that renders the aspect fingerprint, or a unit test of `createArtifact()` with an incremental context and seeded description files. The fix was manually verified against the production project that originally surfaced the bug.

## Deferred

- The `TODO set aspect ID` comment suggests a future first-class aspect-ID field on the artifact or builder, rather than relying on a description property. Preserved for future consideration.
- The `TODO generalize within framework` on the aspect mummification loop in `DefaultImageMummifier.mummifyFile()` indicates intent to move aspect mummification orchestration into the framework. Out of scope for this ticket.
