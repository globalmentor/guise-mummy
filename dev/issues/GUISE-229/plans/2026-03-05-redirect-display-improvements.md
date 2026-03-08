# Plan: Redirect Display Improvements

## Overview

All changes are in `PlanDescriber.java` and `PlanDescriberTest.java` — a single compilation unit pair. The plan is one natural chunk. The output format specification in [describe-flag.md](../designs/describe-flag.md) is updated alongside the plan.

- Step 1: Rename redirect summary labels to "Page Targets:" / "Collection Targets:".
- Step 2: Display redirect detail paths in decoded (human-readable) form.
- Step 3: Sort redirect entries flat by decoded source path, case-insensitive (remove collection-first grouping). Includes TODO comment for future segment-by-segment comparator.
- Step 4: Update tests.

## Step 1: Rename Redirect Summary Labels

In `PlanDescriber.describeTo()`, change the summary labels:

- `"Collection:"` → `"Collection Targets:"`
- `"Page:"` → `"Page Targets:"`

Since these labels are longer, the format width needs adjustment. Current width is `%-14s`. The longest existing label is `"Collections:"` (13 chars). `"Collection Targets:"` is 19 characters. Increase the sub-item format width to `%-22s` for the redirect subcategory lines. Keep the existing `%-14s` for the artifact subcategory lines (Pages, Collections, Images, Other, Posts) and the `%-14s` for top-level lines (Source, Artifacts, Redirects) since those labels remain unchanged.

Alternatively, increase all format widths uniformly — but this would add unnecessary whitespace to the artifact section. Since redirect subcategories are already indented under `Redirects:`, a wider format there is fine.

**Decision:** Use `%-22s` only for the two redirect subcategory lines (`Page Targets:` / `Collection Targets:`). This keeps the artifact section compact while accommodating the longer labels.

The `Warnings:` line under redirects should also use the wider format for visual alignment with its siblings. It is currently 10 characters, well within 22.

**Target:** [PlanDescriber.java](../../mummy/src/main/java/dev/guise/mummy/PlanDescriber.java), lines 113–115.

## Step 2: Display Redirect Detail Paths in Decoded Form

In `PlanDescriber.describeTo()`, the verbose redirect detail line currently uses `URIPath.toString()` (raw/encoded) with a leading `/` prefix:

```java
appendable.append("    /%s -> /%s%s%n".formatted(
    redirect.altLocationReference(), redirect.resourceReference(), warningMarker));
```

Change to use `URIPath.toDecodedString()` and remove the leading `/` prefix. The Guise Mummy model represents all paths as site-root-relative without a leading `/` — they are genuinely relative references, not server-absolute paths. Displaying them with a `/` prefix would misrepresent them as absolute paths and obscure the deployment-location independence that the model provides.

```java
appendable.append("    %s -> %s%s%n".formatted(
    redirect.altLocationReference().toDecodedString(),
    redirect.resourceReference().toDecodedString(), warningMarker));
```

This affects only the display. The `RedirectEntry` record continues to store `URIPath` values in their canonical (encoded) form.

**Target:** [PlanDescriber.java](../../mummy/src/main/java/dev/guise/mummy/PlanDescriber.java), lines 123–124.

## Step 3: Sort Flat by Decoded Source Path, Case-Insensitive

### Sort approach alternatives

1. **`String.CASE_INSENSITIVE_ORDER`** — Java's built-in case-insensitive comparator. Uses `Character.toUpperCase()` then `toLowerCase()` character-by-character. Handles ASCII well. For non-ASCII, it handles basic Latin accented characters reasonably (e.g. `é` = `É`) but does not perform full Unicode collation (e.g. locale-specific ordering of `ö` vs. `o`).

2. **`Collator`** — Full locale-aware Unicode collation. Handles locale-specific ordering (e.g. Swedish `ö` sorts after `z`, German `ö` sorts with `o`). Requires choosing a locale, which introduces a configuration decision. Primary strength (`Collator.PRIMARY`) ignores case and accents; secondary ignores case but distinguishes accents; tertiary distinguishes everything.

3. **`String.compareToIgnoreCase()`** — Same semantics as `String.CASE_INSENSITIVE_ORDER` but as an instance method.

**Decision:** Use `String.CASE_INSENSITIVE_ORDER` on the decoded string. Rationale:

- URL paths are overwhelmingly ASCII. The typical non-ASCII paths are filesystem-derived names (e.g. `café.html`), where case-insensitive ASCII comparison plus basic Unicode case-folding is sufficient.
- A `Collator` requires a locale choice that doesn't have a natural answer for URL paths (the site's locale? the system locale? `ROOT`?). This is a display sort, not a semantic ordering — "close enough" alphabetical is fine.
- `String.CASE_INSENSITIVE_ORDER` is stateless and thread-safe, with no allocation overhead.
- A proper segment-by-segment `URIPath` comparator (see [todo - URI Path Segment Comparator.md](../todo%20-%20URI%20Path%20Segment%20Comparator.md)) will likely supersede whatever sorting logic is implemented here. Once that comparator exists, this `compareTo` method will simply delegate to it. Investing in a more sophisticated approach now (e.g. a `Collator`, manual segment splitting, or custom tiebreaking rules) would be gold-plating work that gets thrown away. The current `String.CASE_INSENSITIVE_ORDER` approach is intentionally provisional — good enough for human-readable output until the proper comparator is available.

