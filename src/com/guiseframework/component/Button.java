package com.guiseframework.component;

import com.guiseframework.model.ActionModel;
import com.guiseframework.model.DefaultActionModel;

/**Control with an action model rendered as a button.
If an image is specified, it will be used instead of the button label, if possible.
@author Garret Wilson
*/
public class Button extends AbstractButtonControl<Button>
{

	/**Default constructor with a default action model.*/
	public Button()
	{
		this(new DefaultActionModel());	//construct the class with a default model
	}

	/**Action model constructor.
	@param actionModel The component action model.
	@exception NullPointerException if the given action model is <code>null</code>.
	*/
	public Button(final ActionModel actionModel)
	{
		super(actionModel);	//construct the parent class
	}

}
