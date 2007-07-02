package com.guiseframework;

import com.guiseframework.platform.Platform;

/**A default implementation of a Guise session.
@author Garret Wilson
*/
public class DefaultGuiseSession extends AbstractGuiseSession
{

	/**Application and platform constructor.
	@param application The Guise application to which this session belongs.
	@param platform The platform on which this session's objects are depicted.
	@exception NullPointerException if the given application and/or platform is <code>null</code>.
	*/
	public DefaultGuiseSession(final GuiseApplication application, final Platform platform)
	{
		super(application, platform);	//construct the parent class
	}

}
