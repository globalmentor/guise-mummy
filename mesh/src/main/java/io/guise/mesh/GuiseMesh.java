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

import static com.globalmentor.xml.XmlDom.*;
import static java.lang.String.format;
import static java.util.Arrays.*;
import static java.util.Objects.*;
import static org.zalando.fauxpas.FauxPas.*;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Stream;

import javax.annotation.*;

import org.w3c.dom.*;

import com.globalmentor.xml.spec.NsName;

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

	//# attributes

	/** The attribute <code>mx:each</code> for iteration source. */
	public static final NsName ATTRIBUTE_EACH = NsName.of(NAMESPACE_STRING, "each");
	/** The attribute <code>mx:index-var</code> for identifying the index variable for iteration. */

	public static final NsName ATTRIBUTE_INDEX_VAR = NsName.of(NAMESPACE_STRING, "index-var");
	/** The attribute <code>mx:item-var</code> for identifying the item variable for iteration. */

	public static final NsName ATTRIBUTE_ITEM_VAR = NsName.of(NAMESPACE_STRING, "item-var");

	/** The attribute <code>mx:text</code> for text replacement. */
	public static final NsName ATTRIBUTE_TEXT = NsName.of(NAMESPACE_STRING, "text");

	//## attribute default values

	/**
	 * The default variable name for an iteration item.
	 * @see #ATTRIBUTE_ITEM_VAR
	 */
	public static final String DEFAULT_ITEM_VAR = "it";

	/**
	 * The default variable name for an iteration index.
	 * @see #ATTRIBUTE_INDEX_VAR
	 */
	public static final String DEFAULT_INDEX_VAR = "i";

	private final MexlEvaluator evaluator;

	/** @return The strategy for evaluating Mesh Expression Language (MEXL) expressions. */
	protected MexlEvaluator getEvaluator() {
		return evaluator;
	}

	/**
	 * Evaluates an expression using the given meshing context and returns the result as an optional value. If the expression evaluates to an instance of
	 * {@link Optional}, that instance will be returned.
	 * @apiNote This is a convenience method for evaluating an expression and returning an optional value. It will never return <code>null</code>. However it will
	 *          not wrap a resulting {@link Optional} instance in another {@link Optional}. Thus this method functions analogously to
	 *          {@link Optional#flatMap(java.util.function.Function)}.
	 * @param context The context of meshing.
	 * @param expression The expression to evaluate.
	 * @return The result of the expression, which will be empty if the expression evaluated to <code>null</code>.
	 * @throws MexlException if there was an error parsing or otherwise processing the expression.
	 */
	protected Optional<Object> findExpressionResult(@Nonnull final MeshContext context, @Nonnull final String expression) throws MexlException {
		final Object result = getEvaluator().evaluate(context, expression);
		@SuppressWarnings("unchecked")
		final Optional<Object> optionalResult = result instanceof Optional ? (Optional<Object>)result : Optional.ofNullable(result);
		return optionalResult;
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
	 * @throws MeshException if there was an error directly related to meshing the document, such as parsing an expression.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	public Document meshDocument(@Nonnull MeshContext context, @Nonnull final Document document) throws IOException, MeshException, DOMException {
		final List<Element> meshedElements = meshElement(context, document.getDocumentElement());
		if(meshedElements.size() != 1 || meshedElements.get(0) != document.getDocumentElement()) {
			throw new UnsupportedOperationException("Document element cannot be removed or replaced when meshing a document.");
		}
		return document;
	}

	/**
	 * Evaluates and transforms a document element.
	 * @param context The context of meshing.
	 * @param element The element to mesh.
	 * @return The meshed element(s), if any, to replace the original element.
	 * @throws IllegalArgumentException if the element has some information that cannot be meshed.
	 * @throws IOException if there is an error meshing the element.
	 * @throws MeshException if there was an error directly related to meshing the document, such as parsing an expression.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	public List<Element> meshElement(@Nonnull MeshContext context, @Nonnull final Element element) throws IOException, MeshException, DOMException {
		//# iteration
		final Optional<List<Element>> iteration = exciseAttribute(element, ATTRIBUTE_EACH).map(throwingFunction(each -> { //mx:each
			final Iterator<?> iterator = findExpressionResult(context, each).map(GuiseMesh::toIterator)
					.orElseThrow(() -> new MeshException(format("Mesh each expression `%s` does not yield anything iterable.", each)));
			final String itemVar = exciseAttribute(element, ATTRIBUTE_ITEM_VAR).orElse(DEFAULT_ITEM_VAR); //mx:item-var
			final String indexVar = exciseAttribute(element, ATTRIBUTE_INDEX_VAR).orElse(DEFAULT_INDEX_VAR); //mx:index-var
			final List<Element> result = new ArrayList<>();
			//TODO enter scope
			for(int index = 0; iterator.hasNext(); index++) {
				final Object item = iterator.next();
				context.setVariable(indexVar, index);
				context.setVariable(itemVar, item);
				final Element eachElement = (Element)element.cloneNode(true); //mesh a clone of this element; iteration attribute have been removed
				result.addAll(meshElement(context, eachElement));
			}
			//TODO exit scope
			return result;

		}));
		if(iteration.isPresent()) { //if iteration occurred, the iterated items have already been recursively processed; return them
			return iteration.get();
		}

		//TODO conditions
		//TODO general attributes
		//TODO specific attributes

		//# text
		exciseAttribute(element, ATTRIBUTE_TEXT).ifPresent(text -> { //mx:text
			final Optional<Object> foundResult = findExpressionResult(context, text);
			element.setTextContent(foundResult.map(Object::toString).orElse(""));
		});

		meshChildElements(context, element);

		//TODO remove all mx-related attributes

		return List.of(element);
	}

	/**
	 * Converts an iteration source object to an {@link Iterable}.
	 * @apiNote Although a general conversion method would normally throw an {@link IllegalArgumentException}, this method throws a {@link MeshException} for
	 *          convenience to the caller.
	 * @implSpec This implementation supports the following source types:
	 *           <ul>
	 *           <li><code>Object[]</code> (of any non-primitive type)</li>
	 *           <li><code>int[]</code></li>
	 *           <li><code>long[]</code></li>
	 *           <li><code>double[]</code></li>
	 *           <li><code>{@link Iterable}</code></li>
	 *           <li><code>{@link Iterator}</code></li>
	 *           <li><code>{@link Enumeration}</code></li>
	 *           <li><code>{@link Stream}</code></li>
	 *           <li><code>{@link Map}</code> (returns iteration of {@link Entry}</li>
	 *           <li><code>{@link Object}</code> (returns iteration of single object)</li>
	 *           </ul>
	 * @param object The iteration source object.
	 * @return An iterator to the items in the iteration source.
	 * @throws MeshException if the object is an array to an unsupported primitive type.
	 */
	protected static Iterator<?> toIterator(@Nonnull final Object object) throws MeshException {
		if(object.getClass().isArray()) {
			if(object instanceof Object[]) {
				return asList((Object[])object).iterator();
			} else if(object instanceof int[]) {
				return stream((int[])object).iterator();
			} else if(object instanceof long[]) {
				return stream((long[])object).iterator();
			} else if(object instanceof double[]) {
				return stream((double[])object).iterator();
			} else {
				throw new MeshException(format("Iteration not supported on array of type %s: %s.", object.getClass().getComponentType().getName(), object));
			}
		} else if(object instanceof Iterable) {
			return ((Iterable<?>)object).iterator();
		} else if(object instanceof Iterator) {
			return (Iterator<?>)object;
		} else if(object instanceof Enumeration) {
			return ((Enumeration<?>)object).asIterator();
		} else if(object instanceof Stream) {
			return ((Stream<?>)object).iterator();
		} else if(object instanceof Map) {
			return ((Map<?, ?>)object).entrySet().iterator();
		} else {
			return Set.of(object).iterator();
		}
	}

	/**
	 * Evaluates and transforms child elements of an existing element.
	 * @implSpec Each child element is replaced with the normalized elements returned from calling {@link #meshElement(MeshContext, Element)}. If only the same
	 *           element is returned, no replacement is made. If no element is returned, the source element is removed.
	 * @param context The context of meshing.
	 * @param element The element the children of which to mesh.
	 * @throws IllegalArgumentException if the elements have some information that cannot be meshed.
	 * @throws IOException if there is an error meshing the child elements.
	 * @throws MeshException if there was an error directly related to meshing the document, such as parsing an expression.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	public void meshChildElements(@Nonnull MeshContext context, @Nonnull final Element element) throws IOException, MeshException, DOMException {
		final NodeList childNodes = element.getChildNodes();
		for(int childNodeIndex = 0; childNodeIndex < childNodes.getLength();) { //advance the index manually as needed
			final Node childNode = childNodes.item(childNodeIndex);
			if(!(childNode instanceof Element)) { //skip non-elements
				childNodeIndex++;
				continue;
			}
			final Element childElement = (Element)childNode;
			final List<Element> meshedElements = meshElement(context, childElement);
			replaceChild(element, childElement, meshedElements);
			childNodeIndex += meshedElements.size(); //manually advance the index based upon the replacement nodes
		}
	}

}
