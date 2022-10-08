/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
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

package io.guise.framework.platform.web;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import io.guise.framework.component.Component;

/**
 * A component depictor that uses its top-level XHTML element as its main or body component.
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 */
public abstract class AbstractSimpleWebComponentDepictor<C extends Component> extends AbstractWebComponentDepictor<C> {

	/** Default constructor with no element representation. */
	public AbstractSimpleWebComponentDepictor() {
		this(null, null); //construct the strategy with no element representation
	}

	/**
	 * Element namespace and local name constructor that doesn't create an empty element, even if there is no content.
	 * @param namespaceURI The URI of the XML namespace of the element, or <code>null</code> if there is no namespace.
	 * @param localName The local name of the element with no prefix, or <code>null</code> if this component should not be rendered as an element.
	 */
	public AbstractSimpleWebComponentDepictor(final URI namespaceURI, final String localName) {
		this(namespaceURI, localName, false); //don't allow an empty element
	}

	/**
	 * Element namespace and local name constructor.
	 * @param namespaceURI The URI of the XML namespace of the element, or <code>null</code> if there is no namespace.
	 * @param localName The local name of the element with no prefix, or <code>null</code> if this component should not be rendered as an element.
	 * @param isEmptyElementAllowed Whether an empty element can be created if there is no content.
	 */
	public AbstractSimpleWebComponentDepictor(final URI namespaceURI, final String localName, final boolean isEmptyElementAllowed) {
		super(namespaceURI, localName, isEmptyElementAllowed); //construct the parent class
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version combines the body styles with the outer styles.
	 * </p>
	 */
	@Override
	protected Map<String, Object> getOuterStyles() {
		final Map<String, Object> outerStyles = super.getOuterStyles(); //get the default outer styles
		outerStyles.putAll(getBodyStyles()); //add the styles for the body
		return outerStyles; //return the combined styles		
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version writes the body ID and class attributes, along with the direction attribute.
	 * </p>
	 */
	@Override
	protected void depictBegin() throws IOException {
		super.depictBegin(); //do the default beginning rendering
		final String localName = getLocalName(); //get the element local name, if there is one
		if(localName != null) { //if there is an element name
			writeBodyIDClassAttributes(null, null); //write the ID and class
			writeDirectionAttribute(); //write the component direction, if this component specifies a direction
		}
	}

}
