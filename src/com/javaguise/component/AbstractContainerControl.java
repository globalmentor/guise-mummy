package com.javaguise.component;

import com.javaguise.GuiseSession;
import com.javaguise.component.layout.Layout;
import com.javaguise.model.ControlModel;
import com.javaguise.model.Model;

/**An abstract implementation of a container that is also a control.
@author Garret Wilson
*/
public abstract class AbstractContainerControl<C extends Container<C> & Control<C>> extends AbstractContainer<C> implements Control<C>
{

	/**@return The data model used by this component.*/
	@SuppressWarnings("unchecked")
	public ControlModel getModel() {return (ControlModel)super.getModel();}

	/**Session, ID, layout, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param layout The layout definition for the container.
	@param model The component data model.
	@exception NullPointerException if the given session, layout, and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractContainerControl(final GuiseSession session, final String id, final Layout layout, final Model model)
	{
		super(session, id, layout, model);	//construct the parent class
	}
}
