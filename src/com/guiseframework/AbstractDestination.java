package com.guiseframework;

import java.net.URI;
import java.util.*;
import static java.util.Collections.*;
import java.util.regex.Pattern;

import com.garretwilson.beans.BoundPropertyObject;
import com.garretwilson.lang.ObjectUtilities;
import static com.garretwilson.lang.ObjectUtilities.*;
import com.garretwilson.net.*;
import static com.garretwilson.net.URIConstants.PATH_SEPARATOR;

/**Abstract implementation of a navigation point, its properties, and its restrictions.
Destinations of identical types with identical paths and path patterns are considered equal.
@author Garret Wilson
*/
public abstract class AbstractDestination extends BoundPropertyObject implements Destination
{

	/**The map of sub-categories; it is not thread-safe, but any changes will simply create a new list.*/
	private List<Category> categories=unmodifiableList(new ArrayList<Category>());	//TODO add a property and fire a change

		/**The read-only iterable of categories.*/
		public Iterable<Category> getCategories() {return categories;}

		/**Sets the categories.
		@param categories The list of new categories.
		*/
		public void setCategories(final List<Category> categories)
		{
			this.categories=unmodifiableList(new ArrayList<Category>(categories));	//create a copy of the list and save the list
		}

	/**The application context-relative path within the Guise container context, which does not begin with '/', or <code>null</code> if there is no path specified for this destination.*/
	private final URIPath path;

		/**@return The application context-relative path within the Guise container context, which does not begin with '/', or <code>null</code> if there is no path specified for this destination.*/
		public URIPath getPath() {return path;}

	/**The pattern to match an application context-relative path within the Guise container context, which does not begin with '/', or <code>null</code> if there is no path pattern specified for this destination.*/
	private final Pattern pathPattern;

		/**@return The pattern to match an application context-relative path within the Guise container context, which does not begin with '/', or <code>null</code> if there is no path pattern specified for this destination.*/
		public Pattern getPathPattern() {return pathPattern;}

	/**Path constructor.
	@param path The application context-relative path within the Guise container context, which does not begin with '/'.
	@exception NullPointerException if the path is <code>null</code>.
	@exception IllegalArgumentException if the provided path is absolute.
	*/
	public AbstractDestination(final URIPath path)
	{
		this.path=checkInstance(path, "Navigation path cannot be null.").checkRelative();	//store the path, making sure it is relative
		this.pathPattern=null;	//indicate that there is no path pattern
	}

	/**Path pattern constructor.
	@param pathPattern The pattern to match an application context-relative path within the Guise container context, which does not begin with '/'.
	@exception NullPointerException if the path pattern is <code>null</code>.
	*/
	public AbstractDestination(final Pattern pathPattern)
	{
		this.pathPattern=checkInstance(pathPattern, "Navigation path pattern cannot be null.");
		this.path=null;	//indicate that there is no path
	}

	/**Determines the path to use for the requested path.
	If there is a preferred path, it is returned; otherwise, the path is returned unmodified.
	If the given path does not exist but the path would exist if made into a a collection path by adding a an ending slash, that new path is returned.
	@param session The current Guise session.
	@param path The application-relative path.
	@param bookmark The bookmark for this path, or <code>null</code> if there is no bookmark.
	@param referrerURI The URI of the referring destination or other entity with no query or fragment, or <code>null</code> if no referring URI is known.
	@return The preferred path.
	@exception NullPointerException if the given session and/or path is <code>null</code>.
	@exception ResourceIOException if there is an error accessing the resource.
	*/
	public URIPath getPath(final GuiseSession session, final URIPath path, final Bookmark bookmark, final URI referrerURI) throws ResourceIOException
	{
		if(!exists(session, path, bookmark, referrerURI))	//if this destination doesn't exist	
		{
			if(!path.isCollection())	//if a non-collection path was requested
			{
				final URIPath collectionPath=new URIPath(path.toString()+PATH_SEPARATOR);	//create a collection version of the path
				if(exists(session, collectionPath, bookmark, referrerURI))	//if the collection form of the path exists
				{
					return collectionPath;	//return the collection path
				}
			}
		}
		return path;	//return the unmodified path by default
	}

	/**Determines if the given location does indeed exist for this destination.
	This version assumes that all paths exist at this destination and returns <code>true</code>.
	@param session The current Guise session. 
	@param navigationPath The navigation path relative to the application context path.
	@param bookmark The bookmark for which navigation should occur at this navigation path, or <code>null</code> if there is no bookmark involved in navigation.
	@param referrerURI The URI of the referring navigation panel or other entity with no query or fragment, or <code>null</code> if no referring URI is known.
	@return Whether the requested path exists.
	@exception NullPointerException if the given navigation path is <code>null</code>.
	@exception ResourceIOException if there is an error accessing the resource.
	*/
	public boolean exists(final GuiseSession session, final URIPath navigationPath, final Bookmark bookmark, final URI referrerURI) throws ResourceIOException
	{
		return true;	//make it easy for simple resource destinations by assuming the resource exists
	}

	/**@return A hash code for this object.*/
	public int hashCode()
	{
		return ObjectUtilities.hashCode(getPath(), getPathPattern());	//construct a hash code from the path and path pattern
	}
	
	/**Determines if this destination is equivalent to the given object.
	This implementation considers destinations of identical types with identical paths and path patterns to be equivalent.
	@param object The object to compare to this object.
	@return <code>true</code> if the given object is an equivalent destination.
	*/
	public boolean equals(final Object object)
	{
		if(getClass().isInstance(object))	//if the given object is an instance of this object's class
		{
			final Destination destination=(Destination)object;	//cast the object to a destination (which it must be, if it's the same type as this instance
			return ObjectUtilities.equals(getPath(), destination.getPath()) && ObjectUtilities.equals(getPathPattern(), destination.getPathPattern());	//see if the paths and path patterns match 
		}
		return false;	//indicate that the objects don't match
	}
}
