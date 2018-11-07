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

package io.guise.framework.geometry;

import static java.util.Objects.*;

import java.util.Arrays;

import com.globalmentor.java.Objects;

/**
 * A measurement such as a length, area, or volume. All zero extents are considered equal, regardless of the unit of measurement. For this implementation only
 * extents of the same unit and degree should be compared, although consistent, if inaccurate, values will still be obtained otherwise.
 * @author Garret Wilson
 */
public class Extent implements Comparable<Extent> {

	/** A convenience one-dimensional extent of zero pixels. */
	public static final Extent ZERO_EXTENT1 = new Extent(0, Unit.PIXEL, 1);

	/** The value of the extent. */
	private final double value;

	/** @return The value of the extent. */
	public double getValue() {
		return value;
	}

	/** @return <code>true</code> if the extent has a zero value. */
	public boolean isEmpty() {
		return value == 0.0;
	}

	/** The unit with which the extent is measured. */
	private final Unit unit;

	/** @return The unit with which the extent is measured. */
	public Unit getUnit() {
		return unit;
	}

	/** The degree of dimensions of the measurement. */
	private final int degree;

	/** @return The degree of dimensions of the measurement. */
	public int getDegree() {
		return degree;
	}

	/**
	 * Value constructor for a one-dimensional pixel extent.
	 * @param value The value of the extent.
	 */
	public Extent(final double value) {
		this(value, Unit.PIXEL); //construct the extent using pixel units
	}

	/**
	 * Value and unit constructor with a degree of one.
	 * @param value The value of the extent.
	 * @param unit The unit with which the extent is measured.
	 * @throws NullPointerException if the given unit is <code>null</code>.
	 */
	public Extent(final double value, final Unit unit) {
		this(value, unit, 1); //construct the extent with a degree of one
	}

	/**
	 * Value, unit, and degree constructor.
	 * @param value The value of the extent.
	 * @param unit The unit with which the extent is measured.
	 * @param degree The degree of dimensions of the measurement.
	 * @throws NullPointerException if the given unit is <code>null</code>.
	 * @throws IllegalArgumentException if the degree is non-positive.
	 */
	public Extent(final double value, final Unit unit, final int degree) {
		if(degree <= 0) { //if a non-positive degree was given
			throw new IllegalArgumentException("Degree must be positive.");
		}
		this.value = value; //save the value
		this.unit = requireNonNull(unit, "Unit cannot be null."); //save the unit
		this.degree = degree; //save the degree
	}

	@Override
	public int hashCode() {
		return Objects.getHashCode(getValue(), getUnit(), getDegree()); //determine the hash code
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation returns whether the object is another extent with identical value, unit, and degree, ignoring the unit if the value is zero.
	 * </p>
	 */
	@Override
	public boolean equals(final Object object) {
		if(this == object) { //if the objects are the same
			return true; //identical objects are always equal
		}
		if(object instanceof Extent) { //if the object is an extent
			final Extent extent = (Extent)object; //get the object as an extent
			final double value = getValue(); //get this extent's value
			if(value == extent.getValue()) { //if values match
				if(getUnit() == extent.getUnit() || value == 0) { //if the units match (or the value is zero, because all zero extents are equal)
					return getDegree() == extent.getDegree(); //if degrees match, everything at this point matches
				}
			}
		}
		return false; //the object did not match this extent
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation is meant to compare extents of identical units; non-identical units will be compared by unit ordinal.
	 * </p>
	 * <p>
	 * This implementation is meant to compare extents of identical degrees; non-identical degrees will be compared by degrees.
	 * </p>
	 */
	@Override
	public int compareTo(final Extent extent) {
		final Unit unit = getUnit(); //get this extent's unit
		final Unit extentUnit = extent.getUnit(); //get the other extent's unit
		if(unit != extentUnit) { //if units are different
			return unit.ordinal() - extentUnit.ordinal(); //extents shouldn't be compared with different units; do something reasonable by sorting based upon the unit
		}
		final int degree = getDegree(); //get this extent's degree
		final int extentDegree = getDegree(); //get the other extent's degree
		if(degree != extentDegree) { //if the degrees are different
			return degree - extentDegree; //extents shouldn't be compared with different degrees; do something reasonable by sorting based upon the degree
		}
		return Double.compare(getValue(), extent.getValue()); //compare values
	}

	@Override
	public String toString() {
		return Arrays.toString(new Object[] { getValue(), getUnit(), getDegree() });
	}
}
