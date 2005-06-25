package com.garretwilson.guise.component;

import com.garretwilson.guise.component.layout.*;

/**An abstract base class for boxes.
@author Garret Wilson
*/
public class AbstractBox extends AbstractContainer implements Box
{

	/**Default constructor with a default vertical flow layout.*/
	public AbstractBox()
	{
		this(null);	//construct the component, indicating that a default ID should be used
	}

	/**ID constructor with a default vertical flow layout.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	*/
	public AbstractBox(final String id)
	{
		this(id, new FlowLayout(Axis.Y));	//default to flowing vertically
	}

	/**ID and layout constructor.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given layout is <code>null</code>.
	*/
	public AbstractBox(final String id, final Layout layout)
	{
		super(id, layout);	//construct the parent class
	}

}
