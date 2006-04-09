package com.guiseframework.component;

/**Default implementation of an application frame with no default component.
@author Garret Wilson
*/
public class DefaultApplicationFrame extends AbstractApplicationFrame<DefaultApplicationFrame>
{

	/**Default constructor.*/
	public DefaultApplicationFrame()
	{
		this(null);	//construct the class with no child component
	}

	/**Component constructor.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	*/
	public DefaultApplicationFrame(final Component<?> component)
	{
		super(component);	//construct the parent class
	}

}
