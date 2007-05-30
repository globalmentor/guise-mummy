package com.guiseframework.event;

/**An abstract input event such as a command that is directed towards the component with input focus.
@author Garret Wilson
*/
public abstract class AbstractFocusedInputEvent extends AbstractInputEvent implements FocusedInputEvent 
{

	/**Source constructor.
	@param source The object on which the event initially occurred.
	@exception NullPointerException if the given source is <code>null</code>.
	*/
	public AbstractFocusedInputEvent(final Object source)
	{
		super(source);	//construct the parent class
	}

}
