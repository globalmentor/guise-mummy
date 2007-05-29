package com.guiseframework.event;

import com.guiseframework.input.Input;

/**An event providing information from input such as a keystroke or a command.
@author Garret Wilson
*/
public interface InputEvent extends GuiseEvent
{

	/**@return Whether the input associated with this event has been consumed.*/
	public boolean isConsumed();

	/**Consumes the input associated with this event.
	The event is marked as consumed so that other listeners will be on notice not to consume the input.
	*/
	public void consume();

	/**@return The input associated with this event, or <code>null</code> if there is no input associated with this event.*/
	public Input getInput();

}
