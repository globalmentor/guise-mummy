# [GUISE-220] Minutes

<!-- Categories: Pivot | Insight | Decision | Finding | Lesson | Milestone | Open | Resolved -->

- 2026-06-05 **Finding**: Root cause identified: `MARKDOWN_WITH_YAML_PATTERN` uses greedy `.*` with `Pattern.DOTALL`, causing the regex to overshoot the real front-matter closing `---` and backtrack to the *last* `---` in the file; any `---` appearing later in the document body (inside a code block, embedded HTML, etc.) is incorrectly matched as the closer.
- 2026-06-05 **Finding**: The closing `---` in the pattern is also not anchored to the start of a line — `---` mid-line (e.g. `<pre><code>---`) could match. Fixing the bug requires both non-greedy (`.*?`) *and* `^` with `Pattern.MULTILINE` on the closer; non-greedy alone is insufficient.
- 2026-06-05 **Finding**: `loadSourceMetadata()` was leaking unchecked `YamlEngineException` through its `throws IOException` boundary. Wrapping in `IOException` is therefore a behavioral correctness fix as well as an error-message improvement.
- 2026-06-05 **Decision**: Error line-number accuracy: rather than adding an explanatory offset clause to the message, prepend `\n` × offset to the YAML string before passing it to SnakeYAML, so its reported line numbers naturally match the source document. Rejected the offset-clause approach as ambiguous to users.
