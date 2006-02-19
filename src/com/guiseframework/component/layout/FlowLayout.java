package com.guiseframework.component.layout;

import com.guiseframework.GuiseSession;

/**A layout that flows information along an axis.
@author Garret Wilson
*/
public class FlowLayout extends AbstractFlowLayout<FlowConstraints>
{

	/**@return The class representing the type of constraints appropriate for this layout.*/
	public Class<? extends FlowConstraints> getConstraintsClass() {return FlowConstraints.class;}

	/**Session constructor with {@link Flow#PAGE} layout.
	@param session The Guise session that owns this layout.
	@exception NullPointerException if the axis is <code>null</code>.
	*/
	public FlowLayout(final GuiseSession session)
	{
		this(session, Flow.PAGE);	//construct the class with page flow layout
	}

	/**Session and flow constructor.
	@param session The Guise session that owns this layout.
	@param flow The logical axis (line or page) along which information is flowed.
	@exception NullPointerException if the axis is <code>null</code>.
	*/
	public FlowLayout(final GuiseSession session, final Flow flow)
	{
		super(session, flow);	//construct the parent class
	}

	/**Creates default constraints for the container.
	@return New default constraints for the container.
	@exception IllegalStateException if this layout does not support default constraints.
	*/
	public FlowConstraints createDefaultConstraints()
	{
		return new FlowConstraints(getSession());	//return a default constraints object
	}
}
