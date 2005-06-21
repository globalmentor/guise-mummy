package com.garretwilson.guise.component;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.garretwilson.event.EventListenerManager;
import com.garretwilson.util.BoundPropertyObject;

/**An abstract implementation of a component.
@author Garret Wilson
*/
public class AbstractComponent extends BoundPropertyObject implements Component
{

	/**The object managing event listeners.*/
	private final EventListenerManager eventListenerManager=new EventListenerManager();

		/**@return The object managing event listeners.*/
		protected EventListenerManager getEventListenerManager() {return eventListenerManager;}

	/**The component identifier*/
	private final String id;

		/**@return The component identifier.*/
		public String getID() {return id;}

	/**The style identifier, or <code>null</code> if there is no style ID.*/
	private String styleID=null;

		/**@return The style identifier, or <code>null</code> if there is no style ID.*/
		public String getStyleID() {return styleID;}

		/**Identifies the style for the component.
		This is a bound property.
		@param newStyleID The style identifier, or <code>null</code> if there is no style ID.
		@see Component#STYLE_ID_PROPERTY
		*/
		public void setStyleID(final String newStyleID)
		{
			if(styleID!=newStyleID)	//if the value is really changing
			{
				final String oldStyleID=styleID;	//get the current value
				styleID=newStyleID;	//update the value
				firePropertyChange(STYLE_ID_PROPERTY, oldStyleID, newStyleID);
			}
		}

	/**Whether the component is visible.*/
	private boolean visible=true;

		/**@return Whether the component is visible.*/
		public boolean isVisible() {return visible;}

		/**Sets whether the component is visible.
		This is a bound property of type <code>Boolean</code>.
		@param newVisible <code>true</code> if the component should be visible, else <code>false</code>.
		@see Component#VISIBLE_PROPERTY
		*/
		public void setVisible(final boolean newVisible)
		{
			if(visible!=newVisible)	//if the value is really changing
			{
				final boolean oldVisible=visible;	//get the current value
				visible=newVisible;	//update the value
				firePropertyChange(VISIBLE_PROPERTY, Boolean.valueOf(oldVisible), Boolean.valueOf(newVisible));
			}
		}

	/**ID constructor.
	@param id The component identifier.
	@exception NullPointerException if the given identifier is <code>null</code>.
	*/
	public AbstractComponent(final String id)
	{
		this.id=checkNull(id, "Component identifier cannot be null.");	//save the ID
	}
}
