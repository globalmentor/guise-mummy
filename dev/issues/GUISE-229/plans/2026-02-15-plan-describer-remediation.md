# Plan: Remediate `PlanDescriber` Path Handling

**Ticket:** [GUISE-229]  
**Design:** [path-representation.md](../designs/path-representation.md)

## Context

`PlanDescriber`, introduced in the [initial --describe implementation](2026-02-14-describe-flag.md), uses ad-hoc string manipulation (`replace('\\', '/')`, `isDirectory` boolean) for path display and `altLocation` resolution. This violates the codebase's established path/URI model documented in the [path-representation design](../designs/path-representation.md): filesystem paths translate to URIs exclusively via `Path.toUri()`, collection semantics come from `instanceof CollectionArtifact` + `toCollectionURI()`, and `altLocation` is a URI path reference processed in the URI domain.

This plan brings `PlanDescriber` into alignment with the existing model by treating it as a greenfield rewrite — the correct design from scratch, not a patch of the existing mistakes.

## Scope

All changes are confined to `PlanDescriber.java`, `PlanDescriberTest.java`, and the `PlanDescriber` constructor call in `GuiseMummy.java`. No new classes or public APIs are introduced.

## Steps

### 1. Change `PlanDescriber` constructor to accept a `URI` site target root

**File:** `mummy/src/main/java/dev/guise/mummy/PlanDescriber.java`

The current constructor takes `Path projectDirectory`, which is semantically wrong: `PlanDescriber` needs the site target root to relativize artifact target paths into site-relative resource references — the same `rootTargetPathUri` parameter used by `S3.plan()`.

Replace the constructor:

```java
// BEFORE
private final Path projectDirectory;

public PlanDescriber(@NonNull final MummyPlan plan, @NonNull final Path projectDirectory) {
    this.plan = requireNonNull(plan);
    this.projectDirectory = requireNonNull(projectDirectory);
}
```

```java
// AFTER
private final URI rootTargetPathUri;

/// Constructor.
/// @param plan The mummy plan to describe.
/// @param rootTargetPathUri The URI form of the root artifact target path, used for relativizing artifact paths into
///        site-relative resource references.
public PlanDescriber(@NonNull final MummyPlan plan, @NonNull final URI rootTargetPathUri) {
    this.plan = requireNonNull(plan);
    this.rootTargetPathUri = requireNonNull(rootTargetPathUri);
}
```

The terminology `rootTargetPathUri` matches `S3.plan()` and `S3Website.planResource()` exactly.

### 2. Replace `toDisplayPath()` with URI-domain resource reference computation

**File:** `mummy/src/main/java/dev/guise/mummy/PlanDescriber.java`

Delete `toDisplayPath()` entirely. It conflates filesystem paths with display paths using string manipulation.

In its place, introduce a private helper that produces a site-relative `URIPath` for any artifact, following the same pattern as `S3.plan()`:

```java
/// Returns the site-relative resource reference for the given artifact.
/// @implSpec This follows the same pattern as [S3#plan(MummyContext, URI, Artifact)]:
///           convert to URI via [Path#toUri()], apply [URIs#toCollectionURI(URI)] for collection artifacts,
///           then relativize against the site target root.
/// @param artifact The artifact whose resource reference to compute.
/// @return The site-relative resource reference as a URI path (e.g. `foo/bar/` for a collection, `foo/page.html` for a file).
private URIPath resourceReference(@NonNull final Artifact artifact) {
    final URI artifactTargetPathUri = artifact.getTargetPath().toUri();
    return URIPath.relativize(rootTargetPathUri,
            artifact instanceof CollectionArtifact ? toCollectionURI(artifactTargetPathUri) : artifactTargetPathUri);
}
```

The display form is `"/" + resourceReference.toString()`. The leading `/` is justified here because the resource reference is site-relative and its display as an absolute-path reference (rooted at the site) is the correct semantic for human output. It is a `URIPath` string, not a fake filesystem path.

### 3. Replace `collectRedirect()` with URI-domain `altLocation` processing

**File:** `mummy/src/main/java/dev/guise/mummy/PlanDescriber.java`

The current `collectRedirect()` treats `altLocation` as a filesystem path string: it resolves it against a filesystem `Path` parent directory. This is wrong — `altLocation` is a URI path reference.

