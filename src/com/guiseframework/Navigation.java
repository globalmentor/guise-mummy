package com.guiseframework;

import java.net.URI;
import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.net.URIUtilities.*;

/**The encapsulation of a point of navigation.
@author Garret Wilson
*/
public class Navigation
{

	/**The old point of navigation, with an absolute path.*/
	private final URI oldNavigationURI;

		/**@return The old point of navigation, with an absolute path.*/
		public URI getOldNavigationURI() {return oldNavigationURI;}

	/**The new point of navigation, with an absolute path.*/
	private final URI newNavigationURI;

		/**@return The new point of navigation, with an absolute path.*/
		public URI getNewNavigationURI() {return newNavigationURI;}

	/**The ID of the viewport in which navigation should occur, or <code>null</code> if navigation should occur in the current viewport.*/
	private final String viewportID;

		/**@return The ID of the viewport in which navigation should occur, or <code>null</code> if navigation should occur in the current viewport..*/
		public String getViewportID() {return viewportID;}

	/**Creates an object encapsulating a point of navigation.
	@param oldNavigationURI The old point of navigation, with an absolute path.
	@param newNavigationURI The new point of navigation, with an absolute path.
	@exception NullPointerException if one of the navigation URIs is <code>null</code>, or does not contain a path.
	@exception IllegalArgumentException if one of the given navigation URIs contains a relative path.
	*/
	public Navigation(final URI oldNavigationURI, final URI newNavigationURI)
	{
		this(oldNavigationURI, newNavigationURI, null);	//construct a navigation object with no viewport ID
	}

	/**Creates an object encapsulating a point of navigation.
	@param oldNavigationURI The old point of navigation, with an absolute path.
	@param newNavigationURI The new point of navigation, with an absolute path.
	@param viewportID The ID of the viewport in which navigation should occur, or <code>null</code> if navigation should occur in the current viewport.
	@exception NullPointerException if one of the navigation URIs is <code>null</code>, or does not contain a path.
	@exception IllegalArgumentException if one of the given navigation URIs contains a relative path.
	*/
	public Navigation(final URI oldNavigationURI, final URI newNavigationURI, final String viewportID)
	{
		checkNull(oldNavigationURI, "Old navigation URI cannot be null.");
		checkNull(newNavigationURI, "New navigation URI cannot be null.");
		if(!oldNavigationURI.isAbsolute() && !isAbsolutePath(oldNavigationURI))	//make sure the old navigation URI has an absolute path or is itself absolute
		{
			throw new IllegalArgumentException("Old navigation URI must have an absolute path: "+oldNavigationURI);
		}
		if(!newNavigationURI.isAbsolute() && !isAbsolutePath(newNavigationURI))	//make sure the new navigation URI has an absolute path
		{
			throw new IllegalArgumentException("New navigation URI must have an absolute path: "+newNavigationURI);
		}
		this.oldNavigationURI=oldNavigationURI;
		this.newNavigationURI=newNavigationURI;
		this.viewportID=viewportID;
	}
}
