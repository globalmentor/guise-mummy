/*
 * Copyright © 2020 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.guise.mesh;

import static com.globalmentor.html.HtmlDom.*;
import static com.globalmentor.html.def.HTML.*;
import static com.globalmentor.java.Enums.*;
import static com.globalmentor.xml.XmlDom.*;
import static com.github.npathai.hamcrestopt.OptionalMatchers.*;
import static dev.guise.mesh.GuiseMesh.*;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.*;

import org.junit.jupiter.api.Test;
import org.w3c.dom.*;

import com.globalmentor.html.HtmlSerializer;
import com.globalmentor.xml.def.NsName;

/// Tests of [GuiseMesh].
/// @author Garret Wilson
public class GuiseMeshTest {

	/// @see GuiseMesh#ATTRIBUTE_MUTATION_NAME_PATTERN
	/// @see GuiseMesh#ATTRIBUTE_MUTATION_NAME_PATTERN_NAME_GROUP
	@Test
	void testAttributeMutationNamePattern() {
		assertThat(GuiseMesh.ATTRIBUTE_MUTATION_NAME_PATTERN.matcher("").matches(), is(false));
		assertThat(GuiseMesh.ATTRIBUTE_MUTATION_NAME_PATTERN.matcher("x").matches(), is(false));
		assertThat(GuiseMesh.ATTRIBUTE_MUTATION_NAME_PATTERN.matcher("foo").matches(), is(false));
		assertThat(GuiseMesh.ATTRIBUTE_MUTATION_NAME_PATTERN.matcher("attr").matches(), is(false));
		assertThat(GuiseMesh.ATTRIBUTE_MUTATION_NAME_PATTERN.matcher("attr-").matches(), is(false));
		assertThat(GuiseMesh.ATTRIBUTE_MUTATION_NAME_PATTERN.matcher("attr-9").matches(), is(false));
		assertThat(GuiseMesh.ATTRIBUTE_MUTATION_NAME_PATTERN.matcher("attr-x").matches(), is(true));
		assertThat(GuiseMesh.ATTRIBUTE_MUTATION_NAME_PATTERN.matcher("attr-x7").matches(), is(true));
		assertThat(GuiseMesh.ATTRIBUTE_MUTATION_NAME_PATTERN.matcher("attr-foo").matches(), is(true));
		assertThat(GuiseMesh.ATTRIBUTE_MUTATION_NAME_PATTERN.matcher("attr-foo5").matches(), is(true));
		assertThat(GuiseMesh.ATTRIBUTE_MUTATION_NAME_PATTERN.matcher("attr-foo$").matches(), is(false));
		assertThat(GuiseMesh.ATTRIBUTE_MUTATION_NAME_PATTERN.matcher("attr-fooBar").matches(), is(true));
		assertThat(GuiseMesh.ATTRIBUTE_MUTATION_NAME_PATTERN.matcher("attr-foo-bar").matches(), is(true));
		assertThat(GuiseMesh.ATTRIBUTE_MUTATION_NAME_PATTERN.matcher("attr-foo_bar").matches(), is(true));
	}

	/// @see GuiseMesh#ATTRIBUTE_MUTATION_NAME_PATTERN
	/// @see GuiseMesh#ATTRIBUTE_MUTATION_NAME_PATTERN_NAME_GROUP
	@Test
	void testAttributeMutationNamePatternGroups() {
		final Matcher matcher = GuiseMesh.ATTRIBUTE_MUTATION_NAME_PATTERN.matcher("attr-foo-bar");
		assertThat(matcher.matches(), is(true));
		assertThat(matcher.group(GuiseMesh.ATTRIBUTE_MUTATION_NAME_PATTERN_NAME_GROUP), is("foo-bar"));
	}

	/// `mx:attr-*`
	@Test
	void testAttributeMutation() throws IOException {
		final Document document = createXHTMLDocument("Test Document");
		final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
		final Element h1Element = appendElement(bodyElement, ELEMENT_H(1), "Dummy Heading");
		h1Element.setAttributeNS(null, ATTRIBUTE_TITLE, "Dummy Title");
		setAttribute(h1Element, NsName.of(NAMESPACE_STRING, "attr-title"), "'Result: '+foo.bar");
		new GuiseMesh().meshDocument(MeshContext.create(Map.of("foo", Map.of("bar", "Success"))), document);
		assertThat(h1Element.getTextContent(), is("Dummy Heading"));
		assertThat(h1Element.getAttributeNS(null, ATTRIBUTE_TITLE), is("Result: Success"));
		assertThat(h1Element.hasAttributeNS(NAMESPACE_STRING, "attr-title"), is(false));
		assertThat(new HtmlSerializer().serialize(document), is(
				"<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>Test Document</title></head><body><h1 title=\"Result: Success\">Dummy Heading</h1></body></html>"));
	}

	/// `mx:attr-*`
	@Test
	void verifyAttributeMutationNullRemovesAttribute() throws IOException {
		final Document document = createXHTMLDocument("Test Document");
		final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
		final Element h1Element = appendElement(bodyElement, ELEMENT_H(1), "Dummy Heading");
		h1Element.setAttributeNS(null, ATTRIBUTE_TITLE, "Dummy Title");
		// A map lookup for a missing key returns `null` in JEXL.
		setAttribute(h1Element, NsName.of(NAMESPACE_STRING, "attr-title"), "foo.missing");
		new GuiseMesh().meshDocument(MeshContext.create(Map.of("foo", Map.of())), document);
		assertThat(h1Element.getTextContent(), is("Dummy Heading"));
		assertThat(h1Element.hasAttributeNS(null, ATTRIBUTE_TITLE), is(false));
		assertThat(h1Element.hasAttributeNS(NAMESPACE_STRING, "attr-title"), is(false));
		assertThat(new HtmlSerializer().serialize(document),
				is("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>Test Document</title></head><body><h1>Dummy Heading</h1></body></html>"));
	}

	/// `mx:attr-*`
	@Test
	void verifyAttributeMutationBooleanTrueSetsFlagValue() throws IOException {
		final Document document = createXHTMLDocument("Test Document");
		final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
		final Element h1Element = appendElement(bodyElement, ELEMENT_H(1), "Dummy Heading");
		h1Element.setAttributeNS(null, "flag", "Dummy Flag");
		setAttribute(h1Element, NsName.of(NAMESPACE_STRING, "attr-flag"), "foo");
		new GuiseMesh().meshDocument(MeshContext.create(Map.of("foo", true)), document);
		assertThat(h1Element.getTextContent(), is("Dummy Heading"));
		assertThat(h1Element.getAttributeNS(null, "flag"), is("flag"));
		assertThat(h1Element.hasAttributeNS(NAMESPACE_STRING, "attr-flag"), is(false));
		assertThat(new HtmlSerializer().serialize(document),
				is("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>Test Document</title></head><body><h1 flag>Dummy Heading</h1></body></html>"));
	}

	/// `mx:attr-*`
	@Test
	void verifyAttributeMutationBooleanFalseRemovesAttribute() throws IOException {
		final Document document = createXHTMLDocument("Test Document");
		final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
		final Element h1Element = appendElement(bodyElement, ELEMENT_H(1), "Dummy Heading");
		h1Element.setAttributeNS(null, "flag", "Dummy Flag");
		setAttribute(h1Element, NsName.of(NAMESPACE_STRING, "attr-flag"), "foo");
		new GuiseMesh().meshDocument(MeshContext.create(Map.of("foo", false)), document);
		assertThat(h1Element.getTextContent(), is("Dummy Heading"));
		assertThat(h1Element.hasAttributeNS(null, "flag"), is(false));
		assertThat(h1Element.hasAttributeNS(NAMESPACE_STRING, "attr-flag"), is(false));
		assertThat(new HtmlSerializer().serialize(document),
				is("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>Test Document</title></head><body><h1>Dummy Heading</h1></body></html>"));
	}

	/// `mx:each`
	@Test
	void testMxEachWithIndexVar() throws IOException {
		for(final Optional<String> indexVarAttribute : List.of(Optional.<String>empty(), Optional.of("index"))) { //test both default and explicit index variable
			final Document document = createXHTMLDocument("Test");
			final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
			final Element ulElement = appendElement(bodyElement, NsName.of(XHTML_NAMESPACE_URI_STRING, ELEMENT_UL));
			final Element liElement = appendElement(ulElement, NsName.of(XHTML_NAMESPACE_URI_STRING, ELEMENT_LI));
			setAttribute(liElement, ATTRIBUTE_EACH.withPrefix(NAMESPACE_PREFIX), "list");
			indexVarAttribute.ifPresent(indexVar -> setAttribute(liElement, ATTRIBUTE_INDEX_VAR.withPrefix(NAMESPACE_PREFIX), indexVar));
			setAttribute(liElement, ATTRIBUTE_TEXT.withPrefix(NAMESPACE_PREFIX), indexVarAttribute.orElse(DEFAULT_INDEX_VAR)); //mx:text for updating value
			new GuiseMesh().meshDocument(MeshContext.create(Map.of("list", List.of("foo", "bar"))), document);
			assertThat(new HtmlSerializer().serialize(document),
					is("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>Test</title></head><body><ul><li>0</li><li>1</li></ul></body></html>"));
		}
	}

	/// `mx:each`
	@Test
	void testMxEachWithItemVar() throws IOException {
		for(final Optional<String> itemVarAttribute : List.of(Optional.<String>empty(), Optional.of("item"))) { //test both default and explicit item variable
			final Document document = createXHTMLDocument("Test");
			final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
			final Element ulElement = appendElement(bodyElement, NsName.of(XHTML_NAMESPACE_URI_STRING, ELEMENT_UL));
			final Element liElement = appendElement(ulElement, NsName.of(XHTML_NAMESPACE_URI_STRING, ELEMENT_LI));
			setAttribute(liElement, ATTRIBUTE_EACH.withPrefix(NAMESPACE_PREFIX), "list");
			itemVarAttribute.ifPresent(itemVar -> setAttribute(liElement, ATTRIBUTE_ITEM_VAR.withPrefix(NAMESPACE_PREFIX), itemVar));
			setAttribute(liElement, ATTRIBUTE_TEXT.withPrefix(NAMESPACE_PREFIX), itemVarAttribute.orElse(DEFAULT_ITEM_VAR)); //mx:text for updating value
			new GuiseMesh().meshDocument(MeshContext.create(Map.of("list", List.of("foo", "bar"))), document);
			assertThat(new HtmlSerializer().serialize(document),
					is("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>Test</title></head><body><ul><li>foo</li><li>bar</li></ul></body></html>"));
		}
	}

	/// `mx:each`
	@Test
	void testMxEachWithIterVar() throws IOException {
		for(final Optional<String> iterVarAttribute : List.of(Optional.<String>empty(), Optional.of("iterState"))) { //test both default and explicit item variable
			final Document document = createXHTMLDocument("Test");
			final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
			final Element ulElement = appendElement(bodyElement, NsName.of(XHTML_NAMESPACE_URI_STRING, ELEMENT_UL));
			final Element liElement = appendElement(ulElement, NsName.of(XHTML_NAMESPACE_URI_STRING, ELEMENT_LI));
			setAttribute(liElement, ATTRIBUTE_EACH.withPrefix(NAMESPACE_PREFIX), "list");
			iterVarAttribute.ifPresent(iterVar -> setAttribute(liElement, ATTRIBUTE_ITER_VAR.withPrefix(NAMESPACE_PREFIX), iterVar));
			setAttribute(liElement, ATTRIBUTE_TEXT.withPrefix(NAMESPACE_PREFIX), iterVarAttribute.orElse(DEFAULT_ITER_VAR) + ".current"); //mx:text for updating value
			new GuiseMesh().meshDocument(MeshContext.create(Map.of("list", List.of("foo", "bar"))), document);
			assertThat(new HtmlSerializer().serialize(document),
					is("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>Test</title></head><body><ul><li>foo</li><li>bar</li></ul></body></html>"));
		}
	}

	/// `mx:each`
	@Test
	void verifyMxEachEmptyIterableRemovesIterationElement() throws IOException {
		final Document document = createXHTMLDocument("Test");
		final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
		final Element ulElement = appendElement(bodyElement, NsName.of(XHTML_NAMESPACE_URI_STRING, ELEMENT_UL));
		final Element liElement = appendElement(ulElement, NsName.of(XHTML_NAMESPACE_URI_STRING, ELEMENT_LI));
		setAttribute(liElement, ATTRIBUTE_EACH.withPrefix(NAMESPACE_PREFIX), "list");
		setAttribute(liElement, ATTRIBUTE_TEXT.withPrefix(NAMESPACE_PREFIX), DEFAULT_ITEM_VAR); //mx:text for updating value
		new GuiseMesh().meshDocument(MeshContext.create(Map.of("list", List.of())), document);
		assertThat(new HtmlSerializer().serialize(document),
				is("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>Test</title></head><body><ul></ul></body></html>"));
	}

	/// `mx:each`
	@Test
	void verifyMxEachNullExpressionResultRemovesIterationElement() throws IOException {
		final Document document = createXHTMLDocument("Test");
		final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
		final Element ulElement = appendElement(bodyElement, NsName.of(XHTML_NAMESPACE_URI_STRING, ELEMENT_UL));
		final Element liElement = appendElement(ulElement, NsName.of(XHTML_NAMESPACE_URI_STRING, ELEMENT_LI));
		setAttribute(liElement, ATTRIBUTE_EACH.withPrefix(NAMESPACE_PREFIX), "ref.get()");
		setAttribute(liElement, ATTRIBUTE_TEXT.withPrefix(NAMESPACE_PREFIX), DEFAULT_ITEM_VAR); //mx:text for updating value
		new GuiseMesh().meshDocument(MeshContext.create(Map.of("ref", new AtomicReference<>(null))), document);
		assertThat(new HtmlSerializer().serialize(document),
				is("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>Test</title></head><body><ul></ul></body></html>"));
	}

	/// `mx:each`
	@Test
	void testMxEachNested() throws IOException {
		for(final Optional<String> itemVarAttribute : List.of(Optional.<String>empty(), Optional.of("item"))) { //test both default and explicit item variable
			final Document document = createXHTMLDocument("Test");
			final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
			final Element outerUlElement = appendElement(bodyElement, NsName.of(XHTML_NAMESPACE_URI_STRING, ELEMENT_UL));
			final Element outerLiElement = appendElement(outerUlElement, NsName.of(XHTML_NAMESPACE_URI_STRING, ELEMENT_LI));
			setAttribute(outerLiElement, ATTRIBUTE_EACH.withPrefix(NAMESPACE_PREFIX), "outerList");
			itemVarAttribute.ifPresent(itemVar -> setAttribute(outerLiElement, ATTRIBUTE_ITEM_VAR.withPrefix(NAMESPACE_PREFIX), itemVar));
			outerLiElement.setAttributeNS(null, ATTRIBUTE_TITLE, format("^{%s}", itemVarAttribute.orElse(DEFAULT_ITEM_VAR))); //<li title="N"> 
			final Element innerUlElement = appendElement(outerLiElement, NsName.of(XHTML_NAMESPACE_URI_STRING, ELEMENT_UL));
			final Element innerLiElement = appendElement(innerUlElement, NsName.of(XHTML_NAMESPACE_URI_STRING, ELEMENT_LI));
			setAttribute(innerLiElement, ATTRIBUTE_EACH.withPrefix(NAMESPACE_PREFIX), "innerList");
			itemVarAttribute.ifPresent(itemVar -> setAttribute(innerLiElement, ATTRIBUTE_ITEM_VAR.withPrefix(NAMESPACE_PREFIX), itemVar));
			setAttribute(innerLiElement, ATTRIBUTE_TEXT.withPrefix(NAMESPACE_PREFIX), itemVarAttribute.orElse(DEFAULT_ITEM_VAR));
			new GuiseMesh().meshDocument(MeshContext.create(Map.of("outerList", List.of("1", "2"), "innerList", List.of("A", "B"))), document);
			assertThat("TODO Outer item variable is shadowed but returns after nested inner iteration.", new HtmlSerializer().serialize(document),
					is("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>Test</title></head><body><ul><li title=\"1\"><ul><li>A</li><li>B</li></ul></li>"
							+ "<li title=\"2\"><ul><li>A</li><li>B</li></ul></li></ul></body></html>"));
		}
	}

	/// `mx:text`
	@Test
	void testMxText() throws IOException {
		final Document document = createXHTMLDocument("Test Document");
		final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
		final Element h1Element = appendElement(bodyElement, ELEMENT_H(1), "Dummy Heading");
		setAttribute(h1Element, ATTRIBUTE_TEXT.withPrefix(NAMESPACE_PREFIX), "'Result: '+foo.bar");
		new GuiseMesh().meshDocument(MeshContext.create(Map.of("foo", Map.of("bar", "Success"))), document);
		assertThat(h1Element.getTextContent(), is("Result: Success"));
		assertThat(hasAttribute(h1Element, ATTRIBUTE_TEXT), is(false));
		assertThat(new HtmlSerializer().serialize(document),
				is("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>Test Document</title></head><body><h1>Result: Success</h1></body></html>"));
	}

	//interpolation

	@Test
	void testAttributeInterpolation() throws IOException {
		final Document document = createXHTMLDocument("Test Document");
		final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
		final Element h1Element = appendElement(bodyElement, ELEMENT_H(1), "Dummy Heading");
		h1Element.setAttributeNS(null, ATTRIBUTE_TITLE, "Result: ^{foo.bar}");
		new GuiseMesh().meshDocument(MeshContext.create(Map.of("foo", Map.of("bar", "Success"))), document);
		assertThat(h1Element.getTextContent(), is("Dummy Heading"));
		assertThat(h1Element.getAttributeNS(null, ATTRIBUTE_TITLE), is("Result: Success"));
		assertThat(new HtmlSerializer().serialize(document), is(
				"<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>Test Document</title></head><body><h1 title=\"Result: Success\">Dummy Heading</h1></body></html>"));
	}

	@Test
	void testTextInterpolation() throws IOException {
		final Document document = createXHTMLDocument("Test Document");
		final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
		final Element h1Element = appendElement(bodyElement, ELEMENT_H(1), "Result: ^{foo.bar}");
		new GuiseMesh().meshDocument(MeshContext.create(Map.of("foo", Map.of("bar", "Success"))), document);
		assertThat(h1Element.getTextContent(), is("Result: Success"));
		assertThat(new HtmlSerializer().serialize(document),
				is("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>Test Document</title></head><body><h1>Result: Success</h1></body></html>"));
	}

	@Test
	void testCDATAInterpolation() throws IOException {
		final Document document = createXHTMLDocument("Test Document");
		final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
		final CDATASection cdataSection = document.createCDATASection("Result: ^{foo.bar}");
		bodyElement.appendChild(cdataSection);
		new GuiseMesh().meshDocument(MeshContext.create(Map.of("foo", Map.of("bar", "Success"))), document);
		assertThat(cdataSection.getData(), is("Result: Success"));
		assertThat(new HtmlSerializer().serialize(document),
				is("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>Test Document</title></head><body><![CDATA[Result: Success]]></body></html>"));
	}

	@Test
	void testCommentInterpolation() throws IOException {
		final Document document = createXHTMLDocument("Test Document");
		final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
		final Comment comment = document.createComment("Result: ^{foo.bar}");
		bodyElement.appendChild(comment);
		new GuiseMesh().meshDocument(MeshContext.create(Map.of("foo", Map.of("bar", "Success"))), document);
		assertThat(comment.getData(), is("Result: Success"));
		assertThat(new HtmlSerializer().serialize(document),
				is("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>Test Document</title></head><body><!--Result: Success--></body></html>"));
	}

	//## `mx:content-as`

	/// Tests that [GuiseMesh#findContentAs(Element)] finds declared content interpretation: own declaration, ancestor inheritance, nearest-wins precedence, empty when none declared, and stamped orphan.
	@Test
	void testFindContentAs() {
		final Document document = createXHTMLDocument("Test");
		final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
		final Element divElement = appendElement(bodyElement, NsName.of(XHTML_NAMESPACE_URI_STRING, "div"));
		final Element spanElement = appendElement(divElement, NsName.of(XHTML_NAMESPACE_URI_STRING, "span"));
		assertThat("empty when none declared", GuiseMesh.findContentAs(divElement), is(Optional.empty()));
		assertThat("child empty when none declared", GuiseMesh.findContentAs(spanElement), is(Optional.empty()));
		setAttribute(divElement, ATTRIBUTE_CONTENT_AS, getSerializationName(ContentAs.LITERAL));
		assertThat("own declaration found", GuiseMesh.findContentAs(divElement), isPresentAndIs(ContentAs.LITERAL));
		assertThat("inherited from ancestor", GuiseMesh.findContentAs(spanElement), isPresentAndIs(ContentAs.LITERAL));
		setAttribute(spanElement, ATTRIBUTE_CONTENT_AS, getSerializationName(ContentAs.TEMPLATE));
		assertThat("nearest declaration wins over farther ancestor", GuiseMesh.findContentAs(spanElement), isPresentAndIs(ContentAs.TEMPLATE));
		final Element orphanElement = document.createElementNS(XHTML_NAMESPACE_URI_STRING, "p");
		setAttribute(orphanElement, ATTRIBUTE_CONTENT_AS, getSerializationName(ContentAs.LITERAL));
		assertThat("stamped orphan resolves to stamped value", GuiseMesh.findContentAs(orphanElement), isPresentAndIs(ContentAs.LITERAL));
	}

	/// Tests that `mx:content-as="literal"` suppresses `^{…}` interpolation of direct character-data content, and that the attribute is excised from output.
	@Test
	void testMxContentAsLiteralSuppressesInterpolation() throws IOException {
		final Document document = createXHTMLDocument("Test");
		final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
		final Element preElement = appendElement(bodyElement, NsName.of(XHTML_NAMESPACE_URI_STRING, "pre"), "git fetch origin HEAD^{tree}");
		setAttribute(preElement, ATTRIBUTE_CONTENT_AS.withPrefix(NAMESPACE_PREFIX), getSerializationName(ContentAs.LITERAL));
		new GuiseMesh().meshDocument(MeshContext.create(Map.of()), document);
		assertThat("literal text passes through unchanged", preElement.getTextContent(), is("git fetch origin HEAD^{tree}"));
		assertThat("mx:content-as attribute is absent from output", preElement.hasAttributeNS(NAMESPACE_STRING, "content-as"), is(false));
	}

	/// Tests that `mx:content-as="literal"` is inherited by descendant element content.
	@Test
	void testMxContentAsLiteralInheritedByDescendants() throws IOException {
		final Document document = createXHTMLDocument("Test");
		final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
		final Element divElement = appendElement(bodyElement, NsName.of(XHTML_NAMESPACE_URI_STRING, "div"));
		setAttribute(divElement, ATTRIBUTE_CONTENT_AS.withPrefix(NAMESPACE_PREFIX), getSerializationName(ContentAs.LITERAL));
		final Element preElement = appendElement(divElement, NsName.of(XHTML_NAMESPACE_URI_STRING, "pre"));
		final Element codeElement = appendElement(preElement, NsName.of(XHTML_NAMESPACE_URI_STRING, "code"), "HEAD^{tree}");
		new GuiseMesh().meshDocument(MeshContext.create(Map.of()), document);
		assertThat("descendant text is not interpolated", codeElement.getTextContent(), is("HEAD^{tree}"));
	}

	/// Tests that an explicit `mx:content-as="template"` overrides an inherited `literal` from an ancestor, restoring interpolation.
	@Test
	void testMxContentAsTemplateOverridesInheritedLiteral() throws IOException {
		final Document document = createXHTMLDocument("Test");
		final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
		final Element divElement = appendElement(bodyElement, NsName.of(XHTML_NAMESPACE_URI_STRING, "div"));
		setAttribute(divElement, ATTRIBUTE_CONTENT_AS.withPrefix(NAMESPACE_PREFIX), getSerializationName(ContentAs.LITERAL));
		final Element spanElement = appendElement(divElement, NsName.of(XHTML_NAMESPACE_URI_STRING, "span"), "^{greeting}");
		setAttribute(spanElement, ATTRIBUTE_CONTENT_AS.withPrefix(NAMESPACE_PREFIX), getSerializationName(ContentAs.TEMPLATE));
		new GuiseMesh().meshDocument(MeshContext.create(Map.of("greeting", "Hello")), document);
		assertThat("template override restores interpolation", spanElement.getTextContent(), is("Hello"));
		assertThat("mx:content-as absent from output on override element", spanElement.hasAttributeNS(NAMESPACE_STRING, "content-as"), is(false));
	}

	/// Tests that `mx:content-as="literal"` does not affect interpolation of non-`mx:` attribute values.
	@Test
	void testMxContentAsLiteralDoesNotAffectAttributeInterpolation() throws IOException {
		final Document document = createXHTMLDocument("Test");
		final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
		final Element aElement = appendElement(bodyElement, NsName.of(XHTML_NAMESPACE_URI_STRING, "a"), "HEAD^{tree}");
		aElement.setAttributeNS(null, "href", "^{url}");
		setAttribute(aElement, ATTRIBUTE_CONTENT_AS.withPrefix(NAMESPACE_PREFIX), getSerializationName(ContentAs.LITERAL));
		new GuiseMesh().meshDocument(MeshContext.create(Map.of("url", "https://example.com")), document);
		assertThat("text content is literal", aElement.getTextContent(), is("HEAD^{tree}"));
		assertThat("attribute interpolation still occurs", aElement.getAttributeNS(null, "href"), is("https://example.com"));
	}

	/// Tests that `mx:content-as="literal"` does not suppress `mx:each` structural iteration.
	@Test
	void testMxContentAsLiteralDoesNotSuppressStructuralDirectives() throws IOException {
		final Document document = createXHTMLDocument("Test");
		final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
		final Element ulElement = appendElement(bodyElement, NsName.of(XHTML_NAMESPACE_URI_STRING, ELEMENT_UL));
		setAttribute(ulElement, ATTRIBUTE_CONTENT_AS.withPrefix(NAMESPACE_PREFIX), getSerializationName(ContentAs.LITERAL));
		final Element liElement = appendElement(ulElement, NsName.of(XHTML_NAMESPACE_URI_STRING, ELEMENT_LI), "^{it}");
		setAttribute(liElement, ATTRIBUTE_EACH.withPrefix(NAMESPACE_PREFIX), "items");
		new GuiseMesh().meshDocument(MeshContext.create(Map.of("items", List.of("a", "b", "c"))), document);
		assertThat("structural iteration still produces correct element count with literal text", new HtmlSerializer().serialize(document),
				is("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>Test</title></head><body>"
						+ "<ul><li>^{it}</li><li>^{it}</li><li>^{it}</li></ul></body></html>"));
	}

	/// Tests that `mx:content-as="literal"` suppresses interpolation of CDATA and comment children.
	@Test
	void testMxContentAsLiteralSuppressesCdataAndComment() throws IOException {
		final Document document = createXHTMLDocument("Test");
		final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
		final Element divElement = appendElement(bodyElement, NsName.of(XHTML_NAMESPACE_URI_STRING, "div"));
		setAttribute(divElement, ATTRIBUTE_CONTENT_AS.withPrefix(NAMESPACE_PREFIX), getSerializationName(ContentAs.LITERAL));
		final CDATASection cdataSection = document.createCDATASection("HEAD^{tree}");
		divElement.appendChild(cdataSection);
		final Comment comment = document.createComment("HEAD^{tree}");
		divElement.appendChild(comment);
		new GuiseMesh().meshDocument(MeshContext.create(Map.of()), document);
		assertThat("CDATA data is not interpolated", cdataSection.getData(), is("HEAD^{tree}"));
		assertThat("comment data is not interpolated", comment.getData(), is("HEAD^{tree}"));
	}

	/// Tests that `mx:content-as="expression"` (reserved but not yet implemented) and an unknown value each throw [MeshException]: the former at usage, the latter at attribute discovery.
	@Test
	void testMxContentAsUnknownOrUnimplementedValueThrows() {
		{
			final Document document = createXHTMLDocument("Test");
			final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
			final Element divElement = appendElement(bodyElement, NsName.of(XHTML_NAMESPACE_URI_STRING, "div"), "text");
			setAttribute(divElement, ATTRIBUTE_CONTENT_AS.withPrefix(NAMESPACE_PREFIX), "expression");
			assertThrows(MeshException.class, () -> new GuiseMesh().meshDocument(MeshContext.create(Map.of()), document));
		}
		{
			final Document document = createXHTMLDocument("Test");
			final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
			final Element divElement = appendElement(bodyElement, NsName.of(XHTML_NAMESPACE_URI_STRING, "div"), "text");
			setAttribute(divElement, ATTRIBUTE_CONTENT_AS.withPrefix(NAMESPACE_PREFIX), "bogus-unsupported");
			assertThrows(MeshException.class, () -> new GuiseMesh().meshDocument(MeshContext.create(Map.of()), document));
		}
	}

	/// Tests that ordinary interpolation is unchanged in the absence of `mx:content-as`.
	@Test
	void testMxContentAsDefaultIsTemplate() throws IOException {
		final Document document = createXHTMLDocument("Test");
		final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
		appendElement(bodyElement, NsName.of(XHTML_NAMESPACE_URI_STRING, "p"), "Result: ^{foo}");
		new GuiseMesh().meshDocument(MeshContext.create(Map.of("foo", "Success")), document);
		assertThat("interpolation still occurs with no mx:content-as", bodyElement.getTextContent(), is("Result: Success"));
	}

	/// Tests that `mx:content-as="literal"` declared on an ancestor is inherited by `mx:each` clones via clone-stamping.
	@Test
	void testMxContentAsLiteralInheritedByMxEachClones() throws IOException {
		final Document document = createXHTMLDocument("Test");
		final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
		final Element ulElement = appendElement(bodyElement, NsName.of(XHTML_NAMESPACE_URI_STRING, ELEMENT_UL));
		setAttribute(ulElement, ATTRIBUTE_CONTENT_AS.withPrefix(NAMESPACE_PREFIX), getSerializationName(ContentAs.LITERAL));
		final Element liElement = appendElement(ulElement, NsName.of(XHTML_NAMESPACE_URI_STRING, ELEMENT_LI), "^{it}");
		setAttribute(liElement, ATTRIBUTE_EACH.withPrefix(NAMESPACE_PREFIX), "items");
		new GuiseMesh().meshDocument(MeshContext.create(Map.of("items", List.of("a", "b"))), document);
		assertThat("literal interpretation inherited from ancestor through clone boundary", new HtmlSerializer().serialize(document),
				is("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>Test</title></head><body>" + "<ul><li>^{it}</li><li>^{it}</li></ul></body></html>"));
	}

	/// Tests that `mx:content-as="literal"` declared directly on the iterated element survives cloning.
	@Test
	void testMxContentAsLiteralOnIteratedElementSurvivesCloning() throws IOException {
		final Document document = createXHTMLDocument("Test");
		final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
		final Element ulElement = appendElement(bodyElement, NsName.of(XHTML_NAMESPACE_URI_STRING, ELEMENT_UL));
		final Element liElement = appendElement(ulElement, NsName.of(XHTML_NAMESPACE_URI_STRING, ELEMENT_LI), "^{it}");
		setAttribute(liElement, ATTRIBUTE_EACH.withPrefix(NAMESPACE_PREFIX), "items");
		setAttribute(liElement, ATTRIBUTE_CONTENT_AS.withPrefix(NAMESPACE_PREFIX), getSerializationName(ContentAs.LITERAL));
		new GuiseMesh().meshDocument(MeshContext.create(Map.of("items", List.of("a", "b"))), document);
		assertThat("literal on iterated element itself survives cloning", new HtmlSerializer().serialize(document),
				is("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>Test</title></head><body>" + "<ul><li>^{it}</li><li>^{it}</li></ul></body></html>"));
	}

	/// Tests that `mx:content-as="template"` on an iterated element overrides an inherited `literal` from an ancestor on each clone.
	@Test
	void testMxContentAsTemplateOverrideOnIteratedElementInLiteralRegion() throws IOException {
		final Document document = createXHTMLDocument("Test");
		final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
		final Element ulElement = appendElement(bodyElement, NsName.of(XHTML_NAMESPACE_URI_STRING, ELEMENT_UL));
		setAttribute(ulElement, ATTRIBUTE_CONTENT_AS.withPrefix(NAMESPACE_PREFIX), getSerializationName(ContentAs.LITERAL));
		final Element liElement = appendElement(ulElement, NsName.of(XHTML_NAMESPACE_URI_STRING, ELEMENT_LI), "^{it}");
		setAttribute(liElement, ATTRIBUTE_EACH.withPrefix(NAMESPACE_PREFIX), "items");
		setAttribute(liElement, ATTRIBUTE_CONTENT_AS.withPrefix(NAMESPACE_PREFIX), getSerializationName(ContentAs.TEMPLATE));
		new GuiseMesh().meshDocument(MeshContext.create(Map.of("items", List.of("a", "b"))), document);
		assertThat("template override on iterated element restores interpolation", new HtmlSerializer().serialize(document),
				is("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>Test</title></head><body>" + "<ul><li>a</li><li>b</li></ul></body></html>"));
	}

	/// Tests that `mx:content-as="literal"` is preserved across two nested levels of `mx:each`.
	@Test
	void testMxContentAsLiteralInheritedAcrossTwoLevelsOfMxEach() throws IOException {
		final Document document = createXHTMLDocument("Test");
		final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
		final Element outerUlElement = appendElement(bodyElement, NsName.of(XHTML_NAMESPACE_URI_STRING, ELEMENT_UL));
		setAttribute(outerUlElement, ATTRIBUTE_CONTENT_AS.withPrefix(NAMESPACE_PREFIX), getSerializationName(ContentAs.LITERAL));
		final Element outerLiElement = appendElement(outerUlElement, NsName.of(XHTML_NAMESPACE_URI_STRING, ELEMENT_LI));
		setAttribute(outerLiElement, ATTRIBUTE_EACH.withPrefix(NAMESPACE_PREFIX), "outer");
		final Element innerUlElement = appendElement(outerLiElement, NsName.of(XHTML_NAMESPACE_URI_STRING, ELEMENT_UL));
		final Element innerLiElement = appendElement(innerUlElement, NsName.of(XHTML_NAMESPACE_URI_STRING, ELEMENT_LI), "^{it}");
		setAttribute(innerLiElement, ATTRIBUTE_EACH.withPrefix(NAMESPACE_PREFIX), "inner");
		new GuiseMesh().meshDocument(MeshContext.create(Map.of("outer", List.of("x"), "inner", List.of("A", "B"))), document);
		assertThat("literal interpretation survives two nested mx:each clone boundaries", new HtmlSerializer().serialize(document),
				is("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>Test</title></head><body>"
						+ "<ul><li><ul><li>^{it}</li><li>^{it}</li></ul></li></ul></body></html>"));
	}

}
