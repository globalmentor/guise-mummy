package com.javaguise.component.layout;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.javaguise.component.Component;

/**A layout that flows information along an axis.
@author Garret Wilson
*/
public class FlowLayout extends AbstractLayout<FlowLayout.Constraints>
{

	/**The logical axis (line or page) along which information is flowed.*/
	private final Orientation.Flow flow;

		/**@return The logical axis (line or page) along which information is flowed.*/
		public Orientation.Flow getFlow() {return flow;}

	/**Axis constructor.
	@param flow The logical axis (line or page) along which information is flowed.
	@exception NullPointerException if the axis is <code>null</code>.
	*/
	public FlowLayout(final Orientation.Flow flow)
	{
		this.flow=checkNull(flow, "Flow cannot be null.");	//store the flow
	}

	/**Creates default constraints for the given component.
	@param component The component for which constraints should be provided.
	@return New default constraints for the given component.
	@exception IllegalStateException if this layout does not support default constraints.
	*/
	public Constraints createDefaultConstraints(final Component<?> component)
	{
		return new Constraints();	//return a default constraints object
	}

	/**Metadata about individual component flow.
	@author Garret Wilson
	*/
	public static class Constraints implements Layout.Constraints
	{
	}

}
