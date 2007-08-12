package com.guiseframework.platform;

import java.io.IOException;
import java.net.URI;


/**Encapsulation of application/xhtml+xml information related to the current depiction.
@author Garret Wilson
*/
public interface XHTMLDepictContext extends XMLDepictContext
{

	/**Generates a JavaScript element that references the given URI.
	The given URI is resolved to the application path.
	@param javascriptURI The application-relative IRO to the JavaScript file.
	@return The state of the element written.
	@exception IOException if there is an error writing the information.
	*/
	public ElementState writeJavaScriptElement(final URI javascriptURI) throws IOException;
}
