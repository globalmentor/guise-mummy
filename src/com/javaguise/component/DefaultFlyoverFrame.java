package com.javaguise.component;

import com.javaguise.model.DefaultLabelModel;
import com.javaguise.model.LabelModel;
import com.javaguise.session.GuiseSession;

/**Default implementation of a flyover frame with default layout panel.
A flyover frame by default is nonmodal, immovable, and not resizable.
For example, with a tether bearing of 250 and a tether resource key of "myTether", a resource key will be requested using "myTether.WSW", "myTether.SWbW", "myTether.SW", etc.
	until all compass points are exhausted, after which a resource key of "myTether" will be requested.
@author Garret Wilson
*/
public class DefaultFlyoverFrame extends AbstractFlyoverFrame<DefaultFlyoverFrame>
{

	/**Session constructor.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public DefaultFlyoverFrame(final GuiseSession session)
	{
		this(session, (String)null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and component constructor.
	@param session The Guise session that owns this component.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public DefaultFlyoverFrame(final GuiseSession session, final Component<?> component)
	{
		this(session, (String)null, component);	//construct the component, indicating that a default ID should be used
	}

	/**Session and model constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public DefaultFlyoverFrame(final GuiseSession session, final LabelModel model)
	{
		this(session, (String)null, model);	//construct the component, indicating that a default ID should be used
	}

	/**Session, model, and component constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public DefaultFlyoverFrame(final GuiseSession session, final LabelModel model, final Component<?> component)
	{
		this(session, (String)null, model, component);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DefaultFlyoverFrame(final GuiseSession session, final String id)
	{
		this(session, id, new DefaultLabelModel(session));	//use a default label model
	}

	/**Session, ID, and component constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DefaultFlyoverFrame(final GuiseSession session, final String id, final Component<?> component)
	{
		this(session, id, new DefaultLabelModel(session), component);	//use a default label model
	}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DefaultFlyoverFrame(final GuiseSession session, final String id, final LabelModel model)
	{
		this(session, id, model, new LayoutPanel(session));	//default to a layout panel
	}

	/**Session, ID, model, and component constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DefaultFlyoverFrame(final GuiseSession session, final String id, final LabelModel model, final Component<?> component)
	{
		super(session, id, model, component);	//construct the parent class
	}

}
