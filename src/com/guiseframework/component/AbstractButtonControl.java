package com.guiseframework.component;

import com.guiseframework.model.ActionModel;

/**Abstract implementation of a button.
@author Garret Wilson
*/
public abstract class AbstractButtonControl<C extends ButtonControl<C>> extends AbstractActionControl<C> implements ButtonControl<C>
{

	/**Action model constructor.
	@param actionModel The component action model.
	@exception NullPointerException if the given action model is <code>null</code>.
	*/
	public AbstractButtonControl(final ActionModel actionModel)
	{
		super(actionModel);	//construct the parent class
	}
}
