package com.guiseframework.event;

import java.util.Set;

/**An event providing information from an input device such as a keyboard or a mouse.
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

	/**@return The keys that were pressed when this event was generated.*/ 
	public Set<Key> getKeys();

	/**Determines whether an Alt key was pressed when this event was generated.
	@return <code>true</code> if one of the Alt keys were pressed when this event was generated.
	@see #getKeys()
	*/
	public boolean hasAltKey();

	/**Determines whether a Control key was pressed when this event was generated.
	@return <code>true</code> if one of the Control keys were pressed when this event was generated.
	@see #getKeys()
	*/
	public boolean hasControlKey();

	/**Determines whether a Shift key was pressed when this event was generated.
	@return <code>true</code> if one of the Shift keys were pressed when this event was generated.
	@see #getKeys()
	*/
	public boolean hasShiftKey();
}
