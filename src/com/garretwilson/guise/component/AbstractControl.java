package com.garretwilson.guise.component;

import com.garretwilson.guise.model.Model;
import com.garretwilson.guise.session.GuiseSession;

/**An abstract implementation of a model component that allows user interaction to modify the model.
@author Garret Wilson
*/
public class AbstractControl<M extends Model> extends AbstractModelComponent<M> implements Control<M>
{

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractControl(final GuiseSession<?> session, final String id, final M model)
	{
		super(session, id, model);	//construct the parent class
	}

}
