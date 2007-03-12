package com.guiseframework.component;

import com.guiseframework.component.layout.*;

/**An abstract base class for panels.
@author Garret Wilson
*/
public abstract class AbstractPanel<C extends Panel<C>> extends AbstractBox<C> implements Panel<C>
{

	/**Layout constructor.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given layout, is <code>null</code>.
	*/
	public AbstractPanel(final Layout<?> layout)
	{
		super(layout);	//construct the parent class
	}

}
