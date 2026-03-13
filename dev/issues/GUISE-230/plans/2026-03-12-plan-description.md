# Plan: Extract `PlanSummary` from `PlanDescriber`

Refactor `PlanDescriber` to separate artifact tree analysis from textual formatting by extracting an immutable `PlanSummary` statistics record. `PlanDescriber` retains two roles: (1) **summarization** — walking the artifact tree (via `ArtifactTreeWalker`) and producing a `PlanSummary`; and (2) **description-writing** — formatting the summary (and other display inputs such as the site source directory) into human-readable output. `PlanSummary` is a pure data carrier holding the accumulated statistics and redirect inventory. A nested `PlanSummary.Builder` serves as the mutable accumulator during the walk.

Both `PlanDescriber` and `PlanSummary` are placed in a new `dev.guise.mummy.plan` subpackage, following the established phase-based convention in which subdirectories correspond to subpackages by operational phase: `mummify/` → build, `deploy/` → deployment, and `plan/` → plan analysis. The plan model types (`MummyPlan`, etc.) remain in the root package — they are model types, just as `Artifact` stays in the root package even though `Mummifier` lives in `dev.guise.mummy.mummify`.

## Overview

**Single chunk** — all steps compile and test at every boundary. No shared interfaces are modified in incompatible ways; the `PlanDescriber` public API changes are additive until the final step, which rewires the single call site.

- Step 1: Create `PlanSummary` record with `Builder`, `RedirectEntry`, `PlanWarning` (new file in `plan/`; pure data, no formatting)
- Step 2: Move `PlanDescriber` to `plan/` package; refactor — add `summarize()` returning `PlanSummary`, add `writeTo()` for formatting, add `findRedirect()`, replace manual walk with `ArtifactTreeWalker`
- Step 3: Update call site in `GuiseMummy.mummify()` — use `describeTo()` convenience or `summarize()` + `writeTo()`
- Step 4: Update `PlanDescriberTest` — adapt existing tests, move to `plan/` package
- Step 5: Add `PlanSummaryTest` in `plan/` — direct tests of builder accumulation

**Notable decisions:**

- **`PlanSummary` and `PlanDescriber` in `dev.guise.mummy.plan`**, following the phase-based subpackage convention established by `dev.guise.mummy.mummify` and `dev.guise.mummy.deploy`. `PlanSummary` is a top-level class, not nested in `PlanDescriber` — it is a first-class concept (the reified result of plan analysis) and may be consumed independently (e.g. by a deploy target needing redirect counts). Top-level also avoids `PlanDescriber.PlanSummary` stutter. The new `plan` subpackage groups plan-phase operations together and declutters the root package (currently 24 classes).
- **`PlanDescriber` has two roles: summarization and description-writing.** `summarize()` produces a `PlanSummary`; `writeTo()` formats the summary (and other display inputs) as human-readable text. This keeps the formatting close to the data producer and allows future expansion (e.g. adding build duration or configuration profile to the description output without polluting the summary record).
- **`RedirectEntry` and `PlanWarning` are nested in `PlanSummary`**, not `PlanDescriber`. They are part of the summary's data model, not the describer's orchestration. Test references change from `PlanDescriber.RedirectEntry` to `PlanSummary.RedirectEntry`.
- **`findRedirect()` stays on `PlanDescriber`** as a package-private static method. It is analysis logic — examining an artifact's properties and deciding whether a redirect exists — not data modeling. It returns `Optional<RedirectEntry>`, eliminating the old mutable-list-as-output-parameter pattern. It takes `rootTargetPathUri` as a parameter (pure function, no instance state needed).
- **Builder exposes typed increment/add methods; the visitor implementation in `PlanDescriber` contains the classification logic.** The builder is a statistics accumulator — it knows how to count, not what to count. The visitor implementation decides whether an artifact is a page, image, collection, etc. and calls the appropriate builder method. This keeps the builder simple and testable (feed it numbers, check the result) and puts the domain logic — which depends on `PageMummifier`, `ImageMummifier`, etc. — in the `PlanDescriber` visitor, where it belongs. The alternative (a single `builder.addArtifact(artifact)` that internalizes classification) would couple the statistics holder to mummifier types.
- **`totalCount` derived, not tracked.** `totalCount = pageCount + collectionCount + imageCount + otherCount`. The builder does not have an `incrementTotalCount()` method; the total is computed in `build()`. This eliminates the possibility of the total diverging from the components. `postCount` is orthogonal (an artifact can be both a page and a post), tracked separately.
- **`siteSourceDirectory` is a parameter to `writeTo()`, not a field of `PlanSummary` or `PlanDescriber`.** It is a display concern — the directory label in the header — not part of the analysis result. Keeping it out of the record means `PlanSummary` is a pure analysis product; keeping it out of `PlanDescriber` means it is provided at write time, not construction time.
- **Subsumed artifacts are skipped entirely** in the visitor. The current `PlanDescriber` walks `CollectionArtifact.getChildArtifacts()`, which excludes subsumed content artifacts. The walker walks `comprisedArtifacts()` (which includes them). The visitor must skip subsumed artifacts for both counting and redirect extraction to maintain identical output. This is correct for redirects (avoids the double-redirect bug — see `plans/2026-03-10-s3-collection-artifact-semantics.md`). The skipping is in the visitor lambda, not in the builder — the builder has no concept of subsumption.
- **`PlanDescriber.describeTo()` retained as a convenience.** After the refactoring, `describeTo(appendable, siteSourceDirectory, verbose)` internally calls `writeTo(appendable, siteSourceDirectory, summarize(), verbose)`. The call site in `GuiseMummy.mummify()` can use either the convenience method or the two-step form.

