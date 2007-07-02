package com.guiseframework.platform.web;

/**A web event for polling the server.
@author Garret Wilson
*/
public class WebPollEvent extends AbstractWebPlatformEvent
{

	/**Context constructor.
	@param context The context in which this control event was produced.
	@exception NullPointerException if the given context is <code>null</code>.
	*/
	public WebPollEvent(final WebPlatform source)
	{
		super(source);	//construct the parent class
	}
}
