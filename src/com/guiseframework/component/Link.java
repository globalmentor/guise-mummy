package com.guiseframework.component;

import com.guiseframework.model.*;
import com.guiseframework.prototype.ActionPrototype;

/**Control with an action model rendered as a link.
@author Garret Wilson
*/
public class Link extends AbstractLinkControl<Link>
{

	/**Default constructor.*/
	public Link()
	{
		this(new DefaultLabelModel(), new DefaultActionModel(), new DefaultEnableable());	//construct the class with default models
	}

	/**Label model, action model, and enableable object constructor.
	@param labelModel The component label model.
	@param actionModel The component action model.
	@param enableable The enableable object in which to store enabled status.
	@exception NullPointerException if the given label model, action model, and/or enableable object is <code>null</code>.
	*/
	public Link(final LabelModel labelModel, final ActionModel actionModel, final Enableable enableable)
	{
		super(labelModel, actionModel, enableable);	//construct the parent class
	}

	/**Prototype constructor.
	@param actionPrototype The prototype on which this component should be based.
	*/
	public Link(final ActionPrototype actionPrototype)
	{
		this(actionPrototype, actionPrototype, actionPrototype);	//use the action prototype as every needed model
	}

}
