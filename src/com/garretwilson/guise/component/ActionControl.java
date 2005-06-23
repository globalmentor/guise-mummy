package com.garretwilson.guise.component;

import com.garretwilson.guise.model.ActionModel;
import com.garretwilson.guise.model.DefaultActionModel;

/**Action control such as a button.
@author Garret Wilson
*/
public class ActionControl extends AbstractControl<ActionModel>
{
	
	/**ID constructor with a default data model.
	@param id The component identifier.
	@exception NullPointerException if the given identifier is <code>null</code>.
	*/
	public ActionControl(final String id)
	{
		this(id, new DefaultActionModel());	//construct the class with a default model
	}

	/**ID and model constructor.
	@param id The component identifier.
	@param model The component data model.
	@exception NullPointerException if the given identifier or model is <code>null</code>.
	*/
	public ActionControl(final String id, final ActionModel model)
	{
		super(id, model);	//construct the parent class
	}

}