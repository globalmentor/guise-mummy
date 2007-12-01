package com.guiseframework.event;

import com.garretwilson.beans.BoundPropertyObject;
import com.guiseframework.Guise;
import com.guiseframework.GuiseSession;

/**A bound property object that reports all property change events to the current session.
This class may postpone certain events if Guise is processing component controller events.
@author Garret Wilson
*/
public class GuiseBoundPropertyObject extends BoundPropertyObject
{
	/**The Guise session that owns this object.*/
	private final GuiseSession session;

		/**@return The Guise session that owns this object.*/
		public GuiseSession getSession() {return session;}

	/**Default constructor.*/
	public GuiseBoundPropertyObject()
	{
		this.session=Guise.getInstance().getGuiseSession();	//store a reference to the current Guise session
	}

	/**Reports that a bound property has changed.
	This implementation delegates to the Guise session to fire or postpone the property change event.
	@param propertyChangeEvent The event to fire.
	@see GuiseSession#queueEvent(com.garretwilson.event.PostponedEvent)
	*/
/*TODO del when postponed property change events are removed
	protected void firePropertyChange(final PropertyChangeEvent propertyChangeEvent)
	{
		getSession().queueEvent(createPostponedPropertyChangeEvent(propertyChangeEvent));	//create and queue a postponed property change event
	}
*/

}
