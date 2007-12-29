package com.guiseframework;

import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;

import com.garretwilson.beans.PropertyBindable;
import com.garretwilson.net.*;

/**Description of a navigation point, its properties, and its restrictions.
@author Garret Wilson
*/
public interface Destination extends PropertyBindable
{

	/**@return The application context-relative path within the Guise container context, which does not begin with '/', or <code>null</code> if there is no path specified for this destination.*/
	public URIPath getPath();

	/**@return The pattern to match an application context-relative path within the Guise container context, which does not begin with '/', or <code>null</code> if there is no path pattern specified for this destination.*/
	public Pattern getPathPattern();

	/**The read-only iterable of categories.*/
	public Iterable<Category> getCategories();

	/**Sets the categories.
	@param categories The list of new categories.
	*/
	public void setCategories(final List<Category> categories);

	/**Determines the path to use for the requested path.
	If there is a preferred path, it is returned; otherwise, the path is returned unmodified.
	If there is no principal or the principal is not the owner of the identified resource; the determined path is a collection path; and there exists a discoverable home page in the collection,
	this version returns the path to the home page.
	@param session The current Guise session.
	@param navigationPath The navigation path relative to the application context path.
	@param bookmark The bookmark for this path, or <code>null</code> if there is no bookmark.
	@param referrerURI The URI of the referring destination or other entity with no query or fragment, or <code>null</code> if no referring URI is known.
	@return The preferred path.
	@exception NullPointerException if the given session and/or path is <code>null</code>.
	@exception ResourceIOException if there is an error accessing the resource.
	*/
	public URIPath getPath(final GuiseSession session, URIPath navigationPath, final Bookmark bookmark, final URI referrerURI) throws ResourceIOException;

	/**Determines if the given path does indeed exist for this destination.
	@param session The current Guise session. 
	@param navigationPath The navigation path relative to the application context path.
	@param bookmark The bookmark for which navigation should occur at this navigation path, or <code>null</code> if there is no bookmark involved in navigation.
	@param referrerURI The URI of the referring navigation panel or other entity with no query or fragment, or <code>null</code> if no referring URI is known.
	@return Whether the requested path exists.
	@exception NullPointerException if the given navigation path is <code>null</code>.
	@exception ResourceIOException if there is an error accessing the resource.
	*/
	public boolean exists(final GuiseSession session, final URIPath navigationPath, final Bookmark bookmark, final URI referrerURI) throws ResourceIOException;

}
