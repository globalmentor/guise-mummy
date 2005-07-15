package com.javaguise.event;

import java.net.URI;

/**An object that listens for action events and in response changes the navigation.
This class if declared final because it encapsulates a set of known, bounded functionality that may be deferred to the client if possible.
@param <S> The type of the event source.
@author Garret Wilson
*/
public final class NavigateActionListener<S> extends AbstractNavigateActionListener<S>
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
	@exception NullPointerException if the given navigation URI is null.
	*/
	public NavigateActionListener(final URI navigationURI)
	{
		super(navigationURI);	//construct the parent class
	}

	/**Called when an action is initiated.
	This implementation requests navigation from the session.
	@param actionEvent The event indicating the source of the action.
	*/
	public void actionPerformed(final ActionEvent<S> actionEvent)
	{
		actionEvent.getSession().navigate(getNavigationURI());	//request that the session navigate to the configured URI
	}

}
