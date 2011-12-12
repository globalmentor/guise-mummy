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

package com.guiseframework.geometry;

import static com.globalmentor.java.Objects.*;

import com.globalmentor.java.Objects;

/**A point in three-dimensional space.
@author Garret Wilson
*/
public class Point
{

	/**A predefined point with zero coordinates.*/
	public final static Point ORIGIN_POINT=new Point(0, 0);
	
	/**The X coordinate.*/
	private final Extent x;

		/**@return The X coordinate.*/
		public Extent getX() {return x;}

	/**The Y coordinate.*/
	private final Extent y;

		/**@return The Y coordinate.*/
		public Extent getY() {return y;}

	/**The Z coordinate.*/
	private final Extent z;

		/**@return The Z coordinate.*/
		public Extent getZ() {return z;}

	/**Two-dimensional primitive pixel coordinate constructor.
	@param x The X coordinate.
	@param y The Y coordinate.
	*/
	public Point(final double x, double y)
	{
		this(x, y, 0);	//construct the point in the zero Z plane		
	}

	/**Primitive pixel coordinate constructor.
	@param x The X coordinate.
	@param y The Y coordinate.
	@param z The Z coordinate.
	*/
	public Point(final double x, double y, double z)
	{
		this(x, y, z, Unit.PIXEL);	//construct the point using pixels
	}

	/**Two-dimensional primitive coordinate and unit constructor.
	@param x The X coordinate.
	@param y The Y coordinate.
	@param unit The unit in which the point is measured.
	*/
	public Point(final double x, double y, final Unit unit)
	{
		this(x, y, 0, unit);	//construct the point in the zero Z plane
	}

	/**Primitive coordinate and unit constructor.
	@param x The X coordinate.
	@param y The Y coordinate.
	@param z The Z coordinate.
	@param unit The unit in which the point is measured.
	*/
	public Point(final double x, double y, double z, final Unit unit)
	{
		this(new Extent(x, unit, 1), new Extent(y, unit, 1), new Extent(z, unit, 1));	//create extents and construct the point
	}
	
	/**Two-dimensional coordinate constructor.
	@param x The X coordinate.
	@param y The Y coordinate.
	@exception NullPointerException if one of the given coordinates is <code>null</code>.
	*/
	public Point(final Extent x, final Extent y)
	{
		this(x, y, Extent.ZERO_EXTENT1);	//construct the point with a zero extent
	}

	/**Coordinate constructor.
	@param x The X coordinate.
	@param y The Y coordinate.
	@param z The Z coordinate.
	@exception NullPointerException if one of the given coordinates is <code>null</code>.
	*/
	public Point(final Extent x, final Extent y, final Extent z)	//TODO maybe check the degree
	{
		this.x=checkInstance(x, "X coordinate cannot be null.");
		this.y=checkInstance(y, "Y coordinate cannot be null.");
		this.z=checkInstance(z, "Z coordinate cannot be null.");
	}


	/**Translates this point in two dimensions by the given coordinate deltas.
	@param dx The X coordinate delta.
	@param dy The Y coordinate delta.
	@return A new point translated by the given deltas.
	*/
	public Point translate(final Extent dx, final Extent dy)
	{
		return translate(dx, dy, Extent.ZERO_EXTENT1);	//translate the point in the x and y coordinates
	}

	/**Translates this point by the given coordinate deltas.
	@param dx The X coordinate delta.
	@param dy The Y coordinate delta.
	@param dz The Z coordinate delta.
	@return A new point translated by the given deltas.
	*/
	public Point translate(final Extent dx, final Extent dy, final Extent dz)
	{
		return translate(dx.getValue(), dy.getValue(), dz.getValue());	//translate by the primitive delta values TODO do any conversions to those values, and check degree
	}

	/**Translates this point in two dimensions by the given primitive coordinate deltas.
	@param dx The X coordinate delta.
	@param dy The Y coordinate delta.
	@return A new point translated by the given deltas.
	*/
	public Point translate(final double dx, final double dy)
	{
		return translate(dx, dy, 0);	//translate the point on the same plane
	}

	/**Translates this point by the given primitive coordinate deltas.
	@param dx The X coordinate delta.
	@param dy The Y coordinate delta.
	@param dz The Z coordinate delta.
	@return A new point translated by the given deltas.
	*/
	public Point translate(final double dx, final double dy, final double dz)
	{
		final Extent x=getX(), y=getY(), z=getZ();	//get the coordinates
		return new Point(new Extent(x.getValue()+dx, x.getUnit(), x.getDegree()), new Extent(y.getValue()+dy, y.getUnit(), y.getDegree()), new Extent(z.getValue()+dz, z.getUnit(), z.getDegree()));	//TODO check the units and degrees		
	}

	/**Translates this point by the given dimensions.
	@param dimensions The dimension by which to translate this point
	@return A new point translated by the given dimensions.
	*/
	public Point translate(final Dimensions dimensions)
	{
		return translate(dimensions.getWidth(), dimensions.getHeight(), dimensions.getDepth());	//translate the point by the dimension values
	}

	/**@return A hash code value for the object.*/
	public int hashCode()
	{
		return Objects.getHashCode(getX(), getY(), getZ());	//determine the hash code
	}

	/**Indicates whether some other object is "equal to" this one.
	This implementation returns whether the object is another point with equivalent coordinates.
	@param object The reference object with which to compare.
	@return <code>true</code> if this object is equivalent to the given object.
	*/
	public boolean equals(final Object object)
	{
		if(object instanceof Point)	//if the object is an point
		{
			final Point point=(Point)object;	//get the object as a point
			return getX().equals(point.getX()) && getY().equals(point.getY()) && getZ().equals(point.getZ());	//see if the coordinates match
		}
		return false;	//the object did not match this point
	}

	/**@return A string representation of the object.*/
	public final String toString()
	{
		return "("+getX().getValue()+", "+getY().getValue()+", "+getZ().getValue()+")";	//"(x, y, z)"
	}
}
