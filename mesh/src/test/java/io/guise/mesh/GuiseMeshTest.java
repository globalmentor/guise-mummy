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

package io.guise.mesh;

import static com.globalmentor.html.HtmlDom.*;
import static com.globalmentor.html.def.HTML.*;
import static com.globalmentor.xml.XmlDom.*;
import static io.guise.mesh.GuiseMesh.*;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.*;

import org.junit.jupiter.api.Test;
import org.w3c.dom.*;

import com.globalmentor.html.HtmlSerializer;
import com.globalmentor.xml.def.NsName;

/**
 * Tests of {@link GuiseMesh}.
 * @author Garret Wilson
 */
public class GuiseMeshTest {

	/**
	 * @see GuiseMesh#ATTRIBUTE_MUTATION_NAME_PATTERN
	 * @see GuiseMesh#ATTRIBUTE_MUTATION_NAME_PATTERN_NAME_GROUP
	 */
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

	/**
	 * @see GuiseMesh#ATTRIBUTE_MUTATION_NAME_PATTERN
	 * @see GuiseMesh#ATTRIBUTE_MUTATION_NAME_PATTERN_NAME_GROUP
	 */
	@Test
	void testAttributeMutationNamePatternGroups() {
		final Matcher matcher = GuiseMesh.ATTRIBUTE_MUTATION_NAME_PATTERN.matcher("attr-foo-bar");
		assertThat(matcher.matches(), is(true));
		assertThat(matcher.group(GuiseMesh.ATTRIBUTE_MUTATION_NAME_PATTERN_NAME_GROUP), is("foo-bar"));
	}

	/** <code>mx:attr-*</code> */
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

	/** <code>mx:attr-*</code> */
	@Test
	void verifyAttributeMutationNullRemovesAttribute() throws IOException {
		final Document document = createXHTMLDocument("Test Document");
		final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
		final Element h1Element = appendElement(bodyElement, ELEMENT_H(1), "Dummy Heading");
		h1Element.setAttributeNS(null, ATTRIBUTE_TITLE, "Dummy Title");
		//Object.class.getSuperclass() is guaranteed to return null as per the API
		setAttribute(h1Element, NsName.of(NAMESPACE_STRING, "attr-title"), "foo.superclass");
		new GuiseMesh().meshDocument(MeshContext.create(Map.of("foo", Object.class)), document);
		assertThat(h1Element.getTextContent(), is("Dummy Heading"));
		assertThat(h1Element.hasAttributeNS(null, ATTRIBUTE_TITLE), is(false));
		assertThat(h1Element.hasAttributeNS(NAMESPACE_STRING, "attr-title"), is(false));
		assertThat(new HtmlSerializer().serialize(document),
				is("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>Test Document</title></head><body><h1>Dummy Heading</h1></body></html>"));
	}

	/** <code>mx:attr-*</code> */
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

	/** <code>mx:attr-*</code> */
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

	/** <code>mx:each</code> */
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

	/** <code>mx:each</code> */
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

	/** <code>mx:each</code> */
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

	/** <code>mx:each</code> */
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

	/** <code>mx:each</code> */
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

	/** <code>mx:each</code> */
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

	/** <code>mx:text</code> */
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

}
