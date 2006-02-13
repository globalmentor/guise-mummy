package com.guiseframework.component.layout;

import static com.garretwilson.lang.ObjectUtilities.*;

/**The four logical flow corners.
@author Garret Wilson
*/
public enum Corner
{

	/**The upper-left corner in left-to-right top-to-bottom orientation.*/
	LINE_NEAR_PAGE_NEAR(Flow.End.NEAR, Flow.End.NEAR),
	/**The upper-right corner in left-to-right top-to-bottom orientation.*/
	LINE_FAR_PAGE_NEAR(Flow.End.FAR, Flow.End.NEAR),
	/**The bottom-left corner in left-to-right top-to-bottom orientation.*/
	LINE_NEAR_PAGE_FAR(Flow.End.NEAR, Flow.End.FAR),
	/**The bottom-right corner in left-to-right top-to-bottom orientation.*/
	LINE_FAR_PAGE_FAR(Flow.End.FAR, Flow.End.FAR);

	/**The line end.*/
	private final Flow.End lineEnd;

		/**The line end.*/
		public Flow.End getLineEnd() {return lineEnd;}

	/**The page end.*/
	private final Flow.End pageEnd;

		/**The page end.*/
		public Flow.End getPageEnd() {return pageEnd;}

	/**Corner constructor.
	@param lineEnd The line end.
	@param pageEnd The page end.
	@exception NullPointerException if the given line end and/or page end is <code>null</code>.
	*/
	private Corner(final Flow.End lineEnd, final Flow.End pageEnd)
	{
		this.lineEnd=checkNull(lineEnd, "Line end cannot be null.");
		this.pageEnd=checkNull(pageEnd, "Page end cannot be null.");
	}

	/**The corners in [line][page] order.*/
	private final static Corner[][] LINE_PAGE_CORNERS=new Corner[][]{{LINE_NEAR_PAGE_NEAR, LINE_NEAR_PAGE_FAR}, {LINE_FAR_PAGE_NEAR, LINE_FAR_PAGE_FAR}};
	
	/**Determines the corner from the given line and page ends.
	@param lineEnd The line end.
	@param pageEnd The page end.
	@return The corner value for the specified line and page ends.
	@exception NullPointerException if the given line end and/or page end is <code>null</code>.
	*/
	public static Corner getCorner(final Flow.End lineEnd, final Flow.End pageEnd)
	{
		return LINE_PAGE_CORNERS[lineEnd.ordinal()][pageEnd.ordinal()];	//look up the corner in our array
	}
}
