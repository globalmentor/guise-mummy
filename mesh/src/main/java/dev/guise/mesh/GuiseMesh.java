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

import static com.globalmentor.io.IO.*;
import static com.globalmentor.xml.XmlDom.*;
import static java.util.Objects.*;
import static java.util.function.Predicate.*;
import static java.util.stream.Collectors.*;
import static java.util.stream.Stream.*;
import static org.zalando.fauxpas.FauxPas.*;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Stream;

import org.jspecify.annotations.*;

import org.w3c.dom.*;

import com.globalmentor.xml.def.NsName;

/// Guise template transformation engine.
/// @author Garret Wilson
public class GuiseMesh {

	/// The string form of the namespace of Guise Mesh elements, such as in an XHTML document or as the leading IRI segment of RDFa metadata.
	public static final String NAMESPACE_STRING = "https://guise.dev/name/mesh/";

	/// The namespace of Guise Mesh elements, such as in an XHTML document or as the leading IRI segment of RDFa metadata.
	public static final URI NAMESPACE = URI.create(NAMESPACE_STRING);

	/// The typical prefix used for the namespace of Guise Mesh elements, such as in an XHTML document or in RDFa metadata.
	public static final String NAMESPACE_PREFIX = "mx";

	//# attributes

	/// The attribute `mx:each` for iteration source.
	public static final NsName ATTRIBUTE_EACH = NsName.of(NAMESPACE_STRING, "each");

	/// The attribute `mx:index-var` for identifying the index variable for iteration.
	public static final NsName ATTRIBUTE_INDEX_VAR = NsName.of(NAMESPACE_STRING, "index-var");

	/// The attribute `mx:item-var` for identifying the item variable for iteration.
	public static final NsName ATTRIBUTE_ITEM_VAR = NsName.of(NAMESPACE_STRING, "item-var");

	/// The attribute `mx:iter-var` for identifying the state of iteration.
	public static final NsName ATTRIBUTE_ITER_VAR = NsName.of(NAMESPACE_STRING, "iter-var");

	/// The attribute `mx:text` for text replacement.
	public static final NsName ATTRIBUTE_TEXT = NsName.of(NAMESPACE_STRING, "text");

	//# attribute mutations

	/// The Mesh attribute name pattern for indicating attributes to mutate. The one matching group is the local name of the attribute to mutate, in no namespace.
	/// @see #ATTRIBUTE_MUTATION_NAME_PATTERN_NAME_GROUP
	static final Pattern ATTRIBUTE_MUTATION_NAME_PATTERN = Pattern.compile("attr-([A-Za-z][\\w-]*)");

	/// The matching group group for name of the attribute to mutate.
	static final int ATTRIBUTE_MUTATION_NAME_PATTERN_NAME_GROUP = 1;

	//## attribute default values

	/// The default variable name for an iteration item.
	/// @see #ATTRIBUTE_ITEM_VAR
	public static final String DEFAULT_ITEM_VAR = "it";

	/// The default variable name for the state of iteration.
	/// @see #ATTRIBUTE_ITER_VAR
	public static final String DEFAULT_ITER_VAR = "iter";

	/// The default variable name for an iteration index.
	/// @see #ATTRIBUTE_INDEX_VAR
	public static final String DEFAULT_INDEX_VAR = "i";

	private final MexlEvaluator evaluator;

	/// Returns the strategy for evaluating Mesh Expression Language (MEXL) expressions.
	/// @return The strategy for evaluating Mesh Expression Language (MEXL) expressions.
	protected MexlEvaluator getEvaluator() {
		return evaluator;
	}

	private final MeshInterpolator interpolator;

	/// Returns the strategy for interpolating strings.
	/// @return The strategy for interpolating strings.
	public MeshInterpolator getInterpolator() {
		return interpolator;
	}

	/// No-arg convenience constructor with no additional permissions.
	/// @implSpec Delegates to [#GuiseMesh(Set, Set)] with empty sets.
	public GuiseMesh() {
		this(Set.of(), Set.of());
	}

	/// Convenience constructor with additional classes and packages permitted for introspection in MEXL expressions.
	/// @param additionalClasses Additional classes to permit for introspection in MEXL expressions, matched by exact class.
	/// @param additionalPackages Additional packages whose classes should be accessible in MEXL expressions, each covering sub-packages.
	/// @implSpec This implementation always includes [MeshIterator] (for `mx:each` iteration introspection, e.g. `${iter.current}`,
	///           `${iter.first}`) in the permitted class set in addition to the caller-supplied classes. It creates a [JexlMexlEvaluator]
	///           with the combined classes and packages and uses [DefaultMeshInterpolator] for interpolation.
	public GuiseMesh(final Set<Class<?>> additionalClasses, final Set<Package> additionalPackages) {
		final var permittedClasses = concat(Stream.of(MeshIterator.class), additionalClasses.stream())
				.collect(toUnmodifiableSet());
		this(new JexlMexlEvaluator(permittedClasses, additionalPackages), DefaultMeshInterpolator.INSTANCE);
	}