### Alternatives considered

- **`writeTo()` on `PlanSummary` (previously `PlanDescription`).** The data record would format itself. Rejected: the description is not just a function of the summary data — it also takes display-only inputs like `siteSourceDirectory` and may take more in the future. The `PlanDescriber` is the natural home for formatting since it is the "plan describer".
- **`findRedirect()` on `PlanSummary`.** Keeps the `RedirectEntry` factory with its type. Rejected: `findRedirect()` is analysis logic (examining artifact properties, resolving URIs), not data modeling. It belongs with the summarization role in `PlanDescriber`.
- **`collectRedirect()` mutating a list.** The original API pattern. Rejected in favor of `findRedirect()` returning `Optional<RedirectEntry>`, which is a pure function — no mutable-list-as-output-parameter.
- **`PlanDescriber.describeTo()` taking `siteSourceDirectory` in the constructor.** The original API. Rejected: `siteSourceDirectory` is a display concern, not a summarization input. Providing it at write time is cleaner and eliminates the redundancy of passing it to both the constructor and `writeTo()`.
- **Naming the data class `PlanDescription`.** Rejected: a "description" is the formatted text, not the data behind it. `PlanSummary` accurately reflects what the object is — accumulated statistics from plan analysis.

---

## Step 1: `PlanSummary` Record with `Builder`

### Location

New file: `mummy/src/main/java/dev/guise/mummy/plan/PlanSummary.java`

### Design

`PlanSummary` is a top-level record in `dev.guise.mummy.plan` holding the immutable analysis result. It is a pure data carrier — no formatting or analysis methods.

#### Record fields

```java
public record PlanSummary(
    long pageCount,
    long collectionCount,
    long imageCount,
    long otherCount,
    long postCount,
    List<RedirectEntry> sortedRedirects
)
```

#### Derived accessors

- `totalCount()` — returns `pageCount + collectionCount + imageCount + otherCount`.
- `redirectCollectionCount()` — computed from `sortedRedirects`.
- `redirectPageCount()` — `sortedRedirects.size() - redirectCollectionCount()`.
- `warningCount()` — count of entries with a present `optionalWarning`.

