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

import static com.globalmentor.java.Objects.*;

import com.globalmentor.java.Objects;

/**
 * A rectangle on a plane.
 * @author Garret Wilson
 */
public class Rectangle {

	/** A predefined rectangle with zero size on the zero plane. */
	public static final Rectangle EMPTY_RECTANGLE = new Rectangle(0, 0, 0, 0);

	/** The position of the upper-left corner of the rectangle. */
	private final Point position;

	/** @return The position of the upper-left corner of the rectangle. */
	public Point getPosition() {
		return position;
	}

	/** The size of the rectangle, which is guaranteed to have zero depth. */
	private final Dimensions size;

	/** @return The size of the rectangle, which is guaranteed to have zero depth. */
	public Dimensions getSize() {
		return size;
	}

	/**
	 * Two-dimensional primitive pixel coordinates and dimensions constructor.
	 * @param x The X coordinate.
	 * @param y The Y coordinate.
	 * @param width The width of the rectangle
	 * @param height The height of the rectangle.
	 */
	public Rectangle(final double x, final double y, final double width, final double height) {
		this(x, y, 0, width, height); //construct the rectangle on the zero plane		
	}

	/**
	 * Primitive pixel coordinates and dimensions constructor.
	 * @param x The X coordinate.
	 * @param y The Y coordinate.
	 * @param z The Z coordinate.
	 * @param width The width of the rectangle
	 * @param height The height of the rectangle.
	 */
	public Rectangle(final double x, final double y, final double z, final double width, final double height) {
		this(x, y, z, width, height, Unit.PIXEL); //construct the rectangle using pixel units
	}

	/**
	 * Two-dimensional primitive coordinates, dimensions, and unit constructor.
	 * @param x The X coordinate.
	 * @param y The Y coordinate.
	 * @param width The width of the rectangle
	 * @param height The height of the rectangle.
	 * @param unit The unit in which the point is measured.
	 */
	public Rectangle(final double x, final double y, final double width, final double height, final Unit unit) {
		this(x, y, 0, width, height, unit); //construct the rectangle on the zero plane
	}

	/**
	 * Primitive coordinates, dimensions, and unit constructor.
	 * @param x The X coordinate.
	 * @param y The Y coordinate.
	 * @param z The Z coordinate.
	 * @param width The width of the rectangle
	 * @param height The height of the rectangle.
	 * @param unit The unit in which the point is measured.
	 */
	public Rectangle(final double x, final double y, final double z, final double width, final double height, final Unit unit) {
		this(new Point(x, y, z, unit), new Dimensions(width, height, 0, unit)); //construct a rectangle from created point and dimensions
	}

	/**
	 * Two-dimensional coordinates and dimensions constructor.
	 * @param x The X coordinate.
	 * @param y The Y coordinate.
	 * @param width The width of the rectangle
	 * @param height The height of the rectangle.
	 */
	public Rectangle(final Extent x, final Extent y, final Extent width, final Extent height) {
		this(x, y, Extent.ZERO_EXTENT1, width, height); //construct a rectangle on the zero plane
	}

	/**
	 * Coordinates and dimensions constructor.
	 * @param x The X coordinate.
	 * @param y The Y coordinate.
	 * @param z The Z coordinate.
	 * @param width The width of the rectangle
	 * @param height The height of the rectangle.
	 */
	public Rectangle(final Extent x, final Extent y, final Extent z, final Extent width, final Extent height) { //TODO maybe check the degree
		this(new Point(x, y, z), new Dimensions(width, height, Extent.ZERO_EXTENT1)); //construct the rectangle with a point and zero-depth dimensions
	}

	/**
	 * Position and size constructor.
	 * @param position The position of the upper-left corner of the rectangle.
	 * @param size The size of the rectangle, which is guaranteed to have zero depth.
	 * @throws NullPointerException if the position and/or size is <code>null</code>.
	 * @throws IllegalArgumentException if the size has a non-zero depth.
	 */
	public Rectangle(final Point position, final Dimensions size) {
		this.position = checkInstance(position, "Position cannot be null.");
		this.size = checkInstance(size, "Size cannot be null.");
		if(!size.getDepth().isEmpty()) { //if the depth is not zero
			throw new IllegalArgumentException("A rectangle cannot have a depth.");
		}
	}

	@Override
	public int hashCode() {
		return Objects.getHashCode(getPosition(), getSize()); //determine the hash code
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation returns whether the object is another rectangle with equivalent position and size.
	 * </p>
	 */
	@Override
	public boolean equals(final Object object) {
		if(object instanceof Rectangle) { //if the object is an rectangle
			final Rectangle rectangle = (Rectangle)object; //get the object as a rectangle
			return getPosition().equals(rectangle.getPosition()) && getSize().equals(rectangle.getSize()); //see if the position and size match
		}
		return false; //the object did not match this point
	}

	@Override
	public final String toString() {
		return "{" + getPosition() + ", " + getSize() + "}"; //"{[x, y, z], [width, depth, height]}"
	}

}
