package com.guiseframework.converter;

import java.net.URI;

/**A URI converter that allows any URI allowed by the URI class, even relative URIs.
@author Garret Wilson
@see URI
*/
public class DefaultURIStringLiteralConverter extends AbstractURIStringLiteralConverter
{

	/**Resolves a converted URI if needed.
	This method returns the URI unchanged.
	@param uri The URI to resolve.
	@return The URI resolved as needed and as appropriate.
	*/
	protected URI resolveURI(URI uri)
	{
		return uri;	//accept the URI as-is
	}
}
