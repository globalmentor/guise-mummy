package com.guiseframework.platform;

import java.io.IOException;

/**Encapsulation of application/xhtml+xml information related to the current depiction.
@author Garret Wilson
*/
public interface XHTMLDepictContext extends XMLDepictContext
{

	/**Generates a JavaScript element that references the given path.
	The given path is resolved to the application path.
	@param javascriptPath The application-relative path to the JavaScript file.
	@return The state of the element written.
	@exception IOException if there is an error writing the information.
	*/
	public ElementState writeJavaScriptElement(final String javascriptPath) throws IOException;
}
