# [GUISE-220] Allow Markdown Documents to Contain Multiple YAML Streams in Code Sections

## Objective

Guise Mummy correctly processes Markdown documents whose content includes YAML fenced code blocks with multiple stream separators (`---`), without mistaking those separators for document-level YAML front matter boundaries.

## Acceptance Criteria

- A Markdown document containing a YAML fenced code block with one or more `---` stream-separator lines is processed without error or incorrect behavior.
- YAML front matter at the start of a Markdown document continues to be correctly detected and parsed.
- `---` separators that appear exclusively inside fenced code blocks have no effect on front-matter detection.

## Background

When a Markdown document includes a YAML fenced code section demonstrating multiple YAML streams—separated by `---`—Guise Mummy incorrectly interprets those separators as boundaries of the document's own YAML front matter. The front-matter detection pattern uses `Pattern.DOTALL` with greedy matching, allowing a `---` inside a fenced code block to be matched as the closing delimiter of the front-matter block rather than stopping at the actual document-level boundary.

## Orientation

The front-matter detection logic is in `MarkdownPageMummifier.MARKDOWN_WITH_YAML_PATTERN`, a `static final` regex constant in `mummy/src/main/java/dev/guise/mummy/mummify/page/MarkdownPageMummifier.java`.
