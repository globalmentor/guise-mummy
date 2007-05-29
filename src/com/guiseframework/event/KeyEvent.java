package com.guiseframework.event;

import com.guiseframework.input.Key;
import com.guiseframework.input.KeyInput;

/**An event providing information on a keyboard key event.
@author Garret Wilson
*/
public interface KeyEvent extends FocusedInputEvent
{

	/**The key that was pressed.*/
	public Key getKey();

	/**@return The input associated with this event, or <code>null</code> if there is no input associated with this event.*/
	public KeyInput getInput();

}
