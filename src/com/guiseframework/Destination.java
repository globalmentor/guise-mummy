package com.guiseframework;

import java.util.List;
import java.util.regex.Pattern;

import com.garretwilson.beans.PropertyBindable;

/**Description of a navigation point, its properties, and its restrictions.
@author Garret Wilson
*/
public interface Destination extends PropertyBindable
{

	/**@return The appplication context-relative path within the Guise container context, which does not begin with '/', or <code>null</code> if there is no path specified for this destination.*/
	public String getPath();

	/**@return The pattern to match an appplication context-relative path within the Guise container context, which does not begin with '/', or <code>null</code> if there is no path pattern specified for this destination.*/
	public Pattern getPathPattern();

	/**@return The style of this destination, or <code>null</code> if no destination-specific style is specified.*/
//TODO del if not needed	public URI getStyle();

	/**The read-only iterable of categories.*/
	public Iterable<Category> getCategories();

	/**Sets the categories.
	@param categories The list of new categories.
	*/
	public void setCategories(final List<Category> categories);

}
