package com.garretwilson.guise.model;

import com.garretwilson.beans.BoundPropertyObject;
import com.garretwilson.event.EventListenerManager;

/**A base class implementing helpful functionality for models.
@author Garret Wilson
*/
public class AbstractModel extends BoundPropertyObject implements Model
{
	/**The object managing event listeners.*/
	private final EventListenerManager eventListenerManager=new EventListenerManager();

		/**@return The object managing event listeners.*/
		protected EventListenerManager getEventListenerManager() {return eventListenerManager;}
}
