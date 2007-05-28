package com.guiseframework.event;

/**An event providing information on a keyboard key press.
@author Garret Wilson
*/
public class KeyPressEvent extends AbstractKeyEvent
{

	/**Source constructor.
	@param source The object on which the event initially occurred.
	@parma key The key that was pressed.
	@param keys The keys that were pressed when this event was generated.
	@exception NullPointerException if the given source and/or keys is <code>null</code>.
	*/
	public KeyPressEvent(final Object source, final Key key, final Key... keys)
	{
		super(source, key, keys);	//construct the parent class
	}

	/**Copy constructor that specifies a different source.
	@param source The object on which the event initially occurred.
	@param keyPressEvent The event the properties of which will be copied.
	@exception NullPointerException if the given source and/or event is <code>null</code>.
	*/
	public KeyPressEvent(final Object source, final KeyPressEvent keyPressEvent)
	{
		this(source, keyPressEvent.getKey(), keyPressEvent.getKeys().toArray(new Key[keyPressEvent.getKeys().size()]));	//construct the class with the specified source		
	}
}
