/*
 * Copyright © 2020 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.mesh;

import static com.globalmentor.html.HtmlDom.*;
import static com.globalmentor.html.spec.HTML.*;
import static com.globalmentor.xml.XmlDom.*;
import static io.guise.mesh.GuiseMesh.*;
import static java.util.Spliterator.*;
import static java.util.Spliterators.*;
import static java.util.stream.Collectors.*;
import static java.util.stream.StreamSupport.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.util.*;

import org.junit.jupiter.api.Test;
import org.w3c.dom.*;

import com.globalmentor.html.HtmlSerializer;
import com.globalmentor.xml.spec.NsName;

/**
 * Tests of {@link GuiseMesh}.
 * @author Garret Wilson
 */
public class GuiseMeshTest {

	/** @see GuiseMesh#toIterator(Object) */
	@Test
	void testToIteratorSupportsArrays() {
		assertThat("String[]", stream(spliteratorUnknownSize(GuiseMesh.toIterator(new String[] {"one", "two", "three"}), ORDERED), false).collect(toList()),
				contains("one", "two", "three"));
		assertThat("Integer[]", stream(spliteratorUnknownSize(GuiseMesh.toIterator(new Integer[] {1, 2, 3}), ORDERED), false).collect(toList()), contains(1, 2, 3));
		assertThat("int[]", stream(spliteratorUnknownSize(GuiseMesh.toIterator(new int[] {1, 2, 3}), ORDERED), false).collect(toList()), contains(1, 2, 3));
		assertThat("long[]", stream(spliteratorUnknownSize(GuiseMesh.toIterator(new long[] {1L, 2L, 3L}), ORDERED), false).collect(toList()), contains(1L, 2L, 3L));
		assertThat("double[]", stream(spliteratorUnknownSize(GuiseMesh.toIterator(new double[] {1.0, 2.0, 3.0}), ORDERED), false).collect(toList()),
				contains(1.0, 2.0, 3.0));
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
			setAttribute(liElement, ATTRIBUTE_TEXT.withPrefix(NAMESPACE_PREFIX), itemVarAttribute.orElse(DEFAULT_ITEM_VAR));
			final GuiseMesh guiseMesh = new GuiseMesh();
			final MeshContext meshContext = new MapMeshContext();
			meshContext.setVariable("list", List.of("foo", "bar"));
			guiseMesh.meshDocument(meshContext, document);
			assertThat(new HtmlSerializer().serialize(document),
					is("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>Test</title></head><body><ul><li>foo</li><li>bar</li></ul></body></html>"));
		}
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
			setAttribute(liElement, ATTRIBUTE_TEXT.withPrefix(NAMESPACE_PREFIX), indexVarAttribute.orElse(DEFAULT_INDEX_VAR));
			final GuiseMesh guiseMesh = new GuiseMesh();
			final MeshContext meshContext = new MapMeshContext();
			meshContext.setVariable("list", List.of("foo", "bar"));
			guiseMesh.meshDocument(meshContext, document);
			assertThat(new HtmlSerializer().serialize(document),
					is("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>Test</title></head><body><ul><li>0</li><li>1</li></ul></body></html>"));
		}
	}

	/** <code>mx:text</code> */
	@Test
	void testMxText() throws IOException {
		final Document document = createXHTMLDocument("Test Document");
		final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
		final Element h1Element = appendElement(bodyElement, ELEMENT_H(1), "Dummy Heading");
		setAttribute(h1Element, ATTRIBUTE_TEXT.withPrefix(NAMESPACE_PREFIX), "'Result: '+foo.bar");
		final GuiseMesh guiseMesh = new GuiseMesh();
		final MeshContext meshContext = new MapMeshContext(Map.of("foo", Map.of("bar", "Success")));
		guiseMesh.meshDocument(meshContext, document);
		assertThat(h1Element.getTextContent(), is("Result: Success"));
		assertThat(hasAttribute(h1Element, ATTRIBUTE_TEXT), is(false));
		assertThat(new HtmlSerializer().serialize(document),
				is("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>Test Document</title></head><body><h1>Result: Success</h1></body></html>"));
	}

}
