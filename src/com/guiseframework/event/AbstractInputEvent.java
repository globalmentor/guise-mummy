package com.guiseframework.event;

/**An abstract event providing information on input such as a keystroke or a command.
@author Garret Wilson
*/
public abstract class AbstractInputEvent extends AbstractGuiseEvent implements InputEvent
{

	/**Whether the input associated with this event has been consumed.*/
	private boolean consumed=false;

		/**@return Whether the input associated with this event has been consumed.*/
		public boolean isConsumed() {return consumed;}

		/**Consumes the input associated with this event.
		The event is marked as consumed so that other listeners will be on notice not to consume the input.
		*/
		public void consume() {consumed=true;}

	/**Source constructor.
	@param source The object on which the event initially occurred.
	@exception NullPointerException if the given source is <code>null</code>.
	*/
	public AbstractInputEvent(final Object source)
	{
		super(source);	//construct the parent class
	}

}
