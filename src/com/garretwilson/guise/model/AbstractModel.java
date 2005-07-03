package com.garretwilson.guise.model;

import java.beans.PropertyChangeEvent;
import java.net.URI;

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

	/**Determines a string value either explicitly set or stored in the resources.
	If a value is explicitly specified, it will be used; otherwise, a value will be loaded from the resources if possible.
	@param value The value explicitly set, which will override any resource.
	@param resourceKey The key for looking up a resource if no value is explicitly set.
	@return The string value, or <code>null</code> if there is no value available, neither explicitly set nor in the resources.
	@exception java.util.MissingResourceException if there was an error loading the value from the resources.
	*/
	protected String getString(final String value, final String resourceKey)
	{
		if(value!=null)	//if a value is provided
		{
			return value;	//return the specified value
		}
		else if(resourceKey!=null)	//if no value is provided, but if a resource key is provided
		{
			return getSession().getStringResource(resourceKey);	//lookup the value from the resources 
		}
		else	//if neither a value nor a resource key are provided
		{
			return null;	//there is no value available
		}
	}

	/**Determines a URI value either explicitly set or stored in the resources.
	If a value is explicitly specified, it will be used; otherwise, a value will be loaded from the resources if possible.
	@param value The value explicitly set, which will override any resource.
	@param resourceKey The key for looking up a resource if no value is explicitly set.
	@return The URI value, or <code>null</code> if there is no value available, neither explicitly set nor in the resources.
	@exception java.util.MissingResourceException if there was an error loading the value from the resources.
	*/
	protected URI getURI(final URI value, final String resourceKey)
	{
		if(value!=null)	//if a value is provided
		{
			return value;	//return the specified value
		}
		else if(resourceKey!=null)	//if no value is provided, but if a resource key is provided
		{
			return getSession().getURIResource(resourceKey);	//lookup the value from the resources 
		}
		else	//if neither a value nor a resource key are provided
		{
			return null;	//there is no value available
		}
	}

}
