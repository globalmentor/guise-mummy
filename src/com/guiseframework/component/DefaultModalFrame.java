package com.guiseframework.component;

/**Default implementation of a modal frame with a default layout panel.
@param <R> The type of modal result this modal frame produces.
@author Garret Wilson
*/
public class DefaultModalFrame<R> extends AbstractModalFrame<R, DefaultModalFrame<R>>
{

	/**Default constructor with a default layout panel.*/
	public DefaultModalFrame()
	{
		this(new LayoutPanel());	//default to a layout panel
	}

	/**Component constructor.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	*/
	public DefaultModalFrame(final Component<?> component)
	{
		super(component);	//construct the parent class
	}

}
