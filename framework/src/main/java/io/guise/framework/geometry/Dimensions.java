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

package io.guise.framework.geometry;

import static java.util.Objects.*;

/**
 * A measurement of an object's width, height, and depth along the X, Y, and Z dimensions, respectively.
 * <p>
 * Dimensions of zero dimensions are considered equal regardless of the units involved.
 * </p>
 * @author Garret Wilson
 */
public class Dimensions {

	/** A convenience dimensions of zero pixel size. */
	public static final Dimensions ZERO_DIMENSIONS = new Dimensions(0, 0, 0, Unit.PIXEL);

	/** The width extent. */
	private final Extent width;

	/** @return The width extent. */
	public Extent getWidth() {
		return width;
	}

	/** The height extent. */
	private final Extent height;

	/** @return The height extent. */
	public Extent getHeight() {
		return height;
	}

	/** The depth extent. */
	private final Extent depth;

	/** @return The depth extent. */
	public Extent getDepth() {
		return depth;
	}

	/**
	 * @return <code>true</code> if all extents of the dimensions are zero.
	 * @see Extent#isEmpty()
	 */
	public boolean isEmpty() {
		return getWidth().isEmpty() && getHeight().isEmpty() && getDepth().isEmpty();
	}

	/** The precalculated hash code of the dimensions. */
	private final int hashCode;

	/**
	 * Width and height unit constructor with a depth of one.
	 * @param width The width.
	 * @param height The height.
	 * @param unit The unit with which the extent is measured.
	 * @throws NullPointerException if the given unit is <code>null</code>.
	 */
	public Dimensions(final double width, final double height, final Unit unit) {
		this(width, unit, height, unit); //create the dimensions with the same unit
	}

	/**
	 * Width unit and height unit constructor with a depth of one.
	 * @param width The width.
	 * @param widthUnit The unit with which the width is measured.
	 * @param height The height.
	 * @param heightUnit The unit with which the height is measured.
	 * @throws NullPointerException if the given width unit and/or height unit is <code>null</code>.
	 */
	public Dimensions(final double width, final Unit widthUnit, final double height, final Unit heightUnit) {
		this(new Extent(width, widthUnit, 1), new Extent(height, heightUnit, 1), Extent.ZERO_EXTENT1); //create extents and construct the class with a zero depth
	}

	/**
	 * Width, height, and depth unit constructor.
	 * @param width The width.
	 * @param height The height.
	 * @param depth The depth.
	 * @param unit The unit with which the extent is measured.
	 * @throws NullPointerException if the given unit is <code>null</code>.
	 */
	public Dimensions(final double width, final double height, final double depth, final Unit unit) {
		this(new Extent(width, unit, 1), new Extent(height, unit, 1), new Extent(depth, unit, 1)); //create extents and construct the class
	}

	/**
	 * Width and height extent constructor with a default depth of zero pixels.
	 * @param width The width extent.
	 * @param height The height extent.
	 * @throws NullPointerException if the given width, and/or height is <code>null</code>.
	 * @throws IllegalArgumentException if the degree of any extent is not <code>1</code>.
	 */
	public Dimensions(final Extent width, final Extent height) {
		this(width, height, Extent.ZERO_EXTENT1); //construct the class with a zero depth
	}

	/**
	 * Width, height, and depth extent constructor.
	 * @param width The width extent.
	 * @param height The height extent.
	 * @param depth The depth extent.
	 * @throws NullPointerException if the given width, height, and/or depth is <code>null</code>.
	 * @throws IllegalArgumentException if the degree of any extent is not <code>1</code>.
	 */
	public Dimensions(final Extent width, final Extent height, final Extent depth) {
		if(width.getDegree() != 1) { //if the width degree is not one
			throw new IllegalArgumentException("Width dimension degree must be 1.");
		}
		if(height.getDegree() != 1) { //if the height degree is not one
			throw new IllegalArgumentException("Height dimension degree must be 1.");
		}
		if(depth.getDegree() != 1) { //if the depth degree is not one
			throw new IllegalArgumentException("Depth dimension degree must be 1.");
		}
		this.width = requireNonNull(width, "Width cannot be null.");
		this.height = requireNonNull(height, "Height cannot be null.");
		this.depth = requireNonNull(depth, "Depth cannot be null.");
		this.hashCode = hash(width, height, depth); //precalculate the hash code
	}

	/**
	 * Constrains these inner dimensions within the given outer dimensions by scaling these dimensions so that no part lies outside the given outer dimensiona.
	 * This implementation currently only supports two dimensions.
	 * @param constrainingDimensions The outer constraining dimensions.
	 * @return A dimension representing the constrained dimension.
	 * @throws NullPointerException if the given constraining dimensions is <code>null</code>.
	 * @throws IllegalArgumentException if the given constraining dimensions use different units than these dimensions.
	 */
	public Dimensions constrain(final Dimensions constrainingDimensions) {
		final Extent width = getWidth();
		final double widthValue = width.getValue();
		final Unit widthUnit = width.getUnit();
		final Extent height = getHeight();
		final double heightValue = height.getValue();
		final Unit heightUnit = height.getUnit();
		final Extent constrainingWidth = constrainingDimensions.getWidth();
		final double constrainingWidthValue = constrainingWidth.getValue();
		final Extent constrainingHeight = constrainingDimensions.getHeight();
		final double constrainingHeightValue = constrainingHeight.getValue();
		if(widthUnit != constrainingWidth.getUnit() || heightUnit != constrainingHeight.getUnit()) { //if the units don't match
			throw new IllegalArgumentException("Units of dimensions " + this + " and " + constrainingDimensions + " do not match.");
		}
		if(widthValue <= constrainingWidthValue && heightValue <= constrainingHeightValue) { //if nothing needs to be constrained
			return this; //return this dimension unchanged
		}
		final double relation = widthValue / heightValue; //determine the relationship of the sides
		//TODO del Log.trace("relation of sides is:", relation);
		double newWidthValue, newHeightValue;
		newHeightValue = constrainingWidthValue / relation; //get the matching height for a constrained width
		//TODO del Log.trace("trying to constrain width to", constrainingWidth, "height to ", newHeight);
		if(newHeightValue <= constrainingHeightValue) { //if the height has been constrained
			newWidthValue = constrainingWidthValue; //constrain the width to the edges
		} else { //if the height needs to be constrained
			newWidthValue = constrainingHeightValue * relation; //get the matching width for a constrained height
			newHeightValue = constrainingHeightValue; //constrain the height to the edges
			//TODO del Log.trace("that didn't work; trying to constrain width to", newWidth, "height to ", newHeight);
		}
		return new Dimensions(newWidthValue, widthUnit, newHeightValue, heightUnit); //return the new constrained dimensions
	}

	@Override
	public int hashCode() {
		return hashCode; //return the precalculated hash code
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation returns whether the object is another dimensions with identical width, height, and depth.
	 * </p>
	 */
	@Override
	public boolean equals(final Object object) {
		if(object instanceof Dimensions) { //if the object is an extent
			final Dimensions dimensions = (Dimensions)object; //get the object as dimensions
			return getWidth().equals(dimensions.getWidth()) && getHeight().equals(dimensions.getHeight()) && getDepth().equals(dimensions.getDepth()); //compare dimensions
		}
		return false; //the object did not match this dimensions
	}

	@Override
	public final String toString() {
		return "[" + getWidth().getValue() + ", " + getHeight().getValue() + ", " + getDepth().getValue() + "]"; //"[width, height, depth]"
	}

}
