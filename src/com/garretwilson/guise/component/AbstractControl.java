package com.garretwilson.guise.component;

import com.garretwilson.guise.model.Model;

/**An abstract implementation of a model component that allows user interaction to modify the model.
@author Garret Wilson
*/
public class AbstractControl<M extends Model> extends AbstractModelComponent<M> implements Control<M>
{

	/**ID and model constructor.
	@param id The component identifier.
	@param model The component data model.
	@exception NullPointerException if the given model is <code>null</code>.
	*/
	public AbstractControl(final String id, final M model)
	{
		super(id, model);	//construct the parent class
	}

}
