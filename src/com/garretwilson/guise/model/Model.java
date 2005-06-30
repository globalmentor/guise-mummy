package com.garretwilson.guise.model;

import com.garretwilson.beans.*;
import com.garretwilson.guise.session.GuiseSession;

/**Base interface for all component models.
A model should never fire a model-related event directly. It should rather create a postponed event and queue that event with the session.
@author Garret Wilson
@see GuiseSession#queueModelEvent(com.garretwilson.event.PostponedEvent)
*/
public interface Model extends PropertyBindable
{

	/**@return The Guise session that owns this model.*/
	public GuiseSession<?> getSession();

}
