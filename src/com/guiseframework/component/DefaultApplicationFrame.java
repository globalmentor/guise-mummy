package com.guiseframework.component;

import com.guiseframework.GuiseSession;

/**Default implementation of an application frame with no default component.
@author Garret Wilson
*/
public class DefaultApplicationFrame extends AbstractApplicationFrame<DefaultApplicationFrame>
{

	/**Session constructor.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public DefaultApplicationFrame(final GuiseSession session)
	{
		this(session, (String)null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and model constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
/*TODO add label model	
	public DefaultApplicationFrame(final GuiseSession session, final Model model)
	{
		this(session, (String)null, model);	//construct the component, indicating that a default ID should be used
	}
*/

	/**Session and ID constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DefaultApplicationFrame(final GuiseSession session, final String id)
	{
		this(session, id/*TODO add label model, new DefaultModel(session)*/, null);	//default to flowing vertically TODO remove last null when other method comes back
	}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
/*TODO add label model
	public DefaultApplicationFrame(final GuiseSession session, final String id, final Model model)
	{
		this(session, id, model, null);	//default to no component
	}
*/

	/**Session, ID, model, and component constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DefaultApplicationFrame(final GuiseSession session, final String id, /*TODO add label model, final Model model, */final Component<?> component)
	{
		super(session, id, /*TODO add label modelmodel, */component);	//construct the parent class
	}

}
