package com.javaguise;

/**A default implementation of a Guise session.
@author Garret Wilson
*/
public class DefaultGuiseSession extends AbstractGuiseSession
{

	/**Application constructor.
	@param application The Guise application to which this session belongs.
	*/
	public DefaultGuiseSession(final GuiseApplication application)
	{
		super(application);	//construct the parent class
	}

}
