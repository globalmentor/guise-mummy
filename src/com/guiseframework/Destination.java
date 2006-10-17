package com.guiseframework;

import com.garretwilson.beans.PropertyBindable;

/**Description of a navigation point, its properties, and its restrictions.
@author Garret Wilson
*/
public interface Destination extends PropertyBindable
{

	/**@return The appplication context-relative path within the Guise container context, which does not begin with '/'.*/
	public String getPath();

	/**@return The style of this destination, or <code>null</code> if no destination-specific style is specified.*/
//TODO del if not needed	public URI getStyle();

}
