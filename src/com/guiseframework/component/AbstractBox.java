package com.guiseframework.component;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.layout.*;

/**An abstract base class for boxes.
@author Garret Wilson
*/
public abstract class AbstractBox<C extends Box<C>> extends AbstractContainer<C> implements Box<C>
{

	/**Session, ID, and layout constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given session, layout, and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractBox(final GuiseSession session, final String id, final Layout layout)
	{
		super(session, id, layout);	//construct the parent class
	}
}
