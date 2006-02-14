package com.guiseframework.component;

import java.util.MissingResourceException;

import com.guiseframework.GuiseSession;
import com.guiseframework.model.ActionModel;

/**Abstract control with an action model.
@author Garret Wilson
*/
public abstract class AbstractActionControl<C extends ActionControl<C>> extends AbstractControl<C> implements ActionControl<C>
{

	/**@return The data model used by this component.*/
	public ActionModel getModel() {return (ActionModel)super.getModel();}

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
