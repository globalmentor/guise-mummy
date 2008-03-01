package com.guiseframework.platform;

import java.io.IOException;
import java.net.URI;

import com.globalmentor.util.StringTemplate;

/**Encapsulation of application/xhtml+xml information related to the current depiction.
@author Garret Wilson
*/
public interface XHTMLDepictContext extends XMLDepictContext
{

	/**The template for "border-?-color".*/
	public final static StringTemplate CSS_PROPERTY_BORDER_X_COLOR_TEMPLATE=new StringTemplate("border-", StringTemplate.STRING_PARAMETER, "-color");
	/**The template for "border-?-style".*/
	public final static StringTemplate CSS_PROPERTY_BORDER_X_STYLE_TEMPLATE=new StringTemplate("border-", StringTemplate.STRING_PARAMETER, "-style");
	/**The template for "border-?-width".*/
	public final static StringTemplate CSS_PROPERTY_BORDER_X_WIDTH_TEMPLATE=new StringTemplate("border-", StringTemplate.STRING_PARAMETER, "-width");
	/**The template for "margin-?".*/
	public final static StringTemplate CSS_PROPERTY_MARGIN_X_TEMPLATE=new StringTemplate("margin-", StringTemplate.STRING_PARAMETER);
	/**The template for "padding-?".*/
	public final static StringTemplate CSS_PROPERTY_PADDING_X_TEMPLATE=new StringTemplate("padding-", StringTemplate.STRING_PARAMETER);

	/**Generates a JavaScript element that references the given URI.
	The given URI is resolved to the application path.
	@param javascriptURI The application-relative IRO to the JavaScript file.
	@return The state of the element written.
	@exception IOException if there is an error writing the information.
	*/
	public ElementState writeJavaScriptElement(final URI javascriptURI) throws IOException;
}
