/*
 * Copyright © 2005-2012 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.guise.framework.platform;

import java.io.IOException;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.*;
import static java.util.Objects.*;

import static com.globalmentor.html.spec.HTML.*;

import com.globalmentor.text.ASCII;
import com.globalmentor.html.spec.HTML;
import com.globalmentor.javascript.JavaScript;
import com.globalmentor.xml.spec.NsQualifiedName;
import com.globalmentor.xml.spec.XML;

import io.guise.framework.*;

/**
 * Abstract encapsulation of <code>application/xhtml+xml</code> information related to the current depiction.
 * <p>
 * This implementation maps the XHTML namespace {@link HTML#XHTML_NAMESPACE_URI} to the <code>null</code> prefix.
 * </p>
 * @author Garret Wilson
 */
public abstract class AbstractXHTMLDepictContext extends AbstractXMLDepictContext implements XHTMLDepictContext {

	/** The map of namespace URIs to be represented as HTML5 data attributes. */
	private final Set<URI> dataAttributeNamespaceURIs = newSetFromMap(new ConcurrentHashMap<URI, Boolean>());

	@Override
	public void registerDataAttributeNamespaceURI(final URI namespaceURI) {
		dataAttributeNamespaceURIs.add(requireNonNull(namespaceURI));
	}

	@Override
	public boolean isDataAttributeNamespaceURI(final URI namespaceURI) {
		return dataAttributeNamespaceURIs.contains(namespaceURI);
	}

	/** Whether all non-default-namespace attributes are encoded at HTML5 data attributes. */
	private boolean allDataAttributes = false;

	@Override
	public boolean isAllDataAttributes() {
		return allDataAttributes;
	}

	@Override
	public void setAllDataAttributes(final boolean dataAttributesEnabled) {
		this.allDataAttributes = dataAttributesEnabled;
	}

	/**
	 * Guise session constructor.
	 * @param session The Guise user session of which this context is a part.
	 * @param destination The destination with which this context is associated.
	 * @throws NullPointerException if the given session and/or destination is null.
	 * @throws IOException If there was an I/O error loading a needed resource.
	 */
	public AbstractXHTMLDepictContext(final GuiseSession session, final Destination destination) throws IOException {
		super(session, destination); //construct the parent class
		getXMLNamespacePrefixManager().registerNamespacePrefix(XHTML_NAMESPACE_URI_STRING, null); //don't use any prefix with the XHTML namespace
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * If HTML5 data attributes are enabled for all attributes or for the given namespace, this implementation converts non-XHTML-namespace attributes into HTML
	 * data attribute form. For example, an attribute in the form <code>example:fooBar</code> will be depicted as <code>data-example-foobar</code>.
	 * {@value XML#XMLNS_NAMESPACE_PREFIX} namespace attribute will never be converted.
	 * </p>
	 * @see <a href="http://www.w3.org/TR/html5/elements.html#embedding-custom-non-visible-data-with-the-data-attributes">HTML 5 Data Attributes</a>
	 * @see #isAllDataAttributes()
	 * @see #isDataAttributeNamespaceURI(URI)
	 */
	@Override
	protected <A extends Appendable> A appendAttributeName(final A appendable, final NsQualifiedName attributeQualifiedName) throws IOException {
		final URI namespaceURI = attributeQualifiedName.getNamespaceUri();
		if(isAllDataAttributes() || (namespaceURI != null && isDataAttributeNamespaceURI(namespaceURI))) { //if we should use data attributes
			if(!XML.XMLNS_NAMESPACE_URI.equals(namespaceURI)) { //if this isn't the XMLNS namespace
				final String prefix = attributeQualifiedName.findPrefix().orElse(null);
				if(prefix != null) { //we're technically only generating attributes for prefixed attributes (although all attributes in a namespace should have a prefix)
					appendable.append(DATA_ATTRIBUTE_ID).append(ATTRIBUTE_DELIMITER_CHAR); //data-
					appendable.append(ASCII.toLowerCase(prefix)).append(ATTRIBUTE_DELIMITER_CHAR); //prefix-
					appendable.append(ASCII.toLowerCase(attributeQualifiedName.getLocalName())); //localName
					return appendable;
				}
			}
		}
		return super.appendAttributeName(appendable, attributeQualifiedName); //append the attribute name normally
	}

	@Override
	public ElementState writeJavaScriptElement(final URI javascriptURI) throws IOException {
		writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_SCRIPT, false); //<xhtml:script> (explicitly don't create an empty <xhtml:script> element, otherwise IE wouldn't recognize it)
		writeAttribute(null, ELEMENT_SCRIPT_ATTRIBUTE_TYPE, JavaScript.OBSOLETE_MEDIA_TYPE.toString()); //type="text/javascript"
		writeAttribute(null, ELEMENT_SCRIPT_ATTRIBUTE_SRC, getDepictionURI(javascriptURI).toString()); //src="javascript.js"
		return writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_SCRIPT); //</xhtml:script>	
	}

	@Override
	public ElementState writeMetaElement(final String property, final String content) throws IOException {
		writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_META, true); //<xhtml:meta>; allow the <meta> element to be empty
		writeAttribute(null, ELEMENT_META_ATTRIBUTE_PROPERTY, property); //property="(property)"
		writeAttribute(null, ELEMENT_META_ATTRIBUTE_CONTENT, content); //content="(content)"
		return writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_META); //</xhtml:meta>
	}

	@Override
	public ElementState writeMetaElement(final URI propertyNamespaceURI, final String propertyLocalName, final String content) throws IOException {
		return writeMetaElement(getQualifiedName(propertyNamespaceURI, propertyLocalName), content); //get the qualified name for the property namespace and local name
	}

}
