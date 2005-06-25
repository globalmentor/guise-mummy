package com.garretwilson.guise.component;

import com.garretwilson.guise.component.layout.*;

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

	/**Default constructor with a default vertical flow layout.*/
	public AbstractFrame()
	{
		this((String)null);	//construct the component, indicating that a default ID should be used
	}

	/**ID constructor with a default vertical flow layout.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	*/
	public AbstractFrame(final String id)
	{
		this(id, new FlowLayout(Axis.Y));	//default to flowing vertically
	}

	/**Layout constructor.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given layout is <code>null</code>.
	*/
	public AbstractFrame(final Layout layout)
	{
		this(null, layout);	//construct the component with the layout, indicating that a default ID should be used
	}

	/**ID and layout constructor.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given layout is <code>null</code>.
	*/
	public AbstractFrame(final String id, final Layout layout)
	{
		super(id, layout);	//construct the parent class
	}

}