Rewrite to follow the `S3Website.planResource()` pattern exactly:

```java
/// If the artifact declares a `mummy/altLocation`, adds a [RedirectEntry] to the list.
/// @implSpec This follows the same URI processing chain as [S3Website#planResource]:
///           parse as [URIPath], resolve against the artifact's target path URI (in collection form for collection
///           artifacts), relativize against the site root, and check for site-boundary violations.
/// @param artifact The artifact to check.
/// @param redirects The list to which any redirect entry is added.
private void collectRedirect(@NonNull final Artifact artifact, @NonNull final List<RedirectEntry> redirects) {
    artifact.getResourceDescription().findPropertyValue(PROPERTY_TAG_MUMMY_ALT_LOCATION)
            .filter(CharSequence.class::isInstance)
            .map(Object::toString)
            .map(URIPath::of) // parse as URI path reference, not filesystem path
            .map(altLocationReference -> { // resolve to absolute file URI, applying collection form for collection artifacts
                final URI artifactTargetUri = artifact.getTargetPath().toUri();
                return resolve(artifact instanceof CollectionArtifact ? toCollectionURI(artifactTargetUri) : artifactTargetUri,
                        altLocationReference);
            })
            .map(altLocationUri -> URIPath.relativize(rootTargetPathUri, altLocationUri)) // relativize to site root
            .ifPresent(altLocationReference -> {
                final URIPath targetReference = resourceReference(artifact);
                final Optional<PlanWarning> optionalWarning = altLocationReference.isSubPath()
                        ? Optional.empty() : Optional.of(PlanWarning.REDIRECT_OUTSIDE_SITE);
                redirects.add(new RedirectEntry(altLocationReference, targetReference,
                        artifact instanceof CollectionArtifact, optionalWarning));
            });
}
```

Key differences from the current code:

- `altLocation` is parsed as `URIPath::of`, not used as a filesystem path string.
- Resolution uses `URIs.resolve(URI, URIPath)` in the URI domain, not `Path.resolve()`.
- The resolution base applies `toCollectionURI()` for collection artifacts — critical because during PLAN phase, target directories don't yet exist on disk, so `Path.toUri()` won't add a trailing slash. Without this, URI resolution would incorrectly strip the last segment of the path.
- Relativization uses `URIPath.relativize(rootTargetPathUri, …)`, not `Path.relativize()`.
- The `boolean isDirectory` parameter is gone — replaced by `artifact instanceof CollectionArtifact`.
- The method is now an instance method (not `static`), since it references `rootTargetPathUri` and `resourceReference()`.
- After relativizing, `isSubPath()` detects alt locations that escape the site boundary. These are still recorded (so the user sees them in output) but flagged with a `PlanWarning`.

### 4. Add `PlanWarning` enum and update `RedirectEntry`

**File:** `mummy/src/main/java/dev/guise/mummy/PlanDescriber.java`

Add a warning enum for diagnostic annotations on plan entries:

```java
/// A diagnostic warning that can be attached to plan entries.
/// @param marker The short marker displayed inline (e.g. `[!]`).
/// @param description The legend description displayed at the bottom of the output.
enum PlanWarning {
    /// The redirect's alternate location resolves outside the site boundary.
    REDIRECT_OUTSIDE_SITE("[!]", "redirect target is outside the site boundary");
    private final String marker;
    private final String description;
    PlanWarning(final String marker, final String description) {
        this.marker = marker;
        this.description = description;
    }
    String marker() { return marker; }
    String description() { return description; }
}
```

The current `RedirectEntry` record stores pre-rendered `String` values, losing type information:

```java
// BEFORE
record RedirectEntry(String sourcePath, String targetPath, boolean collection)
```

Replace with properly typed fields, rename to match terminology, and add warning:

