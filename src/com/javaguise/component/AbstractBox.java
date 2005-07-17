package com.javaguise.component;

import com.javaguise.component.layout.*;
import com.javaguise.session.GuiseSession;

/**An abstract base class for boxes.
@author Garret Wilson
*/
public abstract class AbstractBox<C extends Box<C>> extends AbstractContainer<C> implements Box<C>
{

	/**Session constructor with a default vertical flow layout.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public AbstractBox(final GuiseSession<?> session)
	{
		this(session, (String)null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor with a default vertical flow layout.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractBox(final GuiseSession<?> session, final String id)
	{
		this(session, id, new FlowLayout(Orientation.Flow.PAGE));	//default to flowing vertically
	}

	/**Session and layout constructor.
	@param session The Guise session that owns this component.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given session and/or layout is <code>null</code>.
	*/
	public AbstractBox(final GuiseSession<?> session, final Layout layout)
	{
		this(session, null, layout);	//construct the component with the layout, indicating that a default ID should be used
	}

	/**Session, ID, and layout constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given session and/or layout is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractBox(final GuiseSession<?> session, final String id, final Layout layout)
	{
		super(session, id, layout);	//construct the parent class
	}

}
