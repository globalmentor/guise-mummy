package com.guiseframework.platform.web;

import com.guiseframework.context.GuiseContext;

/**A control event for pinging the server.
@author Garret Wilson
*/
public class PingControlEvent extends AbstractWebPlatformEvent
{

	/**Context constructor.
	@param context The context in which this control event was produced.
	@exception NullPointerException if the given context is <code>null</code>.
	*/
	public PingControlEvent(final GuiseContext context)
	{
		super(context);	//construct the parent class
	}
}