```java
/// A redirect entry pairing the old alternate location reference with the artifact's current resource reference.
/// @param altLocationReference The alternate (old) site-relative resource reference that triggers the redirect.
/// @param resourceReference The artifact's current site-relative resource reference where the redirect sends the request.
/// @param collection Whether this is a collection (directory) redirect.
/// @param optionalWarning A diagnostic warning, if any.
record RedirectEntry(URIPath altLocationReference, URIPath resourceReference, boolean collection,
        Optional<PlanWarning> optionalWarning) implements Comparable<RedirectEntry> {
    /// Validation constructor.
    RedirectEntry {
        requireNonNull(altLocationReference);
        requireNonNull(resourceReference);
        requireNonNull(optionalWarning);
    }
    /// @implSpec Collections sort before non-collections; within each group, entries are sorted alphabetically
    ///           by alternate location reference.
    @Override
    public int compareTo(@NonNull final RedirectEntry other) {
        if(this.collection != other.collection) {
            return this.collection ? -1 : 1;
        }
        return this.altLocationReference.toString().compareTo(other.altLocationReference.toString());
    }
}
```

Naming rationale:

- `sourcePath` / `targetPath` → `altLocationReference` / `resourceReference`. The old names clash with the source-tree/target-tree meaning of "source" and "target" used throughout the codebase. The new names match `S3Website` terminology exactly.
- `URIPath` instead of `String` preserves type safety and collection semantics (`URIPath.isCollection()`).
- `Optional<PlanWarning> optionalWarning` follows the record optional-field convention.

### 5. Update `describeTo()` to use the new APIs

**File:** `mummy/src/main/java/dev/guise/mummy/PlanDescriber.java`

Update the source display and redirect output:
**Post counting:** `isPost()` is an orthogonal dimension — a post can be a page or a collection. A separate `postCount` counter is incremented for any artifact where `artifact.isPost()` returns `true`, independently of the mummifier-type classification. The count is displayed as a `Posts:` sub-line nested under the artifact counts.
**Source display:** The current code relativizes the root artifact's source path against the project directory using filesystem `Path.relativize()`, then feeds it through `toDisplayPath()`. The source path is a filesystem concept (where the content lives on disk), so displaying it as a project-relative filesystem path is appropriate for human output. However the conversion to forward slashes must use `Path.toUri()` rather than `replace('\\', '/')`.

Compute it as:

```java
final Path siteSourceDirectory = rootArtifact.getSourcePath();
final URIPath sourceReference = URIPath.relativize(projectDirectory.toUri(), toCollectionURI(siteSourceDirectory.toUri()));
final String sourceDisplay = "/" + sourceReference;
```

But this requires keeping `projectDirectory` in the constructor, which we removed in Step 1. This raises a design question: what is the `Source:` line actually _for_?

It tells the user where their content lives relative to the project. The simplest correct approach: compute it from the plan itself. The root artifact's source path _is_ the site source directory. Since the plan already carries this, and PlanDescriber already has the plan, we can display the source tree root as an absolute filesystem path (exactly how the codebase logs paths elsewhere), or relative to the project directory.

**Decision:** Pass the site source directory as a separate `Path` parameter (not the project directory). Display it as returned by `Path.toString()` — a platform-native filesystem path, which is how the codebase represents filesystem locations in logging and user output. This is honest: "here is where your source files are on disk." No URI conversion needed since this is a filesystem display, not a web reference.

Revised constructor (amending Step 1):

```java
private final URI rootTargetPathUri;
private final Path siteSourceDirectory;

/// Constructor.
/// @param plan The mummy plan to describe.
/// @param rootTargetPathUri The URI form of the root artifact target path, used for relativizing artifact paths into
///        site-relative resource references.
/// @param siteSourceDirectory The site source directory, displayed in the summary header.
public PlanDescriber(@NonNull final MummyPlan plan, @NonNull final URI rootTargetPathUri, @NonNull final Path siteSourceDirectory) {
    this.plan = requireNonNull(plan);
    this.rootTargetPathUri = requireNonNull(rootTargetPathUri);
    this.siteSourceDirectory = requireNonNull(siteSourceDirectory);
}
```

The source display line becomes:

```java
appendable.append("  %-14s%s\n".formatted("Source:", siteSourceDirectory));
```

**Post count in artifact walk:** Add a `postCount` counter to the walk loop, incremented after the mummifier classification:

```java
// inside the walk loop, after the if/else-if classification chain:
if(artifact.isPost()) {
    postCount++;
}
```

And add the `Posts:` sub-line under the artifact counts:

```java
appendable.append("    %-14s%d\n".formatted("Posts:", postCount));
```

