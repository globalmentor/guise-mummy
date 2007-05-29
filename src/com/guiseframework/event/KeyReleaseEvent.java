package com.guiseframework.event;

import com.guiseframework.input.Key;
import com.guiseframework.input.KeyInput;

/**An event providing information on a keyboard key release.
@author Garret Wilson
*/
public class KeyReleaseEvent extends AbstractKeyEvent
{

	/**Source constructor.
	@param source The object on which the event initially occurred.
	@parma key The key that was pressed.
	@param keys The keys that were pressed when this event was generated.
	@exception NullPointerException if the given source and/or keys is <code>null</code>.
	*/
	public KeyReleaseEvent(final Object source, final Key key, final Key... keys)
	{
		super(source, key, keys);	//construct the parent class
	}

	/**Copy constructor that specifies a different source.
	@param source The object on which the event initially occurred.
	@param keyPressEvent The event the properties of which will be copied.
	@exception NullPointerException if the given source and/or event is <code>null</code>.
	*/
	public KeyReleaseEvent(final Object source, final KeyReleaseEvent keyReleaseEvent)
	{
		this(source, keyReleaseEvent.getKey(), keyReleaseEvent.getKeys().toArray(new Key[keyReleaseEvent.getKeys().size()]));	//construct the class with the specified source		
	}

	/**Returns the key input associated with this event.
	@return The input associated with this event, or <code>null</code> if there is no input associated with this event.
	This version returns <code>null</code>, as a key release, unlike a key press, produces no input.
	@see KeyPressEvent#getInput()
	*/
	public KeyInput getInput()
	{
		return null;	//key presses don't produce input
	}

}
