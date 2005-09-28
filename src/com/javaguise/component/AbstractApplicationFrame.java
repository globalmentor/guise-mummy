package com.javaguise.component;

import com.javaguise.model.LabelModel;
import com.javaguise.session.GuiseSession;

/**Abstract implementation of an application frame.
@author Garret Wilson
@see LayoutPanel
*/
public abstract class AbstractApplicationFrame<C extends ApplicationFrame<C>> extends AbstractFrame<C> implements ApplicationFrame<C>
{

	/**Session, ID, model, and component constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractApplicationFrame(final GuiseSession session, final String id, final LabelModel model, final Component<?> component)
	{
		super(session, id, model, component);	//construct the parent class
	}
}