### Implementation

Replace the `compareTo` method in `RedirectEntry`:

```java
@Override
public int compareTo(@NonNull final RedirectEntry other) {
    //TODO switch to a segment-by-segment URIPath comparator when available in globalmentor-core,
    // for more logical directory-level grouping (e.g. `a/b/c` before `a-suffix`)
    return String.CASE_INSENSITIVE_ORDER.compare(
        this.altLocationReference.toDecodedString(),
        other.altLocationReference.toDecodedString());
}
```

This removes the collection-first grouping and sorts all entries uniformly by decoded source path.

Replace the `@implSpec`:

```java
/// @implSpec Entries are sorted case-insensitively by the decoded form of the alternate location reference.
```

**Target:** [PlanDescriber.java](../../mummy/src/main/java/dev/guise/mummy/PlanDescriber.java), lines 196–204.

## Step 4: Update Tests

### Label and padding changes (Step 1)

With the format width change from `%-14s` to `%-22s` on redirect subcategory lines, all tests asserting on those lines need updated expected strings. The padding math: `%-22s` pads to 22 characters, so `"Page Targets:"` (13 chars) gets 9 spaces, `"Collection Targets:"` (19 chars) gets 3 spaces, and `"Warnings:"` (9 chars) gets 13 spaces.

Affected tests:

- `testDescribeToExtractsPageRedirects`: `"Page:         1"` → `"Page Targets:         1"`, `"Collection:   0"` → `"Collection Targets:   0"`.
- `testDescribeToExtractsCollectionRedirects`: `"Collection:   1"` → `"Collection Targets:   1"`, `"Page:         0"` → `"Page Targets:         0"`.
- `testDescribeToWarnsOnOutOfSiteRedirect`: `"Warnings:     1 [!]"` → `"Warnings:             1 [!]"`.

### Sort order (Step 3)

`testRedirectEntrySorting`: currently asserts `[collectionA, collectionB, pageA, pageB]` (collections first). After the change, the expected order is flat alphabetical by decoded source path, case-insensitive:

- `alpha/` (decoded: `alpha/`)
- `a-page.html` (decoded: `a-page.html`)
- `beta/` (decoded: `beta/`)
- `b-page.html` (decoded: `b-page.html`)

Wait — `alpha/` vs. `a-page.html`: `String.CASE_INSENSITIVE_ORDER` compares character-by-character. `'l'` (from `alpha/`) vs. `'-'` (from `a-page.html`): `'-'` (0x2D) < `'l'` (0x6C), so `a-page.html` sorts before `alpha/`. The expected order is:

- `a-page.html`
- `alpha/`
- `b-page.html`
- `beta/`

Update the assertion: `contains(pageA, collectionA, pageB, collectionB)`. Update the test Javadoc to reflect the new sort contract (case-insensitive by decoded source path, no collection-first grouping).

### Decoded display (Step 2)

The existing test data uses only ASCII paths (e.g. `old-page.html`, `new-page.html`), so encoded and decoded forms are identical — the existing assertions still pass but don't exercise the decoding behavior.

Add a new test method `testDescribeToVerboseDecodesNonAsciiPaths` that constructs artifacts with pre-encoded alt-location properties at three UTF-8 encoding boundaries, verifying that `PlanDescriber` outputs decoded Unicode in verbose mode:

| UTF-8 bytes | Character | Alt-location property | Target filename | Expected decoded display |
|---|---|---|---|---|
| 2-byte | `é` (U+00E9) | `caf%C3%A9.html` | `new-café.html` | `café.html -> new-café.html` |
| 3-byte | `日` (U+65E5, CJK "day/sun") | `%E6%97%A5%E8%A8%98.html` | `新日記.html` | `日記.html -> 新日記.html` |
| 4-byte | `𝄞` (U+1D11E, musical G clef) | `%F0%9D%84%9E.html` | `new-𝄞.html` | `𝄞.html -> new-𝄞.html` |

Each test entry uses a pre-encoded alt-location string and a **non-ASCII target filename**, so that `toDecodedString()` is exercised on both sides of the `->` arrow. The alt-location goes through `URIPath.of()` → `URI.create()`, which treats the percent-encoded octets as encoded byte sequences. The target path goes through `Path.toUri()` (which percent-encodes the Unicode characters) → `URIPath.relativize()` → `toDecodedString()`. In verbose mode, `describeTo` should output both paths in decoded form.

For the 2-byte case, assert `containsString("    café.html -> new-café.html" + NL)`. Apply the same pattern for the 3-byte and 4-byte cases.

Note: `URI.create()` does **not** reject non-ASCII characters in the path component — it stores them as-is in the raw path, making `toString()` and `toDecodedString()` return the same value. The pre-encoded form is the only way to exercise the actual decoding behavior.

Note: `String.CASE_INSENSITIVE_ORDER` operates on `char` values (UTF-16 code units). For supplementary characters (like `𝄞`), `Character.toUpperCase(char)` is a no-op on surrogates, so case folding doesn't apply. This is acceptable for URL paths — supplementary characters with case distinctions (e.g., Deseret script) are exceedingly rare in filenames.
