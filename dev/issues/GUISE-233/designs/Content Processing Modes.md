# [GUISE-233] Design: Content Processing Modes

This design establishes how Guise Mesh decides *how* to process the textual content of an element, introducing the `mx:content-mode` attribute as the control point. It frames the full intended feature set so that successive tickets can extend it; [GUISE-233] implements only the `literal` subset (see *Scope of [GUISE-233]*).

## Processing Axes

Guise Mesh processing decomposes into four independent axes. Conflating them produces the kind of overloaded control that Thymeleaf's `th:inline` exhibits (where one attribute value simultaneously selects a scanning switch and a value-conversion strategy). Keeping them separate is the organizing principle of this design.

- **Structural processing** — element- and attribute-level directives that transform the DOM tree: `mx:each` (iteration), `mx:text` (content replacement from an expression), `mx:attr-*` (attribute mutation), and future directives such as conditionals. Driven by `mx:`-namespaced attributes, each evaluated as a MEXL expression.
- **Inline text processing** — scanning an element's character data for embedded `^{…}` interpolation expressions. This is what `DefaultMeshInterpolator` performs on `CharacterData` child nodes (text, CDATA, comment).
- **Value conversion** — how an evaluated expression result becomes characters. Today this is uniformly `Object::toString` producing plain text inserted through DOM text APIs. A future axis (the analog of Thymeleaf's `th:inline="javascript"`/`"css"`) could convert a value into a JavaScript or CSS literal for embedding in `<script>`/`<style>`.
- **Serialization** — how the DOM becomes bytes. Owned entirely by the downstream serializer (`com.globalmentor.html.HtmlSerializer`), not by Mesh. The serializer already disables character encoding inside `<script>` via `isChildTextEncoded()`, so value conversion and serialization compose cleanly without Mesh involvement.

`mx:content-mode` governs the **inline text processing** axis. It does not touch structural processing, value conversion, or serialization.

## `mx:content-mode`

`mx:content-mode` declares how Mesh interprets the textual content of an element — that is, the element's child character-data nodes. The name answers "how is this content to be taken?" rather than "what is this content?"; the values name a processing disposition, not a media type.

### Value Space

| Value | Meaning |
|---|---|
| `template` | The content is a Mesh template: character data is scanned for `^{…}` and interpolated. This is the default and the engine's historical behavior. |
| `literal` | The content stands for itself: no interpolation is performed, and `^{…}` sequences pass through unchanged. |
| `expression` | The content is a single MEXL expression whose evaluated result replaces the content. *(Reserved; not yet implemented.)* |

The three values mirror the trichotomy that recurs across expression libraries — JEXL's `createExpression()` versus `createTemplate()` versus untouched text; Spring EL's `ParserContext.TEMPLATE_EXPRESSION` versus a plain expression; JavaScript's template literal versus string literal. `template`, `expression`, and `literal` are the closest thing to industry-standard vocabulary for this distinction.

### Default

When no `mx:content-mode` is in effect, element content is processed as `template`. This preserves all existing behavior: every page that does not use the attribute meshes exactly as before.

### Inheritance

`mx:content-mode` is inherited down the DOM subtree. An element without its own `mx:content-mode` takes the mode of its nearest ancestor that declares one; if no ancestor declares one, the mode is `template`. A descendant re-establishes a different mode by declaring its own `mx:content-mode` — most usefully `mx:content-mode="template"` to re-enable interpolation within an otherwise `literal` region.

Inheritance is what makes the attribute practical for the motivating use case: an author wraps a region (for example a `<div>`) once, and all descendant text — including text inside nested `<pre>`, `<code>`, or other elements — is governed without per-element annotation.

### Scope Boundaries

`mx:content-mode` is deliberately narrow. It does **not**:

- **Affect attribute interpolation.** Non-`mx:` attribute values (such as `href="^{url}"`) continue to be interpolated regardless of content mode. Attributes are not element content; a separate control would govern them if ever needed. This is the reason the attribute is named `content-mode` rather than a broader name.
- **Affect structural directives.** `mx:each`, `mx:attr-*`, and `mx:text` continue to operate within a `literal` region. Iterating list items whose text is literal is a legitimate and supported combination: structure is produced, text is left verbatim.
- **Reprocess its own region's structure.** Setting `literal` suppresses interpolation of character data; it does not remove or alter child elements.

### Output

`mx:content-mode`, like all `mx:`-namespaced attributes, is removed from the serialized output.

## Relationship to `mx:text`

`mx:text` is a structural directive: its value is a MEXL expression (as with every `mx:` attribute), evaluated to replace the element's content. It is therefore on the structural-processing axis, not the inline-text-processing axis, and is unaffected by `mx:content-mode`.

A separate, pre-existing question concerns whether the *result* of `mx:text` (or of any expression) should itself be re-scanned for `^{…}`. Mesh currently re-interpolates `mx:text` results as a side effect of evaluation order, while inline `^{…}` results are not re-scanned. That asymmetry is tracked as a TODO in this ticket and is out of scope for the content-mode feature; it is noted here because it belongs to the same conceptual question of "should produced content be processed again," which a future unification of these axes would need to settle.

## Future Extensions

This design anticipates, without implementing, the following:

- **`expression` content mode.** Evaluate an element's entire content as a single MEXL expression and replace it with the result. A later ticket implements this value.
- **`mx:template-engine`.** When `mx:content-mode="template"`, select which template engine interprets the content — `mesh` (the default) or an alternate such as `handlebars`. This makes "template" a family rather than a single behavior. The attribute is meaningful only in `template` mode.
- **Value-converter modes.** The analog of Thymeleaf's `th:inline="javascript"`/`"css"` — selecting how an evaluated value is rendered as a literal for a host language. This belongs on the *value-conversion* axis and would be a distinct attribute (for example `mx:value-format`), not a value of `mx:content-mode`. Folding it into `content-mode` would repeat the `th:inline` conflation this design avoids.
- **Un-escaped / raw-markup insertion.** The ability to emit a value as raw markup rather than escaped text — the third orthogonal axis. Per the Twig precedent (`verbatim` for region skip, `raw` for un-escape are deliberately different words), any such mechanism must be named and controlled separately from `mx:content-mode`.

## Scope of [GUISE-233]

[GUISE-233] implements the minimum that resolves the motivating defect (literal content such as `^{tree}` in embedded snippets being interpreted as an expression):

- The `mx:content-mode` attribute is recognized.
- The values `template` and `literal` are supported; `template` is the default, and inheritance is in effect.
- `literal` suppresses interpolation of an element's character-data content (text, CDATA, comment) and is inherited by descendants.
- Unsupported values (including `expression`, which is reserved but not yet implemented) produce a `MeshException`.
- The attribute is excised from output.

Not implemented here: the `expression` content mode, `mx:template-engine`, value-converter modes, un-escape, and any fix to the `mx:text` re-interpolation asymmetry.
