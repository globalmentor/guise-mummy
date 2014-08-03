/*
 * Copyright © 2005-2012 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package com.guiseframework.geometry;

import static com.globalmentor.collections.Sets.*;
import static com.globalmentor.java.Conditions.*;
import static java.util.Collections.*;

import java.util.*;

import javax.annotation.*;

import com.globalmentor.lex.Identifier;

/**
 * Indicates an absolute side.
 * @author Garret Wilson
 * @see Axis
 * @see com.guiseframework.component.layout.Border
 */
public enum Side implements Identifier {

	/** The left side. */
	LEFT,

	/** The right side. */
	RIGHT,

	/** The top side. */
	TOP,

	/** The bottom side. */
	BOTTOM,

	/**
	 * The back side. This value is currently unused.
	 */
	FRONT,

	/**
	 * The front side. This value is currently unused.
	 */
	BACK;

	/** @return The absolute axis of the side. */
	public Axis getAxis() {
		switch(this) {
			case LEFT:
			case RIGHT:
				return Axis.X;
			case TOP:
			case BOTTOM:
				return Axis.Y;
			case FRONT:
			case BACK:
				return Axis.Z;
			default:
				throw impossible("Unrecognized side: " + this);
		}
	}

	//the map of sides for each axis
	private final static Map<Axis, Set<Side>> AXIS_SIDES;

	/**
	 * Determines the sides on the given axis.
	 * @param axis The axis the sides of which to return.
	 * @return The sides for the given axis.
	 */
	public static Set<Side> getSides(@Nonnull final Axis axis) {
		return AXIS_SIDES.get(axis); //return the sides for this axis
	}

	static {
		final Axis[] axes = Axis.values();
		assert axes.length == 3;
		final Map<Axis, Set<Side>> axisSides = new EnumMap<Axis, Set<Side>>(Axis.class);
		axisSides.put(Axis.X, immutableSetOf(LEFT, RIGHT)); //initialize the sets
		axisSides.put(Axis.Y, immutableSetOf(TOP, BOTTOM));
		axisSides.put(Axis.Z, immutableSetOf(FRONT, BACK));
		AXIS_SIDES = unmodifiableMap(axisSides);
	}

}