	/// Mesh Expression Language (MEXL) evaluator constructor.
	/// @param evaluator The strategy for evaluating MEXL expressions.
	/// @param interpolator The strategy for interpolating strings.
	public GuiseMesh(@NonNull final MexlEvaluator evaluator, @NonNull final MeshInterpolator interpolator) {
		this.evaluator = requireNonNull(evaluator);
		this.interpolator = requireNonNull(interpolator);
	}

	/// Evaluates and transforms a document.
	/// @implSpec This implementation does not allow the document element to be removed or replaced.
	/// @param context The context of meshing.
	/// @param document The document to mesh.
	/// @return The meshed document, which may or may not be the same document supplied as input.
	/// @throws IllegalArgumentException if the elements have some information that cannot be meshed.
	/// @throws IOException if there is an error meshing the document.
	/// @throws MeshException if there was an error directly related to meshing the document, such as parsing an expression.
	/// @throws DOMException if there is some error manipulating the XML document object model.
	public Document meshDocument(@NonNull MeshContext context, @NonNull final Document document) throws IOException, MeshException, DOMException {
		final List<Element> meshedElements = meshElement(context, document.getDocumentElement());
		if(meshedElements.size() != 1 || meshedElements.get(0) != document.getDocumentElement()) {
			throw new UnsupportedOperationException("Document element cannot be removed or replaced when meshing a document.");
		}
		return document;
	}

	/// Evaluates and transforms a document element.
	/// @param context The context of meshing.
	/// @param element The element to mesh.
	/// @return The meshed element(s), if any, to replace the original element.
	/// @throws IllegalArgumentException if the element has some information that cannot be meshed.
	/// @throws IOException if there is an error meshing the element.
	/// @throws MeshException if there was an error directly related to meshing the document, such as parsing an expression.
	/// @throws DOMException if there is some error manipulating the XML document object model.
	@SuppressWarnings("try")
	public List<Element> meshElement(@NonNull MeshContext context, @NonNull final Element element) throws IOException, MeshException, DOMException {
		final MexlEvaluator evaluator = getEvaluator();

		//# iteration
		final Optional<List<Element>> iteration = exciseAttribute(element, ATTRIBUTE_EACH) //mx:each
				.map(each -> evaluator.findExpressionResult(context, each)).map(foundResult -> foundResult.orElseGet(Collections::emptyList)) //consider a null/empty expression to be an empty iteration source
				.map(throwingFunction(iterationSource -> {
					try (final Closeable iterationSourceCleanup = toCloseable(iterationSource)) { //ensure the iteration source is closed, in case it uses resource e.g. a directory listing
						final MeshIterator iterator;
						try {
							iterator = MeshIterator.fromIterationSource(iterationSource);
						} catch(final IllegalArgumentException illegalArgumentException) {
							throw new MeshException(illegalArgumentException.getMessage(), illegalArgumentException);
						}
						final String iterVar = exciseAttribute(element, ATTRIBUTE_ITER_VAR).orElse(DEFAULT_ITER_VAR); //mx:iter-var
						final String itemVar = exciseAttribute(element, ATTRIBUTE_ITEM_VAR).orElse(DEFAULT_ITEM_VAR); //mx:item-var
						final String indexVar = exciseAttribute(element, ATTRIBUTE_INDEX_VAR).orElse(DEFAULT_INDEX_VAR); //mx:index-var
						final List<Element> result = new ArrayList<>();
						try (final MeshContext.ScopeNesting scopeNesting = context.nestScope()) {
							context.setVariable(iterVar, iterator); //TODO consider providing delegate variable manipulation methods to nesting, if nothing else to avoid unused auto-closeable variable warning (`try`)
							while(iterator.hasNext()) {
								final Object item = iterator.next();
								context.setVariable(itemVar, item);
								context.setVariable(indexVar, iterator.getIndex());
								final Element eachElement = (Element)element.cloneNode(true); //mesh a clone of this element; iteration attribute have been removed
								result.addAll(meshElement(context, eachElement));
							}
						}
						return result;
					}
				}));
		if(iteration.isPresent()) { //if iteration occurred, the iterated items have already been recursively processed; return them
			return iteration.get();
		}

		//TODO conditions

		//# attribute interpolation
		final MeshInterpolator interpolator = getInterpolator();
		final Iterator<Attr> attributeInterpolationIterator = attributesIterator(element);
		while(attributeInterpolationIterator.hasNext()) {
			final Attr attribute = attributeInterpolationIterator.next();
			if(!NAMESPACE_STRING.equals(attribute.getNamespaceURI())) { //ignore mx: attributes
				interpolator.findInterpolation(context, attribute.getValue(), evaluator).map(Object::toString).ifPresent(attribute::setValue);
			}
		}

		//# attribute mutation
		//## gather and remove attribute mutation definition attributes
		Map<NsName, String> lazyAttributeUpdates = null; //unevaluated expressions keyed to attributes to mutate; lazily created in case there are no mutations
		final Iterator<Attr> attributeMutationIterator = attributesIterator(element);
		while(attributeMutationIterator.hasNext()) {
			final Attr attribute = attributeMutationIterator.next();
			if(NAMESPACE_STRING.equals(attribute.getNamespaceURI())) { //mx:
				final Matcher attributeMutationMatcher = ATTRIBUTE_MUTATION_NAME_PATTERN.matcher(attribute.getLocalName());
				if(attributeMutationMatcher.matches()) { //mx:attr-foo-bar
					final String attrMutationLocalName = attributeMutationMatcher.group(ATTRIBUTE_MUTATION_NAME_PATTERN_NAME_GROUP);
					final String attrMutationValue = attribute.getValue();
					if(lazyAttributeUpdates == null) { //lazily create the map of attribute mutations
						lazyAttributeUpdates = new HashMap<>();
					}
					lazyAttributeUpdates.put(NsName.of(attrMutationLocalName), attrMutationValue);
					attributeMutationIterator.remove(); //remove the Mesh attribute mutation attribute
				}
			}
		}
		//## apply attribute mutations (apply separately so as not to interfere with attribute iteration)
		if(lazyAttributeUpdates != null) {
			lazyAttributeUpdates.forEach((name, expression) -> {
				final Optional<Object> foundResult = evaluator.findExpressionResult(context, expression);
				foundResult.filter(not(Boolean.FALSE::equals)).ifPresentOrElse(result -> {
					//Boolean results use special XHTML values (or result in attribute removal)  
					final String value = Boolean.TRUE.equals(result) ? name.getLocalName() : result.toString();
					setAttribute(element, name, value);
				}, () -> removeAttribute(element, name)); //if no result, or a result of `false`, remove attribute
			});
		}

		//# text
		exciseAttribute(element, ATTRIBUTE_TEXT).ifPresent(text -> { //mx:text
			final Optional<Object> foundResult = evaluator.findExpressionResult(context, text);
			element.setTextContent(foundResult.map(Object::toString).orElse(""));
		});

		meshChildNodes(context, element);

		//TODO remove all mx-related attributes

		return List.of(element);
	}

