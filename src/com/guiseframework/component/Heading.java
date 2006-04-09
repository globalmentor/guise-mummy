package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;
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

	/**Default constructor with a default label model.*/
	public Heading()
	{
		this(NO_HEADING_LEVEL);	//construct the class with a default model with no heading level
	}

	/**Heading level constructor with a default label model.
	@param level The zero-based level of the heading, or {@link #NO_HEADING_LEVEL} if no level is specified.
	*/
	public Heading(final int level)
	{
		this(new DefaultLabelModel(), level);	//construct the class with a default model
	}

	/**Label model constructor.
	@param labelModel The component label model.
	@exception NullPointerException if the given label model is <code>null</code>.
	*/
	public Heading(final LabelModel labelModel)
	{
		this(labelModel, NO_HEADING_LEVEL);	//construct the class with no heading level
	}

	/**Label model and level constructor.
	@param labelModel The component label model.
	@param level The zero-based level of the heading, or {@link #NO_HEADING_LEVEL} if no level is specified.
	@exception NullPointerException if the given session label model is <code>null</code>.
	*/
	public Heading(final LabelModel labelModel, final int level)
	{
		super(labelModel);	//construct the parent class
		this.level=level;	//save the level
	}
}
