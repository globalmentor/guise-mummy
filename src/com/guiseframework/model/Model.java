package com.guiseframework.model;

import com.garretwilson.beans.*;
import com.guiseframework.GuiseSession;
import com.guiseframework.validator.ValidationException;

/**Base interface for all component models.
A model should never fire a model-related event directly. It should rather create a postponed event and queue that event with the session.
@author Garret Wilson
@see GuiseSession#queueEvent(com.garretwilson.event.PostponedEvent)
*/
public interface Model extends PropertyBindable
{

	/**@return The Guise session that owns this model.*/
	public GuiseSession getSession();

	/**@return Whether the contents of this model are valid.*/
	public boolean isValid();

	/**Validates the contents of this model, throwing an exception if the model is not valid.
	@exception ValidationException if the contents of this model are not valid.	
	*/
	public void validate() throws ValidationException;

}
