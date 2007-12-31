package com.guiseframework;

import java.net.URI;

import static com.garretwilson.lang.Objects.*;

/**The encapsulation of a point of navigation.
@author Garret Wilson
*/
public class Navigation
{

	/**The old point of navigation, either absolute or application-relative.*/
	private final URI oldNavigationURI;

		/**@return The old point of navigation, either absolute or application-relative.*/
		public URI getOldNavigationURI() {return oldNavigationURI;}

	/**The new point of navigation, either absolute or application-relative.*/
	private final URI newNavigationURI;

		/**@return The new point of navigation, either absolute or application-relative.*/
		public URI getNewNavigationURI() {return newNavigationURI;}

	/**The ID of the viewport in which navigation should occur, or <code>null</code> if navigation should occur in the current viewport.*/
	private final String viewportID;

		/**@return The ID of the viewport in which navigation should occur, or <code>null</code> if navigation should occur in the current viewport..*/
		public String getViewportID() {return viewportID;}

	/**Creates an object encapsulating a point of navigation.
	@param oldNavigationURI The old point of navigation, either absolute or application-relative.
	@param newNavigationURI The new point of navigation, either absolute or application-relative.
	@exception NullPointerException if one of the navigation URIs is <code>null</code>, or does not contain a path.
	*/
	public Navigation(final URI oldNavigationURI, final URI newNavigationURI)
	{
		this(oldNavigationURI, newNavigationURI, null);	//construct a navigation object with no viewport ID
	}

	/**Creates an object encapsulating a point of navigation.
	@param oldNavigationURI The old point of navigation, either absolute or application-relative.
	@param newNavigationURI The new point of navigation, either absolute or application-relative.
	@param viewportID The ID of the viewport in which navigation should occur, or <code>null</code> if navigation should occur in the current viewport.
	@exception NullPointerException if one of the navigation URIs is <code>null</code>, or does not contain a path.
	*/
	public Navigation(final URI oldNavigationURI, final URI newNavigationURI, final String viewportID)
	{
		this.oldNavigationURI=checkInstance(oldNavigationURI, "Old navigation URI cannot be null.");
		this.newNavigationURI=checkInstance(newNavigationURI, "New navigation URI cannot be null.");
		this.viewportID=viewportID;
	}
}
