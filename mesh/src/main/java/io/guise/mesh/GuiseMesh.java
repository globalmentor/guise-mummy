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

import static java.util.Objects.*;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import javax.annotation.*;

import org.w3c.dom.*;

/**
 * Guise template transformation engine.
 * @author Garret Wilson
 */
public class GuiseMesh {

	/** The string form of the namespace of Guise Mesh elements, such as in an XHTML document or as the leading IRI segment of RDFa metadata. */
	public static final String NAMESPACE_STRING = "https://guise.io/name/mesh/";

	/** The namespace of Guise Mesh elements, such as in an XHTML document or as the leading IRI segment of RDFa metadata. */
	public static final URI NAMESPACE = URI.create(NAMESPACE_STRING);

	/** The typical prefix used for the namespace of Guise Mesh elements, such as in an XHTML document or in RDFa metadata. */
	public static final String NAMESPACE_PREFIX = "mx";

	private final MexlEvaluator evaluator;

	/** @return The strategy for evaluating Mesh Expression Language (MEXL) expressions. */
	protected MexlEvaluator getEvaluator() {
		return evaluator;
	}

	/**
	 * Default Mesh Expression Language (MEXL) evaluator constructor.
	 * @implSpec This implementation uses an instance of the {@link JexlMexlEvaluator}.
	 */
	public GuiseMesh() {
		this(JexlMexlEvaluator.INSTANCE);
	}

	/**
	 * Mesh Expression Language (MEXL) evaluator constructor.
	 * @param evaluator The strategy for evaluating MEXL expressions.
	 */
	public GuiseMesh(@Nonnull final MexlEvaluator evaluator) {
		this.evaluator = requireNonNull(evaluator);
	}

	/**
	 * Evaluates and transforms a document.
	 * @implSpec This implementation does not allow the document element to be removed or replaced.
	 * @param context The context of meshing.
	 * @param document The document to mesh.
	 * @return The meshed document, which may or may not be the same document supplied as input.
	 * @throws IllegalArgumentException if the elements have some information that cannot be meshed.
	 * @throws IOException if there is an error meshing the document.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	public Document meshDocument(@Nonnull MeshContext context, @Nonnull final Document document) throws IOException, DOMException {
		final List<Element> meshedElements = meshElement(context, document.getDocumentElement());
		if(meshedElements.size() != 1 || meshedElements.get(0) != document.getDocumentElement()) {
			throw new UnsupportedOperationException("Document element cannot be removed or replaced when meshing a document.");
		}
		return document;
	}

	/**
	 * Evaluates and transforms a document element.
	 * <p>
	 * The element is replaced with the returned elements. If only the same element is returned, no replacement is made. If no element is returned, the source
	 * element is removed.
	 * </p>
	 * @param context The context of meshing.
	 * @param element The element to mesh.
	 * @return The meshed element(s), if any, to replace the original element.
	 * @throws IllegalArgumentException if the element has some information that cannot be meshed.
	 * @throws IOException if there is an error meshing the element.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	public List<Element> meshElement(@Nonnull MeshContext context, @Nonnull final Element element) throws IOException, DOMException {
		meshChildElements(context, element);
		return List.of(element);
	}

	/**
	 * Evaluates and transforms child elements of an existing element.
	 * @param context The context of meshing.
	 * @param element The element the children of which to mesh.
	 * @throws IllegalArgumentException if the elements have some information that cannot be meshed.
	 * @throws IOException if there is an error meshing the child elements.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	public void meshChildElements(@Nonnull MeshContext context, @Nonnull final Element element) throws IOException, DOMException {
		final NodeList childNodes = element.getChildNodes();
		for(int childNodeIndex = 0; childNodeIndex < childNodes.getLength();) { //advance the index manually as needed
			final Node childNode = childNodes.item(childNodeIndex);
			if(!(childNode instanceof Element)) { //skip non-elements
				childNodeIndex++;
				continue;
			}
			final Element childElement = (Element)childNode;
			final List<Element> processedElements = meshElement(context, childElement);
			final int processedElementCount = processedElements.size(); //TODO transfer restructuring logic to XML library
			if(!(processedElementCount == 1 && processedElements.get(0) == childElement)) { //if structural changes were requested
				final Node nextSibling = childElement.getNextSibling();
				final Node parentNode = childElement.getParentNode();
				parentNode.removeChild(childElement); //remove the current child (which may get added back if it is one of the elements returned)
				if(nextSibling != null) { //if we're not at the end, do a complicated reverse insert
					Node refChild = nextSibling; //iterate the processed elements in reverse order, inserting them before the next sibling
					final ListIterator<Element> reverseProcessedElementIterator = processedElements.listIterator(processedElementCount);
					while(reverseProcessedElementIterator.hasPrevious()) {
						final Element processedElement = reverseProcessedElementIterator.previous();
						parentNode.insertBefore(processedElement, refChild); //insert the processed element in the earlier position
						refChild = processedElement; //the newly inserted element becomes the new reference for the next insertion
					}
				} else { //if we're at the end of the list
					processedElements.forEach(parentNode::appendChild); //just append the processed elements normally
				}
			}
			childNodeIndex += processedElementCount; //manually advance the index based upon the replacement nodes
		}
	}

}
