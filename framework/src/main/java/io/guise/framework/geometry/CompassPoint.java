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

package io.guise.framework.geometry;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Navigational directions.
 * @author Garret Wilson
 * @see <a href="http://en.wikipedia.org/wiki/Compass">Wikipedia: Compass</a>
 * @see <a href="http://en.wikipedia.org/wiki/Boxing_the_compass">Wikipedia: Boxing the Compass</a>
 */
public enum CompassPoint {
	/** N */
	NORTH("N", new BigDecimal("0.00"), true, false),
	/** NbE */
	NORTH_BY_EAST("NbE", new BigDecimal("11.25"), false, false),
	/** NNE */
	NORTH_NORTHEAST("NNE", new BigDecimal("22.50"), false, false),
	/** NEbN */
	NORTHEAST_BY_NORTH("NEbN", new BigDecimal("33.75"), false, false),
	/** NE */
	NORTHEAST("NE", new BigDecimal("45.00"), false, true),
	/** NEbE */
	NORTHEAST_BY_EAST("NEbE", new BigDecimal("56.25"), false, false),
	/** ENE */
	EAST_NORTHEAST("ENE", new BigDecimal("67.50"), false, false),
	/** EbN */
	EAST_BY_NORTH("EbN", new BigDecimal("78.75"), false, false),
	/** E */
	EAST("E", new BigDecimal("90.00"), true, false),
	/** EbS */
	EAST_BY_SOUTH("EbS", new BigDecimal("101.25"), false, false),
	/** ESE */
	EAST_SOUTHEAST("ESE", new BigDecimal("112.50"), false, false),
	/** SEbE */
	SOUTHEAST_BY_EAST("SEbE", new BigDecimal("123.75"), false, false),
	/** SE */
	SOUTHEAST("SE", new BigDecimal("135.00"), false, true),
	/** SEbS */
	SOUTHEAST_BY_SOUTH("SEbS", new BigDecimal("146.25"), false, false),
	/** SSE */
	SOUTH_SOUTHEAST("SSE", new BigDecimal("157.50"), false, false),
	/** SbE */
	SOUTH_BY_EAST("SbE", new BigDecimal("168.75"), false, false),
	/** S */
	SOUTH("S", new BigDecimal("180.00"), true, false),
	/** SbW */
	SOUTH_BY_WEST("SbW", new BigDecimal("191.25"), false, false),
	/** SSW */
	SOUTH_SOUTHWEST("SSW", new BigDecimal("202.50"), false, false),
	/** SWbS */
	SOUTHWEST_BY_SOUTH("SWbS", new BigDecimal("213.75"), false, false),
	/** SW */
	SOUTHWEST("SW", new BigDecimal("225.00"), false, true),
	/** SWbW */
	SOUTHWEST_BY_WEST("SWbW", new BigDecimal("236.26"), false, false),
	/** WSW */
	WEST_SOUTHWEST("WSW", new BigDecimal("247.50"), false, false),
	/** WbS */
	WEST_BY_SOUTH("WbS", new BigDecimal("258.75"), false, false),
	/** W */
	WEST("W", new BigDecimal("270.00"), true, false),
	/** WbN */
	WEST_BY_NORTH("WbN", new BigDecimal("281.25"), false, false),
	/** WNW */
	WEST_NORTHWEST("WNW", new BigDecimal("292.50"), false, false),
	/** NWbW */
	NORTHWEST_BY_WEST("NWbW", new BigDecimal("303.75"), false, false),
	/** NW */
	NORTHWEST("NW", new BigDecimal("315.00"), false, true),
	/** NWbN */
	NORTHWEST_BY_NORTH("NWbN", new BigDecimal("326.25"), false, false),
	/** NNW */
	NORTH_NORTHWEST("NNW", new BigDecimal("337.50"), false, false),
	/** NbW */
	NORTHBY_WEST("NbW", new BigDecimal("348.75"), false, false);

	/** The maximum bearing available; synonymous with {@link #NORTH}. */
	public static final BigDecimal MAX_BEARING = new BigDecimal(360);

	/** The abbreviation of this compass point. */
	private final String abbreviation;

	/** @return The abbreviation of this compass point. */
	public String getAbbreviation() {
		return abbreviation;
	}

	/** The compass direction. */
	private final BigDecimal bearing;

	/** @return The compass direction. */
	public BigDecimal getBearing() {
		return bearing;
	}

	/** Whether this compass point represents one of the four cardinal directions. */
	private final boolean cardinal;

