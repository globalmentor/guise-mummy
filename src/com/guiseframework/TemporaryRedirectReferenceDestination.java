package com.guiseframework;

import java.util.regex.Pattern;

import com.garretwilson.net.URIPath;

/**A destination that temporarily redirects to another referenced destination.
@author Garret Wilson
*/
public class TemporaryRedirectReferenceDestination extends AbstractReferenceDestination implements TemporaryRedirectDestination
{

	/**Path and referenced destination constructor.
	@param path The application context-relative path within the Guise container context, which does not begin with '/'.
	@param destination The referenced destination.
	@exception NullPointerException if the path and/or destination is <code>null</code>.
	@exception IllegalArgumentException if the provided path is absolute.
	*/
	public TemporaryRedirectReferenceDestination(final URIPath path, final Destination destination)
	{
		super(path, destination);	//construct the parent class
	}

	/**Path pattern and referenced destination constructor.
	@param pathPattern The pattern to match an application context-relative path within the Guise container context, which does not begin with '/'.
	@param destination The referenced destination.
	@exception NullPointerException if the path pattern and/or destination is <code>null</code>.
	*/
	public TemporaryRedirectReferenceDestination(final Pattern pathPattern, final Destination destination)
	{
		super(pathPattern, destination);	//construct the parent class
	}
}
