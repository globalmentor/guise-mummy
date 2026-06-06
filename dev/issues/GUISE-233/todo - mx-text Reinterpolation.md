# TODO: Investigate `mx:text` result re-interpolation asymmetry and file a ticket if warranted.

Investigate an inconsistency in how Guise Mesh re-processes the output of an expression, and open a dedicated ticket for it if the investigation confirms it is a defect. The behavior was noticed while discussing [GUISE-233] (per-element suppression of Mesh processing) but is a separate, pre-existing issue with its own scope. The design of [GUISE-233] is still under discussion and its outcome should not be assumed here.

## Observed behavior

Guise Mesh has two ways for an expression result to land as element text content, and they differ in whether that result is then scanned again for `^{…}` interpolation:

- **Inline interpolation (`^{…}` in a text node).** `DefaultMeshInterpolator.findInterpolation()` appends the evaluated result to its output buffer and resumes scanning *after* the expression block. The result is never re-scanned, so an `^{…}` sequence appearing inside a result is left intact.
- **`mx:text` attribute.** In `GuiseMesh.meshElement()`, the `mx:text` branch evaluates the expression via `MexlEvaluator.findExpressionResult()` and writes the result with `Element.setTextContent()`, creating a fresh child text node. The method then calls `meshChildNodes()`, which walks all child `CharacterData` nodes — including the one just produced — and runs `MeshInterpolator.findInterpolation()` over it. As a result, an `^{…}` sequence contained in the `mx:text` result *is* evaluated as a further expression.

So `<span mx:text="x">` and an inline `^{x}` do not behave the same when the value of `x` contains caret-brace text. The asymmetry is undocumented: the Mesh readme describes `mx:text` only as replacing the element's text content with the expression result, and there is no code comment indicating the re-interpolation is intentional. The most likely explanation is evaluation order — `mx:text` was implemented as `setTextContent()` followed by the pre-existing child-node walk, and the walk now operates on text the engine itself just generated.

`mx:text` is the only current construct that injects a child text node *before* the `meshChildNodes()` walk in `meshElement()`; iteration returns early, and attribute interpolation and `mx:attr-*` mutation write to attributes rather than child text nodes. The general principle to check during investigation is whether any Mesh construct deposits content into a node that the same pass subsequently re-visits.

## Why it matters

- **Correctness, without any attacker.** Any context variable whose value legitimately contains `^{…}` renders differently depending on which mechanism is used. For example, a page summary or Markdown excerpt that describes Mesh syntax displays intact through inline `^{page.summary}` but is mangled through `<span mx:text="page.summary">`.
- **Security shape.** If a value flowing through `mx:text` can be influenced by an untrusted source, that source can embed MEXL via `^{…}` and have Mesh evaluate it. This is not arbitrary code execution: `JexlMexlEvaluator` sandboxes evaluation against a permitted-classes/packages list. The exposure is evaluation of arbitrary MEXL against the current `MeshContext` — context-state disclosure and traversal of object graphs the page did not intend to expose. It also widens the trapdoor for any future MEXL-facing feature added without awareness of this re-scan.

## Current risk

In Guise Mummy as it stands, the practical risk is low: the variables Mesh sees (`page`, `artifact`, `plan`, and similar) are site-author-controlled metadata rather than end-user input. The concern is latent and becomes material if a future feature routes externally-influenced data through `mx:text`, or if author content incidentally contains caret-brace sequences.

## Possible directions (not yet decided)

These are sketches to frame the eventual ticket, not a chosen fix:

- Evaluate `mx:text` *after* `meshChildNodes()` so the produced text node is not part of the subsequent walk.
- Have the `mx:text` branch mark or otherwise exclude its produced text node from re-interpolation.
- Restructure `meshElement()` so engine-produced content is never re-visited by the same pass.

A prerequisite decision: whether non-recursion of results is even the intended contract. The [GUISE-233] discussion has raised the broader question of when included or interpolated content should or should not be further meshed (compare the JSP include-directive versus include-action distinction). Whatever that turns out to be, the two existing paths should either behave consistently or differ deliberately and documentedly. This investigation should determine which, then file a ticket capturing the decision and the fix.

## Next step

Reproduce the divergence with a focused test (a context variable whose value contains a literal `^{…}` sequence, rendered both inline and via `mx:text`), confirm the behavior, and open a ticket if the divergence is confirmed as unintended. Keep this separate from the [GUISE-233] feature work so the existing-behavior fix is not entangled with the new-feature design.
