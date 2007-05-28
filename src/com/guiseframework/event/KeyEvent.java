package com.guiseframework.event;

/**An event providing information on a keyboard key event.
@author Garret Wilson
*/
public interface KeyEvent extends FocusedInputEvent
{

	/**The key that was pressed.*/
	public Key getKey();

}
