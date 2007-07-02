package com.guiseframework.platform;

import java.io.IOException;

import static com.garretwilson.javascript.JavaScriptConstants.*;
import com.garretwilson.text.xml.xhtml.XHTMLConstants;
import static com.garretwilson.text.xml.xhtml.XHTMLConstants.*;

import com.guiseframework.Destination;
import com.guiseframework.GuiseSession;

/**Abstract encapsulation of application/xhtml+xml information related to the current depiction.
This implementation maps the XHTML namespace {@value XHTMLConstants#XHTML_NAMESPACE_URI} to the <code>null</code> prefix.
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

	/**Generates a JavaScript element that references the given path.
	The given path is resolved to the application path.
	@param javascriptPath The application-relative path to the JavaScript file.
	@return The state of the element written.
	@exception IOException if there is an error writing the information.
	*/
	public ElementState writeJavaScriptElement(final String javascriptPath) throws IOException
	{
		writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_SCRIPT, false);	//<xhtml:script> (explicitly don't create an empty <xhtml:script> element, otherwise IE wouldn't recognize it)
		writeAttribute(null, ELEMENT_SCRIPT_ATTRIBUTE_TYPE, JAVASCRIPT_CONTENT_TYPE.toString());	//type="text/javascript"
		writeAttribute(null, ELEMENT_SCRIPT_ATTRIBUTE_SRC, getSession().getApplication().resolvePath(javascriptPath));	//src="javascript.js"	(resolved to the application)
		return writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_SCRIPT);	//</xhtml:script>	
	}

}
