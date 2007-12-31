package com.guiseframework.event;

import static com.garretwilson.lang.Objects.*;

import com.guiseframework.input.Key;

/**An abstract event providing information on a keyboard key event.
@author Garret Wilson
*/
public abstract class AbstractKeyboardEvent extends AbstractFocusedGestureInputEvent implements KeyboardEvent
{

	/**The key that was pressed.*/
	private final Key key;

		/**The key that was pressed.*/
		public Key getKey() {return key;}

	/**Source constructor.
	@param source The object on which the event initially occurred.
	@parma key The key that was pressed.
	@param keys The keys that were pressed when this event was generated.
	@exception NullPointerException if the given source, key, and/or keys is <code>null</code>.
	*/
	public AbstractKeyboardEvent(final Object source, final Key key, final Key... keys)
	{
		super(source, keys);	//construct the parent class
		this.key=checkInstance(key, "Key cannot be null.");	//save the key
	}
}
