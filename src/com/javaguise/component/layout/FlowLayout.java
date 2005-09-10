package com.javaguise.component.layout;

import com.javaguise.session.GuiseSession;

/**A layout that flows information along an axis.
@author Garret Wilson
*/
public class FlowLayout extends AbstractFlowLayout<FlowLayout.Constraints>
{

	/**Session and flow constructor.
	@param session The Guise session that owns this layout.
	@param flow The logical axis (line or page) along which information is flowed.
	@exception NullPointerException if the axis is <code>null</code>.
	*/
	public FlowLayout(final GuiseSession<?> session, final Orientation.Flow flow)
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
