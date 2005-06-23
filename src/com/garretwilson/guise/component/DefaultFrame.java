package com.garretwilson.guise.component;

import com.garretwilson.guise.component.layout.Layout;

/**Default implementation of a frame.
@author Garret Wilson
*/
public class DefaultFrame extends AbstractBox implements Frame
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

	/**ID constructor.
	@param id The component identifier.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given identifier or layout is <code>null</code>.
	*/
	public DefaultFrame(final String id, final Layout layout)
	{
		super(id, layout);	//construct the parent class
	}

}
