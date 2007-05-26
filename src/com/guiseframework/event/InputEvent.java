package com.guiseframework.event;

import java.util.Set;

/**An event providing information from an input device such as a keyboard or a mouse.
@author Garret Wilson
*/
public interface InputEvent extends GuiseEvent
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
}
