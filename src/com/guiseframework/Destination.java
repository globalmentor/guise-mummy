package com.guiseframework;

import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;

import com.garretwilson.beans.PropertyBindable;
import com.garretwilson.net.ResourceIOException;

/**Description of a navigation point, its properties, and its restrictions.
@author Garret Wilson
*/
public interface Destination extends PropertyBindable
{

	/**@return The application context-relative path within the Guise container context, which does not begin with '/', or <code>null</code> if there is no path specified for this destination.*/
	public String getPath();

	/**@return The pattern to match an application context-relative path within the Guise container context, which does not begin with '/', or <code>null</code> if there is no path pattern specified for this destination.*/
	public Pattern getPathPattern();

	/**The read-only iterable of categories.*/
	public Iterable<Category> getCategories();

	/**Sets the categories.
	@param categories The list of new categories.
	*/
	public void setCategories(final List<Category> categories);

	/**Determines if the given path does indeed exist for this destination.
	@param session The current Guise Session. 
	@param navigationPath The navigation path relative to the application context path.
	@param bookmark The bookmark for which navigation should occur at this navigation path, or <code>null</code> if there is no bookmark involved in navigation.
	@param referrerURI The URI of the referring navigation panel or other entity with no query or fragment, or <code>null</code> if no referring URI is known.
	@return Whether the requested path exists.
	@exception NullPointerException if the given navigation path is <code>null</code>.
	@exception ResourceIOException if there is an error accessing the resource.
	*/
	public boolean exists(final GuiseSession session, final String navigationPath, final Bookmark bookmark, final URI referrerURI) throws ResourceIOException;

}
