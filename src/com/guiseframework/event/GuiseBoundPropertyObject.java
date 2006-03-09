package com.guiseframework.event;

import java.beans.PropertyChangeEvent;

import com.garretwilson.beans.BoundPropertyObject;
import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.GuiseSession;

import static com.garretwilson.lang.ObjectUtilities.*;

/**A bound property object that reports all property change events to the current session.
This class postpones events if Guise is processing component controller events.
@author Garret Wilson
*/
public class GuiseBoundPropertyObject extends BoundPropertyObject
{
	/**The Guise session that owns this object.*/
	private final GuiseSession session;

		/**@return The Guise session that owns this object.*/
		public GuiseSession getSession() {return session;}

	/**Session constructor.
	@param session The Guise session that owns this object.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public GuiseBoundPropertyObject(final GuiseSession session)
	{
		this.session=checkNull(session, "Session cannot be null");	//save the session
	}

	/**Reports that a bound property has changed. This method can be called	when a bound property has changed and it will send the appropriate property change event to any registered property change listeners.
	No event is fired if old and new are both <code>null</code> or are both non-<code>null</code> and equal according to the {@link Object#equals(java.lang.Object)} method.
	No event is fired if no listeners are registered for the given property.
	This method delegates actual firing of the event to {@link #firePropertyChange(PropertyChangeEvent)}.
	This implementation fires an event of type {@link GuisePropertyChangeEvent}, although the generic type of that event's source is not specific to the actual subclass used.
	@param propertyName The name of the property being changed.
	@param oldValue The old property value.
	@param newValue The new property value.
	@see #firePropertyChange(PropertyChangeEvent)
	@see #hasListeners(String)
	@see GuisePropertyChangeEvent
	@see GuisePropertyChangeListener
	*/
	protected <V> void firePropertyChange(final String propertyName, final V oldValue, final V newValue)
	{
		if(hasListeners(propertyName)) //if we have listeners registered for this property
		{
			if(!ObjectUtilities.equals(oldValue, newValue))	//if the values are different
			{					
				firePropertyChange(new GuisePropertyChangeEvent<V>(this, propertyName, oldValue, newValue));	//create and fire a genericized subclass of a property change event
			}
		}
	}

	/**Reports that a bound property has changed.
	This implementation delegates to the Guise session to fire or postpone the property change event.
	@param propertyChangeEvent The event to fire.
	@see GuiseSession#queueEvent(com.garretwilson.event.PostponedEvent)
	*/
	protected void firePropertyChange(final PropertyChangeEvent propertyChangeEvent)
	{
		getSession().queueEvent(createPostponedPropertyChangeEvent(propertyChangeEvent));	//create and queue a postponed property change event
	}

}
