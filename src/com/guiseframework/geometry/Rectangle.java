package com.guiseframework.geometry;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.garretwilson.lang.ObjectUtilities;

/**A rectangle on a plane.
@author Garret Wilson
*/
public class Rectangle
{

	/**The position of the upper-left corner of the rectangle.*/
	private final Point position;

		/**@return The position of the upper-left corner of the rectangle.*/
		public Point getPosition() {return position;}

	/**The size of the rectangle, which is guaranteed to have zero depth.*/
	private final Dimensions size;

		/**@return The size of the rectangle, which is guaranteed to have zero depth.*/
		public Dimensions getSize() {return size;}

	/**Two-dimensional primitive pixel coordinates and dimensions constructor.
	@param x The X coordinate.
	@param y The Y coordinate.
	@param width The width of the rectangle
	@param height The height of the rectangle.
	*/
	public Rectangle(final double x, final double y, final double width, final double height)
	{
		this(x, y, 0, width, height);	//construct the rectangle on the zero plane		
	}

	/**Primitive pixel coordinates and dimensions constructor.
	@param x The X coordinate.
	@param y The Y coordinate.
	@param z The Z coordinate.
	@param width The width of the rectangle
	@param height The height of the rectangle.
	*/
	public Rectangle(final double x, final double y, final double z, final double width, final double height)
	{
		this(x, y, z, width, height, Extent.Unit.PIXEL);	//construct the rectangle using pixel units
	}

	/**Two-dimensional primitive coordinates, dimensions, and unit constructor.
	@param x The X coordinate.
	@param y The Y coordinate.
	@param width The width of the rectangle
	@param height The height of the rectangle.
	@param unit The unit in which the point is measured.
	*/
	public Rectangle(final double x, final double y, final double width, final double height, final Extent.Unit unit)
	{
		this(x, y, 0, width, height, unit);	//construct the rectangle on the zero plane
	}
		
	/**Primitive coordinates, dimensions, and unit constructor.
	@param x The X coordinate.
	@param y The Y coordinate.
	@param z The Z coordinate.
	@param width The width of the rectangle
	@param height The height of the rectangle.
	@param unit The unit in which the point is measured.
	*/
	public Rectangle(final double x, final double y, final double z, final double width, final double height, final Extent.Unit unit)
	{
		this(new Point(x, y, z, unit), new Dimensions(width, height, 0, unit));	//construct a rectangle from created point and dimensions
	}

	/**Two-dimensional coordinates and dimensions constructor.
	@param x The X coordinate.
	@param y The Y coordinate.
	@param width The width of the rectangle
	@param height The height of the rectangle.
	*/
	public Rectangle(final Extent x, final Extent y, final Extent width, final Extent height)
	{
		this(x, y, Extent.ZERO_EXTENT1, width, height);	//construct a rectangle on the zero plane
	}

	/**Coordinates and dimensions constructor.
	@param x The X coordinate.
	@param y The Y coordinate.
	@param z The Z coordinate.
	@param width The width of the rectangle
	@param height The height of the rectangle.
	*/
	public Rectangle(final Extent x, final Extent y, final Extent z, final Extent width, final Extent height)	//TODO maybe check the degree
	{
		this(new Point(x, y, z), new Dimensions(width, height, Extent.ZERO_EXTENT1));	//construct the rectangle with a point and zero-depth dimensions
	}
		
	/**Position and size constructor.
	@param position The position of the upper-left corner of the rectangle.
	@param size The size of the rectangle, which is guaranteed to have zero depth.
	@exception NullPointerException if the position and/or size is <code>null</code>.
	@exception IllegalArgumentException if the size has a non-zero depth.
	*/
	public Rectangle(final Point position, final Dimensions size)
	{
		this.position=checkNull(position, "Position cannot be null.");
		this.size=checkNull(size, "Size cannot be null.");
		if(size.getDepth().getValue()!=0)	//if the depth is not zero
		{
			throw new IllegalArgumentException("A rectangle cannot have a depth.");
		}
	}
	
	/**@return A hash code value for the object.*/
	public int hashCode()
	{
		return ObjectUtilities.hashCode(getPosition(), getSize());	//determine the hash code
	}

	/**Indicates whether some other object is "equal to" this one.
	This implementation returns whether the object is another rectangle with equivalent position and size.
	@param object The reference object with which to compare.
	@return <code>true</code> if this object is equivalent to the given object.
	*/
	public boolean equals(final Object object)
	{
		if(object instanceof Rectangle)	//if the object is an rectangle
		{
			final Rectangle rectangle=(Rectangle)object;	//get the object as a rectangle
			return getPosition().equals(rectangle.getPosition()) && getSize().equals(rectangle.getSize());	//see if the position and size match
		}
		return false;	//the object did not match this point
	}

	/**@return A string representation of the object.*/
	public final String toString()
	{
		return "{"+getPosition()+", "+getSize()+"}";	//"{[x, y, z], [width, depth, height]}"
	}

}
