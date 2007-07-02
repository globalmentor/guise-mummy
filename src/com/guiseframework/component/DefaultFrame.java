package com.guiseframework.component;

/**Default implementation of a nonmodal frame with default layout panel.
@author Garret Wilson
*/
public class DefaultFrame extends AbstractFrame
{

	/**Default constructor with a default layout panel child component.*/
	public DefaultFrame()
	{
		this(new LayoutPanel());	//default to a layout panel
	}

	/**Component constructor.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	*/
	public DefaultFrame(final Component component)
	{
		super(component);	//construct the parent class
	}

}
