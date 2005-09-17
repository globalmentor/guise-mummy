package com.javaguise.component;

import com.javaguise.component.layout.Layout;
import com.javaguise.model.LabelModel;
import com.javaguise.session.GuiseSession;

/**An abstract implementation of a container that is also a control.
@param <M> The type of model contained in the component.
@author Garret Wilson
*/
public abstract class AbstractContainerControl<M extends LabelModel, C extends Container<C> & Control<M, C>> extends AbstractModelContainer<M, C> implements Control<M, C>
{

	/**Session, ID, layout, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param layout The layout definition for the container.
	@param model The component data model.
	@exception NullPointerException if the given session, layout, and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractContainerControl(final GuiseSession session, final String id, final Layout layout, final M model)
	{
		super(session, id, layout, model);	//construct the parent class
	}
}
