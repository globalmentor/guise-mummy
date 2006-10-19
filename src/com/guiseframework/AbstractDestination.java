package com.guiseframework;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.net.URIUtilities.*;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;

import com.garretwilson.beans.BoundPropertyObject;

/**Abstract implementation of a navigation point, its properties, and its restrictions.
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

	/**The appplication context-relative path within the Guise container context, which does not begin with '/'.*/
	private final String path;

		/**@return The appplication context-relative path within the Guise container context, which does not begin with '/'.*/
		public String getPath() {return path;}

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
	}
}
