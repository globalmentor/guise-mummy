package com.garretwilson.guise.model;

import com.garretwilson.beans.PropertyBindable;
import com.garretwilson.guise.session.GuiseSession;

/**Base interface for all component models.
@author Garret Wilson
*/
public interface Model extends PropertyBindable
{

	/**@return The Guise session that owns this model.*/
	public GuiseSession<?> getSession();

}