**Redirect detail lines** use `URIPath.toString()` directly, prepending `/` for display as site-absolute paths. The `→` character is replaced with ASCII `->` for Windows terminal compatibility. If a warning is present, the marker is appended after the target path:

```java
for(final RedirectEntry redirect : redirects) {
    final String warningMarker = redirect.optionalWarning().map(w -> " " + w.marker()).orElse("");
    appendable.append("    /%s -> /%s%s\n".formatted(
            redirect.altLocationReference(), redirect.resourceReference(), warningMarker));
}
```

**Warning count in summary:** The redirect summary section (always shown, regardless of verbose) includes a `Warnings:` line when any redirects have warnings. This ensures the user is alerted even in non-verbose mode:

```java
if(warningCount > 0) {
    appendable.append("    %-14s%d [!]\n".formatted("Warnings:", warningCount));
}
```

This produces output like:

```
  Artifacts:    147
    Pages:        42
    Collections:  15
    Images:       68
    Other:        22
    Posts:        7
  Redirects:    3
    Collection:   1
    Page:         2
    Warnings:     1 [!]
```

Counts are left-aligned (all starting at the same column via the fixed-width `%-14s` label format). Sub-counts nest with additional indentation.

**Legend:** After the redirect details (verbose only) or after the summary (non-verbose, when warnings exist), append a legend section listing each distinct warning type:

```java
final EnumSet<PlanWarning> warnings = EnumSet.noneOf(PlanWarning.class);
redirects.stream().flatMap(r -> r.optionalWarning().stream()).forEach(warnings::add);
if(!warnings.isEmpty()) {
    appendable.append("\n");
    for(final PlanWarning warning : warnings) {
        appendable.append("  %s %s\n".formatted(warning.marker(), warning.description()));
    }
}
```

The legend is emitted regardless of verbose mode — if there are warnings, the user always sees the legend explaining `[!]`.

**The `siteTargetRoot` local variable** is removed. The `rootTargetPathUri` field replaces it.

### 6. Update `collectRedirect()` call site in `describeTo()`

**File:** `mummy/src/main/java/dev/guise/mummy/PlanDescriber.java`

The call changes from `collectRedirect(artifact, siteTargetRoot, redirects)` to `collectRedirect(artifact, redirects)`, since `rootTargetPathUri` is now a field.

### 7. Update the constructor call in `GuiseMummy.java`

**File:** `mummy/src/main/java/dev/guise/mummy/GuiseMummy.java`

Current call (line 302):

```java
new PlanDescriber(plan, context.getProject().getDirectory()).describeTo(System.out, isVerbose());
```

The root target path URI comes from `toCollectionURI(rootArtifact.getTargetPath().toUri())`. The `toCollectionURI()` call is **critical**: during PLAN phase the target directory does not yet exist on disk, so `Path.toUri()` omits the trailing slash (e.g. `file:///C:/project/target/site` instead of `file:///C:/project/target/site/`). Without `toCollectionURI()`, every `URIPath.relativize()` call would treat `site` as a filename and relativize against the parent — producing wrong paths for every artifact.

Note: `S3.plan()` does _not_ apply `toCollectionURI()` to the root, because it runs during DEPLOY phase when directories exist. `PlanDescriber` runs during PLAN phase and must apply it explicitly.

The site source directory comes from `context.getSiteSourceDirectory()` — the canonical configuration-derived path that `DirectoryMummifier.plan()` was called with (line 297). While `rootArtifact.getSourcePath()` returns the same value, `context.getSiteSourceDirectory()` makes the provenance explicit:

```java
new PlanDescriber(plan, toCollectionURI(rootArtifact.getTargetPath().toUri()), context.getSiteSourceDirectory()).describeTo(System.out, isVerbose());
```

### 8. Update imports in `PlanDescriber.java`

**File:** `mummy/src/main/java/dev/guise/mummy/PlanDescriber.java`

Remove:

- `java.nio.file.Path` — no longer used (except for `siteSourceDirectory` display; still needed)

Add:

- `import static com.globalmentor.net.URIs.*;` — for `toCollectionURI()` and `resolve()`
- `import java.net.URI;`
- `import com.globalmentor.net.URIPath;`

Actually `Path` is still needed for `siteSourceDirectory`. The full import list:

