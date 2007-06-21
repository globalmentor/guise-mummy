package com.guiseframework;

import com.guiseframework.platform.GuisePlatform;

/**A default implementation of a Guise session.
@author Garret Wilson
*/
public class DefaultGuiseSession extends AbstractGuiseSession
{

	/**Application constructor.
	@param application The Guise application to which this session belongs.
	@param environment The initial environment of the session.
	@param platform The platform on which this session's objects are depicted.
	@exception NullPointerException if the given application, environment, and/or platform is <code>null</code>.
	*/
	public DefaultGuiseSession(final GuiseApplication application, final GuiseEnvironment environment, final GuisePlatform platform)
	{
		super(application, environment, platform);	//construct the parent class
	}

}
