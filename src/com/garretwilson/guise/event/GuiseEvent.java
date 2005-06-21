package com.garretwilson.guise.event;

import java.util.EventObject;

/**The base class for all Guise events.
@author Garret Wilson
*/
public class GuiseEvent<S> extends EventObject
{

	/**@return The source of the event.*/
	@SuppressWarnings("unchecked")
	public S getSource()
	{
		return (S)super.getSource();	//cast the event to the appropriate type
	}

	/**Source constructor.
	@param source The object on which the event initially occurred.
	@exception IllegalArgumentException if source is <code>null</code>.
	*/
	public GuiseEvent(final S source)
	{
		super(source);	//construct the parent class
	}

}