```java
import static com.globalmentor.net.URIs.*;
import static dev.guise.mummy.Artifact.*;
import static java.util.Objects.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;

import org.jspecify.annotations.*;

import com.globalmentor.net.URIPath;
import dev.guise.mummy.mummify.image.ImageMummifier;
import dev.guise.mummy.mummify.page.PageMummifier;
```

### 9. Rewrite `PlanDescriberTest.java`

**File:** `mummy/src/test/java/dev/guise/mummy/PlanDescriberTest.java`

The tests need to adapt to the new constructor signature and the URI-based path handling. Key changes:

**Constructor calls:** Replace `new PlanDescriber(plan, PROJECT_DIRECTORY)` with `new PlanDescriber(plan, toCollectionURI(TARGET_DIRECTORY.toUri()), SOURCE_DIRECTORY)`. The `toCollectionURI()` is mandatory for the same reason as in Step 7: `TARGET_DIRECTORY` is a constant `Path` that does not exist on disk, so `.toUri()` omits the trailing slash.

**`testToDisplayPath`:** Delete. The method no longer exists.

**`testRedirectEntrySorting`:** Update to use `URIPath` fields and include `Optional.empty()` for the warning parameter:

```java
final var collectionB = new PlanDescriber.RedirectEntry(URIPath.of("beta/"), URIPath.of("new-beta/"), true, Optional.empty());
final var collectionA = new PlanDescriber.RedirectEntry(URIPath.of("alpha/"), URIPath.of("new-alpha/"), true, Optional.empty());
final var pageB = new PlanDescriber.RedirectEntry(URIPath.of("b-page.html"), URIPath.of("new-b.html"), false, Optional.empty());
final var pageA = new PlanDescriber.RedirectEntry(URIPath.of("a-page.html"), URIPath.of("new-a.html"), false, Optional.empty());
```

**Source display assertion:** The source path will now be the native filesystem `Path.toString()`, not the `/src/site/` URI-style form. Update `testDescribeToShowsRelativeSourcePath` to assert the actual `SOURCE_DIRECTORY.toString()` value:

```java
assertThat("source path is the site source directory", output.toString(), containsString("Source:       " + SOURCE_DIRECTORY));
```

**Redirect output assertions:** The `testDescribeToVerboseShowsRedirectDetails` assertion changes from `/old-page.html → /new-page.html` to use `->`. The resolution of `../old-page.html` against `{TARGET_DIRECTORY}/new-page.html` will yield `old-page.html` relative to the target root, so the output becomes `/old-page.html -> /new-page.html` — the same content, but now computed correctly through the URI chain rather than coincidentally matching via filesystem string hacks.

**Post count test:** Add a test that creates artifacts with `isPost()` returning `true` (both pages and collections) and verifies the `Posts:` count line appears in the output with the correct value.

**Warning tests:** Add a test that verifies a redirect with an `altLocation` that escapes the site boundary (e.g. `../../outside.html`) produces the `[!]` marker in verbose output and includes the legend line.

**Import changes:** Add `import com.globalmentor.net.URIPath;` and `import java.util.Optional;`. Remove the `OperatingSystem` import if `getTempDirectory()` is no longer needed — but it is still used for `PROJECT_DIRECTORY`, so keep it.

### 10. Verify

- [ ] `mvn test -pl mummy` — unit tests pass
- [ ] `mvn verify -pl mummy` — integration tests pass  
- [ ] Manual: run `guise plan --describe` on `demo-basic` and confirm output is correct on Windows (backslash in filesystem source, forward slash in resource references, `->` arrow)
- [ ] Confirm no `replace('\', '/')`, `boolean isDirectory`, or `→` remains in `PlanDescriber`
- [ ] Verify out-of-site redirect displays `[!]` marker and legend in verbose output

## Deferred Reporting Categories

The following artifact categories were researched for inclusion in plan description output but are deferred due to model limitations. The reasons are documented here to avoid re-research.

### Assets

Asset status is **not persisted on the `Artifact` after planning**. The detection is transient: `DirectoryMummifier.plan()` checks the source filename against `CONFIG_KEY_MUMMY_ASSET_NAME_PATTERN` (default `$(.*)`) and downgrades a `PageMummifier` to the default opaque mummifier (lines 163-174 of `DirectoryMummifier.java`). There is no `isAsset()` method on `Artifact`. Reporting asset count would require either a model change (adding an `isAsset()` flag set during planning) or passing `MummyContext` to `PlanDescriber` so it can re-check the pattern — neither is appropriate for a display-only feature.

