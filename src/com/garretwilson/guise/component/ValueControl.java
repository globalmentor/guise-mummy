package com.garretwilson.guise.component;

import com.garretwilson.guise.model.DefaultValueModel;
import com.garretwilson.guise.model.ValueModel;

/**Control to accept input from the user.
@author Garret Wilson
*/
public class ValueControl<V> extends AbstractControl<ValueModel<V>>
{
	
	/**ID constructor with a default data model to represent a given type.
	@param id The component identifier.
	@param valueClass The class indicating the type of value held in the model.
	@exception NullPointerException if the given identifier is <code>null</code>.
	*/
	public ValueControl(final String id, final Class<V> valueClass)
	{
		this(id, new DefaultValueModel<V>(valueClass));	//construct the class with a default model
	}

	/**ID and model constructor.
	@param id The component identifier.
	@param model The component data model.
	@exception NullPointerException if the given identifier or model is <code>null</code>.
	*/
	public ValueControl(final String id, final ValueModel<V> model)
	{
		super(id, model);	//construct the parent class
	}

}
