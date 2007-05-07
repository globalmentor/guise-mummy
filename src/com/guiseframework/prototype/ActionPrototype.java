package com.guiseframework.prototype;

import java.net.URI;

/**Contains prototype information for an action control.
@author Garret Wilson
*/
public class ActionPrototype extends AbstractActionPrototype
{

	/**Default constructor.*/
	public ActionPrototype()
	{
		this(null);	//construct the class with no label
	}

	/**Label constructor.
	@param label The text of the label, or <code>null</code> if there should be no label.
	*/
	public ActionPrototype(final String label)
	{
		this(label, null);	//construct the label model with no icon
	}

	/**Label and icon constructor.
	@param label The text of the label, or <code>null</code> if there should be no label.
	@param icon The icon URI, which may be a resource URI, or <code>null</code> if there is no icon URI.
	*/
	public ActionPrototype(final String label, final URI icon)
	{
		super(label, icon);	//construct the parent class
	}

}
