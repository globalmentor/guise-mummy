package com.guiseframework.event;

import com.guiseframework.input.MouseInput;

/**An event providing information on input from a mouse.
@author Garret Wilson
*/
public interface MouseInputEvent extends GestureInputEvent
{

	/**@return The input associated with this event, or <code>null</code> if there is no input associated with this event.*/
	public MouseInput getInput();

}
