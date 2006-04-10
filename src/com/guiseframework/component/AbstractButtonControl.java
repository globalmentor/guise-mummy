package com.guiseframework.component;

import com.guiseframework.model.*;

/**Abstract implementation of a button.
@author Garret Wilson
*/
public abstract class AbstractButtonControl<C extends ButtonControl<C>> extends AbstractActionControl<C> implements ButtonControl<C>
{

	/**Label model, action model, and enableable object constructor.
	@param labelModel The component label model.
	@param actionModel The component action model.
	@param enableable The enableable object in which to store enabled status.
	@exception NullPointerException if the given label model, action model, and/or enableable object is <code>null</code>.
	*/
	public AbstractButtonControl(final LabelModel labelModel, final ActionModel actionModel, final Enableable enableable)
	{
		super(labelModel, actionModel, enableable);	//construct the parent class
	}
}