These are methods on the record, not stored fields. They are cheap to compute (small lists), and storing them would create redundancy.

#### `PlanWarning` enum

Moved from `PlanDescriber`. No changes to the enum itself.

```java
enum PlanWarning {
    REDIRECT_OUTSIDE_SITE("[!]", "redirect target is outside the site boundary");
    ...
}
```

#### `RedirectEntry` record

Moved from `PlanDescriber`. No changes to the record itself.

```java
record RedirectEntry(URIPath altLocationReference, URIPath resourceReference, boolean collection,
        Optional<PlanWarning> optionalWarning) implements Comparable<RedirectEntry> { ... }
```

#### `Builder` nested class

Mutable accumulator with typed methods:

```java
public static final class Builder {
    private long pageCount;
    private long collectionCount;
    private long imageCount;
    private long otherCount;
    private long postCount;
    private final List<RedirectEntry> redirects = new ArrayList<>();

    Builder() { }

    public void incrementPageCount() { pageCount++; }
    public void incrementCollectionCount() { collectionCount++; }
    public void incrementImageCount() { imageCount++; }
    public void incrementOtherCount() { otherCount++; }
    public void incrementPostCount() { postCount++; }
    public void addRedirect(RedirectEntry entry) { redirects.add(entry); }

    public PlanSummary build() {
        final List<RedirectEntry> sorted = redirects.stream().sorted().toList();
        return new PlanSummary(pageCount, collectionCount, imageCount, otherCount, postCount, sorted);
    }
}
```

Static factory on `PlanSummary`:

```java
public static Builder builder() { return new Builder(); }
```

#### Validation constructor

```java
PlanSummary {
    if(pageCount < 0 || collectionCount < 0 || imageCount < 0 || otherCount < 0 || postCount < 0) {
        throw new IllegalArgumentException("Counts must not be negative.");
    }
    sortedRedirects = List.copyOf(sortedRedirects);
}
```

---

## Step 2: Move and Refactor `PlanDescriber`

### Package move

`PlanDescriber` moves from `dev.guise.mummy` to `dev.guise.mummy.plan`. Use `git mv` to preserve history:

```sh
git mv mummy/src/main/java/dev/guise/mummy/PlanDescriber.java mummy/src/main/java/dev/guise/mummy/plan/
```

Update the `package` declaration to `dev.guise.mummy.plan`. Imports from the root package (`Artifact`, `CollectionArtifact`, `MummyPlan`, etc.) become explicit.

### Constructor

`siteSourceDirectory` is removed from the constructor. It becomes a parameter of `writeTo()` only.

```java
public PlanDescriber(final MummyPlan plan, final URI rootTargetPathUri) {
    this.plan = requireNonNull(plan);
    this.rootTargetPathUri = requireNonNull(rootTargetPathUri);
}
```

### New method: `summarize()`

```java
public PlanSummary summarize() {
    final var builder = PlanSummary.builder();
    plan.walk((artifact, subsumed) -> {
        if(subsumed) {
            return; // skip subsumed artifacts for counting and redirect extraction
        }
        if(artifact instanceof CollectionArtifact) {
            builder.incrementCollectionCount();
        } else if(artifact.getMummifier() instanceof PageMummifier) {
            builder.incrementPageCount();
        } else if(artifact.getMummifier() instanceof ImageMummifier) {
            builder.incrementImageCount();
        } else {
            builder.incrementOtherCount();
        }
        if(artifact.isPost()) {
            builder.incrementPostCount();
        }
        findRedirect(rootTargetPathUri, artifact).ifPresent(builder::addRedirect);
    });
    return builder.build();
}
```

### New method: `writeTo()`

Formatting logic lifted from the current `PlanDescriber.describeTo()`. All references to local variables (`totalCount`, `pageCount`, `sortedRedirects`, etc.) become `PlanSummary` accessor calls. `siteSourceDirectory` comes from the parameter.

