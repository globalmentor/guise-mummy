# [GUISE-220] Plan: Fix Front Matter Pattern

## Overview

This plan is monolithic; the work was not decomposable into independent chunks.

- Step 1: Update `MARKDOWN_WITH_YAML_PATTERN`
- Step 2: Update existing `MARKDOWN_WITH_YAML_PATTERN` unit tests
- Step 3: Add regression tests for the corrected behavior

## Step 1: Update `MARKDOWN_WITH_YAML_PATTERN`

**File:** `mummy/src/main/java/dev/guise/mummy/mummify/page/MarkdownPageMummifier.java`

Replace the existing constant declaration:

```java
static final Pattern MARKDOWN_WITH_YAML_PATTERN = Pattern.compile("(?:---[\\r\\n]+(.*?)^---[ \\t]*(?:[\\r\\n]+|$))?(.*)", Pattern.DOTALL | Pattern.MULTILINE);
```

Three changes from the original `"(?:---[\\r\\n]+(.*)---(?:[\\r\\n]+|$))?(.*)"` with `Pattern.DOTALL`:

1. **Non-greedy YAML group** (`.*` → `.*?`): the greedy form consumed the entire document then backtracked to the last `---` in the file. Non-greedy stops at the first qualifying closer.

2. **Start-of-line anchor on the closer** (bare `---` → `^---` with `Pattern.MULTILINE` added): `^` in multiline mode requires the closing delimiter to appear at the start of a line. A `---` embedded mid-line—such as `<pre><code class="language-markdown">---`—cannot match. `Pattern.DOTALL` is retained; `Pattern.MULTILINE` is added alongside it.

3. **Trailing-whitespace-only closer** (`---` → `---[ \t]*`): the closing delimiter may be followed only by spaces or tabs before the line ending. `---extra` is not a valid closer.

The YAML group now captures the trailing newline before the closer (e.g., `"title: Test\n"` rather than `"title: Test"`). This is intentional: the `^` anchor is zero-width and sits after the preceding `\n`, so `.*?` includes that `\n` in its match. SnakeYAML is unaffected by a trailing newline in the YAML string.

Update the doc comment on the constant to reflect the corrected anchoring and trailing-whitespace behavior. The `MARKDOWN_WITH_YAML_PATTERN_YAML_GROUP` and `MARKDOWN_WITH_YAML_PATTERN_MARKDOWN_GROUP` constants and their doc comments need no change.

No import changes are needed; `Pattern.MULTILINE` is in `java.util.regex.Pattern`, which is already imported via `java.util.regex.*`.

## Step 2: Update Existing Unit Tests

**File:** `mummy/src/test/java/dev/guise/mummy/mummify/page/MarkdownPageMummifierTest.java`

Three existing tests use inputs in which the closing `---` is embedded at the end of the last YAML line (e.g., `example:test---`) rather than on its own line. These were test-data irregularities that the old greedy pattern happened to accept. The corrected pattern requires `^---` at the start of a line, so the inputs and expected YAML group values must be updated.

**`testMarkdownWithYamlPattern()`**

- Old input: `"---\nfoo:bar\nexample:test---\n# Heading\n\nBody text."`
- New input: `"---\nfoo:bar\nexample:test\n---\n# Heading\n\nBody text."`
- Old expected YAML group: `"foo:bar\nexample:test"`
- New expected YAML group: `"foo:bar\nexample:test\n"`
- Expected Markdown group: `"# Heading\n\nBody text."` (unchanged)

**`testMarkdownWithYamlPatternNoMarkdown()`**

- Old input: `"---\nfoo:bar\nexample:test---"`
- New input: `"---\nfoo:bar\nexample:test\n---"`
- Old expected YAML group: `"foo:bar\nexample:test"`
- New expected YAML group: `"foo:bar\nexample:test\n"`
- Expected Markdown group: `""` (unchanged)

**`testMarkdownWithYamlPatternEmptyLineMarkdown()`**

- Old input: `"---\nfoo:bar\nexample:test---\n"`
- New input: `"---\nfoo:bar\nexample:test\n---\n"`
- Old expected YAML group: `"foo:bar\nexample:test"`
- New expected YAML group: `"foo:bar\nexample:test\n"`
- Expected Markdown group: `""` (unchanged)

The other existing pattern tests (`testMarkdownWithYamlPatternNoYaml()`, `testMarkdownWithYamlPatternEmptyYaml()`, `testMarkdownWithYamlPatternSingleLineYamlNotRecognized()`) already use proper inputs and need no changes.

