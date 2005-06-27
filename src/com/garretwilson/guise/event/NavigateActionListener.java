package com.garretwilson.guise.event;

import java.net.URI;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.net.URIUtilities.*;

/**An object that listens for action events.
@param <S> The type of the event source.
@author Garret Wilson
*/
public class NavigateActionListener<S> implements ActionListener<S>
{

	/**The requested navigation URI.*/
	private final URI navigationURI;

		/**@return The requested navigation URI.*/
		public URI getNavigationURI() {return navigationURI;}

	/**Constructs a listener to navigate to the provided path.
	@param navigationPath A path that is either relative to the application context path or is absolute.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case {@link NavigateActionListener(ActionEvent<S>}</code> should be used instead).
	*/
	public NavigateActionListener(final String navigationPath)
	{
		this(createPathURI(navigationPath));	//create a URI from the path and construct the class
	}

	/**Constructs a listener to navigate to the provided URI.
	@param navigationURI The URI for navigation when the action occurs.
	@exception NullPointerException if the given navigation URI is null.
	*/
	public NavigateActionListener(final URI navigationURI)
	{
		this.navigationURI=checkNull(navigationURI, "Navigation URI cannot be null.");
	}

	/**Called when an action is initiated.
	This implementation requests navigation from the session.
	@param actionEvent The event indicating the source of the action.
	*/
	public void onAction(final ActionEvent<S> actionEvent)
	{
		actionEvent.getSession().navigate(getNavigationURI());	//request that the session navigate to the configured URI
	}

}
