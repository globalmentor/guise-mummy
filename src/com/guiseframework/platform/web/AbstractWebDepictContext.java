package com.guiseframework.platform.web;

import java.io.IOException;

import com.guiseframework.*;
import com.guiseframework.platform.AbstractXHTMLDepictContext;
import com.guiseframework.platform.web.WebPlatform;
import static com.guiseframework.platform.web.WebPlatform.*;

/**Abstract implementation of information related to the current depiction on the web platform.
This implementation maps the XHTML namespace {@value WebPlatform#GUISE_ML_NAMESPACE_URI} to the prefix {@value WebPlatform#GUISE_ML_NAMESPACE_PREFIX}.
@author Garret Wilson
*/
public abstract class AbstractWebDepictContext extends AbstractXHTMLDepictContext implements WebDepictContext
{

	/**@return The web platform on which Guise objects are depicted.*/
	public WebPlatform getPlatform() {return (WebPlatform)super.getPlatform();}

	/**Guise session constructor.
	@param session The Guise user session of which this context is a part.
	@param destination The destination with which this context is associated.
	@exception NullPointerException if the given session and/or destination is null.
	@exception IOException If there was an I/O error loading a needed resource.
	*/
	public AbstractWebDepictContext(final GuiseSession session, final Destination destination) throws IOException
	{
		super(session, destination);	//construct the parent class
		getXMLNamespacePrefixManager().registerNamespacePrefix(GUISE_ML_NAMESPACE_URI.toString(), GUISE_ML_NAMESPACE_PREFIX);	//map the Guise namespace to the Guise prefix
	}
}
