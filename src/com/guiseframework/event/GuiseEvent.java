package com.guiseframework.event;

import com.guiseframework.GuiseSession;

/**A Guise event.
@author Garret Wilson
*/
public interface GuiseEvent
{

	/**@return The Guise session in which this event was generated.*/
	public GuiseSession getSession();
}
