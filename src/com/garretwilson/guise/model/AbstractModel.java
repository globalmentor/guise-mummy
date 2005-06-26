package com.garretwilson.guise.model;

import com.garretwilson.beans.BoundPropertyObject;
import com.garretwilson.event.EventListenerManager;
import com.garretwilson.guise.session.GuiseSession;
import static com.garretwilson.lang.ObjectUtilities.*;

/**A base class implementing helpful functionality for models.
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
}
