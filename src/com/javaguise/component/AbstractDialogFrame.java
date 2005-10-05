package com.javaguise.component;

import com.javaguise.model.ValueModel;
import com.javaguise.session.GuiseSession;

/**Abstract implementation of a frame meant for communication of a value.
A dialog frame by default is modal and movable but not resizable.
@param <V> The value to be communicated.
@author Garret Wilson
*/
public abstract class AbstractDialogFrame<V, C extends DialogFrame<V, C>> extends AbstractFrame<C> implements DialogFrame<V, C>
{

	/**@return The data model used by this component.*/
	@SuppressWarnings("unchecked")
	public ValueModel<V> getModel() {return (ValueModel<V>)super.getModel();}

	/**Session, ID, model, and component constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractDialogFrame(final GuiseSession session, final String id, final ValueModel<V> model, final Component<?> component)
	{
		super(session, id, model, component);	//construct the parent class
		setModal(true);	//default to being a modal frame
		setMovable(true);	//default to being movable
		setResizable(false);	//default to not allowing resizing
	}

}
