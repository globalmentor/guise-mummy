# [GUISE-220] Summary

Markdown documents containing `---` separators in their body (inside code blocks, embedded HTML, or any non-front-matter context) are now processed correctly by Guise Mummy without triggering a spurious YAML parse error.

## Outcome

The root cause was `MarkdownPageMummifier.MARKDOWN_WITH_YAML_PATTERN`, which used a greedy `.*` capture group under `Pattern.DOTALL`. With greedy matching, the regex consumed the entire document and backtracked to the *last* `---` in the file as the front-matter closer — not the first one. Any `---` appearing anywhere in the body, including mid-line in HTML blocks, could be matched as the closing delimiter.

The pattern was corrected with three changes:

- **Non-greedy content group** (`.*?`): stops at the first qualifying closer rather than the last.
- **Start-of-line anchor on the closer** (`^---` with `Pattern.MULTILINE`): `---` mid-line (e.g. `<pre><code class="language-markdown">---`) can no longer match as a front-matter delimiter.
- **Trailing-whitespace-only closer** (`---[ \t]*`): `---extra` is not a valid closer; only spaces and tabs may follow.

The opener was also tightened from `[\r\n]+` to `(?:\r\n|[\r\n])` — exactly one logical line ending. This makes the YAML content group's start position deterministic (always line 2 of the document), which in turn simplifies the SnakeYAML error-offset handling.

## Error Message Improvement

A secondary issue was that `loadSourceMetadata()` allowed `YamlEngineException` (an unchecked exception) to escape through its `throws IOException` boundary uncaught. This was corrected as part of the same work: SnakeYAML exceptions are now caught and rethrown as `IOException`, with the original exception preserved as the cause. To make SnakeYAML's reported line numbers accurate relative to the source document, a single `\n` is prepended to the YAML string before parsing — shifting SnakeYAML's line-1 to match the actual line-2 start of the front matter block in the file.

## Handoff Notes

The pattern's correct behavior depends on `matcher.matches()` at the call site, which anchors matching to the full input string. The opener has no explicit `^` anchor; the start-of-document constraint is enforced by `matches()`, not by the pattern itself. This is documented in the `MARKDOWN_WITH_YAML_PATTERN` Javadoc.

The `\n` prepend in `loadSourceMetadata()` is a deliberate coupling to the pattern: it is correct only because the opener is known to consume exactly one line ending, placing the YAML content on line 2. If the opener were ever changed to consume more than one line ending, the prepend would need to be revisited. This dependency is noted in the inline comment.
