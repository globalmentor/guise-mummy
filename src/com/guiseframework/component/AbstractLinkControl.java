package com.guiseframework.component;

import com.guiseframework.model.ActionModel;

/**Abstract implementation of a link.
@author Garret Wilson
*/
public abstract class AbstractLinkControl<C extends LinkControl<C>> extends AbstractActionControl<C> implements LinkControl<C>
{

	/**Action model constructor.
	@param actionModel The component action model.
	@exception NullPointerException if the given action model is <code>null</code>.
	*/
	public AbstractLinkControl(final ActionModel actionModel)	//TODO create a label model parameter
	{
		super(actionModel);	//construct the parent class
	}
}
