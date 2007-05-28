package com.guiseframework.platform.web;

import com.guiseframework.context.GuiseContext;
import com.guiseframework.controller.AbstractControlEvent;

/**A control event for pinging the server.
@author Garret Wilson
*/
public class PingControlEvent extends AbstractControlEvent
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
