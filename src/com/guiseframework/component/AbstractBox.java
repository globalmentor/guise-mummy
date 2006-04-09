package com.guiseframework.component;

import com.guiseframework.component.layout.*;

/**An abstract base class for boxes.
@author Garret Wilson
*/
public abstract class AbstractBox<C extends Box<C>> extends AbstractContainer<C> implements Box<C>
{

	/**Layout constructor.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given layout is <code>null</code>.
	*/
	public AbstractBox(final Layout layout)
	{
		super(layout);	//construct the parent class
	}
}
