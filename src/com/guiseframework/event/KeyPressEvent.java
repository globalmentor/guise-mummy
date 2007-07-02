package com.guiseframework.event;

import com.guiseframework.input.Key;
import com.guiseframework.input.KeystrokeInput;
/**An event providing information on a keyboard key press.
@author Garret Wilson
*/
public class KeyPressEvent extends AbstractKeyboardEvent
{

	/**Source constructor.
	@param source The object on which the event initially occurred.
	@parma key The key that was pressed.
	@param keys The keys that were pressed when this event was generated.
	@exception NullPointerException if the given source, key, and/or keys is <code>null</code>.
	*/
	public KeyPressEvent(final Object source, final Key key, final Key... keys)
	{
		super(source, key, keys);	//construct the parent class
	}

	/**Keystroke input constructor.
	@param source The object on which the event initially occurred.
	@param keystrokeInput The keystroke input the properties of which will be copied.
	@exception NullPointerException if the given source and/or input is <code>null</code>.
	*/
	public KeyPressEvent(final Object source, final KeystrokeInput keystrokeInput)
	{
		this(source, keystrokeInput.getKey(), keystrokeInput.getKeys().toArray(new Key[keystrokeInput.getKeys().size()]));	//construct the class with the specified source		
	}

	/**Copy constructor that specifies a different source.
	@param source The object on which the event initially occurred.
	@param keyPressEvent The event the properties of which will be copied.
	@exception NullPointerException if the given source, key, and/or event is <code>null</code>.
	*/
	public KeyPressEvent(final Object source, final KeyPressEvent keyPressEvent)
	{
		this(source, keyPressEvent.getKey(), keyPressEvent.getKeys().toArray(new Key[keyPressEvent.getKeys().size()]));	//construct the class with the specified source		
	}

	/**@return The key input associated with this event.*/
	public KeystrokeInput getInput()
	{
		return new KeystrokeInput(getKey(), getKeys().toArray(new Key[getKeys().size()]));	//return new key input based upon this event
	}
}
