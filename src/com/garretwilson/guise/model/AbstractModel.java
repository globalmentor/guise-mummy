package com.garretwilson.guise.model;

import java.beans.PropertyChangeEvent;

import com.garretwilson.beans.BoundPropertyObject;
import com.garretwilson.event.EventListenerManager;
import com.garretwilson.guise.session.GuiseSession;

import static com.garretwilson.lang.ObjectUtilities.*;

/**A base abstract class implementing helpful functionality for models.
@author Garret Wilson
*/
public class AbstractModel extends BoundPropertyObject implements Model
{
	/**The object managing event listeners.*/
	private final EventListenerManager eventListenerManager=new EventListenerManager();

		/**@return The object managing event listeners.*/
		protected EventListenerManager getEventListenerManager() {return eventListenerManager;}

	/**The Guise session that owns this model.*/
	private final GuiseSession<?> session;

		/**@return The Guise session that owns this model.*/
		public GuiseSession<?> getSession() {return session;}

	/**Session constructor.
	@param session The Guise session that owns this model.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public AbstractModel(final GuiseSession<?> session)
	{
		this.session=checkNull(session, "Session cannot be null");	//save the session
	}

	/**Reports that a bound property has changed.
	This implementation delegates to the Guise session to fire or postpone the property change event.
	@param propertyChangeEvent The event to fire.
	@see GuiseSession#queueModelEvent(com.garretwilson.event.PostponedEvent)
	*/
	protected void firePropertyChange(final PropertyChangeEvent propertyChangeEvent)
	{
		getSession().queueModelEvent(createPostponedPropertyChangeEvent(propertyChangeEvent));	//create and queue a postponed property change event
	}

}
