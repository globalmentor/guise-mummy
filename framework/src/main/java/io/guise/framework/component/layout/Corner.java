/*
 * Copyright © 2005-2012 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.component.layout;

import static java.util.Objects.*;

/**
 * The four logical flow corners.
 * @author Garret Wilson
 * @see Flow
 */
public enum Corner {

	/** The upper-left corner in left-to-right top-to-bottom orientation. */
	LINE_NEAR_PAGE_NEAR(Border.LINE_NEAR, Border.PAGE_NEAR),
	/** The upper-right corner in left-to-right top-to-bottom orientation. */
	LINE_FAR_PAGE_NEAR(Border.LINE_FAR, Border.PAGE_NEAR),
	/** The bottom-left corner in left-to-right top-to-bottom orientation. */
	LINE_NEAR_PAGE_FAR(Border.LINE_NEAR, Border.PAGE_FAR),
	/** The bottom-right corner in left-to-right top-to-bottom orientation. */
	LINE_FAR_PAGE_FAR(Border.LINE_FAR, Border.PAGE_FAR);

	/**
	 * The two borders, indexed by flow ordinal.
	 * @see Flow
	 */
	private final Border[] borders;

	/**
	 * Determines the border for the given flow.
	 * @param flow The flow for which the border should be returned.
	 * @return The corner border for the given flow.
	 * @throws NullPointerException if the given flow is <code>null</code>.
	 */
	public Border getBorder(final Flow flow) {
		return borders[flow.ordinal()];
	}

	/**
	 * Line and page border constructor.
	 * @param lineBorder The line border.
	 * @param pageBorder The page border.
	 * @throws NullPointerException if the given line border and/or page border is <code>null</code>.
	 */
	private Corner(final Border lineBorder, final Border pageBorder) {
		assert Flow.values().length == 2 : "Corners only support two flows.";
		borders = new Border[Flow.values().length];
		borders[Flow.LINE.ordinal()] = requireNonNull(lineBorder, "Line border cannot be null.");
		borders[Flow.PAGE.ordinal()] = requireNonNull(pageBorder, "Page border cannot be null.");
	}

	/** The corners in [line][page] order. */
	private static final Corner[][] LINE_PAGE_CORNERS = new Corner[][] { { LINE_NEAR_PAGE_NEAR, LINE_NEAR_PAGE_FAR }, { LINE_FAR_PAGE_NEAR, LINE_FAR_PAGE_FAR } };

	/**
	 * Determines the corner from the given line and page ends.
	 * @param lineEnd The line end.
	 * @param pageEnd The page end.
	 * @return The corner value for the specified line and page ends.
	 * @throws NullPointerException if the given line end and/or page end is <code>null</code>.
	 */
	public static Corner getCorner(final Flow.End lineEnd, final Flow.End pageEnd) {
		return LINE_PAGE_CORNERS[lineEnd.ordinal()][pageEnd.ordinal()]; //look up the corner in our array
	}
}
