package com.guiseframework.component;

import com.guiseframework.component.layout.*;

/**An abstract base class for panels.
@author Garret Wilson
*/
public abstract class AbstractPanel extends AbstractBox implements Panel
{

	/**Layout constructor.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given layout, is <code>null</code>.
	*/
	public AbstractPanel(final Layout<? extends Constraints> layout)
	{
		super(layout);	//construct the parent class
	}

}
