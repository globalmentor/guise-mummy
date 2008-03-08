package com.guiseframework.component;

import static com.guiseframework.Resources.*;

import com.guiseframework.Resources;

/**Default implementation of an application frame with no default component.
@author Garret Wilson
*/
public class DefaultApplicationFrame extends AbstractApplicationFrame
{

	/**Default constructor.*/
	public DefaultApplicationFrame()
	{
		this(null);	//construct the class with no child component
	}

	/**Component constructor.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	*/
	public DefaultApplicationFrame(final Component component)
	{
		super(component);	//construct the parent class
	}

	/**Retrieves the plain-text base title to use when constructing a label.
	This implementation returns a string reference to the application name.
	@return A base plain-text string to use when constructing a label, or <code>null</code> if there is no base label.
	@see #updateLabel()
	@see Resources#APPLICATION_NAME
	*/
	protected String getBasePlainLabel()
	{
		return APPLICATION_NAME;
	}

}
