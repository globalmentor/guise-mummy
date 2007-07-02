package com.guiseframework.style;

/**Representation of a color.
@author Garret Wilson
*/
public interface Color
{

	/**@return The color in the HSL color space.*/
  public HSLColor asHSL();

	/**@return The color in the RGB color space.*/
  public RGBColor asRGB();

}
