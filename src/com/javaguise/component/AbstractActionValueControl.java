package com.javaguise.component;

import com.javaguise.model.ActionValueModel;
import com.javaguise.session.GuiseSession;

/**Abstract implementation of an action control containing a value.
@author Garret Wilson
@param <V> The type of value the control represents.
*/
public abstract class AbstractActionValueControl<V, C extends ActionValueControl<V, C>> extends AbstractActionControl<C> implements ActionValueControl<V, C>
{

	/**@return The data model used by this component.*/
	@SuppressWarnings("unchecked")
	public ActionValueModel<V> getModel() {return (ActionValueModel<V>)super.getModel();}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractActionValueControl(final GuiseSession session, final String id, final ActionValueModel<V> model)
	{
		super(session, id, model);	//construct the parent class
	}
}
