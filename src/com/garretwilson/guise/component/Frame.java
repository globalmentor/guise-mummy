package com.garretwilson.guise.component;

import static com.garretwilson.lang.ClassUtilities.*;

/**A root-level container such as a window or an HTML page.
Each frame must provide either a default constructor or a single ID string constructor.
@author Garret Wilson
*/
public interface Frame extends Box
{
	/**The title bound property.*/
	public final static String TITLE_PROPERTY=getPropertyName(Frame.class, "title");

	/**@return The frame title, or <code>null</code> if there is no title.*/
	public String getTitle();

	/**Sets the title of the frame.
	This is a bound property.
	@param newTitle The new title of the frame.
	@see #TITLE_PROPERTY
	*/
	public void setTitle(final String newTitle);

}
