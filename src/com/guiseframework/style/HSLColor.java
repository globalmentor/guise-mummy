package com.guiseframework.style;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.garretwilson.lang.IntegerUtilities.*;
import static com.garretwilson.lang.Numbers.*;
import com.garretwilson.text.ArgumentSyntaxException;

/**Encapsulates a color value of the HSL color space.
@author Garret Wilson
@see <a href="http://en.wikipedia.org/wiki/HSL_color_space">HSL color space</a>
@see <a href="http://richnewman.wordpress.com/hslcolor-class/">HSLColor Class</a>
@see <a href="http://www.easyrgb.com/math.php">EasyRGB</a>
*/
public class HSLColor extends AbstractColor<HSLColor.Component>
{

	/**A color component of HSL.*/
	public enum Component{HUE, SATURATION, LIGHTNESS;}

	/**Creates an HSL color with the specified hue in the range (0-359), and saturation, and lightness component values in the range (0.0-1.0).
	@param hue The hue component as an integer value in the range (0-359).
	@param saturation The saturation component in the range (0.0-1.0).
	@param lightness The lightness component in the range (0.0-1.0).
	@exception IllegalArgumentException if the hue is outside the range (0-359), and/or if one of the other values is outside the range (0.0-1.0).
	*/
	public HSLColor(final int hue, final double saturation, final double lightness)
	{
		this((double)checkRange(hue, 0, 359)/360.0, saturation, lightness);	//construct the hue to a relative amount and construct the class
	}

	/**Creates an HSL color with the specified hue, saturation, and lightness component values in the range (0.0-1.0).
	@param hue The hue component.
	@param saturation The saturation component.
	@param lightness The lightness component.
	@exception IllegalArgumentException if one of the values is outside the range (0.0-1.0).
	*/
	public HSLColor(final double hue, final double saturation, final double lightness)
	{
		super(hue, saturation, lightness);	//construct the parent class
	}

	/**@return The hue component value.*/
	public double getHue()
	{
		return getComponent(Component.HUE);	//return hue component
	}

  /**Determines the value of the hue in degrees in the range (0.0-360.0].
	For example, retrieving a hue with value 0.5 would produce 180.
	@return The absolute value of the hue in degrees.
	*/
  public double getHueDegrees()
  {
  	return Math.round(getHue()*360.0);	//multiply the hue by the degree range
  }

	/**@return The saturation component value.*/
	public double getSaturation()
	{
		return getComponent(Component.SATURATION);	//return the saturation component
	}

	/**@return The lightness component value.*/
	public double getLightness()
	{
		return getComponent(Component.LIGHTNESS);	//return the lightness component
	}

	/**@return The color in the HSL color space.*/
	public HSLColor asHSL()
	{
		return this;	//this color object is already an HSL color
	}

	/**Converts this HSL color to an RGB color.
	@return The color in the RGB color space.
	@see <a href="http://en.wikipedia.org/wiki/HSL_color_space">HSL color space: Converting to RGB</a>
	@see <a href="http://www.easyrgb.com/math.php?MATH=M19#text19">EasyRGB HSL->RGB</a>
	*/
	public RGBColor asRGB()
	{
		final double hue=getHue();	//get the HSL values
		final double saturation=getSaturation();
		final double lightness=getLightness();
		final double red, green, blue;	//we'll calculate the RGB values
		if(saturation==0)	//if there is no saturation
		{
			red=green=blue=lightness;	//red, green, and blue are each equal to the lightness
		}
		else	//if there is saturation
		{
			final double q=lightness>=0.5 ? lightness*(1.0+saturation) : lightness+saturation-(lightness*saturation);
			final double p=2.0*lightness-q;
			red=getHueRGBComponent(hue+ONE_THIRD_DOUBLE, p, q);
			green=getHueRGBComponent(hue, p, q);
			blue=getHueRGBComponent(hue-ONE_THIRD_DOUBLE, p, q);
		}
		return new RGBColor(red, green, blue);	//return a new RGB color from our values
	}

