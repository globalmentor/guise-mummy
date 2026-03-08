# TODO: Segment-by-segment `URIPath` comparator.

Add a `Comparator<URIPath>` to `globalmentor-core` that compares URI paths segment by segment rather than as flat strings. This comparator would be useful anywhere URI paths are sorted for human display, such as the redirect detail listing in Guise Mummy's `PlanDescriber`.

## Problem

Flat string comparison of URI paths produces an ordering where punctuation characters (e.g. `-`, `.`) sort before letters, causing paths like `a-page.html` to sort before `alpha/` even though they are in different "directories." This is because `-` (U+002D) has a lower code point than `l` (U+006C), so the character-by-character comparison decides at position 1.

### Example: Without Segment-by-Segment Comparison

Given these paths sorted by flat `String.CASE_INSENSITIVE_ORDER` on the decoded string:

```
/a-page.html
/alpha/
/alpha/nested.html
/b-page.html
/beta/
/beta/deep/item.html
```

The `a-page.html` file sorts before the `alpha/` directory and its children, even though a user scanning a directory-like listing would expect directories and their contents to be grouped.

### Example: With Segment-by-Segment Comparison

A segment-aware comparator would split on `/`, compare corresponding segments individually, and treat segment boundaries as primary sort boundaries:

```
/a-page.html
/alpha/
/alpha/nested.html
/b-page.html
/beta/
/beta/deep/
/beta/deep/item.html
```

Here `alpha/` and `alpha/nested.html` group together because the first segment `alpha` is compared as a unit against `a-page.html` (where the full first segment is `a-page.html`), and `alpha` > `a-page.html` when compared as whole segment strings.

Additionally, a segment-aware comparator could optionally sort collection paths (those ending in `/`) before non-collection siblings at the same level, producing a directory-first ordering analogous to file managers.

## Design Considerations

The comparator implementation must address several interrelated concerns:

- **Encoding.** `URIPath` stores paths in percent-encoded form (`URIPath.toString()` returns the raw path). Comparison should operate on decoded segments (`URIPath.toDecodedString()` or per-segment decoding) so that `caf%C3%A9` and `café` are treated as equivalent. The comparator should document whether it compares encoded or decoded forms.

- **Case sensitivity.** URI paths are technically case-sensitive per RFC 3986, but for human-oriented display sorting, case-insensitive ordering is more natural (e.g. `About.html` should sort near `about.html`). The comparator should support a case-insensitive mode. Java's `String.CASE_INSENSITIVE_ORDER` provides basic case folding via `Character.toUpperCase()`/`toLowerCase()`, which handles ASCII and basic Latin accented characters.

- **Collation vs. naive case-insensitivity.** For full Unicode correctness, a `java.text.Collator` with configurable strength (primary, secondary, tertiary) would handle locale-specific ordering (e.g. Swedish `ö` after `z` vs. German `ö` with `o`). However, URL path segments don't have an inherent locale, making locale choice arbitrary. `String.CASE_INSENSITIVE_ORDER` is stateless and sufficient for the common case of ASCII-dominated paths with occasional accented characters. The design should decide whether to support pluggable comparison strategies or default to one approach.

- **Normalization.** Unicode normalization (NFC vs. NFD) can cause visually identical strings to compare as unequal (e.g. `é` as U+00E9 vs. `e` + U+0301). If the comparator operates on decoded segments, it should consider whether to normalize to NFC before comparison. This may be a broader concern for `URIPath` equality as well.

## Location

The comparator belongs in `globalmentor-core` alongside `URIPath` (`com.globalmentor.net.URIPath`) since it is a general-purpose utility not specific to any application. It could be a static factory method on `URIPath` itself (e.g. `URIPath.segmentComparator()`) or a standalone `Comparator` constant/factory. `URIPath` currently has a `getBasePaths()` method that returns cumulative path prefixes as a `List<URIPath>`, but no method to split into individual segments — such a method would be a prerequisite or co-deliverable.

## Current Workaround

[GUISE-229] uses flat `String.CASE_INSENSITIVE_ORDER` comparison on `URIPath.toDecodedString()` in `PlanDescriber.RedirectEntry.compareTo()`, with a TODO comment referencing this future comparator. When the comparator is available, `RedirectEntry.compareTo()` should be updated to use it.

[GUISE-229]: ../GUISE-229/
