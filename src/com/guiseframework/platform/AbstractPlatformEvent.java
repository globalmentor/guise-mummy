package com.guiseframework.platform;

import com.guiseframework.event.AbstractGuiseEvent;

/**The base class for events to or from the platform on which objects are being depicted.
@author Garret Wilson
*/
public abstract class AbstractPlatformEvent extends AbstractGuiseEvent implements PlatformEvent
{

	/**Source constructor.
	@param source The object on which the event initially occurred.
	@exception NullPointerException if the given source is <code>null</code>.
	*/
	public AbstractPlatformEvent(final Object source)
	{
		super(source);	//construct the parent class
	}

}
