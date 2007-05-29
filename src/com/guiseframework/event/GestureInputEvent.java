package com.guiseframework.event;

import java.util.Set;

import com.guiseframework.input.GestureInput;
import com.guiseframework.input.Key;

/**An event providing information on input from a user gesture.
@author Garret Wilson
*/
public interface GestureInputEvent extends InputEvent
{

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

	/**@return The input associated with this event, or <code>null</code> if there is no input associated with this event.*/
	public GestureInput getInput();

}
