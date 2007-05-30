package com.guiseframework.event;

import com.guiseframework.input.*;

/**An event providing information on input from a keyboard.
@author Garret Wilson
*/
public interface KeyboardEvent extends GestureInputEvent, FocusedInputEvent
{

	/**The key that was pressed.*/
	public Key getKey();

	/**@return The input associated with this event, or <code>null</code> if there is no input associated with this event.*/
	public KeystrokeInput getInput();

}
