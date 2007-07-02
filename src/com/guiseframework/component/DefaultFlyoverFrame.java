package com.guiseframework.component;

/**Default implementation of a flyover frame with default layout panel.
A flyover frame by default is nonmodal, immovable, and not resizable.
For example, with a tether bearing of 250 and a tether resource key of "myTether", a resource key will be requested using "myTether.WSW", "myTether.SWbW", "myTether.SW", etc.
	until all compass points are exhausted, after which a resource key of "myTether" will be requested.
@author Garret Wilson
*/
public class DefaultFlyoverFrame extends AbstractFlyoverFrame
{

	/**Default constructor.*/
	public DefaultFlyoverFrame()
	{
		this(new LayoutPanel());	//default to a layout panel
	}

	/**Component constructor.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	*/
	public DefaultFlyoverFrame(final Component component)
	{
		super(component);	//construct the parent class
	}

}
