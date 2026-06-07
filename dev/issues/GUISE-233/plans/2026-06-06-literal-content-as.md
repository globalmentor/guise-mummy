# [GUISE-233] Plan: Literal Content Interpretation

## Overview

This plan is monolithic; the work was not decomposable into independent chunks. All changes are internal to the `mesh` module's `GuiseMesh` (plus its tests and the module readme); no shared interface spanning compilation units changes, so there is no intermediate-state boundary to chunk on.

The plan implements the `literal` subset of the `mx:content-as` feature described in [Content Interpretation](<../designs/Content Interpretation.md>). It adds recognition of `mx:content-as`, supports the `template` (default) and `literal` values with subtree inheritance, suppresses character-data interpolation under `literal`, rejects unsupported values, and removes the attribute from output.

- Step 1: Add `mx:content-as` attribute and value constants ([specification](#step-1-constants))
- Step 2: Add ancestor-walk resolution of the effective content mode; stamp effective mode onto `mx:each` clones at the iteration boundary ([specification](#step-2-effective-mode-resolution))
- Step 3: Apply the mode in `meshChildNodes`; validate and reject unsupported values ([specification](#step-3-apply-the-mode))
- Step 4: Excise `mx:content-as` after child processing ([specification](#step-4-excise-the-attribute))
- Step 5: Tests ([specification](#step-5-tests))
- Step 6: Update the Mesh readme attribute table ([specification](#step-6-readme))

## Step 1: Constants

Add to `GuiseMesh`, alongside the existing attribute constants (`ATTRIBUTE_EACH`, `ATTRIBUTE_TEXT`, etc.) in `mesh/src/main/java/dev/guise/mesh/GuiseMesh.java`:

```java
/// The attribute `mx:content-as` for selecting how an element's textual content is interpreted.
public static final NsName ATTRIBUTE_CONTENT_AS = NsName.of(NAMESPACE_STRING, "content-as");

/// The `mx:content-as` value indicating that content is interpreted as a Mesh template (the default): character data is interpolated.
public static final String CONTENT_AS_TEMPLATE = "template";

/// The `mx:content-as` value indicating that content is interpreted as literal: no interpolation is performed.
public static final String CONTENT_AS_LITERAL = "literal";
```

Define only the two values this ticket implements. `expression` is reserved by the design but is intentionally not declared here, so no unused constant is introduced; a later ticket adds it when it implements that mode.

No test note: declarations only.

## Step 2: Effective Content Interpretation Resolution

Add a package-private static helper to `GuiseMesh` that resolves the effective content interpretation for an element by walking the ancestor chain, returning the nearest declared `mx:content-as` value or `CONTENT_AS_TEMPLATE` when none is declared:

```java
/// Resolves the effective `mx:content-as` content interpretation for an element, inheriting from the nearest ancestor that declares one.
/// @param element The element whose effective content interpretation is desired.
/// @return The nearest declared `mx:content-as` value walking up the ancestor chain, or [#CONTENT_AS_TEMPLATE] if none is declared.
static String findEffectiveContentAs(@NonNull Element element) {
	Element current = element;
	do {
		final Optional<String> foundMode = findAttribute(current, ATTRIBUTE_CONTENT_AS);
		if(foundMode.isPresent()) {
			return foundMode.get();
		}
		current = asInstance(current.getParentNode(), Element.class).orElse(null);
	} while(current != null);
	return CONTENT_AS_TEMPLATE;
}
```

This uses `XmlDom.findAttribute(Element, NsName)` for the per-element lookup and follows the ancestor-walk pattern already present in `XmlDom.getDefinedNamespaceURI()`. The walk reads, but does not excise, the attribute — excision is deferred to Step 4 so ancestors remain visible while their descendants are processed.

`asInstance(...)` is the GlobalMentor `com.globalmentor.java.Objects` helper used elsewhere in `XmlDom`; confirm the existing static import situation in `GuiseMesh` and add the import if needed.

There is no existing GlobalMentor utility for "find an attribute walking up the ancestor chain" (only `getDefinedNamespaceURI` for namespace declarations and `hasAncestorElementNS` for element names). Keep `findEffectiveContentAs` private to `GuiseMesh` for now and add a `// TODO` to extract a general `findAncestorAttribute` utility into `XmlDom` later.

**Test:** `findEffectiveContentAs` is package-private and tested directly — own declaration found; inherited from an ancestor; nearest declaration wins over a farther one; default `template` when none is declared; a stamped clone (attribute set directly on an orphaned element) resolves to the stamped value.

### Stamping onto `mx:each` Clones

`findEffectiveContentMode` walks `getParentNode()` upward; iteration clones produced by `mx:each` are detached at processing time — `cloneNode(true)` severs the parent pointer — so the walk cannot reach an ancestor-declared mode.

Fix: in the `mx:each` iteration branch of `meshElement`, resolve the effective content interpretation from the *original* element (still attached at clone time) and stamp it onto each clone as `mx:content-as` before recursing:

```java
final Element eachElement = (Element)element.cloneNode(true); //mesh a clone of this element; iteration attributes have been removed
// Stamp the effective content interpretation so the clone carries it regardless of detachment.
//TODO consider carrying content interpretation in MeshContext instead, so the DOM is not the carrier (see Rejected Alternatives).
setAttribute(eachElement, ATTRIBUTE_CONTENT_AS, findEffectiveContentAs(element));
result.addAll(meshElement(context, eachElement));
```

`findEffectiveContentAs(clone)` then finds the stamped attribute at step zero before needing a parent pointer. If the original element already declared `mx:content-as` directly, `findEffectiveContentAs(element)` returns that same value — a no-op stamp. `setAttribute(Element, NsName, String)` is already used in `GuiseMesh` for `mx:attr-*`; no new import is needed.

When no interpretation is in effect, `findEffectiveContentAs(element)` returns `CONTENT_AS_TEMPLATE`, so every clone that passes through iteration carries an explicit `mx:content-as` attribute. Step 4's `exciseAttribute` removes it from output regardless of which value was stamped.

## Step 3: Apply the Content Interpretation

In `meshChildNodes(MeshContext, Element)`, resolve the effective content interpretation once at the top of the method (it is identical for all of `element`'s direct character-data children), validate it, and use it to gate interpolation:

```java
public void meshChildNodes(@NonNull MeshContext context, @NonNull final Element element) throws IOException, MeshException, DOMException {
	final MeshInterpolator interpolator = getInterpolator();
	final MexlEvaluator evaluator = getEvaluator();
	final String contentAs = findEffectiveContentAs(element);
	final boolean interpolateContent = switch(contentAs) {
		case CONTENT_AS_TEMPLATE -> true;
		case CONTENT_AS_LITERAL -> false;
		default -> throw new MeshException("Unsupported `mx:content-as` value `%s`.".formatted(contentAs));
	};
	final NodeList childNodes = element.getChildNodes();
	for(int childNodeIndex = 0; childNodeIndex < childNodes.getLength(); childNodeIndex++) {
		final Node childNode = childNodes.item(childNodeIndex);
		if(childNode instanceof CharacterData childCharacterData) { //Text, Comment, or CDATA
			if(interpolateContent) {
				interpolator.findInterpolation(context, childCharacterData.getData(), evaluator).map(Object::toString).ifPresent(childCharacterData::setData);
			}
		} else if(childNode instanceof Element childElement) {
			final List<Element> meshedElements = meshElement(context, childElement);
			replaceChild(element, childElement, meshedElements);
			childNodeIndex += meshedElements.size() - 1; //adjust the index based upon the number of replaced elements (by default the loop advances by one)
		}
	}
}
```

Only character-data interpolation is gated. Child *elements* are still recursed into unconditionally, so structural directives (`mx:each`, `mx:attr-*`, `mx:text`) continue to operate inside a `literal` region, and a descendant that declares `mx:content-as="template"` re-enables interpolation for its own subtree (because `findEffectiveContentAs` finds the nearer declaration).

Validation occurs here on the resolved (nearest) value. Because every element's `meshChildNodes` runs and resolves with that element as the starting point, each element's own declared value is validated when its content is processed, before recursion reaches its descendants; an invalid value therefore throws before any descendant walk could observe it.

The exception message names the offending value but tests must not assert on message text (the contract specifies the exception type, not the wording).

**Test:** covered by the Step 5 cases (literal suppression, inheritance, override, independence from structural directives and attribute interpolation, unsupported-value rejection).

## Step 4: Excise the Attribute

After `meshChildNodes(context, element)` returns in `meshElement`, remove the attribute so it does not appear in output:

```java
meshChildNodes(context, element);

exciseAttribute(element, ATTRIBUTE_CONTENT_AS); //mx:content-as (after child processing, so descendants can inherit it during their meshing)

//TODO remove all mx-related attributes

return List.of(element);
```

Excision is deferred until after child processing precisely so the ancestor walk in Step 2 can observe an ancestor's `mx:content-as` while that ancestor's descendants are being meshed. The existing `// TODO remove all mx-related attributes` remains; this step removes only `mx:content-as` explicitly, consistent with how `mx:each`/`mx:text`/`mx:attr-*` each remove their own attributes.

For the iteration early-return path, no change is needed: the original element is replaced by its meshed clones, and each clone passes through the full `meshElement` (and thus this excision) because the iteration attributes were removed before cloning.

**Test:** Step 5 asserts the attribute is absent from the serialized output.

## Step 5: Tests

Add tests to `mesh/src/test/java/dev/guise/mesh/GuiseMeshTest.java`, following the existing patterns (`createXHTMLDocument`, `findHtmlBodyElement`, `appendElement`, serialize-and-compare with `HtmlSerializer`, Hamcrest matchers). Group related assertions per method with descriptive reasons.

Cases:

- **`literal` suppresses interpolation of direct content.** An element with `mx:content-as="literal"` whose text contains `^{…}` (use the motivating `HEAD^{tree}` shape) serializes with the `^{tree}` intact, and the `mx:content-as` attribute is absent from output.
- **`literal` is inherited by descendant content.** A `<div mx:content-as="literal">` containing a nested element (e.g. `<pre><code>…^{tree}…</code></pre>` shape) leaves the nested text literal.
- **Explicit `template` overrides inherited `literal`.** Within a `literal` region, a descendant element declaring `mx:content-as="template"` has its own `^{…}` interpolated.
- **`literal` does not affect attribute interpolation.** An element with `mx:content-as="literal"` and a non-`mx:` attribute value containing `^{…}` (e.g. `title="^{foo.bar}"`) still has the attribute interpolated while its text content stays literal.
- **`literal` does not suppress structural directives.** `<ul mx:content-as="literal">` with a child `<li mx:each="items">` containing `^{it}`-shaped text still iterates structurally, producing one `<li>` per item with literal (un-interpolated) text. This demonstrates the independence of structural processing from content interpretation.
- **CDATA and comment children are suppressed under `literal`.** Mirror the existing `testCDATAInterpolation`/`testCommentInterpolation` cases with a `literal` ancestor and assert the data is unchanged.
- **Unsupported value throws.** An element with `mx:content-as="expression"` (reserved but unimplemented) and with an arbitrary unknown value each cause `meshDocument` to throw `MeshException`; assert via `assertThrows` on the type only, not the message.
- **Default behavior unchanged.** A sanity case with no `mx:content-as` confirms ordinary interpolation still occurs (this is largely covered by existing tests but is worth one explicit assertion alongside the new ones).
- **`literal` inherited by `mx:each` clones from an ancestor.** `<ul mx:content-as="literal">` containing `<li mx:each="items">^{it}</li>` (with `it` resolvable in context) produces one `<li>` per item with `^{it}` intact. This is the primary test of the clone-stamping mechanism: the interpretation is declared on the `<ul>`, not on the iterated `<li>`, and must survive the clone boundary.
- **`mx:content-as="literal"` on the iterated element itself survives cloning.** `<li mx:each="items" mx:content-as="literal">^{it}</li>` — each clone carries the attribute directly; content is literal. Confirms no double-stamping or interaction issue when the element declares the interpretation itself.
- **`template` override on an iterated element within an ancestor `literal` region.** `<ul mx:content-as="literal">` containing `<li mx:each="items" mx:content-as="template">^{it}</li>` — each clone's own `template` declaration overrides the inherited interpretation, so `^{it}` IS interpolated. Confirm `mx:content-as` is absent from output.
- **Deeply inherited: `literal` across two levels of `mx:each`.** A `literal` ancestor containing an outer `mx:each` element which in turn contains an inner `mx:each` — the interpretation must survive both stamping boundaries: the outer clone is stamped from the ancestor, and the outer clone's child iteration stamps its own clones.

Direct unit tests for `findEffectiveContentAs` per Step 2, including: own declaration found; inherited from an ancestor; nearest declaration wins over a farther one; default `template` when none is declared; a stamped clone (attribute set on an orphaned element) resolves to the stamped value.

## Step 6: Readme

Update the attribute table in `mesh/readme.md` to add a `mx:content-as` row, documenting the `template` (default) and `literal` values and noting that the setting is inherited by descendant content. Keep the readme scoped to what is implemented; the broader design (the `expression` value, `mx:template-engine`, value-converter modes) lives in the design document, not the engine readme, until those are implemented.

No test note: documentation.

## Open Questions and Assumptions

- **`xmlns:mx` in the Markdown wrapper template.** For the motivating Markdown-embedded-HTML use case, the author must currently declare `xmlns:mx="https://guise.dev/name/mesh/"` on the wrapping element (confirmed to work through the Flexmark → namespace-aware `DocumentBuilder` pipeline). Adding `xmlns:mx` to the `XHTML_TEMPLATE` in `MarkdownPageMummifier` would let authors write `mx:content-as` without a per-element namespace declaration. This is an ergonomic enhancement in the `mummy` module, independent of the Mesh-layer feature, and is not required for the feature to function. Decision deferred: include it in this ticket or split to a follow-up.
- **Assumption: error policy for `expression`.** This plan treats `expression` like any other unsupported value (single `MeshException`, no special-casing). If a distinct "reserved but not yet implemented" signal is wanted, it would be a separate message or exception — not currently planned.
- **Assumption: helper stays private.** `findEffectiveContentAs` remains a `GuiseMesh` package-private method with a `// TODO` to extract a general ancestor-attribute utility into `XmlDom`. If the broader utility is wanted now, that becomes an additional step in `globalmentor-web`.

## Rejected Alternatives

- **`mx:text-mode` coupling content processing to `mx:text`.** An earlier formulation had a single attribute governing both `mx:text` values and child content, with defaults of `expression` for `mx:text` and `template` for content. Rejected because it entangles the structural-processing axis (`mx:text` is an expression directive like all `mx:` attributes) with the inline-text-processing axis, and produced multiple ill-defined interactions (what `expression` means for child content; what an inherited `literal` does to a descendant `mx:text`).
- **Ancestor-walk without clone-stamping.** Resolving `mx:content-mode` solely by walking the live-DOM ancestor chain at processing time. Fails for `mx:each` clones: clones are detached at processing time (`cloneNode(true)` produces a parentless node), so the walk cannot reach an ancestor-declared mode. Replaced by the stamping approach in Step 2.
- **Context-propagation (via `MeshContext.nestScope()`) for inheritance.** Threading the mode through the existing scope-nesting mechanism — the same mechanism by which `mx:each` iteration variables already cross the clone boundary — would eliminate dependence on the DOM for traversal-scoped state entirely. Deferred, not adopted, for this ticket: clone-stamping achieves correctness with no new interface surface on `MeshContext`; context-propagation remains a future optimization. A TODO comment at the stamping site records this.
- **A boolean flag (e.g. `mx:no-mesh` / `mx:literal`).** Rejected in favor of an enumerated `content-as` that extends cleanly to `template`/`expression` and aligns with the four-axis model; the industry survey showed enumerated modes are justified only when more than two states exist, which the design's full value space provides.
- **Reusing an HTML/CDATA/comment/processing-instruction signal.** Rejected as syntactic reuse masquerading as semantic reuse: those constructs carry unrelated contracts (preformatting, raw markup, ignore-for-rendering) and none means "do not template this region" at the Mesh layer.
- **`mx:content="literal"` framed as a content/media-type classification.** Rejected because values like `template`, `literal`, and `expression` name interpretive dispositions, not media types; a media-type framing (`text/plain`) does not determine whether interpolation should occur (a literal can still be a template, as JavaScript's template literals show).
- **`mx:content-mode` as prior attribute name.** The attribute was originally named `mx:content-mode`, but "mode" describes an operational state of the engine rather than a classification of how content is interpreted. The values `template`, `literal`, and `expression` have no single action verb in common — each implies a different engine operation — so "mode of processing" does not accurately name the axis. `mx:content-as` was adopted because it reads as the elision of "interpret/read/treat content as …" and completes naturally with all three values; the HTML `<link as>` attribute provides ecosystem precedent for `as` meaning "tell the processor what category to treat this as."

## Skip / Do Not Touch

- **The `mx:text` re-interpolation asymmetry.** Tracked as a separate TODO in this ticket. Do not fix it as part of this work; the content-as feature must not become entangled with that order-of-operations issue.
- **`expression` content mode, `mx:template-engine`, value-converter modes, un-escape.** Documented in the design as future work; not implemented here.
