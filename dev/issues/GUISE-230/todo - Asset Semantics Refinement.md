# TODO: Refine asset semantics to suppress content transformation, and add type-specific mummifiers.

During [GUISE-230] work on `GenericFileMummifier` and content-type detection, analysis revealed two related gaps in the Guise Mummy mummifier model: (1) the "asset" concept does not prevent content transformation, and (2) there is no type-specific mummifier for text files, leading to incorrect charset metadata.

## Asset Transformation Gap

The current asset designation (source filename convention, default `$` prefix) only suppresses page generation. When `DirectoryMummifier.plan()` encounters an asset file whose registered mummifier is a `PageMummifier`, it replaces the mummifier with the default file mummifier (currently `GenericFileMummifier`). All other registered mummifiers are used unchanged — including `DefaultImageMummifier`, which will scale, recompress, and strip metadata from large images (above the 800KB threshold).

This contradicts the web-authoring expectation that "asset" means "copy verbatim." A font file, a pre-optimized image, or a PDF placed in an asset directory should appear byte-for-byte identical in the output. Small images currently survive only because they fall below the size threshold — not by design.

### Proposed Approach

Discussed during [GUISE-230] and converged on a design:

1. **Record asset status on the artifact.** Add an `isAsset()` indication to `Artifact` (or use an `EnumSet<ArtifactDesignation>` if formalizing the full set of designations — veiled, asset, post, navigable). `DirectoryMummifier.plan()` already determines asset status; it would record this on the artifact rather than discarding it after mummifier selection.

2. **Keep the registered mummifier for assets.** Remove the mummifier-swapping logic in `DirectoryMummifier.plan()` (the `instanceof PageMummifier` check at ~line 174). A `.md` file in an asset tree keeps `MarkdownPageMummifier`; a `.png` keeps `DefaultImageMummifier`. The mummifier still performs identification (`getArtifactMediaType()`) and description (`loadSourceMetadata()`), providing correct media type and metadata.

3. **Suppress transformation in the mummifier base class.** `AbstractFileMummifier.mummify()` is already `final`. It would check asset status before calling `mummifyFile()`. For assets, it copies the file verbatim (as `OpaqueFileMummifier`/`GenericFileMummifier` do today) and skips the subclass transformation. The planning phase (`plan()` → `loadArtifactDescription()` → `getArtifactMediaType()`) is unaffected — identification and metadata extraction still run.

This means:
- Page mummifiers naturally stop generating pages for assets (their `mummifyFile()` is never called).
- Image mummifiers stop scaling/recompressing assets.
- Any future mummifier automatically respects asset semantics without special-case code.

### Key Code Locations

- `DirectoryMummifier.plan()` — asset mummifier-swapping logic: `mummy/src/main/java/dev/guise/mummy/mummify/collection/DirectoryMummifier.java` ~line 174.
- `AbstractFileMummifier.mummify()` — `final` mummification entry point with incremental checks: `mummy/src/main/java/dev/guise/mummy/mummify/AbstractFileMummifier.java` ~line 173.
- `AbstractFileMummifier.mummifyFile()` — the overridable transformation method that would be bypassed for assets: same file ~line 233.
- `Artifact` interface — where `isAsset()` (or designation enum) would be added: `mummy/src/main/java/dev/guise/mummy/Artifact.java`.

### Related: Artifact Designation Unification

The `isVeiled()` and `isAsset()` methods currently live on `PageMummifier` (computed on-the-fly from filename conventions), while `isNavigable()` and `isPost()` live on `Artifact`. If asset status is added to `Artifact`, consider also moving `isVeiled()` there — or introducing an `EnumSet<ArtifactDesignation>` to hold all such designations computed once during planning. This is a natural consolidation but not strictly required for the asset transformation fix.

## Text File Charset Gap

`GenericFileMummifier` maps `.txt` → `text/plain`, but RFC 2046 §4.1.2 specifies that the default charset for `text/*` is US-ASCII. Without a `charset` parameter, browsers and other consumers will misinterpret UTF-8 text files. This is a pre-existing gap (before `GenericFileMummifier`, `OpaqueFileMummifier` sent no media type at all), but `GenericFileMummifier` makes it more visible by providing a media type that implies the wrong encoding.

### Proposed Approach

Create a `TextFileMummifier` registered for text-oriented extensions (`.txt`, `.csv`, and potentially others like `.css`, `.js`, `.json`, `.xml` — though some of these have encoding defaults in their own specs). It would:

- Detect encoding (BOM detection, UTF-8 byte sequence validation).
- Produce the correct media type with charset parameter, e.g. `text/plain; charset=utf-8`.
- For non-assets: optionally normalize encoding to UTF-8 (a transformation that would be suppressed for assets under the refined asset semantics).
- For assets: still identify the correct media type with charset, but copy content verbatim.

This follows the principle that type-specific knowledge belongs in the type-specific mummifier, not in the generic fallback. `GenericFileMummifier` remains the default for truly unknown file types; `TextFileMummifier` handles files where encoding detection is meaningful.

## Documentation

The architecture document (`mummy/architecture.md`) has been updated during [GUISE-230] to clarify the current asset semantics (page-generation suppression only, no transformation suppression). When this work is implemented, the asset definition should be updated to reflect the stronger guarantee, and the `TextFileMummifier` should be added to the concrete mummifiers table.

[GUISE-230]: ../../GUISE-230/
