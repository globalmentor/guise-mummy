package com.garretwilson.guise.component;

import com.garretwilson.guise.component.layout.*;
import com.garretwilson.guise.session.GuiseSession;

/**Application frame that represents a point of navigation.
Each navigation frame must provide either a Guise session constructor; or a Guise session constructor and single ID string constructor.
@author Garret Wilson
@see #NavigationFrame(GuiseSession)
@see #NavigationFrame(GuiseSession, String)
*/
public class NavigationFrame extends AbstractFrame
{

	/**The Guise session that owns this frame.*/
	private final GuiseSession<?> session;

		/**@return The Guise session that owns this frame.*/
		public GuiseSession<?> getSession() {return session;}

	/**Session constructor with a default vertical flow layout.
	@param session The Guise session that owns this frame.
	@exception NullPointerException if the provided session is null.
	*/
	public NavigationFrame(final GuiseSession<?> session)
	{
		this(session, (String)null);	//construct the component, indicating that a default ID should be used
	}

	/**ID constructor with a default vertical flow layout.
	@param session The Guise session that owns this frame.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the provided session is null.
	*/
	public NavigationFrame(final GuiseSession<?> session, final String id)
	{
		this(session, id, new FlowLayout(Axis.Y));	//default to flowing vertically
	}

	/**Layout constructor.
	@param session The Guise session that owns this frame.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given session or layout is <code>null</code>.
	*/
	public NavigationFrame(final GuiseSession<?> session, final Layout layout)
	{
		this(session, null, layout);	//construct the component with the layout, indicating that a default ID should be used
	}

	/**ID and layout constructor.
	@param session The Guise session that owns this frame.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given session or layout is <code>null</code>.
	*/
	public NavigationFrame(final GuiseSession<?> session, final String id, final Layout layout)
	{
		super(id, layout);	//construct the parent class
		this.session=session;	//store the session
	}

}
