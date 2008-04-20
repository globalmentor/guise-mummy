package com.guiseframework.converter;

import java.net.URI;

import com.globalmentor.net.URIs;
import static com.globalmentor.net.URIs.*;

/**A URI converter that interprets relative URIs as path URIs with the {@value URIs#PATH_SCHEME} scheme.
@author Garret Wilson
@see URI
*/
public class PathURIStringLiteralConverter extends AbstractURIStringLiteralConverter
{

	/**Resolves a converted URI if needed.
	If the URI is already absolute, no action occurs.
	If the URI is relative, this implementation creates a URI with the {@value URIs#PATH_SCHEME} scheme.
	@param uri The URI to resolve.
	@return The URI resolved as needed and as appropriate.
	*/
	protected URI resolveURI(URI uri)
	{
		return uri.isAbsolute() ? uri : createURI(PATH_SCHEME, uri.toString());	//if the URI is relative, create a URI with the path: scheme
	}
}
