package com.guiseframework.component;

/**Abstract implementation of an application frame.
@author Garret Wilson
@see LayoutPanel
*/
public abstract class AbstractApplicationFrame<C extends ApplicationFrame<C>> extends AbstractFrame<C> implements ApplicationFrame<C>
{

	/**Component constructor.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	*/
	public AbstractApplicationFrame(final Component<?> component)
	{
		super(component);	//construct the parent class
	}

	/**Determines whether the frame should be allowed to close.
	This implementation returns <code>false</code>.
	This method is called from {@link #close()}.
	@return <code>true</code> if the frame should be allowed to close.
	*/
	public boolean canClose()
	{
		return false;	//don't allow application frames to be closed
	}
}
