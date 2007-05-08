package com.guiseframework;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.net.URIUtilities.*;
import static java.util.Collections.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.garretwilson.beans.BoundPropertyObject;
import com.garretwilson.lang.ObjectUtilities;
import com.garretwilson.net.ResourceIOException;

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

	/**The appplication context-relative path within the Guise container context, which does not begin with '/', or <code>null</code> if there is no path specified for this destination.*/
	private final String path;

		/**@return The appplication context-relative path within the Guise container context, which does not begin with '/', or <code>null</code> if there is no path specified for this destination.*/
		public String getPath() {return path;}

	/**The pattern to match an appplication context-relative path within the Guise container context, which does not begin with '/', or <code>null</code> if there is no path pattern specified for this destination.*/
	private final Pattern pathPattern;

		/**@return The pattern to match an appplication context-relative path within the Guise container context, which does not begin with '/', or <code>null</code> if there is no path pattern specified for this destination.*/
		public Pattern getPathPattern() {return pathPattern;}

	/**Path constructor.
	@param path The appplication context-relative path within the Guise container context, which does not begin with '/'.
	@exception NullPointerException if the path is <code>null</code>.
	@exception IllegalArgumentException if the provided path is absolute.
	*/
	public AbstractDestination(final String path)
	{
		this.path=checkInstance(path, "Navigation path cannot be null.");
		if(isAbsolutePath(path))	//if the path is absolute
		{
			throw new IllegalArgumentException("Navigation path cannot be absolute: "+path);
		}
		this.pathPattern=null;	//indicate that there is no path pattern
	}

	/**Path pattern constructor.
	@param pathPattern The pattern to match an appplication context-relative path within the Guise container context, which does not begin with '/'.
	@exception NullPointerException if the path pattern is <code>null</code>.
	*/
	public AbstractDestination(final Pattern pathPattern)
	{
		this.pathPattern=checkInstance(pathPattern, "Navigation path pattern cannot be null.");
		this.path=null;	//indicate that there is no path
	}

	/**Determines if the given location does indeed exist for this destination.
	This version assumes that all paths exist at this destination and returns <code>true</code>.
	@param session The current Guise Session. 
	@param navigationPath The navigation path relative to the application context path.
	@param bookmark The bookmark for which navigation should occur at this navigation path, or <code>null</code> if there is no bookmark involved in navigation.
	@param referrerURI The URI of the referring navigation panel or other entity with no query or fragment, or <code>null</code> if no referring URI is known.
	@return Whether the requested path exists.
	@exception NullPointerException if the given navigation path is <code>null</code>.
	@exception ResourceIOException if there is an error accessing the resource.
	*/
	public boolean exists(final GuiseSession session, final String navigationPath, final Bookmark bookmark, final URI referrerURI) throws ResourceIOException
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
