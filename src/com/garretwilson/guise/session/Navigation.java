package com.garretwilson.guise.session;

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

	/**The new point of navigation, with an absolute path..*/
	private final URI newNavigationURI;

		/**@return The new point of navigation, with an absolute path.*/
		public URI getNewNavigationURI() {return newNavigationURI;}

	/**Creates an object encapsulating a point of navigation.
	@param oldNavigationURI The old point of navigation, with an absolute path.
	@param newNavigationURI The new point of navigation, with an absolute path.
	@exception NullPointerException if one of the navigation URIs is <code>null</code>, or does not contain a path.
	@exception IllegalArgumentException if one of the given navigation URIs contains a relative path.
	*/
	public Navigation(final URI oldNavigationURI, final URI newNavigationURI)
	{
		if(!isAbsolutePath(checkNull(oldNavigationURI, "Old navigation URI cannot be null.")))	//make sure the old navigation URI has an absolute path
		{
			throw new IllegalArgumentException("Old navigation URI must have an absolute path.");
		}
		if(!isAbsolutePath(checkNull(newNavigationURI, "New navigation URI cannot be null.")))	//make sure the new navigation URI has an absolute path
		{
			throw new IllegalArgumentException("New navigation URI must have an absolute path.");
		}
		this.oldNavigationURI=oldNavigationURI;
		this.newNavigationURI=newNavigationURI;
	}
}
