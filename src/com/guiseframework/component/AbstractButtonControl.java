package com.guiseframework.component;

import com.guiseframework.GuiseSession;
import com.guiseframework.model.ActionModel;

/**Abstract implementation of a button.
@author Garret Wilson
*/
public abstract class AbstractButtonControl<C extends ButtonControl<C>> extends AbstractActionControl<C> implements ButtonControl<C>
{

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param actionModel The component action model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractButtonControl(final GuiseSession session, final String id, final ActionModel actionModel)
	{
		super(session, id, actionModel);	//construct the parent class
	}
}