```java
public void writeTo(final Appendable appendable, final Path siteSourceDirectory,
        final PlanSummary summary, final boolean verbose) throws IOException {
    // formatting code: summary header, counts, redirect details, legend
}
```

The formatting logic is:

1. Summary header: source, total, per-category counts, redirect counts.
2. Warning count line (only if > 0).
3. Verbose redirect detail lines (if `verbose` and redirects exist).
4. Legend (if any warnings present).

No behavioral changes — identical output.

### Convenience method: `describeTo()`

```java
public void describeTo(final Appendable appendable, final Path siteSourceDirectory,
        final boolean verbose) throws IOException {
    writeTo(appendable, siteSourceDirectory, summarize(), verbose);
}
```

### `findRedirect()` — redirect analysis

Package-private static method, renamed from the current `collectRedirect()`. Returns `Optional<RedirectEntry>` instead of mutating a list.

```java
static Optional<PlanSummary.RedirectEntry> findRedirect(final URI rootTargetPathUri, final Artifact artifact) {
    return artifact.getResourceDescription().findPropertyValue(PROPERTY_TAG_MUMMY_ALT_LOCATION)
            .filter(CharSequence.class::isInstance)
            .map(Object::toString)
            .map(URIPath::of)
            .map(altLocationReference -> {
                final URI artifactTargetUri = artifact.getTargetPath().toUri();
                return resolve(artifact instanceof CollectionArtifact ? toCollectionURI(artifactTargetUri) : artifactTargetUri,
                        altLocationReference);
            })
            .map(altLocationUri -> URIPath.relativize(rootTargetPathUri, altLocationUri))
            .map(altLocationReference -> {
                final URIPath targetReference = Artifact.relativizeResourceReference(rootTargetPathUri, artifact);
                final Optional<PlanSummary.PlanWarning> optionalWarning = altLocationReference.isSubPath()
                        ? Optional.empty() : Optional.of(PlanSummary.PlanWarning.REDIRECT_OUTSIDE_SITE);
                return new PlanSummary.RedirectEntry(altLocationReference, targetReference,
                        artifact instanceof CollectionArtifact, optionalWarning);
            });
}
```

### Class-level changes

- Remove the manual stack-based walk code entirely.
- Remove the local counter variables and `ArrayList<RedirectEntry>`.
- Remove `RedirectEntry` and `PlanWarning` (moved to `PlanSummary`).
- Rename `collectRedirect()` to `findRedirect()`, change return to `Optional`, keep as static method on `PlanDescriber`.
- Update `package` declaration to `dev.guise.mummy.plan`.
- Add imports for root-package types (`Artifact`, `CollectionArtifact`, `MummyPlan`, etc.).
- Update class Javadoc to reflect the two roles: summarizing a plan into a `PlanSummary`, and writing the human-readable description.

---

## Step 3: Update Call Site in `GuiseMummy.mummify()`

