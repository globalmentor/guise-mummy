package com.guiseframework.input;

import java.util.Set;

/**An encapsulation of user input from a gesture.
@author Garret Wilson
*/
public interface GestureInput extends Input
{

	/**@return The keys that were pressed when this input occurred.*/ 
	public Set<Key> getKeys();

	/**Determines whether an Alt key was pressed when this input occurred.
	@return <code>true</code> if one of the Alt keys were pressed when this input occurred.
	@see #getKeys()
	*/
	public boolean hasAltKey();

	/**Determines whether a Control key was pressed when this input occurred.
	@return <code>true</code> if one of the Control keys were pressed when this input occurred.
	@see #getKeys()
	*/
	public boolean hasControlKey();

	/**Determines whether a Shift key was pressed when this input occurred.
	@return <code>true</code> if one of the Shift keys were pressed when this input occurred.
	@see #getKeys()
	*/
	public boolean hasShiftKey();
}
