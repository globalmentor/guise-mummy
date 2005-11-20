package com.javaguise.component.layout;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.javaguise.session.GuiseSession;

/**A layout that flows information along an axis.
@param <T> The type of layout constraints associated with each component.
@author Garret Wilson
*/
public abstract class AbstractFlowLayout<T extends AbstractFlowLayout.Constraints> extends AbstractLayout<T>
{

	/**The logical axis (line or page) along which information is flowed.*/
	private final Flow flow;

		/**@return The logical axis (line or page) along which information is flowed.*/
		public Flow getFlow() {return flow;}

	/**Session and flow constructor.
	@param session The Guise session that owns this layout.
	@param flow The logical axis (line or page) along which information is flowed.
	@exception NullPointerException if the axis is <code>null</code>.
	*/
	public AbstractFlowLayout(final GuiseSession session, final Flow flow)
	{
		super(session);	//construct the parent class
		this.flow=checkNull(flow, "Flow cannot be null.");	//store the flow
	}

	/**Metadata about individual component flow.
	@author Garret Wilson
	*/
	public static class Constraints extends AbstractConstraints
	{
	}

}
