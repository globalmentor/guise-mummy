package com.guiseframework;

import java.util.regex.Pattern;

/**A destination that permanently redirects to another referenced destination.
@author Garret Wilson
*/
public class PermanentRedirectReferenceDestination extends AbstractReferenceDestination implements PermanentRedirectDestination
{

	/**Path and referenced destination constructor.
	@param path The appplication context-relative path within the Guise container context, which does not begin with '/'.
	@param destination The referenced destination.
	@exception NullPointerException if the path and/or destination is <code>null</code>.
	@exception IllegalArgumentException if the provided path is absolute.
	*/
	public PermanentRedirectReferenceDestination(final String path, final Destination destination)
	{
		super(path, destination);	//construct the parent class
	}

	/**Path pattern and referenced destination constructor.
	@param pathPattern The pattern to match an appplication context-relative path within the Guise container context, which does not begin with '/'.
	@param destination The referenced destination.
	@exception NullPointerException if the path pattern and/or destination is <code>null</code>.
	*/
	public PermanentRedirectReferenceDestination(final Pattern pathPattern, final Destination destination)
	{
		super(pathPattern, destination);	//construct the parent class
	}
}
