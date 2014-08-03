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

/**
 * The points at which the four logical flows end.
 * @author Garret Wilson
 * @see com.guiseframework.geometry.Side
 */
public enum Border {

	/** The left side in left-to-right top-to-bottom orientation. */
	LINE_NEAR(Flow.LINE, Flow.End.NEAR),
	/** The right side in left-to-right top-to-bottom orientation. */
	LINE_FAR(Flow.LINE, Flow.End.FAR),
	/** The top side in left-to-right top-to-bottom orientation. */
	PAGE_NEAR(Flow.PAGE, Flow.End.NEAR),
	/** The bottom side in left-to-right top-to-bottom orientation. */
	PAGE_FAR(Flow.PAGE, Flow.End.FAR);

	/** The flow. */
	private final Flow flow;

	/** The flow. */
	public Flow getFlow() {
		return flow;
	}

	/** The end. */
	private final Flow.End end;

	/** The end. */
	public Flow.End getEnd() {
		return end;
	}

	/**
	 * Flow and end constructor.
	 * @param flow The flow
	 * @param end The end.
	 * @throws NullPointerException if the given flow and/or end is <code>null</code>.
	 */
	private Border(final Flow flow, final Flow.End end) {
		this.flow = checkInstance(flow, "Flow cannot be null.");
		this.end = checkInstance(end, "End cannot be null.");
	}

	/** The borders in [flow][end] order. */
	private final static Border[][] FLOW_END_BORDERS = new Border[][] { { LINE_NEAR, LINE_FAR }, { PAGE_NEAR, PAGE_FAR } };

	/**
	 * Determines the border from the flow and end.
	 * @param flow The flow
	 * @param end The end.
	 * @return The border for the specified flow and page end.
	 * @throws NullPointerException if the given flow and/or end is <code>null</code>.
	 */
	public static Border getBorder(final Flow flow, final Flow.End end) {
		return FLOW_END_BORDERS[flow.ordinal()][end.ordinal()]; //look up the border in our array
	}
}
