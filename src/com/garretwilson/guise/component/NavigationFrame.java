package com.garretwilson.guise.component;

import com.garretwilson.guise.session.GuiseSession;

/**Application frame that represents a point of navigation.
Each navigation frame subclass must provide either a Guise session constructor; or a Guise session and string ID constructor.
@author Garret Wilson
@see #NavigationFrame(GuiseSession)
@see #NavigationFrame(GuiseSession, String)
*/
public class NavigationFrame extends AbstractFrame<NavigationFrame>
{

	/**Session constructor.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public NavigationFrame(final GuiseSession<?> session)
	{
		this(session, (String)null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public NavigationFrame(final GuiseSession<?> session, final String id)
	{
		this(session, id, null);	//default to no content component
	}

	/**Session and content component constructor.
	@param session The Guise session that owns this component.
	@param content The component representing this frame's content, <code>null</code> if this frame has no content.
	@exception NullPointerException if the given session and/or content component is <code>null</code>.
	*/
	public NavigationFrame(final GuiseSession<?> session, final Component<?> content)
	{
		this(session, null, content);	//construct the component with the content component, indicating that a default ID should be used
	}

	/**Session, ID, and content component constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param content The component representing this frame's content, <code>null</code> if this frame has no content.
	@exception NullPointerException if the given session and/or content component is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public NavigationFrame(final GuiseSession<?> session, final String id, final Component<?> content)
	{
		super(session, id, content);	//construct the parent class
	}

}
