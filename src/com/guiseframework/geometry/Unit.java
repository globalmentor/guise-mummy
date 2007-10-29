package com.guiseframework.geometry;

/**A unit of measurement.
@author Garret Wilson
*/
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