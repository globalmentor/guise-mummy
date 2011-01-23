/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

import static com.globalmentor.javascript.JavaScript.*;
import static com.globalmentor.text.xml.xhtml.XHTML.*;

import com.globalmentor.text.xml.xhtml.XHTML;
import com.guiseframework.Destination;
import com.guiseframework.GuiseSession;

/**Abstract encapsulation of application/xhtml+xml information related to the current depiction.
This implementation maps the XHTML namespace {@value XHTML#XHTML_NAMESPACE_URI} to the <code>null</code> prefix.
@author Garret Wilson
*/
public abstract class AbstractXHTMLDepictContext extends AbstractXMLDepictContext implements XHTMLDepictContext
{

	/**Guise session constructor.
	@param session The Guise user session of which this context is a part.
	@param destination The destination with which this context is associated.
	@exception NullPointerException if the given session and/or destination is null.
	@exception IOException If there was an I/O error loading a needed resource.
	*/
	public AbstractXHTMLDepictContext(final GuiseSession session, final Destination destination) throws IOException
	{
		super(session, destination);	//construct the parent class
		getXMLNamespacePrefixManager().registerNamespacePrefix(XHTML_NAMESPACE_URI.toString(), null);	//don't use any prefix with the XHTML namespace
	}

	/**Generates a JavaScript element that references the given URI.
	The given URI is resolved to the application URI.
	@param javascriptURI The application-relative URI to the JavaScript file.
	@return The state of the element written.
	@exception IOException if there is an error writing the information.
	*/
	public ElementState writeJavaScriptElement(final URI javascriptURI) throws IOException
	{
		writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_SCRIPT, false);	//<xhtml:script> (explicitly don't create an empty <xhtml:script> element, otherwise IE wouldn't recognize it)
		writeAttribute(null, ELEMENT_SCRIPT_ATTRIBUTE_TYPE, JAVASCRIPT_OBSOLETE_CONTENT_TYPE.toString());	//type="text/javascript"
		writeAttribute(null, ELEMENT_SCRIPT_ATTRIBUTE_SRC, getDepictionURI(javascriptURI).toString());	//src="javascript.js"
		return writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_SCRIPT);	//</xhtml:script>	
	}

}
