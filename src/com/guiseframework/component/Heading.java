package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;
import com.guiseframework.GuiseSession;
import com.guiseframework.model.*;

/**A heading component.
@author Garret Wilson
*/
public class Heading extends AbstractComponent<Heading>
{

	/**The heading level value indicating no heading level.*/
	public final static int NO_HEADING_LEVEL=-1;
	
	/**The level bound property.*/
	public final static String LEVEL_PROPERTY=getPropertyName(Heading.class, "level");

	/**The zero-based level of the heading, or {@link #NO_HEADING_LEVEL} if no level is specified.*/
	private int level;

		/**@return The zero-based level of the heading, or {@link #NO_HEADING_LEVEL} if no level is specified.*/
		public int getLevel() {return level;}

		/**Sets the level of the heading.
		This is a bound property of type <code>Integer</code>.
		@param newLevel The new zero-based heading level, or {@link #NO_HEADING_LEVEL} if no level is specified.
		@see #LEVEL_PROPERTY
		*/
		public void setLevel(final int newLevel)
		{
			if(level!=newLevel)	//if the value is really changing
			{
				final int oldLevel=level;	//get the old value
				level=newLevel;	//actually change the value
				firePropertyChange(LEVEL_PROPERTY, oldLevel, newLevel);	//indicate that the value changed
			}			
		}

	/**Session constructor with a default model.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public Heading(final GuiseSession session)
	{
		this(session, null);	//construct the component, indicating that a default ID should be used
	}

	/**Session constructor with a default model with the given heading level.
	@param session The Guise session that owns this component.
	@param level The zero-based level of the heading, or {@link #NO_HEADING_LEVEL} if no level is specified.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public Heading(final GuiseSession session, final int level)
	{
		this(session, null, level);	//construct the component, indicating that a default ID and the given heading level should be used
	}

	/**Session and ID constructor with a default data model.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Heading(final GuiseSession session, final String id)
	{
		this(session, id, NO_HEADING_LEVEL);	//construct the class with a default model with no heading level
	}

	/**Session, ID, and heading level.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param level The zero-based level of the heading, or {@link #NO_HEADING_LEVEL} if no level is specified.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Heading(final GuiseSession session, final String id, final int level)
	{
		this(session, id, new DefaultModel(session), level);	//construct the class with a default model
	}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Heading(final GuiseSession session, final String id, final Model model)
	{
		this(session, id, model, NO_HEADING_LEVEL);	//construct the class with no heading level
	}

	/**Session, ID, model, and level constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param level The zero-based level of the heading, or {@link #NO_HEADING_LEVEL} if no level is specified.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Heading(final GuiseSession session, final String id, final Model model, final int level)
	{
		super(session, id, model);	//construct the parent class
		this.level=level;	//save the level
	}
}
