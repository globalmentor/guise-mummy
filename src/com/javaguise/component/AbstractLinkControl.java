package com.javaguise.component;

import com.javaguise.model.ActionModel;
import com.javaguise.session.GuiseSession;

/**Abstract implementation of a link.
@author Garret Wilson
*/
public abstract class AbstractLinkControl<C extends LinkControl<C>> extends AbstractActionControl<C> implements LinkControl<C>
{

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractLinkControl(final GuiseSession session, final String id, final ActionModel model)
	{
		super(session, id, model);	//construct the parent class
	}
}
