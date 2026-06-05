# [GUISE-232] Summary

Resolved as invalid — Flexmark and Guise Mummy parse Markdown correctly. The reported XHTML error was a consequence of Markdown source structure that conflicts with the CommonMark HTML block classification rules, not a parser bug or missing configuration.

## Root Cause

The CommonMark specification (§4.6) defines HTML blocks as *lexical line-spans*, not element-tree representations. Block type is determined solely by the first tag on the freshly-evaluated start line; HTML nesting is not tracked. A type-6 block (opened by any recognized block-level tag such as `<div>`) terminates at the first following blank line, after which the parser resumes normal Markdown processing.

In the reported structure — `<div>` on one line, then `<pre><code>`, then a blank line — the blank line after `<pre><code>` ends the `<div>` type-6 block. Everything that follows is re-entered into the Markdown parser. The lines `- foo` / `- bar` become a `<ul>` list, and the closing `</code></pre>` lands on a single line that satisfies no HTML block start condition (type 6 rejects `code` because it is not in the enumerated block-tag list; type 7 rejects two tags on one line). That line is therefore wrapped in a `<p>` element, producing `<p></code></pre></p>` — unbalanced XML that the XHTML document builder rejects.

## Fix

Insert a blank line between the outer wrapper tag and `<pre><code>`:

```markdown
<div>

<pre><code class="language-markdown">- foo
- bar
</code></pre>

</div>
```

The blank line makes `<div>` a standalone one-line type-6 block. `<pre>` is then a freshly-evaluated start line and matches the type-1 start condition (begins with `<pre`). A type-1 block is terminated only by a line *containing* `</pre>` (position-independent), so internal blank lines are safe and `</code></pre>` on one line passes through raw inside the still-open type-1 block.

## Handoff: CommonMark HTML Block Rules for Markdown Authors

Two properties of the CommonMark spec are non-obvious and govern correct use of embedded HTML in Guise Mummy source pages:

**Type-1 protection requires `<pre>` to be outermost.** If `<pre>` is nested inside a `<div>` or other type-6 opener, the enclosing type-6 block terminates at the first blank line before `<pre>` is reached, and `<pre>` never becomes a type-1 start. A blank line between the outer wrapper and `<pre>` is the structural requirement that promotes `<pre>` to type-1.

**Closing tags after a type-1 block must not be tab-indented.** After `</pre>` closes the type-1 block, the following lines are freshly evaluated for start conditions. A leading tab character is treated as 4 columns of indentation, which exceeds the 3-space maximum for HTML block starts; such a line is demoted to an indented code block and the tag text is emitted as escaped literal content rather than raw HTML. Closing tags for outer wrappers (`</figure>`, `</details>`, etc.) must appear at column 0 (or with no more than 3 leading spaces).

**Opening tags inside the type-6 block may be indented freely.** Interior lines of an already-open HTML block are never re-evaluated, so indentation of tags such as `<summary>`, `<figure>`, and `<figcaption>` inside the opening type-6 run is irrelevant to parsing. The indentation constraint applies only to freshly-evaluated start lines.
