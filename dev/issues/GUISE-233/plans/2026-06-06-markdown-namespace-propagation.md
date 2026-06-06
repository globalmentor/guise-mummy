# [GUISE-233] Plan: Markdown Embedded HTML Namespace Propagation Test

## Overview

This plan is monolithic; the work is a focused addition of namespace-propagation tests to `MarkdownPageMummifierTest`. It verifies that an XML namespace declared on an HTML element embedded in Markdown — a CommonMark HTML block — passes through the Flexmark → `DocumentBuilder` pipeline as a true XML namespace, accessible via DOM namespace APIs, across the CommonMark HTML block types that yield namespaced elements.

The tests mitigate a risk identified in the [GUISE-233] literal-content-mode plan: `mx:content-mode` is useful in Markdown sources precisely because authors can embed `mx:`-attributed HTML blocks; the namespace propagation behavior that makes this possible has no test coverage. If it silently does not work, the Mesh feature lands correctly but the motivating use case fails.

- Step 1: Add namespace propagation tests for CommonMark HTML block types 1 and 6 to `MarkdownPageMummifierTest` ([specification](#step-1-tests))

## Step 1: Tests

Add namespace-propagation tests to `mummy/src/test/java/dev/guise/mummy/mummify/page/MarkdownPageMummifierTest.java`, one per CommonMark HTML block type under test.

**Scope.** Each test calls `loadSourceDocument(mummyContext, inputStream, "test.md")` directly. This exercises exactly: Markdown → Flexmark render → XHTML wrapper → namespace-aware `DocumentBuilder.parse`. No template application, no Mesh, no mummification pipeline.

**CommonMark HTML block types.** CommonMark §4.6 defines seven kinds of HTML block, each passed through raw and unescaped. Two are relevant here because they produce DOM elements that can carry namespaced attributes:

- **Type 6** — a line beginning with `<` or `</` followed by a tag name in the CommonMark type-6 list (which includes `aside`, `div`, `section`, and most block-level element names). The block ends at the next blank line, the end of the containing block, or the end of the document. A single-line `<aside …>content</aside>` is a complete type-6 block.
- **Type 1** — a line beginning with `<pre`, `<script`, `<style`, or `<textarea` (a raw-text element). The block ends at the line containing the matching close tag (`</pre>`, etc.). The content between is raw text that may span multiple lines, with the close tag on a separate line; this multi-line raw-text span is the parse path that distinguishes type 1 from type 6.

Both types can interrupt a paragraph, so the blank lines around the embedded blocks in the test inputs are conventional formatting, not required for block recognition. Only CommonMark type 7 (a complete open tag with any other tag name) cannot interrupt a paragraph; it is out of scope (see *Skip / Do Not Touch*).

**Shared assertion helper.** Both tests parse a Markdown source and assert that the embedded element carries the `foo:bar` attribute resolved in the sentinel namespace. Factor that into a helper (mirroring the existing `assertSimpleBody` helper), inline via `ByteArrayInputStream` (consistent with `testLoadSourceMetadataInvalidYamlWrapsException`; no test resource file needed):

```java
/// Asserts that parsing the given Markdown surfaces an element with the given local name in the XHTML namespace,
/// carrying a `foo:bar="sentinel"` attribute resolved in the namespace `https://example.com/ns/test/`.
/// @param markdown The Markdown source whose embedded HTML block declares the namespace.
/// @param elementLocalName The local name of the embedded element expected to carry the namespaced attribute.
/// @throws IOException if the Markdown cannot be parsed.
protected void assertEmbeddedHtmlNamespacePropagates(@NonNull final String markdown, @NonNull final String elementLocalName) throws IOException {
	final MarkdownPageMummifier mummifier = new MarkdownPageMummifier();
	final Document document;
	try (final InputStream inputStream = new ByteArrayInputStream(markdown.getBytes(UTF_8))) {
		document = mummifier.loadSourceDocument(mummyContext, inputStream, "test.md");
	}
	final Element body = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
	final Element embedded = childElementsByNameNS(body, XHTML_NAMESPACE_URI_STRING, elementLocalName).findFirst().orElseThrow(AssertionError::new);
	assertThat("namespaced attribute resolved in its namespace", embedded.hasAttributeNS("https://example.com/ns/test/", "bar"), is(true));
	assertThat("namespaced attribute value", embedded.getAttributeNS("https://example.com/ns/test/", "bar"), is("sentinel"));
}
```

The helper finds the embedded element by namespace and local name rather than by position, so it is robust to any preceding heading or other body content.

**Type 6 test (`<aside>`).** A single-line type-6 block:

```java
/// Tests that an XML namespace declared on a CommonMark type-6 HTML block (`<aside>`) embedded in Markdown
/// is resolved into the parsed DOM tree, making the namespaced attribute accessible via DOM namespace APIs.
/// @see MarkdownPageMummifier#loadSourceDocument(MummyContext, InputStream, String)
@Test
void testType6HtmlBlockNamespacePropagatesToDom() throws IOException {
	assertEmbeddedHtmlNamespacePropagates("""
			# Heading

			<aside xmlns:foo="https://example.com/ns/test/" foo:bar="sentinel">content</aside>
			""", ELEMENT_ASIDE);
}
```

**Type 1 test (`<pre>`).** A multi-line type-1 raw-text block with the namespaced attribute on the open tag, raw content on intervening lines, and `</pre>` on its own line:

```java
/// Tests that an XML namespace declared on a CommonMark type-1 HTML block (`<pre>`, a raw-text element
/// whose close tag sits on a separate line with content in between) is resolved into the parsed DOM tree.
/// @see MarkdownPageMummifier#loadSourceDocument(MummyContext, InputStream, String)
@Test
void testType1HtmlBlockNamespacePropagatesToDom() throws IOException {
	assertEmbeddedHtmlNamespacePropagates("""
			# Heading

			<pre xmlns:foo="https://example.com/ns/test/" foo:bar="sentinel">
			first line
			second line
			</pre>
			""", ELEMENT_PRE);
}
```

**Setup.** The `mummyContext` from `@BeforeEach` uses `FakeMummyContext extends BaseMummyContext`, which provides `BaseMummyContext.newPageDocumentBuilder()` — a real, namespace-aware `DocumentBuilder`. No additional setup is needed.

**Imports.** All required symbols (`ByteArrayInputStream`, `ELEMENT_ASIDE`, `ELEMENT_PRE`, `XHTML_NAMESPACE_URI_STRING`, `childElementsByNameNS`, `findHtmlBodyElement`, `UTF_8`) are already imported in the test file (`ELEMENT_PRE` via `testCodeMarkdownLanguage`; `childElementsByNameNS` via `findFirstParagraphText`). No new imports are needed.

**What the tests confirm.** The critical assertions are `hasAttributeNS` and `getAttributeNS` on the embedded element — verifying that `xmlns:foo` on the embedded block was parsed by the namespace-aware `DocumentBuilder` into a real namespace binding. If Flexmark had escaped or modified the HTML block, or if the `DocumentBuilder` were not namespace-aware, one or both assertions would fail. Covering both type 6 (single-line, tag-name list) and type 1 (multi-line raw-text, separate close tag) confirms the propagation holds across the two distinct CommonMark block parse paths that yield namespaced elements.

## Skip / Do Not Touch

- **CommonMark type 7 HTML blocks.** A complete open tag whose tag name is not in the type-1 or type-6 lists is a type-7 block, which alone among the block types cannot interrupt a paragraph and must be separated from preceding text by a blank line. Type 7 is the path a *custom-named* embedded element would take, rather than a namespaced attribute on a standard element. It is not the motivating case for `mx:content-mode` — which places namespaced attributes on standard HTML elements (types 1 and 6) — and is deliberately left untested here. A separate test would be warranted if custom namespaced *element names* embedded in Markdown become a supported authoring pattern.
