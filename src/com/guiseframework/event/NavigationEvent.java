package com.guiseframework.event;

import java.net.URI;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.garretwilson.net.URIPath;
import com.guiseframework.Bookmark;

/**An event indicating that navigation has occurred.
The source of this event is the Guise session in which navigation occurred.
@author Garret Wilson
*/
public class NavigationEvent extends AbstractGuiseEvent
{

	/**The navigation path relative to the application context path.*/
	private final URIPath navigationPath;

		/**@public The navigation path relative to the application context path.*/
		public URIPath getNavigationPath() {return navigationPath;}

	/**The bookmark for which navigation should occur at the navigation path, or <code>null</code> if there is no bookmark involved in navigation.*/
	private final Bookmark bookmark;

		/**@return The bookmark for which navigation should occur at the navigation path, or <code>null</code> if there is no bookmark involved in navigation.*/
		public Bookmark getBookmark() {return bookmark;}
	
	/**The URI of the referring navigation panel or other entity with no query or fragment, or <code>null</code> if no referring URI is known.*/
	private final URI referrerURI;

		/**@return The URI of the referring navigation panel or other entity with no query or fragment, or <code>null</code> if no referring URI is known.*/
		public URI getReferrerURI() {return referrerURI;}

	/**Source constructor.
	@param source The object on which the event initially occurred.
	@param navigationPath The navigation path relative to the application context path.
	@param bookmark The bookmark for which navigation should occur at this navigation path, or <code>null</code> if there is no bookmark involved in navigation.
	@param referrerURI The URI of the referring navigation panel or other entity with no query or fragment, or <code>null</code> if no referring URI is known.
	@exception NullPointerException if the given source, and/or navigation path is <code>null</code>.
	*/
	public NavigationEvent(final Object source, final URIPath navigationPath, final Bookmark bookmark, final URI referrerURI)
	{
		super(source);	//construct the parent class
		this.navigationPath=checkInstance(navigationPath, "Navigation path cannot be null.");
		this.bookmark=bookmark;
		this.referrerURI=referrerURI;
	}

}
