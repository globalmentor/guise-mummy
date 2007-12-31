package com.guiseframework.component.layout;

import static com.garretwilson.lang.Objects.*;

/**The points at which the four logical flows end.
@author Garret Wilson
@see com.guiseframework.geometry.Side
*/
public enum Border
{

	/**The left side in left-to-right top-to-bottom orientation.*/
	LINE_NEAR(Flow.LINE, Flow.End.NEAR),
	/**The right side in left-to-right top-to-bottom orientation.*/
	LINE_FAR(Flow.LINE, Flow.End.FAR),
	/**The top side in left-to-right top-to-bottom orientation.*/
	PAGE_NEAR(Flow.PAGE, Flow.End.NEAR),
	/**The bottom side in left-to-right top-to-bottom orientation.*/
	PAGE_FAR(Flow.PAGE, Flow.End.FAR);

	/**The flow.*/
	private final Flow flow;

		/**The flow.*/
		public Flow getFlow() {return flow;}

	/**The end.*/
	private final Flow.End end;

		/**The end.*/
		public Flow.End getEnd() {return end;}

	/**Flow and end constructor.
	@param flow The flow
	@param end The end.
	@exception NullPointerException if the given flow and/or end is <code>null</code>.
	*/
	private Border(final Flow flow, final Flow.End end)
	{
		this.flow=checkInstance(flow, "Flow cannot be null.");
		this.end=checkInstance(end, "End cannot be null.");
	}

	/**The borders in [flow][end] order.*/
	private final static Border[][] FLOW_END_BORDERS=new Border[][]{{LINE_NEAR, LINE_FAR}, {PAGE_NEAR, PAGE_FAR}};
	
	/**Determines the border from the flow and end.
	@param flow The flow
	@param end The end.
	@return The border for the specified flow and page end.
	@exception NullPointerException if the given flow and/or end is <code>null</code>.
	*/
	public static Border getBorder(final Flow flow, final Flow.End end)
	{
		return FLOW_END_BORDERS[flow.ordinal()][end.ordinal()];	//look up the border in our array
	}
}
