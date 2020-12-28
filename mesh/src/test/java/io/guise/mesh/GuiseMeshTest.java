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
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.w3c.dom.*;

/**
 * Tests of {@link GuiseMesh}.
 * @author Garret Wilson
 */
public class GuiseMeshTest {

	/**
	 * Sanity test for document meshing using <code>mx:text</code>.
	 * @see GuiseMesh#meshDocument(MeshContext, Document)
	 */
	@Test
	void mxTextShouldSetElementTextContentInDocument() throws IOException {
		final Document document = createXHTMLDocument("Test Document");
		final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
		final Element h1Element = appendElement(bodyElement, ELEMENT_H(1), "Dummy Heading");
		setAttribute(h1Element, GuiseMesh.ATTRIBUTE_TEXT.withPrefix(GuiseMesh.NAMESPACE_PREFIX), "'Result: '+foo.bar");
		final GuiseMesh guiseMesh = new GuiseMesh();
		final MeshContext meshContext = new MapMeshContext(Map.of("foo", Map.of("bar", "Success")));
		guiseMesh.meshDocument(meshContext, document);
		assertThat(h1Element.getTextContent(), is("Result: Success"));
	}

	/**
	 * <code>mx:text</code>
	 * @see GuiseMesh#meshElement(MeshContext, Element)
	 */
	@Test
	void mxTextShouldSetElementTextContent() throws IOException {
		final Document document = createXHTMLDocument("Test Document");
		final Element bodyElement = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
		final Element h1Element = appendElement(bodyElement, ELEMENT_H(1), "Dummy Heading");
		setAttribute(h1Element, GuiseMesh.ATTRIBUTE_TEXT.withPrefix(GuiseMesh.NAMESPACE_PREFIX), "'Result: '+foo.bar");
		final GuiseMesh guiseMesh = new GuiseMesh();
		final MeshContext meshContext = new MapMeshContext(Map.of("foo", Map.of("bar", "Success")));
		guiseMesh.meshElement(meshContext, h1Element);
		assertThat(h1Element.getTextContent(), is("Result: Success"));
		assertThat(hasAttribute(h1Element, GuiseMesh.ATTRIBUTE_TEXT), is(false));
	}

}
