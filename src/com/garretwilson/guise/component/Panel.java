package com.garretwilson.guise.component;

import com.garretwilson.guise.component.layout.*;

/**A general container component.
@author Garret Wilson
*/
public class Panel extends AbstractBox
{

	/**Default constructor with a default vertical flow layout.*/
	public Panel()
	{
		this((String)null);	//construct the component, indicating that a default ID should be used
	}

	/**ID constructor with a default vertical flow layout.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	*/
	public Panel(final String id)
	{
		this(id, new FlowLayout(Axis.Y));	//default to flowing vertically
	}

	/**Layout constructor.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given layout is <code>null</code>.
	*/
	public Panel(final Layout layout)
	{
		this(null, layout);	//construct the component with the layout, indicating that a default ID should be used
	}

	/**ID and layout constructor.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given layout is <code>null</code>.
	*/
	public Panel(final String id, final Layout layout)
	{
		super(id, layout);	//construct the parent class
	}

}
