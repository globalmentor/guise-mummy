package com.javaguise.component;

import com.javaguise.model.LabelModel;
import com.javaguise.session.GuiseSession;

/**An abstract implementation of a model component that allows user interaction to modify the model.
@author Garret Wilson
*/
public abstract class AbstractControl<M extends LabelModel, C extends Control<M, C>> extends AbstractModelComponent<M, C> implements Control<M, C>
{

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractControl(final GuiseSession session, final String id, final M model)
	{
		super(session, id, model);	//construct the parent class
	}

}
