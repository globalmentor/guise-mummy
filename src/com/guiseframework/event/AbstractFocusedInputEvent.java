package com.guiseframework.event;

/**An abstract input event such as a keypress that is directed towards the component with input focus.
@author Garret Wilson
*/
public abstract class AbstractFocusedInputEvent extends AbstractInputEvent implements FocusedInputEvent 
{

	/**Source constructor.
	@param source The object on which the event initially occurred.
	@param keys The keys that were pressed when this event was generated.
	@exception NullPointerException if the given source and/or keys is <code>null</code>.
	*/
	public AbstractFocusedInputEvent(final Object source, final Key... keys)
	{
		super(source, keys);	//construct the parent class
	}

}
