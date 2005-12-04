package com.javaguise.component.layout;

import com.javaguise.GuiseSession;

/**A layout for a menu that flows along an axis.
@author Garret Wilson
*/
public class MenuLayout extends AbstractFlowLayout<MenuLayout.Constraints>
{

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
	public Constraints createDefaultConstraints()
	{
		return new Constraints();	//return a default constraints object
	}

	/**Metadata about individual component flow.
	@author Garret Wilson
	*/
	public static class Constraints extends AbstractFlowLayout.Constraints
	{
	}

}