	/// Evaluates and transforms child nodes of an existing element.
	/// - Interpolates each child text, CDATA, and comment node.
	/// - Recursively meshes each child element
	///
	/// @implSpec Each child element is replaced with the normalized elements returned from calling [#meshElement(MeshContext, Element)]. If only the same
	///           element is returned, no replacement is made. If no element is returned, the source element is removed.
	/// @param context The context of meshing.
	/// @param element The element the children of which to mesh.
	/// @throws IllegalArgumentException if the elements have some information that cannot be meshed.
	/// @throws IOException if there is an error meshing the child elements.
	/// @throws MeshException if there was an error directly related to meshing the document, such as parsing an expression.
	/// @throws DOMException if there is some error manipulating the XML document object model.
	/// @see #getInterpolator()
	/// @see #getEvaluator()
	public void meshChildNodes(@NonNull MeshContext context, @NonNull final Element element) throws IOException, MeshException, DOMException {
		final MeshInterpolator interpolator = getInterpolator();
		final MexlEvaluator evaluator = getEvaluator();
		final NodeList childNodes = element.getChildNodes();
		for(int childNodeIndex = 0; childNodeIndex < childNodes.getLength(); childNodeIndex++) {
			final Node childNode = childNodes.item(childNodeIndex);
			if(childNode instanceof CharacterData childCharacterData) { //Text, Comment, or CDATA
				interpolator.findInterpolation(context, childCharacterData.getData(), evaluator).map(Object::toString).ifPresent(childCharacterData::setData);
			} else if(childNode instanceof Element childElement) {
				final List<Element> meshedElements = meshElement(context, childElement);
				replaceChild(element, childElement, meshedElements);
				childNodeIndex += meshedElements.size() - 1; //adjust the index based upon the number of replaced elements (by default the loop advances by one)
			}
		}
	}

}
