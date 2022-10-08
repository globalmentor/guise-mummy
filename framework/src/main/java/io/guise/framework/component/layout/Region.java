/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
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

import io.guise.framework.geometry.Axis;

/**
 * Indicates a region of a larger area in internationalized relative terms.
 * @author Garret Wilson
 */
public enum Region {

	/** At the beginning of a line; "left" in left-to-right, top-to-bottom orientation. */
	LINE_START,

	/** At the end of a line; "right" in left-to-right, top-to-bottom orientation. */
	LINE_END,

	/** At the beginning of a page; "top" in left-to-right, top-to-bottom orientation. */
	PAGE_START,

	/** At the end of a page; "bottom" in left-to-right, top-to-bottom orientation. */
	PAGE_END,

	/** In the center of the area. */
	CENTER;

	/** The number of regions for each of the the line and page flows. */
	public static final int FLOW_REGION_COUNT = 3;

	/** The three regions for each axis/direction combination. */
	protected static final Region[][][] REGIONS = new Region[Axis.values().length][Flow.Direction.values().length][FLOW_REGION_COUNT];

	/**
	 * Determines the corresponding region for an orientation flow and absolute region number. For example, a left-to-right, top-to-bottom page flow of index
	 * <code>2</code> will yield {@link #PAGE_END}, while a right-to-left, top-to-bottom line flow of index <code>2</code> will yield {@link #LINE_START}.
	 * @param orientation The component orientation.
	 * @param flow The flow (line or page).
	 * @param regionIndex The absolute region index (0, 1, or 2) from the upper left-hand corner.
	 * @throws IllegalArgumentException if the given region index is less than <code>0</code> or greater than <code>2</code>.
	 * @return The corresponding region for the orientation.
	 */
	public static Region getRegion(final Orientation orientation, final Flow flow, final int regionIndex) {
		if(regionIndex < 0 || regionIndex >= FLOW_REGION_COUNT) { //if an invalid region index was given
			throw new IllegalArgumentException("Illegal region index: " + regionIndex);
		}
		return REGIONS[orientation.getAxis(flow).ordinal()][orientation.getDirection(flow).ordinal()][regionIndex]; //look up the region in the table
	}

	static {
		//initialize the regions lookup table
		REGIONS[Axis.X.ordinal()][Flow.Direction.INCREASING.ordinal()] = new Region[] { LINE_START, CENTER, LINE_END };
		REGIONS[Axis.X.ordinal()][Flow.Direction.DECREASING.ordinal()] = new Region[] { LINE_END, CENTER, LINE_START };
		REGIONS[Axis.Y.ordinal()][Flow.Direction.INCREASING.ordinal()] = new Region[] { PAGE_START, CENTER, PAGE_END };
		REGIONS[Axis.Y.ordinal()][Flow.Direction.DECREASING.ordinal()] = new Region[] { PAGE_END, CENTER, PAGE_START };
		REGIONS[Axis.Z.ordinal()][Flow.Direction.INCREASING.ordinal()] = new Region[] { null, null, null }; //we don't currently use the Z axis
		REGIONS[Axis.Z.ordinal()][Flow.Direction.DECREASING.ordinal()] = new Region[] { null, null, null };
	}
}
