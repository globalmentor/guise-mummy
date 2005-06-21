package com.garretwilson.guise.component;

/**Default implementation of a frame.
@author Garret Wilson
*/
public class DefaultFrame extends AbstractBox implements Frame
{

	/**ID constructor.
	@param id The component identifier.
	@exception NullPointerException if the given identifier is <code>null</code>.
	*/
	public DefaultFrame(final String id)
	{
		super(id);	//construct the parent class
	}

}