### Veiled Artifacts

`Artifact.isNavigable()` is **incomplete for directory artifacts**. `DirectoryArtifact.isNavigable()` always returns `true`, even for directories whose source filename matches the veil pattern `_(...)`. Only `AbstractSourceFileArtifact` stores a computed `isNavigable` boolean (set from `GuiseMummy.CONFIG_KEY_MUMMY_VEIL_NAME_PATTERN`). Reporting veiled-artifact count would undercount because all veiled directories would be missed. A model fix is needed first.

### Collections with Content

`DirectoryArtifact.findContentArtifact()` works, but phantom content files are **auto-generated during planning** for directories that have no explicit content source file. This means nearly all directories have content (only asset directories don't), making the count misleading. A useful "collections with authored content" count would require distinguishing phantom from authored content artifacts, which the model does not currently expose. This becomes useful only after asset detection is available (to exclude asset directories).

### Subsumed Artifacts

Subsumed status is derivable (an artifact is subsumed when another artifact's `altLocation` points at its path). However, a simple count is not actionable for users — they would need to know _which_ artifacts are subsumed and _by what_, which is a report-level feature beyond the scope of the current summary/verbose output modes.

## Alternatives Considered

### Keep `projectDirectory` and convert to URI for source display

Instead of showing the raw filesystem `Path` for the source directory, convert `projectDirectory` to URI, relativize the source path against it, and display the result as a URI path. This was rejected because the source display is not a web resource reference — it's a filesystem location. Showing a native filesystem path is honest and matches how the rest of the codebase logs paths.

### Pass `MummyContext` to `PlanDescriber`

Instead of extracting `rootTargetPathUri` and `siteSourceDirectory` into the constructor, pass the whole context. This was rejected because `PlanDescriber` only needs two values, and coupling it to the full context would violate interface segregation and make testing harder.

### Derive `rootTargetPathUri` from `plan.getRootArtifact()` inside PlanDescriber

Instead of passing `rootTargetPathUri` as a constructor parameter, compute it from `plan.getRootArtifact().getTargetPath().toUri()` inside `PlanDescriber`. This is tempting but violates the principle of making dependencies explicit. `S3.plan()` also receives `rootTargetPathUri` as a parameter rather than computing it internally, establishing the precedent. The parameter also allows the caller to apply `toCollectionURI()` if desired, though for the root artifact this is always a collection.

**Counter-argument:** The root artifact in a `MummyPlan` is always a `CollectionArtifact` (it's a `DirectoryArtifact`), so `toCollectionURI(rootArtifact.getTargetPath().toUri())` is deterministic. Computing it internally would reduce the constructor to `(plan, siteSourceDirectory)`. This is a reasonable simplification if `PlanDescriber` never needs a non-collection root — which it doesn't.

**Resolution:** Accept the explicit parameter for now. The caller is responsible for applying `toCollectionURI()` — see Step 7. Unlike `S3.plan()`, which runs during DEPLOY phase when directories exist on disk (so `Path.toUri()` already adds the trailing slash), `PlanDescriber` runs during PLAN phase when they don't, making `toCollectionURI()` mandatory at the call site.

## Resolved Questions

- **What should `Source:` display?** The site source directory as a platform-native filesystem path via `context.getSiteSourceDirectory()` — the canonical configuration-derived path. This is the honest answer: "this is where your files are on disk."
- **Should redirect paths show a leading `/`?** Yes. The `URIPath` resource references are site-relative (e.g. `old-page.html`). Prepending `/` for display forms an absolute-path reference rooted at the site — the intuitive form for a user ("this is `/old-page.html` on your site"). This is a display choice in the output formatter, not baked into the `URIPath` values.
- **What arrow character for redirect display?** ASCII `->` instead of Unicode `→`, for Windows terminal compatibility.
- **How to handle out-of-site redirects?** Display them (so the user sees the problem) but annotate with `[!]` inline marker and a legend at the bottom of the output. Detected via `URIPath.isSubPath()` after relativization against the site root.

[GUISE-229]: ../../GUISE-229/
