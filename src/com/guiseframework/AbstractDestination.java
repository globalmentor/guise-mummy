package com.guiseframework;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.net.URIUtilities.*;

import com.garretwilson.beans.BoundPropertyObject;

/**Abstract implementation of a navigation point, its properties, and its restrictions.
@author Garret Wilson
*/
public abstract class AbstractDestination extends BoundPropertyObject implements Destination
{

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
