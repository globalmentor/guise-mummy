package com.guiseframework.converter;

import java.net.URI;

import static com.globalmentor.java.Objects.*;

/**A URI converter that resolves relative URIs to some base URI.
@author Garret Wilson
@see URI
*/
public class BaseURIStringLiteralConverter extends AbstractURIStringLiteralConverter
{

	/**The base URI for resolving any relative URI.*/
	private final URI baseURI;

		/**@return The base URI for resolving any relative URI.*/
		public URI getBaseURI() {return baseURI;}

	/**Base URI constructor.
	@param baseURI The base URI for resolving any relative URI.
	@throws NullPointerException if the given base URI is <code>null</code>.
	*/
	public BaseURIStringLiteralConverter(final URI baseURI)
	{
		this.baseURI=checkInstance(baseURI, "Base URI cannot be null.");
	}

	/**Resolves a converted URI if needed.
	If the URI is already absolute, no action occurs.
	If the URI is relative, this implementation resolves the URI against the URI returned by {@link #getBaseURI()}.
	@param uri The URI to resolve.
	@return The URI resolved as needed and as appropriate.
	@see #getBaseURI()
	*/
	protected URI resolveURI(URI uri)
	{
		return uri.isAbsolute() ? uri : getBaseURI().resolve(uri);	//if the URI is relative, resolve it against the base URI
	}
}
