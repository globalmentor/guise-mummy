# [GUISE-232] Markdown HTML Block Parsing

## Objective

Guise Mummy correctly processes Markdown source pages that contain embedded HTML blocks whose content includes blank lines, and does not produce malformed XHTML that fails the XHTML parse step.

## Acceptance Criteria

- A Markdown source page containing an embedded HTML block (e.g., `<div><pre><code>…</code></pre></div>`) with blank lines inside is mummified without an XHTML parse error.
- Content inside an embedded HTML block is not re-interpreted as Markdown (e.g., lines beginning with `- ` are not converted to `<ul>/<li>` elements).
- The root cause is identified: whether Flexmark is behaving correctly per the CommonMark spec, whether a configuration option addresses the behavior, or whether a workaround is needed.
- A unit test in `MarkdownPageMummifierTest` (with a corresponding Markdown resource fixture) reproduces the failure before the fix and passes after.

## Background

Mummifying a Markdown page that embeds raw HTML fails when the HTML block contains a blank line. Flexmark renders the content after the blank line as Markdown rather than preserving it as raw HTML, producing malformed nesting. When the resulting HTML is wrapped in the XHTML document template and parsed, the XML parser rejects it:

> The element type "p" must be terminated by the matching end-tag `</p>`.

For example, the following Markdown:

```markdown
# FooBar

<div>
<pre><code>

- foo
- bar

</code></pre>
</div>
```

produces HTML similar to:

```html
<h1>FooBar</h1>
<div>
<pre><code>
<ul>
<li>foo</li>
<li>bar</li>
</ul>
<p></code></pre></p>
</div>
```

The `- foo` / `- bar` lines are being converted to a `<ul>` list, and the closing `</code></pre>` ends up wrapped in a spurious `<p>` element — both wrong.

The CommonMark specification (§4.6) defines several HTML block types. Type-6 blocks, which begin with block-level tags such as `<div>`, are ended by a blank line rather than by the matching closing tag. If Flexmark follows this rule, the blank line after `<pre><code>` ends the HTML block, and `- foo` / `- bar` are subsequently parsed as a Markdown list. Whether this is the actual mechanism, whether Flexmark's behavior is compliant, and whether a configuration option (e.g., for non-CommonMark strict HTML block handling) can address it must be determined at the start of work.

## Orientation

- `mummy/src/main/java/dev/guise/mummy/mummify/page/MarkdownPageMummifier.java` — Flexmark parser and renderer construction; `loadSourceDocument()` where Markdown is parsed, HTML is generated, and the XHTML document is built and parsed.
- `mummy/src/test/java/dev/guise/mummy/mummify/page/MarkdownPageMummifierTest.java` — existing unit tests; the new test fixture goes here.
- `mummy/src/test/resources/dev/guise/mummy/mummify/page/` — Markdown resource fixtures for `MarkdownPageMummifierTest`.
