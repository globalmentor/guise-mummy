package com.garretwilson.guise.component;

import com.garretwilson.guise.model.*;
import com.garretwilson.guise.session.GuiseSession;

/**A label component.
@author Garret Wilson
*/
public class Label extends AbstractModelComponent<LabelModel, Label>
{

	/**Session constructor with a default model.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public Label(final GuiseSession<?> session)
	{
		this(session, null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor with a default data model.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Label(final GuiseSession<?> session, final String id)
	{
		this(session, id, new DefaultLabelModel(session));	//construct the class with a default model
	}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Label(final GuiseSession<?> session, final String id, final LabelModel model)
	{
		super(session, id, model);	//construct the parent class
	}
}
