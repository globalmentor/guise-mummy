package com.garretwilson.guise.component;

import com.garretwilson.guise.component.layout.*;
import com.garretwilson.guise.model.DefaultLabelModel;
import com.garretwilson.guise.model.LabelModel;
import com.garretwilson.guise.session.GuiseSession;

/**Default implementation of a frame.
@author Garret Wilson
*/
public abstract class AbstractFrame<C extends Frame<C>> extends AbstractModelBox<LabelModel, C> implements Frame<C>
{

	/**Session constructor with a default vertical flow layout.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public AbstractFrame(final GuiseSession<?> session)
	{
		this(session, (String)null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and model constructor with a default vertical flow layout.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public AbstractFrame(final GuiseSession<?> session, final LabelModel model)
	{
		this(session, (String)null, model);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor with a default vertical flow layout.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractFrame(final GuiseSession<?> session, final String id)
	{
		this(session, id, new FlowLayout(Axis.Y));	//default to flowing vertically
	}

	/**Session, ID, and model constructor with a default vertical flow layout.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractFrame(final GuiseSession<?> session, final String id, final LabelModel model)
	{
		this(session, id, new FlowLayout(Axis.Y), model);	//default to flowing vertically
	}

	/**Session and layout constructor.
	@param session The Guise session that owns this component.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given session and/or layout is <code>null</code>.
	*/
	public AbstractFrame(final GuiseSession<?> session, final Layout layout)
	{
		this(session, null, layout);	//construct the component with the layout, indicating that a default ID should be used
	}

	/**Session, layout, and model constructor.
	@param session The Guise session that owns this component.
	@param layout The layout definition for the container.
	@param model The component data model.
	@exception NullPointerException if the given session, layout, and/or model is <code>null</code>.
	*/
	public AbstractFrame(final GuiseSession<?> session, final Layout layout, final LabelModel model)
	{
		this(session, null, layout, model);	//construct the component with the layout, indicating that a default ID should be used
	}

	/**Session, ID, and layout constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given session and/or layout is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractFrame(final GuiseSession<?> session, final String id, final Layout layout)
	{
		super(session, id, layout, new DefaultLabelModel(session));	//construct the parent class
	}

	/**Session, ID, layout, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param layout The layout definition for the container.
	@param model The component data model.
	@exception NullPointerException if the given session, layout, and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractFrame(final GuiseSession<?> session, final String id, final Layout layout, final LabelModel model)
	{
		super(session, id, layout, model);	//construct the parent class
	}
}
