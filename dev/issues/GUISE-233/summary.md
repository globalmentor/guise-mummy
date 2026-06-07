# [GUISE-233] Summary

The `mx:content-as` attribute was added to `GuiseMesh`, allowing an element's text content to be designated as `literal` (exempt from `^{…}` interpolation) rather than the default `template` (scanned and interpolated). The setting inherits down the DOM subtree; a descendant can re-enable interpolation with an explicit `mx:content-as="template"`. As a by-product, the ticket established a design framework for the full content-interpretation axis (see `designs/Content Interpretation.md`) and produced a documentation refresh of the Guise Mummy and Mesh foundational docs.

## Key Decisions and Pivots

**Attribute design.** The attribute began as `mx:content-mode` with string constants before two significant pivots. First, the axis name was corrected from "mode" (which describes engine state) to "content-as" (which describes how content is interpreted/classified), following analysis that no single verb — "process," "apply," "scan" — generalizes across all three value categories. Second, the value constants were replaced by a nested `ContentAs` enum implementing `Identifier`, which uses `Enums.getSerializationName`/`getSerializedEnum` for `CONSTANT_CASE`↔`kebab-case` round-tripping and gives an exhaustive compiler-enforced dispatch. See the minutes entries from 2026-06-06 for the rejected alternatives (`content-form`, `content-kind`, `content-parse`, `content-type`) and the rationale behind `literal` over `raw`/`verbatim`/`plain`.

**Clone-detachment invariant.** `mx:each` clones are processed while detached from the live DOM, so an ancestor-walk for the effective `content-as` value fails for any subtree that passes through a cloning directive. The adopted fix stamps the resolved effective value onto each clone as a DOM attribute before detachment, while the source element is still attached. Deferred: the correct long-term mechanism is carrying this state in `MeshContext` (the existing precedent for how iteration variables cross the clone boundary), where no DOM manipulation is needed; a TODO comment marks the site.

**Axis separation.** The design separates content interpretation (`mx:content-as`) from structural processing (`mx:each`, `mx:text`, `mx:attr-*`), value conversion, and serialization. `literal` suppresses character-data interpolation only; it does not suppress `mx:` directives or attribute interpolation within the same element. This separation is what enables `mx:each` over a `literal` region (iterate structurally; leave text verbatim) to work without special cases.

## What Was Produced

- `mx:content-as` attribute in `GuiseMesh`, supporting `template` and `literal`; the reserved `EXPRESSION` value is recognized but throws "not yet implemented" at dispatch.
- `ContentAs` nested enum in `GuiseMesh`.
- `findContentAs(Element)` ancestor-walk returning `Optional<ContentAs>` (absent = inheriting default; present-but-unrecognized = throws `MeshException` immediately).
- 197 lines of new tests in `GuiseMeshTest`, covering inheritance, two-level nested `mx:each`, re-enable with explicit `template`, and rejection of unknown values.
- `designs/Content Interpretation.md` — durable design reasoning for the full four-axis model, including the reserved `expression` mode and anticipated future axes.
- Foundational documentation refresh (commit `57f11b59`): `mummy/readme.md`, `mesh/readme.md`, `mummy/architecture.md`, root `readme.md`, and `cli/readme.md`.
- `todo - mx-text Reinterpolation.md` — deferred ticket seed for the `mx:text` re-interpolation asymmetry discovered during implementation.

## Handoff Notes

**`EXPRESSION` not yet implemented.** The value is in the enum and recognized, but the dispatch throws at use. A follow-on ticket should implement evaluation of the element's entire character content as a single MEXL expression replacing that content.

**`mx:text` re-interpolation asymmetry.** `mx:text` results are re-scanned for `^{…}` because `setTextContent()` creates a new text node that the child-walk then encounters; inline `^{…}` results are not re-scanned. The asymmetry has correctness and latent security implications and is tracked in `todo - mx-text Reinterpolation.md`.

**Clone-stamp DOM anomaly.** The stamp attribute written by `ATTRIBUTE_CONTENT_AS.withPrefix(NAMESPACE_PREFIX)` has a non-null namespace URI and an explicit prefix — a state that cannot arise from parsing well-formed XML, though DOM lookup (`getAttributeNS`) finds it correctly. The attribute is excised before serialization, so the anomaly has no observable effect given the current invariant. Switching to `MeshContext` propagation removes the anomaly entirely.

**Markdown namespace propagation.** Investigation confirmed that `xmlns:mx` declarations on embedded HTML block elements survive the Flexmark→XHTML-wrapper→`DocumentBuilder` pipeline. The open question of whether to add `xmlns:mx` to `MarkdownPageMummifier`'s XHTML wrapper template — which would allow `mx:` attributes in Markdown-embedded HTML without per-element namespace declarations — was deferred; see `plans/2026-06-06-markdown-namespace-propagation.md`.
