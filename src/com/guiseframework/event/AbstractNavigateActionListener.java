package com.guiseframework.event;

import java.net.URI;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.net.URIUtilities.*;

/**An abstract that listens for action events and keeps information for navigating in response.
@author Garret Wilson
*/
public abstract class AbstractNavigateActionListener implements ActionListener
{

	/**The requested navigation URI.*/
	private final URI navigationURI;

		/**@return The requested navigation URI.*/
		public URI getNavigationURI() {return navigationURI;}

	/**The ID of the viewport in which navigation should occur, or <code>null</code> if navigation should occur in the current viewport.*/
	private final String viewportID;

		/**@return The ID of the viewport in which navigation should occur, or <code>null</code> if navigation should occur in the current viewport..*/
		public String getViewportID() {return viewportID;}

	/**Constructs a listener to navigate to the provided path.
	@param navigationPath A path that is either relative to the application context path or is absolute.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case {@link #AbstractNavigateActionListener(URI)}</code> should be used instead).
	*/
	public AbstractNavigateActionListener(final String navigationPath)
	{
		this(createPathURI(navigationPath));	//create a URI from the path and construct the class
	}

	/**Constructs a listener to navigate to the provided URI.
	@param navigationURI The URI for navigation when the action occurs.
	@exception NullPointerException if the given navigation URI is <code>null</code>.
	*/
	public AbstractNavigateActionListener(final URI navigationURI)
	{
		this(navigationURI, null);	//construct the class with navigation to the current viewport 
	}

	/**Constructs a listener to navigate to the provided URI in the identified viewport.
	@param navigationURI The URI for navigation when the action occurs.
	@param viewportID The ID of the viewport in which navigation should occur, or <code>null</code> if navigation should occur in the current viewport.
	@exception NullPointerException if the given navigation URI is <code>null</code>.
	*/
	public AbstractNavigateActionListener(final URI navigationURI, final String viewportID)
	{
		this.navigationURI=checkInstance(navigationURI, "Navigation URI cannot be null.");
		this.viewportID=viewportID;
	}

	/**Called when an action is initiated.
	This implementation requests navigation from the session.
	@param actionEvent The event indicating the source of the action.
	*/
	public void actionPerformed(final ActionEvent actionEvent)
	{
		actionEvent.getSession().navigate(getNavigationURI(), getViewportID());	//request that the session navigate to the configured URI in the identified viewport, if any
	}

}