[GuiseMummy.java](mummy/src/main/java/dev/guise/mummy/GuiseMummy.java#L305-L306)

Add import for `dev.guise.mummy.plan.PlanDescriber`.

Current:

```java
new PlanDescriber(plan, toCollectionURI(rootArtifact.getTargetPath().toUri()), context.getSiteSourceDirectory())
        .describeTo(System.out, isVerbose());
```

Updated (using convenience `describeTo()`):

```java
new PlanDescriber(plan, toCollectionURI(rootArtifact.getTargetPath().toUri()))
        .describeTo(System.out, context.getSiteSourceDirectory(), isVerbose());
```

Or equivalently using the two-step form when the summary is needed elsewhere:

```java
final var describer = new PlanDescriber(plan, toCollectionURI(rootArtifact.getTargetPath().toUri()));
final PlanSummary summary = describer.summarize();
describer.writeTo(System.out, context.getSiteSourceDirectory(), summary, isVerbose());
```

The convenience form is appropriate here since the summary is not reused.

---

## Step 4: Update `PlanDescriberTest`

Move test to `dev.guise.mummy.plan` package using `git mv`:

```sh
git mv mummy/src/test/java/dev/guise/mummy/PlanDescriberTest.java mummy/src/test/java/dev/guise/mummy/plan/
```

### Changes required

1. **Package declaration** — update to `dev.guise.mummy.plan`.
2. **`planDescriber()` helper** — remove `SOURCE_DIRECTORY` parameter (constructor no longer takes it).
3. **Tests that call `describeTo()`** — change to `planDescriber(plan).describeTo(output, SOURCE_DIRECTORY, verbose)` (convenience method signature change: `siteSourceDirectory` moves from constructor to method parameter).
4. **`findRedirect()` tests** — update method name from `collectRedirect()` to `findRedirect()`, update to check `Optional` return instead of list mutation.
5. **`RedirectEntry` sorting test** — update qualifier from `PlanDescriber.RedirectEntry` to `PlanSummary.RedirectEntry`.
6. **Import changes** — add `PlanSummary` import; root-package artifact types become explicit imports.

### Behavioral equivalence

All existing test assertions remain unchanged. The tests verify the same output — only the API path to produce that output changes. The `findRedirect()` tests change from checking list contents to checking `Optional` values, but the extracted `RedirectEntry` fields are verified identically.

---

## Step 5: Add `PlanSummaryTest`

### Location

New file: `mummy/src/test/java/dev/guise/mummy/plan/PlanSummaryTest.java`

### Purpose

Direct tests of `PlanSummary` as an independent unit — builder accumulation — without requiring artifact tree construction.

### Test cases

**Builder accumulation:**

- `testBuilderAccumulatesCounts` — call increment methods, verify `build()` produces correct counts and `totalCount()` is the sum.
- `testBuilderSortsRedirects` — add unsorted `RedirectEntry` instances, verify `build()` produces sorted list.
- `testBuilderDefaultsToZeroCounts` — `builder().build()` has all counts zero and empty redirects.

**Derived accessors:**

- `testRedirectCollectionCount` — build with mixed collection/page redirects, verify `redirectCollectionCount()` and `redirectPageCount()`.
- `testWarningCount` — build with some warned redirects, verify `warningCount()`.

---

## Behavioral equivalence verification

The refactoring must produce identical output for all existing test cases. The key behavioral change is the traversal mechanism:

| Aspect | Before (manual stack) | After (ArtifactTreeWalker) |
|---|---|---|
| Traversal | `CollectionArtifact.getChildArtifacts()` (stack-based) | `CompositeArtifact.comprisedArtifacts()` (recursive) |
| Subsumed artifacts | Never visited (not in `getChildArtifacts()`) | Visited with `subsumed=true`; visitor skips them |
| `AspectualArtifact` composites | Silently skipped (only `CollectionArtifact` pushes children) | Visited (walker handles all `CompositeArtifact` subtypes) |
| Counting | Identical — subsumed artifacts excluded from counts |
| Redirect extraction | Identical — subsumed artifacts excluded from redirect extraction |
| Formatting | Identical — same format strings, same output |

The `AspectualArtifact` difference is a latent fix: the old code would miss comprised artifacts of non-`CollectionArtifact` composites. No existing tests exercise this (none construct `AspectualArtifact` trees), so no test output changes. This is a correct improvement, consistent with the walker's design rationale (noted in the walker plan: "Converting `PlanDescriber` would also fix a latent gap").

---

## Implementation order rationale

Step 1 is purely additive (new file, new types). Step 2 modifies `PlanDescriber` internals and adds the formatting/analysis methods. Step 3 updates the single call site. Step 4 adapts existing tests. Step 5 adds new tests. This ordering ensures the new types exist before the code that uses them (compilation order), and existing behavior is preserved before new tests are added.

Steps 2–3 must be sequential (PlanDescriber changes before call site). Steps 4–5 are ordered by dependency (existing test adaptation before new tests) but could be interleaved.