	/** @return Whether this compass point represents one of the four cardinal directions. */
	public boolean isCardinal() {
		return cardinal;
	}

	/** Whether this compass point represents one of the four ordinal or intercardinal directions. */
	private final boolean ordinal;

	/** @return Whether this compass point represents one of the four ordinal or intercardinal directions. */
	public boolean isOrdinal() {
		return ordinal;
	}

	/**
	 * Abbreviation and bearing constructor.
	 * @param abbreviation The abbreviation of this compass point.
	 * @param bearing The compass direction.
	 * @param cardinal Whether this compass point represents one of the four cardinal directions.
	 * @param ordinal Whether this compass point represents one of the four ordinal or intercardinal directions.
	 */
	private CompassPoint(final String abbreviation, final BigDecimal bearing, final boolean cardinal, final boolean ordinal) {
		this.abbreviation = abbreviation;
		this.bearing = bearing;
		this.cardinal = cardinal;
		this.ordinal = ordinal;
	}

	/**
	 * Determines the closes compass point to the given bearing.
	 * @param bearing The bearing for which a compass point should be returned.
	 * @return The compass point closest to the given bearing.
	 * @throws IllegalArgumentException if the given bearing is greater than 360.
	 */
	public static CompassPoint getCompassPoint(final BigDecimal bearing) {
		final CompassPoint[] compassPoints = values(); //get the available compass points
		final int compassPointCount = compassPoints.length; //get the number of compass points available
		final int compassPointOrdinal = Math.round(checkBearing(bearing).divide(MAX_BEARING, RoundingMode.HALF_EVEN).multiply(new BigDecimal(compassPointCount))
				.floatValue()); //find out the ordinal of the nearest compass point: round((bearing/360)*32)
		return compassPoints[compassPointOrdinal == compassPointCount ? 0 : compassPointOrdinal]; //return the correct compass point (using NORTH for the 360 bearing at ordinal 32)		
	}

	/**
	 * Returns the ordinal or inter-cardinal compass point corresponding to 45 degrees between the given cardinal latitude and longitude compass points.
	 * @param latitudeCompassPoint The cardinal compass point of the latitude; either {@link CompassPoint#WEST} or {@link CompassPoint#EAST}.
	 * @param longitudeCompassPoint The cardinal compass point of the longitude; either {@link CompassPoint#NORTH} or {@link CompassPoint#SOUTH}.
	 * @return The ordinal compass position corresponding to 45 degrees between the given cardinal longitude and latitude compass points.
	 * @throws NullPointerException if the given latitude and/or longitude compass point is <code>null</code>.
	 * @throws IllegalArgumentException if the given latitude compass point is not {@link CompassPoint#WEST} or {@link CompassPoint#EAST}; and/or if the given
	 *           longitude compass point is not {@link CompassPoint#NORTH} or {@link CompassPoint#SOUTH}.
	 */
	public static CompassPoint getOrdinalCompassPoint(final CompassPoint latitudeCompassPoint, final CompassPoint longitudeCompassPoint) {
		switch(longitudeCompassPoint) {
			case NORTH:
				switch(latitudeCompassPoint) {
					case WEST:
						return CompassPoint.NORTHWEST;
					case EAST:
						return CompassPoint.NORTHEAST;
					default:
						throw new IllegalArgumentException("Latitude compass point " + latitudeCompassPoint + " must be either " + CompassPoint.WEST + " or "
								+ CompassPoint.WEST);
				}
			case SOUTH:
				switch(latitudeCompassPoint) {
					case WEST:
						return CompassPoint.SOUTHWEST;
					case EAST:
						return CompassPoint.SOUTHEAST;
					default:
						throw new IllegalArgumentException("Latitude compass point " + latitudeCompassPoint + " must be either " + CompassPoint.WEST + " or "
								+ CompassPoint.WEST);
				}
			default:
				throw new IllegalArgumentException("Longitude compass point " + longitudeCompassPoint + " must be either " + CompassPoint.NORTH + " or "
						+ CompassPoint.SOUTH);
		}
	}

	/**
	 * Checks to ensure that the given bearing is valid.
	 * @param bearing The bearing to check.
	 * @return The valid bearing.
	 * @throws IllegalArgumentException if the given bearing is greater than 360.
	 */
	public static BigDecimal checkBearing(final BigDecimal bearing) {
		if(bearing.compareTo(MAX_BEARING) > 0) { //if the bearing is greater than 360
			throw new IllegalArgumentException("Bearing " + bearing + " is greater than 360.");
		}
		return bearing; //return the bearing, which as been confirmed to be valid
	}
}
