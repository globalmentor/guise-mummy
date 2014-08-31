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

import java.net.URI;

import com.globalmentor.util.StringTemplate;
import com.guiseframework.geometry.*;

import static com.globalmentor.java.Objects.*;
import static com.guiseframework.Resources.*;

/**
 * The orientation of a particular flow; its axis and direction.
 * @author Garret Wilson
 */
public enum FlowOrientation {

	/** {@link Flow.Direction#INCREASING} flow on the {@link Axis#X} axis. */
	LEFT_TO_RIGHT(Axis.X, Flow.Direction.INCREASING),
	/** {@link Flow.Direction#DECREASING} flow on the {@link Axis#X} axis. */
	RIGHT_TO_LEFT(Axis.X, Flow.Direction.DECREASING),
	/** {@link Flow.Direction#INCREASING} flow on the {@link Axis#Y} axis. */
	TOP_TO_BOTTOM(Axis.Y, Flow.Direction.INCREASING),
	/** {@link Flow.Direction#DECREASING} flow on the {@link Axis#Y} axis. */
	BOTTOM_TO_TOP(Axis.Y, Flow.Direction.DECREASING);

	/** {@link Flow.Direction#INCREASING} flow on the {@link Axis#Z} axis. */
	//TODO fix when compass point issue is resolved	FRONT_TO_BACK(Axis.Z, Flow.Direction.INCREASING),
	/** {@link Flow.Direction#DECREASING} flow on the {@link Axis#Z} axis. */
	//TODO fix when compass point issue is resolved	BACK_TO_FRONT(Axis.Z, Flow.Direction.DECREASING);

	/** The resource key template for each glyph. */
	private static final StringTemplate GLYPH_RESOURCE_KEY_TEMPLATE = new StringTemplate("flow.orientation.", StringTemplate.STRING_PARAMETER, ".glyph");

	/** @return The resource reference for the glyph, using the session's default orientation. */
	public URI getGlyph() {
		return createURIResourceReference(GLYPH_RESOURCE_KEY_TEMPLATE.apply(getResourceKeyName(this))); //create a resource reference using the resource key name of this enum value TODO add something to the glyph resource key to allow better lookup of the physical layout
	}

	/** The axis of the flow. */
	private Axis axis;

	/** @return The axis of the flow. */
	public Axis getAxis() {
		return axis;
	}

	/** The direction of the flow on the axis. */
	private Flow.Direction direction;

	/** @return The direction of the flow on the axis. */
	public Flow.Direction getDirection() {
		return direction;
	}

	/** The compass points for each end. */
	private CompassPoint[] compassPoints = new CompassPoint[Flow.End.values().length];

	/**
	 * Axis and direction constructor.
	 * @param axis The axis of the flow.
	 * @param direction The direction of the flow on the axis.
	 * @throws NullPointerException if the given axis and/or direction is <code>null</code>.
	 */
	private FlowOrientation(final Axis axis, final Flow.Direction direction) {
		this.axis = checkInstance(axis, "Axis cannot be null.");
		this.direction = checkInstance(direction, "Direction cannot be null.");
		switch(axis) { //see which axis this is
			case X:
				switch(direction) { //see which direction this is
					case INCREASING:
						compassPoints[Flow.End.NEAR.ordinal()] = CompassPoint.WEST;
						compassPoints[Flow.End.FAR.ordinal()] = CompassPoint.EAST;
						break;
					case DECREASING:
						compassPoints[Flow.End.NEAR.ordinal()] = CompassPoint.EAST;
						compassPoints[Flow.End.FAR.ordinal()] = CompassPoint.WEST;
						break;
					default:
						throw new AssertionError("Unrecognized direciton: " + direction);
				}
				break;
			case Y:
				switch(direction) { //see which direction this is
					case INCREASING:
						compassPoints[Flow.End.NEAR.ordinal()] = CompassPoint.NORTH;
						compassPoints[Flow.End.FAR.ordinal()] = CompassPoint.SOUTH;
						break;
					case DECREASING:
						compassPoints[Flow.End.NEAR.ordinal()] = CompassPoint.SOUTH;
						compassPoints[Flow.End.FAR.ordinal()] = CompassPoint.NORTH;
						break;
					default:
						throw new AssertionError("Unrecognized direciton: " + direction);
				}
				break;
			default:
				throw new AssertionError("Unrecognized axis: " + axis);
		}
	}

	/**
	 * Retrieves a cardinal compass point indicating the absolute direction based upon the given flow end.
	 * @param end The end of the flow.
	 * @return The cardinal compass point indicating the absolute direction of the given end.
	 * @throws NullPointerException if the given end is <code>null</code>.
	 * @see CompassPoint#NORTH
	 * @see CompassPoint#EAST
	 * @see CompassPoint#SOUTH
	 * @see CompassPoint#WEST
	 */
	public CompassPoint getCompassPoint(final Flow.End end) {
		return compassPoints[end.ordinal()]; //return the compass point for this end
	}

}
