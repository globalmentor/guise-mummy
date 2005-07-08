package com.garretwilson.guise.session;

import java.net.URI;
import static com.garretwilson.lang.ObjectUtilities.*;

/**The encapsulation of a point of navigation.
@author Garret Wilson
*/
public class Navigation
{

	/**The point of navigation.*/
	private final URI navigationURI;

		/**@return The point of navigation.*/
		public URI getNavigationURI() {return navigationURI;}

	/**Creates an object encapsulating a point of navigation.
	@param navigationURI The point of navigation.
	@exception NullPointerException if the given navigation URI is <code>null</code>.
	*/
	public Navigation(final URI navigationURI)
	{
		this.navigationURI=checkNull(navigationURI, "Navigation URI cannot be null.");
	}
}
