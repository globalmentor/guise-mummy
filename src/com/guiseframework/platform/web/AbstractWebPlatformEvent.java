package com.guiseframework.platform.web;

import com.guiseframework.platform.AbstractPlatformEvent;

/**The base class for events to or from the web platform.
@author Garret Wilson
*/
public abstract class AbstractWebPlatformEvent extends AbstractPlatformEvent implements WebPlatformEvent
{

	/**Source constructor.
	@param source The object on which the event initially occurred.
	@exception NullPointerException if the given source is <code>null</code>.
	*/
	public AbstractWebPlatformEvent(final Object source)
	{
		super(source);	//construct the parent class
	}

}
