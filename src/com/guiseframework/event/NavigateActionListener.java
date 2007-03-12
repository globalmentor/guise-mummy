package com.guiseframework.event;

import java.net.URI;

/**An object that listens for action events and in response changes the navigation.
This class if declared final because it encapsulates a set of known, bounded functionality that may be deferred to the client if possible.
@author Garret Wilson
*/
public final class NavigateActionListener extends AbstractNavigateActionListener
{

	/**Constructs a listener to navigate to the provided path.
	@param navigationPath A path that is either relative to the application context path or is absolute.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case {@link #NavigateActionListener(URI)}</code> should be used instead).
	*/
	public NavigateActionListener(final String navigationPath)
	{
		super(navigationPath);	//construct the parent class
	}

	/**Constructs a listener to navigate to the provided URI.
	@param navigationURI The URI for navigation when the action occurs.
	@exception NullPointerException if the given navigation URI is <code>null</code>.
	*/
	public NavigateActionListener(final URI navigationURI)
	{
		super(navigationURI);	//construct the parent class
	}

	/**Constructs a listener to navigate to the provided URI in the identified viewport.
	@param navigationURI The URI for navigation when the action occurs.
	@param viewportID The ID of the viewport in which navigation should occur, or <code>null</code> if navigation should occur in the current viewport.
	@exception NullPointerException if the given navigation URI is <code>null</code>.
	*/
	public NavigateActionListener(final URI navigationURI, final String viewportID)
	{
		super(navigationURI, viewportID);	//construct the parent class
	}

}
