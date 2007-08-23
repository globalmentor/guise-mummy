package com.guiseframework.event;

/**An event indicating that coarse-grained editing occurred.
The event target indicates the component that originally initiated the action.
@author Garret Wilson
@see EditListener
*/
public class EditEvent extends AbstractTargetedGuiseEvent
{

	/**Source constructor.
	The target will be set to be the same as the given source.
	@param source The object on which the event initially occurred.
	@exception NullPointerException if the given source is <code>null</code>.
	*/
	public EditEvent(final Object source)
	{
		this(source, source);	//construct the class with the same target as the source
	}

	/**Source and target constructor.
	@param source The object on which the event initially occurred.
	@param target The target of the event.
	@exception NullPointerException if the given source and/or target is <code>null</code>.
	*/
	public EditEvent(final Object source, final Object target)
	{
		super(source, target);	//construct the parent class
	}

	/**Copy constructor that specifies a different source.
	@param source The object on which the event initially occurred.
	@param editEvent The event the properties of which will be copied.
	@exception NullPointerException if the given source and/or event is <code>null</code>.
	*/
	public EditEvent(final Object source, final EditEvent editEvent)
	{
		this(source, editEvent.getTarget());	//construct the class with the same target		
	}

}
