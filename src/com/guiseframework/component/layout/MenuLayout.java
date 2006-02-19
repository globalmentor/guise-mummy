package com.guiseframework.component.layout;

import com.guiseframework.GuiseSession;

/**A layout for a menu that flows along an axis.
@author Garret Wilson
*/
public class MenuLayout extends AbstractFlowLayout<MenuConstraints>	//TODO probably move this into the menu class
{

	/**@return The class representing the type of constraints appropriate for this layout.*/
	public Class<? extends MenuConstraints> getConstraintsClass() {return MenuConstraints.class;}

	/**Session and flow constructor.
	@param session The Guise session that owns this layout.
	@param flow The logical axis (line or page) along which information is flowed.
	@exception NullPointerException if the axis is <code>null</code>.
	*/
	public MenuLayout(final GuiseSession session, final Flow flow)
	{
		super(session, flow);	//construct the parent class
	}

	/**Creates default constraints for the container.
	@return New default constraints for the container.
	@exception IllegalStateException if this layout does not support default constraints.
	*/
	public MenuConstraints createDefaultConstraints()
	{
		return new MenuConstraints(getSession());	//return a default constraints object
	}

}
