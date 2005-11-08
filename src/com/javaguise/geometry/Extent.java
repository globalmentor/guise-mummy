package com.javaguise.geometry;

import static com.garretwilson.lang.ObjectUtilities.*;
import com.garretwilson.lang.ObjectUtilities;

/**A measurement such as a length, area, or volume.
All zero extents are considered equal, regardless of the unit of measurement.
@author Garret Wilson
*/
public class Extent
{

	/**A convenience one-dimensional extent of zero pixels.*/
	public final static Extent ZERO_EXTENT1=new Extent(0, Unit.PIXEL, 1);

	/*The unit of measurement.*/
	public enum Unit
	{
			//relative units
		/**The size of a font.*/
		EM,
		/**The x-height of a font.*/
		EX,
		/**Pixels relative to the viewing device.*/
		PIXEL,
			//absolute units
		/**Inches.*/
		INCH,
		/**Centimeters.*/
		CENTIMETER,
		/**Millimeters.*/
		MILLIMETER,
		/**Points, or 1/72 of an inch.*/
		POINT,
		/**Picas, or 12 points.*/
		PICA,
			//pure relative units
		/**Pure relative units (i.e. the fractional form of a percentage).*/
		RELATIVE;
	}

	/**The value of the extent.*/
	private final double value;

		/**@return The value of the extent.*/
		public double getValue() {return value;}

	/**The unit with which the extent is measured.*/
	private final Unit unit;

		/**@return The unit with which the extent is measured.*/
		public Unit getUnit() {return unit;}

	/**The degree of dimensions of the measurement.*/ 
	private final int degree;

		/**@return The degree of dimensions of the measurement.*/ 
		public int getDegree() {return degree;}

	/**Value constructor for a one-dimensional pixel extent.
	@param value The value of the extent.
	*/
	public Extent(final double value)
	{
		this(value, Unit.PIXEL);	//construct the extent using pixel units
	}

	/**Value and unit constructor with a degree of one.
	@param value The value of the extent.
	@param unit The unit with which the extent is measured.
	@exception NullPointerException if the given unit is <code>null</code>.
	*/
	public Extent(final double value, final Unit unit)
	{
		this(value, unit, 1);	//construct the extent with a degree of one
	}

	/**Value, unit, and degree constructor.
	@param value The value of the extent.
	@param unit The unit with which the extent is measured.
	@param degree The degree of dimensions of the measurement.
	@exception NullPointerException if the given unit is <code>null</code>.
	@exception IllegalArgumentException if the degree is non-positive.
	*/
	public Extent(final double value, final Unit unit, final int degree)
	{
		if(degree<=0)	//if a non-positive degree was given
		{
			throw new IllegalArgumentException("Degree must be positive.");
		}
		this.value=value;	//save the value
		this.unit=checkNull(unit, "Unit cannot be null.");	//save the unit
		this.degree=degree;	//save the degree		
	}

	/**@return A hash code value for the object.*/
	public int hashCode()
	{
		return ObjectUtilities.hashCode(getValue(), getUnit(), getDegree());	//determine the hash code
	}

	/**Indicates whether some other object is "equal to" this one.
	This implementation returns whether the object is another extent with identical value, unit, and degree, ignoring the unit if the value is zero.
	@param object The reference object with which to compare.
	@return <code>true</code> if this object is equivalent to the given object.
	*/
	public boolean equals(final Object object)
	{
		if(object instanceof Extent)	//if the object is an extent
		{
			final Extent extent=(Extent)object;	//get the object as an extent
			final double value=getValue();	//get this extent's value
			if(value==extent.getValue())	//if values match
			{
				if(getUnit()==extent.getUnit() || value==0)	//if the units match (or the value is zero, because all zero extents are equal)
				{
					return getDegree()==extent.getDegree();	//if degrees match, everything at this point matches
				}
			}
		}
		return false;	//the object did not match this extent
	}

}
