package com.guiseframework;

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
}
