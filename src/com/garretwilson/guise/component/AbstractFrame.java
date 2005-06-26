package com.garretwilson.guise.component;

import com.garretwilson.guise.component.layout.*;
import com.garretwilson.guise.session.GuiseSession;

/**Default implementation of a frame.
@author Garret Wilson
*/
public abstract class AbstractFrame extends AbstractBox implements Frame
{

	/**The frame title, or <code>null</code> if there is no title.*/
	private String title=null;

		/**@return The frame title, or <code>null</code> if there is no title.*/
		public String getTitle() {return title;}

		/**Sets the title of the frame.
		This is a bound property.
		@param newTitle The new title of the frame.
		@see Frame#TITLE_PROPERTY
		*/
		public void setTitle(final String newTitle)
		{
			if(title!=newTitle)	//if the value is really changing
			{
				final String oldTitle=title;	//get the old value
				title=newTitle;	//actually change the value
				firePropertyChange(TITLE_PROPERTY, oldTitle, newTitle);	//indicate that the value changed
			}			
		}

		/**Session constructor with a default vertical flow layout.
		@param session The Guise session that owns this component.
		@exception NullPointerException if the given session is <code>null</code>.
		*/
		public AbstractFrame(final GuiseSession<?> session)
		{
			this(session, (String)null);	//construct the component, indicating that a default ID should be used
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

		/**Session and layout constructor.
		@param session The Guise session that owns this component.
		@param layout The layout definition for the container.
		@exception NullPointerException if the given session and/or layout is <code>null</code>.
		*/
		public AbstractFrame(final GuiseSession<?> session, final Layout layout)
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
		public AbstractFrame(final GuiseSession<?> session, final String id, final Layout layout)
		{
			super(session, id, layout);	//construct the parent class
		}

}
