package com.javaguise.component;

import com.javaguise.component.layout.*;
import com.javaguise.model.Model;
import com.javaguise.session.GuiseSession;

/**An abstract base class for panels.
@author Garret Wilson
*/
public abstract class AbstractPanel<C extends Panel<C>> extends AbstractBox<C> implements Panel<C>
{

	/**Session, ID, layout, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param layout The layout definition for the container.
	@param model The component data model.
	@exception NullPointerException if the given session, layout, and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractPanel(final GuiseSession session, final String id, final Layout layout, final Model model)
	{
		super(session, id, layout, model);	//construct the parent class
	}

}
