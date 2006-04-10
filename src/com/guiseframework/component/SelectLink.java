package com.guiseframework.component;

import com.guiseframework.model.*;

/**Selectable link.
@author Garret Wilson
*/
public class SelectLink extends AbstractSelectActionControl<SelectLink> implements SelectLinkControl<SelectLink>
{

	/**Default constructor.*/
	public SelectLink()
	{
		this(new DefaultLabelModel(), new DefaultActionModel(), new DefaultEnableable());	//construct the class with default models
	}

	/**Label model, action model, and enableable object constructor.
	@param labelModel The component label model.
	@param actionModel The component action model.
	@param enableable The enableable object in which to store enabled status.
	@exception NullPointerException if the given label model, action model, and/or enableable object is <code>null</code>.
	*/
	public SelectLink(final LabelModel labelModel, final ActionModel actionModel, final Enableable enableable)
	{
		super(labelModel, actionModel, enableable);	//construct the parent class
	}

	/**Prototype constructor.
	@param actionPrototype The prototype on which this component should be based.
	*/
/*TODO fix
	public SelectLink(final ActionPrototype actionPrototype)
	{
		this(actionPrototype, actionPrototype, actionPrototype);	//use the action prototype as every needed model
	}
*/
}
