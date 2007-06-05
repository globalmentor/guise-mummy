package com.guiseframework.component;

import com.guiseframework.model.*;
import com.guiseframework.prototype.ValuePrototype;

/**Button that stores a Boolean value in its model representing the selected state.
A validator requiring a non-<code>null</code> value is automatically installed.
@author Garret Wilson
*/
public class BooleanSelectButton extends AbstractBooleanSelectActionControl<BooleanSelectButton> implements SelectButtonControl<BooleanSelectButton>
{

	/**Default constructor.*/
	public BooleanSelectButton()
	{
		this(new DefaultLabelModel(), new DefaultActionModel(), new DefaultValueModel<Boolean>(Boolean.class, Boolean.FALSE), new DefaultEnableable());	//construct the class with default models
	}

	/**Label model, action model, value model, and enableable object constructor.
	@param labelModel The component label model.
	@param actionModel The component action model.
	@param valueModel The component value model.
	@param enableable The enableable object in which to store enabled status.
	@exception NullPointerException if the given label model, action model, and/or enableable object is <code>null</code>.
	*/
	public BooleanSelectButton(final LabelModel labelModel, final ActionModel actionModel, final ValueModel<Boolean> valueModel, final Enableable enableable)
	{
		super(labelModel, actionModel, valueModel, enableable);	//construct the parent class		
	}

	/**Prototype constructor.
	@param valuePrototype The prototype on which this component should be based.
	*/
	public BooleanSelectButton(final ValuePrototype<Boolean> valuePrototype)
	{
		this(valuePrototype, new DefaultActionModel(), valuePrototype, valuePrototype);	//use the value prototype as every needed model except for the action model
	}
}
