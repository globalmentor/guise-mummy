/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guiseframework.component.layout;

import static com.globalmentor.java.Objects.*;

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

	/**Line and page end constructor.
	@param lineEnd The line end.
	@param pageEnd The page end.
	@exception NullPointerException if the given line end and/or page end is <code>null</code>.
	*/
	private Corner(final Flow.End lineEnd, final Flow.End pageEnd)
	{
		this.lineEnd=checkInstance(lineEnd, "Line end cannot be null.");
		this.pageEnd=checkInstance(pageEnd, "Page end cannot be null.");
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
