package com.javaguise.component;

import com.javaguise.model.ActionModel;
import com.javaguise.session.GuiseSession;

/**Abstract control with an action model.
@author Garret Wilson
*/
public abstract class AbstractActionControl<C extends AbstractActionControl<C>> extends AbstractControl<ActionModel, C> implements ActionControl<C>
{

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractActionControl(final GuiseSession session, final String id, final ActionModel model)
	{
		super(session, id, model);	//construct the parent class
	}
}
