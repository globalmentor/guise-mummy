package com.guiseframework.component;

import com.guiseframework.GuiseSession;

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

	/**Session and ID constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DefaultFlyoverFrame(final GuiseSession session, final String id)
	{
		this(session, id, new LayoutPanel(session));	//default to a layout panel
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
		super(session, id, component);	//construct the parent class
	}

}
