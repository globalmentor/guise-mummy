package com.guiseframework.geometry;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**Navigational directions.
@author Garret Wilson
@see http://en.wikipedia.org/wiki/Compass
@see http://en.wikipedia.org/wiki/Boxing_the_compass
*/
public enum CompassPoint
{
	NORTH("N", new BigDecimal("0.00")),
	NORTH_BY_EAST("NbE", new BigDecimal("11.25")),
	NORTH_NORTHEAST("NNE", new BigDecimal("22.50")),
	NORTHEAST_BY_NORTH("NEbN", new BigDecimal("33.75")),
	NORTHEAST("NE", new BigDecimal("45.00")),
	NORTHEAST_BY_EAST("NEbE", new BigDecimal("56.25")),
	EAST_NORTHEAST("ENE", new BigDecimal("67.50")),
	EAST_BY_NORTH("EbN", new BigDecimal("78.75")),
	EAST("E", new BigDecimal("90.00")),
	EAST_BY_SOUTH("EbS", new BigDecimal("101.25")),
	EAST_SOUTHEAST("ESE", new BigDecimal("112.50")),
	SOUTHEAST_BY_EAST("SEbE", new BigDecimal("123.75")),
	SOUTHEAST("SE", new BigDecimal("135.00")),
	SOUTHEAST_BY_SOUTH("SEbS", new BigDecimal("146.25")),
	SOUTH_SOUTHEAST("SSE", new BigDecimal("157.50")),
	SOUTH_BY_EAST("SbE", new BigDecimal("168.75")),
	SOUTH("S", new BigDecimal("180.00")),
	SOUTH_BY_WEST("SbW", new BigDecimal("191.25")),
	SOUTH_SOUTHWEST("SSW", new BigDecimal("202.50")),
	SOUTHWEST_BY_SOUTH("SWbS", new BigDecimal("213.75")),
	SOUTHWEST("SW", new BigDecimal("225.00")),
	SOUTHWEST_BY_WEST("SWbW", new BigDecimal("236.26")),
	WEST_SOUTHWEST("WSW", new BigDecimal("247.50")),
	WEST_BY_SOUTH("WbS", new BigDecimal("258.75")),
	WEST("W", new BigDecimal("270.00")),
	WEST_BY_NORTH("WbN", new BigDecimal("281.25")),
	WEST_NORTHWEST("WNW", new BigDecimal("292.50")),
	NORTHWEST_BY_WEST("NWbW", new BigDecimal("303.75")),
	NORTHWEST("NW", new BigDecimal("315.00")),
	NORTHWEST_BY_NORTH("NWbN", new BigDecimal("326.25")),
	NORTH_NORTHWEST("NNW", new BigDecimal("337.50")),
	NORTHBY_WEST("NbW", new BigDecimal("348.75"));	

	/**The maximum bearing available; synonymous with {@link #NORTH}.*/
	public final static BigDecimal MAX_BEARING=new BigDecimal(360);

	/**The abbreviation of this compass point.*/
	private final String abbreviation;

		/**@return The abbreviation of this compass point.*/
		public String getAbbreviation() {return abbreviation;}

	/**The compass direction.*/
	private final BigDecimal bearing;

		/**@return The compass direction.*/
		public BigDecimal getBearing() {return bearing;}

	/**Abbreviation and bearing constructor.	
	@param abbreviation The abbreviation of this compass point.
	@param bearing The compass direction.
	*/
	private CompassPoint(final String abbreviation, final BigDecimal bearing)
	{
		this.abbreviation=abbreviation;
		this.bearing=bearing;
	}

	/**Determines the closes compass point to the given bearing.
	@param bearing The bearing for which a compass point should be returned.
	@return The compass point closest to the given bearing.
	@exception IllegalArgumentException if the given bearing is greater than 360.
	*/
	public static CompassPoint getCompassPoint(final BigDecimal bearing)
	{
		final CompassPoint[] compassPoints=values();	//get the available compass points
		final int compassPointCount=compassPoints.length;	//get the number of compass points available
		final int compassPointOrdinal=Math.round(checkBearing(bearing).divide(MAX_BEARING, RoundingMode.HALF_EVEN).multiply(new BigDecimal(compassPointCount)).floatValue());	//find out the ordinal of the nearest compass point: round((bearing/360)*32)
		return compassPoints[compassPointOrdinal==compassPointCount ? 0 : compassPointOrdinal];	//return the correct compass point (using NORTH for the 360 bearing at ordinal 32)		
	}

	/**Checks to ensure that the given bearing is valid.
	@param bearing The bearing to check.
	@return The valid bearing.
	@exception IllegalArgumentException if the given bearing is greater than 360.
	*/
	public static BigDecimal checkBearing(final BigDecimal bearing)
	{
		if(bearing.compareTo(MAX_BEARING)>0)	//if the bearing is greater than 360
		{
			throw new IllegalArgumentException("Bearing "+bearing+" is greater than 360.");
		}
		return bearing;	//return the bearing, which as been confirmed to be valid
	}
}