The resource-file-based tests (`testSimpleMarkdown()`, `testSimpleMarkdownDocumentMetadata()`, etc.) exercise `loadSourceDocument()` and `loadSourceMetadata()` against `.md` files under `src/test/resources`. Verify that these pass without modification when running the full test suite; standard authoring practice places the closing `---` on its own line, so no resource file changes are expected.

## Step 3: Add Regression Tests

**File:** `mummy/src/test/java/dev/guise/mummy/mummify/page/MarkdownPageMummifierTest.java`

Add the following four tests immediately after the existing `MARKDOWN_WITH_YAML_PATTERN` tests.

**`testMarkdownWithYamlPatternBodyDashesIgnored()`** — Verifies that `---` appearing at the start of a line in the body (such as a Markdown horizontal rule after the front matter) does not confuse front matter detection. The non-greedy match correctly stops at the first `^---` (the proper front matter closer), and any subsequent `---` lines land in group 2.

```java
/// Tests that `---` on its own line in the body does not confuse the front matter closer.
/// @see MarkdownPageMummifier#MARKDOWN_WITH_YAML_PATTERN
@Test
public void testMarkdownWithYamlPatternBodyDashesIgnored() {
    final Matcher matcher = MARKDOWN_WITH_YAML_PATTERN.matcher("---\ntitle: Test\n---\nBefore\n---\nAfter");
    assertThat(matcher.matches(), is(true));
    assertThat(matcher.group(MARKDOWN_WITH_YAML_PATTERN_YAML_GROUP), is("title: Test\n"));
    assertThat(matcher.group(MARKDOWN_WITH_YAML_PATTERN_MARKDOWN_GROUP), is("Before\n---\nAfter"));
}
```

**`testMarkdownWithYamlPatternMidLineDashesIgnored()`** — Verifies the primary bug scenario: `---` appearing mid-line in the body (such as inside a `<pre><code>` block) does not match the front matter closer and is captured in the Markdown body group as-is.

```java
/// Tests that `---` mid-line in the body (as in a `<pre><code>` block) does not match the front matter closer.
/// @see MarkdownPageMummifier#MARKDOWN_WITH_YAML_PATTERN
@Test
public void testMarkdownWithYamlPatternMidLineDashesIgnored() {
    final Matcher matcher = MARKDOWN_WITH_YAML_PATTERN.matcher("---\ntitle: Test\n---\nBody.\n<pre><code class=\"language-markdown\">---\n</code></pre>");
    assertThat(matcher.matches(), is(true));
    assertThat(matcher.group(MARKDOWN_WITH_YAML_PATTERN_YAML_GROUP), is("title: Test\n"));
    assertThat(matcher.group(MARKDOWN_WITH_YAML_PATTERN_MARKDOWN_GROUP), is("Body.\n<pre><code class=\"language-markdown\">---\n</code></pre>"));
}
```

**`testMarkdownWithYamlPatternCloserTrailingWhitespace()`** — Verifies that the closing `---` may have trailing spaces and tabs before the line ending.

```java
/// Tests that the front matter closer tolerates trailing spaces and tabs.
/// @see MarkdownPageMummifier#MARKDOWN_WITH_YAML_PATTERN
@Test
public void testMarkdownWithYamlPatternCloserTrailingWhitespace() {
    final Matcher matcher = MARKDOWN_WITH_YAML_PATTERN.matcher("---\ntitle: Test\n--- \t \n# Heading\n");
    assertThat(matcher.matches(), is(true));
    assertThat(matcher.group(MARKDOWN_WITH_YAML_PATTERN_YAML_GROUP), is("title: Test\n"));
    assertThat(matcher.group(MARKDOWN_WITH_YAML_PATTERN_MARKDOWN_GROUP), is("# Heading\n"));
}
```

**`testMarkdownWithYamlPatternCloserWithNonWhitespaceNotRecognized()`** — Verifies that `---` followed by non-whitespace is not recognized as a front matter closer. The optional front matter group fails (no valid closer found), and the document is treated as having no front matter.

```java
/// Tests that `---` with trailing non-whitespace is not recognized as a front matter closer.
/// @see MarkdownPageMummifier#MARKDOWN_WITH_YAML_PATTERN
@Test
public void testMarkdownWithYamlPatternCloserWithNonWhitespaceNotRecognized() {
    final Matcher matcher = MARKDOWN_WITH_YAML_PATTERN.matcher("---\ntitle: Test\n---extra\n# Heading\n");
    assertThat(matcher.matches(), is(true));
    assertThat(matcher.group(MARKDOWN_WITH_YAML_PATTERN_YAML_GROUP), is(nullValue()));
    assertThat(matcher.group(MARKDOWN_WITH_YAML_PATTERN_MARKDOWN_GROUP), is("---\ntitle: Test\n---extra\n# Heading\n"));
}
```
