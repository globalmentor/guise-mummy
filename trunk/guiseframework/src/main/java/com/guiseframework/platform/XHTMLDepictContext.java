/*
 * Copyright © 2005-2011 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package com.guiseframework.platform;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import com.globalmentor.util.StringTemplate;
import com.guiseframework.geometry.Side;

/**
 * Encapsulation of application/xhtml+xml information related to the current depiction.
 * @author Garret Wilson
 */
public interface XHTMLDepictContext extends XMLDepictContext
{

	/** The template for "border-?-color". */
	public final static StringTemplate CSS_PROPERTY_BORDER_X_COLOR_TEMPLATE = new StringTemplate("border-", StringTemplate.STRING_PARAMETER, "-color");
	/** The template for "border-?-style". */
	public final static StringTemplate CSS_PROPERTY_BORDER_X_STYLE_TEMPLATE = new StringTemplate("border-", StringTemplate.STRING_PARAMETER, "-style");
	/** The template for "border-?-width". */
	public final static StringTemplate CSS_PROPERTY_BORDER_X_WIDTH_TEMPLATE = new StringTemplate("border-", StringTemplate.STRING_PARAMETER, "-width");
	/** The template for "margin-?". */
	public final static StringTemplate CSS_PROPERTY_MARGIN_X_TEMPLATE = new StringTemplate("margin-", StringTemplate.STRING_PARAMETER);
	/** The template for "padding-?". */
	public final static StringTemplate CSS_PROPERTY_PADDING_X_TEMPLATE = new StringTemplate("padding-", StringTemplate.STRING_PARAMETER);

	/** The sides supported by CSS. */
	public final Set<Side> CSS_SIDES = EnumSet.of(Side.LEFT, Side.RIGHT, Side.TOP, Side.BOTTOM);

	/**
	 * Generates a JavaScript element that references the given URI. The given URI is resolved to the application path.
	 * @param javascriptURI The application-relative IRO to the JavaScript file.
	 * @return The state of the element written.
	 * @exception IOException if there is an error writing the information.
	 */
	public ElementState writeJavaScriptElement(final URI javascriptURI) throws IOException;

	/**
	 * Generates a meta element suitable for the head of an XHTML document.
	 * @param property The meta property name.
	 * @param content The meta property content.
	 * @return The state of the element written.
	 * @exception IOException if there is an error writing the information.
	 */
	public ElementState writeMetaElement(final String property, final String content) throws IOException;

	/**
	 * Generates a meta element suitable for the head of an XHTML document, creating a qualified name or the meta property based upon the given namespace URI and
	 * local name.
	 * @param propertyNamespaceURI The URI of the XML namespace of the meta property name, or <code>null</code> if there is no namespace.
	 * @param propertyLocalName The local name of the meta property name with no prefix.
	 * @param content The meta property content.
	 * @return The state of the element being written.
	 * @exception IOException if there is an error writing the information.
	 */
	public ElementState writeMetaElement(final URI propertyNamespaceURI, final String propertyLocalName, final String content) throws IOException;
}
