package com.garretwilson.guise.component;

import com.garretwilson.guise.component.layout.Layout;

/**A general container component.
@author Garret Wilson
*/
public class Panel extends AbstractBox
{

	/**ID constructor.
	@param id The component identifier.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given identifier or layout is <code>null</code>.
	*/
	public Panel(final String id, final Layout layout)
	{
		super(id, layout);	//construct the parent class
	}

}
