package com.guiseframework.geometry;

import static com.globalmentor.java.Objects.*;

import com.globalmentor.java.Objects;

/**A measurement of an object's width, height, and depth along the X, Y, and Z dimensions, respectively.
@author garret
*/ 
public class Dimensions
{

	/**A convenience dimensions of zero pixel size.*/
	public final static Dimensions ZERO_DIMENSIONS=new Dimensions(0, 0, 0, Unit.PIXEL);

	/**The width extent.*/
	private final Extent width;

		/**@return The width extent.*/
		public Extent getWidth() {return width;}

	/**The height extent.*/
	private final Extent height;

		/**@return The height extent.*/
		public Extent getHeight() {return height;}

	/**The depth extent.*/
	private final Extent depth;

		/**@return The depth extent.*/
		public Extent getDepth() {return depth;}

	/**The precalculated hash code of the dimensions.*/
	private final int hashCode;

	/**Width and height unit constructor.
	@param width The width.
	@param height The height.
	@param unit The unit with which the extent is measured.
	@exception NullPointerException if the given unit is <code>null</code>.
	*/
	public Dimensions(final double width, final double height, final Unit unit)
	{
		this(new Extent(width, unit, 1), new Extent(height, unit, 1), new Extent(0, unit, 1));	//create extents and construct the class with a zero depth
	}

	/**Width, height, and depth unit constructor.
	@param width The width.
	@param height The height.
	@param depth The depth.
	@param unit The unit with which the extent is measured.
	@exception NullPointerException if the given unit is <code>null</code>.
	*/
	public Dimensions(final double width, final double height, final double depth, final Unit unit)
	{
		this(new Extent(width, unit, 1), new Extent(height, unit, 1), new Extent(depth, unit, 1));	//create extents and construct the class
	}

	/**Width and height extent constructor with a default depth of zero pixels.
	@param width The width extent.
	@param height The height extent.
	@exception NullPointerException if the given width, and/or height is <code>null</code>.
	@exception IllegalArgumentException if the degree of any extent is not <code>1</code>.
	*/
	public Dimensions(final Extent width, final Extent height)
	{
		this(width, height, Extent.ZERO_EXTENT1);	//construct the class with a zero depth
	}

	/**Width, height, and depth extent constructor.
	@param width The width extent.
	@param height The height extent.
	@param depth The depth extent.
	@exception NullPointerException if the given width, height, and/or depth is <code>null</code>.
	@exception IllegalArgumentException if the degree of any extent is not <code>1</code>.
	*/
	public Dimensions(final Extent width, final Extent height, final Extent depth)
	{
		if(width.getDegree()!=1)	//if the width degree is not one
		{
			throw new IllegalArgumentException("Width dimension degree must be 1.");
		}
		if(height.getDegree()!=1)	//if the height degree is not one
		{
			throw new IllegalArgumentException("Height dimension degree must be 1.");
		}
		if(depth.getDegree()!=1)	//if the depth degree is not one
		{
			throw new IllegalArgumentException("Depth dimension degree must be 1.");
		}
		this.width=checkInstance(width, "Width cannot be null.");
		this.height=checkInstance(height, "Height cannot be null.");
		this.depth=checkInstance(depth, "Depth cannot be null.");
		this.hashCode=Objects.hashCode(width, height, depth);	//precalculate the hash code
	}

	/**@return A hash code value for the object.*/
	public int hashCode()
	{
		return hashCode;	//return the precalculated hash code
	}

	/**Indicates whether some other object is "equal to" this one.
	This implementation returns whether the object is another dimensions with identical width, height, and depth.
	@param object The reference object with which to compare.
	@return <code>true</code> if this object is equivalent to the given object.
	*/
	public boolean equals(final Object object)
	{
		if(object instanceof Dimensions)	//if the object is an extent
		{
			final Dimensions dimensions=(Dimensions)object;	//get the object as dimensions
			return getWidth().equals(dimensions.getWidth()) && getHeight().equals(dimensions.getHeight()) && getDepth().equals(dimensions.getDepth());	//compare dimensions
		}
		return false;	//the object did not match this dimensions
	}

	/**@return A string representation of the object.*/
	public final String toString()
	{
		return "["+getWidth().getValue()+", "+getHeight().getValue()+", "+getDepth().getValue()+"]";	//"[width, height, depth]"
	}

}
