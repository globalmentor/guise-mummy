package com.guiseframework.event;

import static java.util.Collections.*;
import java.util.Set;

import static com.garretwilson.lang.EnumUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;

/**An abstract event providing information from an input device such as a keyboard or a mouse.
@author Garret Wilson
*/
public abstract class AbstractInputEvent extends AbstractGuiseEvent implements InputEvent
{

	/**The keys that were pressed when this event was generated.*/ 
	private final Set<Key> keys;

		/**@return The keys that were pressed when this event was generated.*/ 
		public Set<Key> getKeys() {return keys;}

	/**Determines whether an Alt key was pressed when this event was generated.
	@return <code>true</code> if one of the Alt keys were pressed when this event was generated.
	@see #getKeys()
	*/
	public boolean hasAltKey()
	{
		return getKeys().contains(Key.ALT_LEFT) || getKeys().contains(Key.ALT_RIGHT);	//see if an Alt key is included in the key set
	}

	/**Determines whether a Control key was pressed when this event was generated.
	@return <code>true</code> if one of the Control keys were pressed when this event was generated.
	@see #getKeys()
	*/
	public boolean hasControlKey()
	{
		return getKeys().contains(Key.CONTROL_LEFT) || getKeys().contains(Key.CONTROL_RIGHT);	//see if a Control key is included in the key set
	}
		
	/**Source constructor.
	@param source The object on which the event initially occurred.
	@param keys The keys that were pressed when this event was generated.
	@exception NullPointerException if the given source and/or keys is <code>null</code>.
	*/
	public AbstractInputEvent(final Object source, final Key... keys)
	{
		super(source);	//construct the parent class
		this.keys=unmodifiableSet(createEnumSet(Key.class, checkInstance(keys, "Keys cannot be null.")));	//save a read-only set of the keys
	}

}
