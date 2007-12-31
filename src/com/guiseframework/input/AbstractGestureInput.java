package com.guiseframework.input;

import static java.util.Collections.*;
import java.util.Set;

import com.garretwilson.util.AbstractHashObject;

import static com.garretwilson.lang.EnumUtilities.*;
import static com.garretwilson.lang.Objects.*;

/**An abstract encapsulation of user input from a gesture.
@author Garret Wilson
*/
public abstract class AbstractGestureInput extends AbstractHashObject implements GestureInput
{

	/**The keys that were pressed when this input occurred.*/ 
	private final Set<Key> keys;

		/**@return The keys that were pressed when this input occurred.*/ 
		public Set<Key> getKeys() {return keys;}

	/**Determines whether an Alt key was pressed when this input occurred.
	@return <code>true</code> if one of the Alt keys were pressed when this input occurred.
	@see #getKeys()
	*/
	public boolean hasAltKey()
	{
		return getKeys().contains(Key.ALT_LEFT) || getKeys().contains(Key.ALT_RIGHT);	//see if an Alt key is included in the key set
	}

	/**Determines whether a Control key was pressed when this input occurred.
	@return <code>true</code> if one of the Control keys were pressed when this input occurred.
	@see #getKeys()
	*/
	public boolean hasControlKey()
	{
		return getKeys().contains(Key.CONTROL_LEFT) || getKeys().contains(Key.CONTROL_RIGHT);	//see if a Control key is included in the key set
	}

	/**Determines whether a Shift key was pressed when this input occurred.
	@return <code>true</code> if one of the Shift keys were pressed when this input occurred.
	@see #getKeys()
	*/
	public boolean hasShiftKey()
	{
		return getKeys().contains(Key.SHIFT_LEFT) || getKeys().contains(Key.SHIFT_RIGHT);	//see if a Shift key is included in the key set
	}

	/**Keys constructor.
	@param keys The keys that were pressed when this input occurred.
	@exception NullPointerException if the given keys is <code>null</code>.
	*/
	public AbstractGestureInput(final Key... keys)
	{
		super((Object[])keys);	//construct the parent class
		this.keys=unmodifiableSet(createEnumSet(Key.class, checkInstance(keys, "Keys cannot be null.")));	//save a read-only set of the keys
	}

}
