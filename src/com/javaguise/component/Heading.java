package com.javaguise.component;

import com.javaguise.model.*;
import com.javaguise.session.GuiseSession;

/**A heading component.
@author Garret Wilson
*/
public class Heading extends AbstractModelComponent<HeadingModel, Heading>
{

	/**Session constructor with a default model.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public Heading(final GuiseSession<?> session)
	{
		this(session, null);	//construct the component, indicating that a default ID should be used
	}

	/**Session constructor with a default model with the given heading level.
	@param session The Guise session that owns this component.
	@param level The zero-based level of the heading, or <code>-1</code> if no level is specified.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public Heading(final GuiseSession<?> session, final int level)
	{
		this(session, null, level);	//construct the component, indicating that a default ID and the given heading level should be used
	}

	/**Session and ID constructor with a default data model.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Heading(final GuiseSession<?> session, final String id)
	{
		this(session, id, HeadingModel.NO_HEADING_LEVEL);	//construct the class with a default model with no heading level
	}

	/**Session, ID, and heading level.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param level The zero-based level of the heading, or <code>-1</code> if no level is specified.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Heading(final GuiseSession<?> session, final String id, final int level)
	{
		super(session, id, new DefaultHeadingModel(session, level));	//construct the class with a default model
	}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Heading(final GuiseSession<?> session, final String id, final HeadingModel model)
	{
		super(session, id, model);	//construct the parent class
	}
}
