package com.guiseframework.component;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.layout.CardLayout;

/**A tabbed panel with a card layout.
The panel's value model reflects the currently selected component, if any.
@author Garret Wilson
@see CardLayout
*/
public class TabbedPanel extends AbstractCardPanel<TabbedPanel>
{

	/**Session constructor.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public TabbedPanel(final GuiseSession session)
	{
		this(session, (String)null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public TabbedPanel(final GuiseSession session, final String id)
	{
		this(session, id, new CardLayout(session));	//construct the panel using a default layout
	}

	/**Session and layout constructor.
	@param session The Guise session that owns this component.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given session and/or layout is <code>null</code>.
	*/
	public TabbedPanel(final GuiseSession session, final CardLayout layout)
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
	protected TabbedPanel(final GuiseSession session, final String id, final CardLayout layout)
	{
		super(session, id, layout);	//construct the parent class, using the card layout's value model
	}

}
