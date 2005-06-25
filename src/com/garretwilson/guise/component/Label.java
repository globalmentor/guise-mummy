package com.garretwilson.guise.component;

import com.garretwilson.guise.model.*;

/**A label component.
@author Garret Wilson
*/
public class Label extends AbstractModelComponent<MessageModel>
{

	/**Default constructor with a default identifier and default model.*/
	public Label()
	{
		this(null);	//construct the component, indicating that a default ID should be used
	}

	/**ID constructor with a default data model.
	@param id The component identifier.
	*/
	public Label(final String id)
	{
		this(id, new DefaultMessageModel());	//construct the class with a default model
	}

	/**ID and model constructor.
	@param id The component identifier.
	@param model The component data model.
	@exception NullPointerException if the given model is <code>null</code>.
	*/
	public Label(final String id, final MessageModel model)
	{
		super(id, model);	//construct the parent class
	}
}
