package com.garretwilson.guise.component;

/**An abstract base class for boxes.
@author Garret Wilson
*/
public class AbstractBox extends AbstractContainer implements Box
{

	/**ID constructor.
	@param id The component identifier.
	@exception NullPointerException if the given identifier is <code>null</code>.
	*/
	public AbstractBox(final String id)
	{
		super(id);	//construct the parent class
	}

}
