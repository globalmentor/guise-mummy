package com.guiseframework.component;

import com.guiseframework.model.*;

/**Button that stores a Boolean value in its model representing the selected state.
A validator requiring a non-<code>null</code> value is automatically installed.
@author Garret Wilson
*/
public class BooleanSelectButton extends AbstractBooleanSelectActionControl<BooleanSelectButton> implements SelectButtonControl<BooleanSelectButton>
{

	/**Default constructor with a default value model.*/
	public BooleanSelectButton()
	{
		this(new DefaultValueModel<Boolean>(Boolean.class, Boolean.FALSE));	//construct the class with a default model
	}

	/**Value model constructor.
	@param valueModel The component value model.
	@exception NullPointerException if the given value model is <code>null</code>.
	*/
	public BooleanSelectButton(final ValueModel<Boolean> valueModel)
	{
		super(valueModel);	//construct the parent class
	}

}
