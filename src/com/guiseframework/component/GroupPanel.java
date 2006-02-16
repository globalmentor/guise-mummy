package com.guiseframework.component;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.layout.*;

/**A panel for grouping multiple components with a default page flow layout.
@author Garret Wilson
*/
public class GroupPanel extends AbstractBox<GroupPanel> implements Panel<GroupPanel>	//TODO maybe add a label model constructor parameter; see old revisions
{

	/**Session constructor with a default vertical flow layout.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public GroupPanel(final GuiseSession session)
	{
		this(session, (String)null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor with a default vertical flow layout.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public GroupPanel(final GuiseSession session, final String id)
	{
		this(session, id, new FlowLayout(session, Flow.PAGE));	//default to flowing vertically
	}

	/**Session and layout constructor.
	@param session The Guise session that owns this component.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given session and/or layout is <code>null</code>.
	*/
	public GroupPanel(final GuiseSession session, final Layout layout)
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
	public GroupPanel(final GuiseSession session, final String id, final Layout layout)
	{
		super(session, id, layout);	//construct the parent class
	}

}