	/**Determines the RGB component value for a particular hue.
	@param hue The hue, which appears in the (0.0-1.0) range +/- 1.0.
	@see <a href="http://en.wikipedia.org/wiki/HSL_color_space">HSL color space: Converting to RGB</a>
	@see <a href="http://www.easyrgb.com/math.php?MATH=M19#text19">EasyRGB HSL->RGB</a>
	@return The new RGB component value based upon the given hue and parameters
	*/
	private static double getHueRGBComponent(double hue, final double p, final double q)
	{
		if(hue<0.0)	//if hue is below zero
		{
			hue+=1;	//place the hue back in the range
		}
		else if(hue>1.0)	//if the hue is greater than one
		{
			hue-=1;	//place the hue back in the range			
		}
		if(6*hue<1.0)
		{
			return p+((q-p)*6.0*hue);
		}
		else if(2*hue<1.0)
		{
			return q;
		}
		else if(3*hue<2.0)
		{
			return p+((q-p)*((2/3)-hue)*6.0);
		}
		else
		{
			return p;
		}
	}

	/**A regular expression pattern matching <code>hsl(<var>hue</var>,<var>saturation</var>,<var>lightness</var>)</code>, with the first three groups representing the three HSL character sequences.*/
	private final static Pattern HSL_ABSOLUTE_FUNCTION_PATTERN=Pattern.compile("hsl\\((\\d{0,3}),\\s*(\\d{0,3}),\\s*(\\d{0,3})\\)");
	/**A regular expression pattern matching <code>hsl(<var>hue</var>%,<var>saturation</var>%,<var>lightness</var>%)</code>, with the first three groups representing the three HSL character sequences.*/
	private final static Pattern HSL_PERCENT_FUNCTION_PATTERN=Pattern.compile("hsl\\(([\\d\\.]+)%,\\s*([\\d\\.]+)%,\\s*([\\d\\.]+)%\\)");

	/**Creates an HSL color from a string representation.
	This representation can be in one of the following forms:
	<ul>
		<li><code>hsl(<var>hue</var>,<var>saturation</var>,<var>lightness</var>)</code>, with decimal representation with a depth of eight bits (0-255).</li>
		<li><code>hsl(<var>hue</var>%,<var>saturation</var>%,<var>lightness</var>%)</code>, with decimal component values multiplied by 100 (0.0-100.0%).</li>
	</ul>
	@param charSequence The character sequence representation of an HSL color. 
	@return An RGB color object representing the color represented by the given string.
	@exception NullPointerException if the given string is <code>null</code>.
	@exception IllegalArgumentException if a color cannot be determined from the given string. 
	*/
	public static HSLColor valueOf(final CharSequence charSequence)
	{
		final Matcher hslAbsoluteFunctionMatcher=HSL_ABSOLUTE_FUNCTION_PATTERN.matcher(charSequence);	//match against hsl(h, s, l)
		if(hslAbsoluteFunctionMatcher.matches())	//if the character sequence matches
		{
			return new HSLColor(Integer.parseInt(hslAbsoluteFunctionMatcher.group(1), 10), Integer.parseInt(hslAbsoluteFunctionMatcher.group(2), 10), Integer.parseInt(hslAbsoluteFunctionMatcher.group(3), 10));	//extract the HSL values and return a new color
		}
		final Matcher hslPercentFunctionMatcher=HSL_PERCENT_FUNCTION_PATTERN.matcher(charSequence);	//match against hsl(h%, s%, l%)
		if(hslPercentFunctionMatcher.matches())	//if the character sequence matches
		{
			return new HSLColor(Double.parseDouble(hslPercentFunctionMatcher.group(1))/100, Double.parseDouble(hslPercentFunctionMatcher.group(2))/100, Double.parseDouble(hslPercentFunctionMatcher.group(3))/100);	//extract the HSL values and return a new color
		}
		throw new ArgumentSyntaxException("Character sequence "+charSequence+" does not represent an HSL color.");
	}
}
