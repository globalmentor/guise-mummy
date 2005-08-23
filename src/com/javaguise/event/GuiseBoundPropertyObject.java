package com.javaguise.event;

import java.beans.PropertyChangeEvent;

import com.garretwilson.beans.*;
import com.javaguise.session.GuiseSession;
import com.garretwilson.lang.ObjectUtilities;

import static com.garretwilson.lang.ObjectUtilities.*;

/**A bound property object that reports all property change events to the current session.
@author Garret Wilson
*/
public class GuiseBoundPropertyObject extends BoundPropertyObject
{
	/**The Guise session that owns this model.*/
	private final GuiseSession<?> session;

		/**@return The Guise session that owns this model.*/
		public GuiseSession<?> getSession() {return session;}

	/**Session constructor.
	@param session The Guise session that owns this object.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public GuiseBoundPropertyObject(final GuiseSession<?> session)
	{
		this.session=checkNull(session, "Session cannot be null");	//save the session
	}

	/**Reports that a bound property has changed. This method can be called	when a bound property has changed and it will send the appropriate property change event to any registered property change listeners.
	This version fires a property change event even if no listeners are attached, so that the Guise session can be notified of the event.
	No event is fired if old and new are both <code>null</code> or are both non-<code>null</code> and equal according to the {@link Object#equals(java.lang.Object)} method.
	This method delegates actual firing of the event to {@link #firePropertyChange(PropertyChangeEvent)}.
	@param propertyName The name of the property being changed.
	@param oldValue The old property value.
	@param newValue The new property value.
	@see #firePropertyChange(PropertyChangeEvent)
	@see #hasListeners(String)
	@see PropertyValueChangeEvent
	@see PropertyValueChangeListener
	*/
	protected <V> void firePropertyChange(final String propertyName, V oldValue, final V newValue)
	{
		if(!ObjectUtilities.equals(oldValue, newValue))	//if the values are different
		{					
			firePropertyChange(new PropertyValueChangeEvent<V>(this, propertyName, oldValue, newValue));	//create and fire a genericized subclass of a property change event
		}
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