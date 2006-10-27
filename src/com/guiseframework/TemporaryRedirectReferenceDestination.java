package com.guiseframework;

/**A destination that temporarily redirects to another referenced destination.
@author Garret Wilson
*/
public class TemporaryRedirectReferenceDestination extends AbstractReferenceDestination implements TemporaryRedirectDestination
{

	/**Path and referenced destination constructor.
	@param path The appplication context-relative path within the Guise container context, which does not begin with '/'.
	@param destination The referenced destination.
	@exception NullPointerException if the path and/or destination is <code>null</code>.
	@exception IllegalArgumentException if the provided path is absolute.
	*/
	public TemporaryRedirectReferenceDestination(final String path, final Destination destination)
	{
		super(path, destination);	//construct the parent class
	}
}
