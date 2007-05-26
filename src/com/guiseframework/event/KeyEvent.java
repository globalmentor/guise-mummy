package com.guiseframework.event;

/**An event providing information on a key press from a keyboard.
@author Garret Wilson
*/
public class KeyEvent extends AbstractInputEvent
{

	/**The key that was pressed.*/
	private final Key key;

		/**The key that was pressed.*/
		public final Key getKey() {return key;}

	/**Source constructor.
	@param source The object on which the event initially occurred.
	@parma key The key that was pressed.
	@param keys The keys that were pressed when this event was generated.
	@exception NullPointerException if the given source and/or keys is <code>null</code>.
	*/
	public KeyEvent(final Object source, final Key key, final Key... keys)
	{
		super(source, keys);	//construct the parent class
		this.key=key;	//save the key
	}
}
