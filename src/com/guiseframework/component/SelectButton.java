package com.guiseframework.component;

import com.guiseframework.model.*;

/**Selectable button.
@author Garret Wilson
*/
public class SelectButton extends AbstractSelectActionControl<SelectButton> implements SelectButtonControl<SelectButton>
{

	/**Default constructor with a default data model.*/
	public SelectButton()
	{
		this(new DefaultActionModel());	//construct the class with a default model
	}

	/**Action model constructor.
	@param actionModel The component action model.
	@exception NullPointerException if the given action model is <code>null</code>.
	*/
	public SelectButton(final ActionModel actionModel)
	{
		super(actionModel);	//construct the parent class
	}

}
