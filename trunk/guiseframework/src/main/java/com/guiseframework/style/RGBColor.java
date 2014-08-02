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

package com.guiseframework.style;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.*;

import static com.globalmentor.java.Maths.*;
import static com.globalmentor.java.Numbers.*;

import com.globalmentor.text.ArgumentSyntaxException;

/**Encapsulates a color value of the sRGB color space.
@author Garret Wilson
@see <a href="http://www.w3.org/pub/WWW/Graphics/Color/sRGB.html">A Standard Default Color Space for the Internet - sRGB</a>
@see <a href="http://www.w3.org/TR/css3-color/">CSS 3 Color Module</a>
@see <a href="http://en.wikipedia.org/wiki/X11_Color_Names">X11 color names</a>
@see <a href="http://msdn.microsoft.com/library/default.asp?url=/library/en-us/dnwebgen/html/X11_names.asp">Colors By Name</a>
@see <a href="http://www.w3schools.com/html/html_colornames.asp">HTML Color Names</a>
@see <a href="http://www.w3.org/TR/css3-color/#svg-color">CSS 3 Color Module: SVG color keywords</a>
@see <a href="http://www.w3.org/TR/SVG11/types.html#ColorKeywords">SVG 1.1: Recognized color keyword names</a>
@see <a href="http://lists.w3.org/Archives/Public/www-svg/2002Apr/0052.html">Re: color names in SVG-1.0 conflict with /usr/lib/X11/rgb.txt</a>
*/
public class RGBColor extends AbstractModeledColor<RGBColor.Component>
{

	/**The shared map of RGB colors keyed to lowercase, non-delimited color names, for reading only.*/
	private final static Map<String, RGBColor> namedColorMap;

	/**A color component of sRGB.*/
	public enum Component implements ModeledColor.Component
	{
		ALPHA,
		RED,
		GREEN,
		BLUE;
	}

	/**Creates an RGB color from the specified string representation.
	This representation can be in one of the following forms:
	<ul>
		<li><code><var>colorname</var></code>, one of the {@link <a href="http://www.w3schools.com/html/html_colornames.asp">HTML color names</a>}, which must be in all lowercase without delimiters, such as "aliceblue".</li>
		<li><code>#<var>rgb</var></code>, with hexadecimal representation of color components without regard to case.</li>
		<li><code>#<var>rrggbb</var></code>, with hexadecimal representation of color components without regard to case.</li>
	</ul>
	This method also recognizes the <code>transparent</code> color name as equivalent to rgba(0, 0, 0, 0), or black with zero alpha.
	In most instances, the {@link #valueOf(CharSequence)} static method is preferred for string-based construction, as it allows singleton instances of named RGB colors to be used.
	@param charSequence The character sequence representation of a color, either a lowercase name of a standard HTML color, or a three or six-digit hex code beginning with '#'. 
	@throws NullPointerException if the given string is <code>null</code>.
	@throws IllegalArgumentException if a color cannot be determined from the given string. 
	*/
	public RGBColor(final CharSequence charSequence)	//TODO probably change back to a String constructor
	{
		super(valueOf(charSequence).getValues());	//create a new color and construct this class from the color values of that color TODO create another static function that obviates the need to construct another RGBColor if the values can be determined
	}

	/**Creates an opaque sRGB color from the specified sRGB color value.
	Any alpha present is ignored and replaced with alpha of 0xFF.
	@param rgb The color value composed of 8 bits each of red, green, and blue.
	*/
	public RGBColor(final int rgb)
	{
		this(rgb, false);	//construct the class from the value with no alpha
	}

	/**Creates an sRGB color from the specified sRGB color value and alpha indication.
	@param argb The color value composed of 8 bits each of alpha, red, green, and blue (0x<var>AARRGGBB</var>).
	@param hasAlpha <code>true</code> if the given value contains alpha information, else <code>false</code> if default opaque alpha should be added.
	*/
	public RGBColor(final int argb, final boolean hasAlpha)
	{
		this((argb>>16) & 0xFF, (argb>>8) & 0xFF, (argb>>0) & 0xFF, hasAlpha ? (argb>>24) & 0xFF : 0xFF);	//construct the class with the absolute component values, defaulting to full alpha if none is present
	}

	/**Creates an opaque sRGB color with the specified absolute red, green, and blue component values.
	The alpha component will be set to its maximum (0xFF).
	@param red The red component on an absolute scale in the range (0x00-0xFF).
	@param green The green component on an absolute scale in the range (0x00-0xFF).
	@param blue The blue component on an absolute scale in the range (0x00-0xFF).
	@throws IllegalArgumentException if one of the values is outside the range (0x00-0xFF).
	*/
	public RGBColor(final int red, final int green, final int blue)
	{
		this(red, green, blue, 0xFF);	//construct the color with full alpha
	}

	/**Creates an sRGB color with the specified absolute red, green, blue, and alpha component values.
	@param red The red component on an absolute scale in the range (0x00-0xFF).
	@param green The green component on an absolute scale in the range (0x00-0xFF).
	@param blue The blue component on an absolute scale in the range (0x00-0xFF).
	@param alpha The alpha component on an absolute scale in the range (0x00-0xFF).
	@throws IllegalArgumentException if one of the values is outside the range (0x00-0xFF).
	*/
	public RGBColor(final int red, final int green, final int blue, final int alpha)
	{
		this((double)red/0xFF, (double)green/0xFF, (double)blue/0xFF, (double)alpha/0xFF);	//convert the components into relative amounts
	}

	/**Creates an opaque sRGB color with the specified red, green, and blue component values.
	The alpha component will be set to its maximum (1.0).
	@param red The red component.
	@param green The green component.
	@param blue The blue component.
	@throws IllegalArgumentException if one of the values is outside the range (0.0-1.0).
	*/
	public RGBColor(final double red, final double green, final double blue)
	{
		this(red, green, blue, 1.0);	//construct the color with full alpha
	}

	/**Creates an sRGB color with the specified red, green, blue, and alpha component values.
	@param red The red component.
	@param green The green component.
	@param blue The blue component.
	@param alpha The alpha component.
	@throws IllegalArgumentException if one of the values is outside the range (0.0-1.0).
	*/
	public RGBColor(final double red, final double green, final double blue, final double alpha)
	{
		super(alpha, red, green, blue);	//construct the parent class
	}

	/**@return The red component value.*/
	public double getRed()
	{
		return getComponent(Component.RED);	//return red component
	}

	/**@return The absolute red value at a depth of 8 bits.*/
	public int getAbsoluteRed8()
	{
		return (int)getAbsoluteComponent(Component.RED, 8);	//return the absolute red component at 8 bits
	}

	/**@return The green component value.*/
	public double getGreen()
	{
		return getComponent(Component.GREEN);	//return the green component
	}

	/**@return The absolute green value at a depth of 8 bits.*/
	public int getAbsoluteGreen8()
	{
		return (int)getAbsoluteComponent(Component.GREEN, 8);	//return the absolute green component at 8 bits
	}

	/**@return The blue component value.*/
	public double getBlue()
	{
		return getComponent(Component.BLUE);	//return the blue component
	}

	/**@return The absolute blue value at a depth of 8 bits.*/
	public int getAbsoluteBlue8()
	{
		return (int)getAbsoluteComponent(Component.BLUE, 8);	//return the absolute blue component at 8 bits
	}

	/**@return The alpha component value.*/
	public double getAlpha()
	{
		return getComponent(Component.ALPHA);	//return the alpha component
	}

	/**@return The absolute alpha value at a depth of 8 bits.*/
	public int getAbsoluteAlpha8()
	{
		return (int)getAbsoluteComponent(Component.ALPHA, 8);	//return the absolute alpha component at 8 bits
	}

	/**Converts this RGB color to an HSL color.
	@return The color in the HSL color space.
	@see <a href="http://en.wikipedia.org/wiki/HSL_color_space">HSL color space: Converting to RGB</a>
	@see <a href="http://www.easyrgb.com/math.php?MATH=M18#text18">EasyRGB RGB->HSL</a>
	*/
	public HSLColor asHSL()
	{
		final double red=getRed();	//get the RGB values
		final double green=getGreen();
		final double blue=getBlue();
		final double hue, saturation, lightness;	//we'll calculate the HSL values
		final double min=min(red, green, blue);	//get the minimum RGB component
		final double max=max(red, green, blue);	//get the maximum RGB component
		final double delta=max-min;	//get the delta RGB value
		lightness=(max+min)/2;
		if(delta==0)	//if there is no difference between the least and greatest RGB component values, this is gray with no chroma
		{
			hue=0;
			saturation=0;
		}
		else	//calculate chromatic data
		{
			saturation=lightness<0.5 ? delta/(max+min) : delta/(2-(max+min));	//Wikipedia uses <=; easyRGB uses <; with zero-based values < makes more sense
			double deltaRed=(((max-red)/6)+(delta/2))/delta;
			double deltaGreen=(((max-green)/6)+(delta/2))/delta;
			double deltaBlue=(((max-blue)/6)+(delta/2))/delta;
			double tempHue;	//determine a temporary hue value and then normalize it
			if(red==max)
			{
				tempHue=deltaBlue-deltaGreen;
			}
			else if(green==max)
			{
				tempHue=ONE_THIRD_DOUBLE+deltaRed-deltaBlue;
			}
			else if(blue==max)
			{
				tempHue=TWO_THIRDS_DOUBLE+deltaGreen-deltaRed;
			}
			else
			{
				throw new AssertionError("No color component matched maximum value "+max);
			}
			if(tempHue<0)	//normalize the hue value
			{
				hue=tempHue+1;
			}
			else if(tempHue>1)
			{
				hue=tempHue-1;
			}
			else	//if the hue is already normalized
			{
				hue=tempHue;	//use the hue as-is
			}
		}
		return new HSLColor(hue, saturation, lightness);	//return the HSL color	
	}

	/**@return The color in the RGB color space.*/
	public RGBColor asRGB()
	{
		return this;	//this color object is already an RGB color
	}

	/**A regular expression pattern matching <code>#<var>rgb</var></code>, with the first three groups representing the three RGB character sequences.*/
	private final static Pattern RGB_PATTERN=Pattern.compile("#(\\p{XDigit})(\\p{XDigit})(\\p{XDigit})");
	/**A regular expression pattern matching <code>#<var>rrggbb</var></code>, with the first three groups representing the three RGB character sequences.*/
	private final static Pattern RRGGBB_PATTERN=Pattern.compile("#(\\p{XDigit}{2})(\\p{XDigit}{2})(\\p{XDigit}{2})");
	/**A regular expression pattern matching <code>rgb(<var>red</var>,<var>green</var>,<var>blue</var>)</code>, with the first three groups representing the three RGB character sequences.*/
	private final static Pattern RGB_ABSOLUTE_FUNCTION_PATTERN=Pattern.compile("rgb\\((\\d{0,3}),\\s*(\\d{0,3}),\\s*(\\d{0,3})\\)");
	/**A regular expression pattern matching <code>rgb(<var>red</var>%,<var>green</var>%,<var>blue</var>%)</code>, with the first three groups representing the three RGB character sequences.*/
	private final static Pattern RGB_PERCENT_FUNCTION_PATTERN=Pattern.compile("rgb\\(([\\d\\.]+)%,\\s*([\\d\\.]+)%,\\s*([\\d\\.]+)%\\)");
	/**A regular expression pattern matching <code>rgba(<var>red</var>,<var>green</var>,<var>blue</var>,<var>alpha</var>)</code>, with the first four groups representing the three RGB character sequences.*/
	private final static Pattern RGBA_ABSOLUTE_FUNCTION_PATTERN=Pattern.compile("rgb\\((\\d{0,3}),\\s*(\\d{0,3}),\\s*(\\d{0,3}),\\s*(\\d{0,3})\\)");
	/**A regular expression pattern matching <code>rgba(<var>red</var>,<var>green</var>%,<var>blue</var>%,<var>alpha</var>%)</code>, with the first four groups representing the three RGB character sequences.*/
	private final static Pattern RGBA_PERCENT_FUNCTION_PATTERN=Pattern.compile("rgb\\(([\\d\\.]+)%,\\s*([\\d\\.]+)%,\\s*([\\d\\.]+)%,\\s*([\\d\\.]+)%\\)");

	/**Creates an RGB color from a string representation.
	This representation can be in one of the following forms:
	<ul>
		<li><code><var>colorname</var></code>, one of the {@link <a href="http://www.w3schools.com/html/html_colornames.asp">HTML color names</a>}, which must be in all lowercase without delimiters, such as "aliceblue".</li>
		<li><code>#<var>rgb</var></code>, with hexadecimal representation of color components without regard to case.</li>
		<li><code>#<var>rrggbb</var></code>, with hexadecimal representation of color components without regard to case.</li>
		<li><code>rgb(<var>red</var>,<var>green</var>,<var>blue</var>)</code>, with decimal representation with a depth of eight bits (0-255).</li>
		<li><code>rgb(<var>red</var>%,<var>green</var>%,<var>blue</var>%)</code>, with decimal component values multiplied by 100 (0.0-100.0%).</li>
		<li><code>rgba(<var>red</var>,<var>green</var>,<var>blue</var>,<var>alpha</var>)</code>, with decimal representation with a depth of eight bits (0-255).</li>
		<li><code>rgba(<var>red</var>%,<var>green</var>%,<var>blue</var>%,<var>alpha</var>%)</code>, with decimal component values multiplied by 100 (0.0%-100.0%).</li>
	</ul>
	This method also recognizes the <code>transparent</code> color name as equivalent to <code>rgba(0, 0, 0, 0)</code>, or black with zero alpha.
	@param charSequence The character sequence representation of an RGB color. 
	@return An RGB color object representing the color represented by the given string.
	@throws NullPointerException if the given string is <code>null</code>.
	@throws IllegalArgumentException if a color cannot be determined from the given string. 
	*/
	public static RGBColor valueOf(final CharSequence charSequence)
	{
		final Matcher rgbMatcher=RGB_PATTERN.matcher(charSequence);	//match against #rgb
		if(rgbMatcher.matches())	//if the character sequence matches
		{
			final int rNibble=Integer.parseInt(rgbMatcher.group(1), 16);	//extract the four-bit RGB values
			final int gNibble=Integer.parseInt(rgbMatcher.group(2), 16);
			final int bNibble=Integer.parseInt(rgbMatcher.group(3), 16);
			return new RGBColor(rNibble<<4+rNibble, gNibble<<4+gNibble, bNibble*4+bNibble);	//convert the RGB values to byte-byte sized and return a new color
		}
		final Matcher rrggbbMatcher=RRGGBB_PATTERN.matcher(charSequence);	//match against #rrggbb
		if(rrggbbMatcher.matches())	//if the character sequence matches
		{
			return new RGBColor(Integer.parseInt(rrggbbMatcher.group(1), 16), Integer.parseInt(rrggbbMatcher.group(2), 16), Integer.parseInt(rrggbbMatcher.group(3), 16));	//extract the RGB values and return a new color
		}
		final RGBColor namedColor=namedColorMap.get(charSequence.toString());	//try to look up a named color
		if(namedColor!=null)	//if there is a matching color name
		{
			return namedColor;	//return the named color
		}
		final Matcher rgbAbsoluteFunctionMatcher=RGB_ABSOLUTE_FUNCTION_PATTERN.matcher(charSequence);	//match against rgb(r, g, b)
		if(rgbAbsoluteFunctionMatcher.matches())	//if the character sequence matches
		{
			return new RGBColor(Integer.parseInt(rgbAbsoluteFunctionMatcher.group(1), 10), Integer.parseInt(rgbAbsoluteFunctionMatcher.group(2), 10), Integer.parseInt(rgbAbsoluteFunctionMatcher.group(3), 10));	//extract the RGB values and return a new color
		}
		final Matcher rgbPercentFunctionMatcher=RGB_PERCENT_FUNCTION_PATTERN.matcher(charSequence);	//match against rgb(r%, g%, b%)
		if(rgbPercentFunctionMatcher.matches())	//if the character sequence matches
		{
			return new RGBColor(Double.parseDouble(rgbPercentFunctionMatcher.group(1))/100, Double.parseDouble(rgbPercentFunctionMatcher.group(2))/100, Double.parseDouble(rgbPercentFunctionMatcher.group(3))/100);	//extract the RGB values and return a new color
		}
		final Matcher rgbaAbsoluteFunctionMatcher=RGBA_ABSOLUTE_FUNCTION_PATTERN.matcher(charSequence);	//match against rgb(r, g, b, a)
		if(rgbaAbsoluteFunctionMatcher.matches())	//if the character sequence matches
		{
			return new RGBColor(Integer.parseInt(rgbaAbsoluteFunctionMatcher.group(1), 10), Integer.parseInt(rgbaAbsoluteFunctionMatcher.group(2), 10), Integer.parseInt(rgbaAbsoluteFunctionMatcher.group(3), 10), Integer.parseInt(rgbaAbsoluteFunctionMatcher.group(4), 10));	//extract the RGBA values and return a new color
		}
		final Matcher rgbaPercentFunctionMatcher=RGBA_PERCENT_FUNCTION_PATTERN.matcher(charSequence);	//match against rgba(r%, g%, b%)
		if(rgbaPercentFunctionMatcher.matches())	//if the character sequence matches
		{
			return new RGBColor(Double.parseDouble(rgbaPercentFunctionMatcher.group(1))/100, Double.parseDouble(rgbaPercentFunctionMatcher.group(2))/100, Double.parseDouble(rgbaPercentFunctionMatcher.group(3))/100, Double.parseDouble(rgbaPercentFunctionMatcher.group(4))/100);	//extract the RGBA values and return a new color
		}
		throw new ArgumentSyntaxException("Character sequence "+charSequence+" does not represent an RGB color.");
	}

	public final static RGBColor TRANSPARENT=new RGBColor(0x00000000, true);

	public final static RGBColor ALICE_BLUE=new RGBColor(0xF0F8FF);
	public final static RGBColor ANTIQUE_WHITE=new RGBColor(0xFAEBD7);
	public final static RGBColor AQUA=new RGBColor(0x00FFFF);
	public final static RGBColor AQUA_MARINE=new RGBColor(0x7FFFD4);
	public final static RGBColor AZURE=new RGBColor(0xF0FFFF);
	public final static RGBColor BEIGE=new RGBColor(0xF5F5DC);
	public final static RGBColor BISQUE=new RGBColor(0xFFE4C4);
	public final static RGBColor BLACK=new RGBColor(0x000000);
	public final static RGBColor BLANCHE_DALMOND=new RGBColor(0xFFEBCD);
	public final static RGBColor BLUE=new RGBColor(0x0000FF);
	public final static RGBColor BLUE_VIOLET=new RGBColor(0x8A2BE2);
	public final static RGBColor BROWN=new RGBColor(0xA52A2A);
	public final static RGBColor BURLY_WOOD=new RGBColor(0xDEB887);
	public final static RGBColor CADET_BLUE=new RGBColor(0x5F9EA0);
	public final static RGBColor CHARTREUSE=new RGBColor(0x7FFF00);
	public final static RGBColor CHOCOLATE=new RGBColor(0xD2691E);
	public final static RGBColor CORAL=new RGBColor(0xFF7F50);
	public final static RGBColor CORNFLOWER_BLUE=new RGBColor(0x6495ED);
	public final static RGBColor CORNSILK=new RGBColor(0xFFF8DC);
	public final static RGBColor CRIMSON=new RGBColor(0xDC143C);
	public final static RGBColor CYAN=new RGBColor(0x00FFFF);
	public final static RGBColor DARK_BLUE=new RGBColor(0x00008B);
	public final static RGBColor DARK_CYAN=new RGBColor(0x008B8B);
	public final static RGBColor DARK_GOLDEN_ROD=new RGBColor(0xB8860B);
	public final static RGBColor DARK_GRAY=new RGBColor(0xA9A9A9);
	public final static RGBColor DARK_GREEN=new RGBColor(0x006400);
	public final static RGBColor DARK_KHAKI=new RGBColor(0xBDB76B);
	public final static RGBColor DARK_MAGENTA=new RGBColor(0x8B008B);
	public final static RGBColor DARK_OLIVE_GREEN=new RGBColor(0x556B2F);
	public final static RGBColor DARK_ORANGE=new RGBColor(0xFF8C00);
	public final static RGBColor DARK_ORCHID=new RGBColor(0x9932CC);
	public final static RGBColor DARK_RED=new RGBColor(0x8B0000);
	public final static RGBColor DARK_SALMON=new RGBColor(0xE9967A);
	public final static RGBColor DARK_SEA_GREEN=new RGBColor(0x8FBC8F);
	public final static RGBColor DARK_SLATE_BLUE=new RGBColor(0x483D8B);
	public final static RGBColor DARK_SLATE_GRAY=new RGBColor(0x2F4F4F);
	public final static RGBColor DARK_TURQUOISE=new RGBColor(0x00CED1);
	public final static RGBColor DARK_VIOLET=new RGBColor(0x9400D3);
	public final static RGBColor DEEP_PINK=new RGBColor(0xFF1493);
	public final static RGBColor DEEP_SKY_BLUE=new RGBColor(0x00BFFF);
	public final static RGBColor DIM_GRAY=new RGBColor(0x696969);
	public final static RGBColor DODGER_BLUE=new RGBColor(0x1E90FF);
	public final static RGBColor FELDSPAR=new RGBColor(0xD19275);
	public final static RGBColor FIRE_BRICK=new RGBColor(0xB22222);
	public final static RGBColor FLORAL_WHITE=new RGBColor(0xFFFAF0);
	public final static RGBColor FOREST_GREEN=new RGBColor(0x228B22);
	public final static RGBColor FUCHSIA=new RGBColor(0xFF00FF);
	public final static RGBColor GAINSBORO=new RGBColor(0xDCDCDC);
	public final static RGBColor GHOST_WHITE=new RGBColor(0xF8F8FF);
	public final static RGBColor GOLD=new RGBColor(0xFFD700);
	public final static RGBColor GOLDEN_ROD=new RGBColor(0xDAA520);
	public final static RGBColor GRAY=new RGBColor(0x808080);
	public final static RGBColor GREEN=new RGBColor(0x008000);
	public final static RGBColor GREEN_YELLOW=new RGBColor(0xADFF2F);
	public final static RGBColor HONEY_DEW=new RGBColor(0xF0FFF0);
	public final static RGBColor HOT_PINK=new RGBColor(0xFF69B4);
	public final static RGBColor INDIAN_RED=new RGBColor(0xCD5C5C);
	public final static RGBColor INDIGO=new RGBColor(0x4B0082);
	public final static RGBColor IVORY=new RGBColor(0xFFFFF0);
	public final static RGBColor KHAKI=new RGBColor(0xF0E68C);
	public final static RGBColor LAVENDER=new RGBColor(0xE6E6FA);
	public final static RGBColor LAVENDER_BLUSH=new RGBColor(0xFFF0F5);
	public final static RGBColor LAWN_GREEN=new RGBColor(0x7CFC00);
	public final static RGBColor LEMON_CHIFFON=new RGBColor(0xFFFACD);
	public final static RGBColor LIGHT_BLUE=new RGBColor(0xADD8E6);
	public final static RGBColor LIGHT_CORAL=new RGBColor(0xF08080);
	public final static RGBColor LIGHT_CYAN=new RGBColor(0xE0FFFF);
	public final static RGBColor LIGHT_GOLDEN_ROD_YELLOW=new RGBColor(0xFAFAD2);
	public final static RGBColor LIGHT_GREY=new RGBColor(0xD3D3D3);
	public final static RGBColor LIGHT_GREEN=new RGBColor(0x90EE90);
	public final static RGBColor LIGHT_PINK=new RGBColor(0xFFB6C1);
	public final static RGBColor LIGHT_SALMON=new RGBColor(0xFFA07A);
	public final static RGBColor LIGHT_SEA_GREEN=new RGBColor(0x20B2AA);
	public final static RGBColor LIGHT_SKY_BLUE=new RGBColor(0x87CEFA);
	public final static RGBColor LIGHT_SLATE_BLUE=new RGBColor(0x8470FF);
	public final static RGBColor LIGHT_SLATE_GRAY=new RGBColor(0x778899);
	public final static RGBColor LIGHT_STEEL_BLUE=new RGBColor(0xB0C4DE);
	public final static RGBColor LIGHT_YELLOW=new RGBColor(0xFFFFE0);
	public final static RGBColor LIME=new RGBColor(0x00FF00);
	public final static RGBColor LIME_GREEN=new RGBColor(0x32CD32);
	public final static RGBColor LINEN=new RGBColor(0xFAF0E6);
	public final static RGBColor MAGENTA=new RGBColor(0xFF00FF);
	public final static RGBColor MAROON=new RGBColor(0x800000);
	public final static RGBColor MEDIUM_AQUA_MARINE=new RGBColor(0x66CDAA);
	public final static RGBColor MEDIU_MBLUE=new RGBColor(0x0000CD);
	public final static RGBColor MEDIUM_ORCHID=new RGBColor(0xBA55D3);
	public final static RGBColor MEDIUM_PURPLE=new RGBColor(0x9370D8);
	public final static RGBColor MEDIUM_SEA_GREEN=new RGBColor(0x3CB371);
	public final static RGBColor MEDIUM_SLATE_BLUE=new RGBColor(0x7B68EE);
	public final static RGBColor MEDIUM_SPRING_GREEN=new RGBColor(0x00FA9A);
	public final static RGBColor MEDIUM_TURQUOISE=new RGBColor(0x48D1CC);
	public final static RGBColor MEDIUM_VIOLET_RED=new RGBColor(0xC71585);
	public final static RGBColor MIDNIGHT_BLUE=new RGBColor(0x191970);
	public final static RGBColor MINT_CREAM=new RGBColor(0xF5FFFA);
	public final static RGBColor MISTY_ROSE=new RGBColor(0xFFE4E1);
	public final static RGBColor MOCCASIN=new RGBColor(0xFFE4B5);
	public final static RGBColor NAVAJO_WHITE=new RGBColor(0xFFDEAD);
	public final static RGBColor NAVY=new RGBColor(0x000080);
	public final static RGBColor OLD_LACE=new RGBColor(0xFDF5E6);
	public final static RGBColor OLIVE=new RGBColor(0x808000);
	public final static RGBColor OLIVE_DRAB=new RGBColor(0x6B8E23);
	public final static RGBColor ORANGE=new RGBColor(0xFFA500);
	public final static RGBColor ORANGE_RED=new RGBColor(0xFF4500);
	public final static RGBColor ORCHID=new RGBColor(0xDA70D6);
	public final static RGBColor PALE_GOLDEN_ROD=new RGBColor(0xEEE8AA);
	public final static RGBColor PALE_GREEN=new RGBColor(0x98FB98);
	public final static RGBColor PALE_TURQUOISE=new RGBColor(0xAFEEEE);
	public final static RGBColor PALE_VIOLET_RED=new RGBColor(0xD87093);
	public final static RGBColor PAPAYA_WHIP=new RGBColor(0xFFEFD5);
	public final static RGBColor PEACH_PUFF=new RGBColor(0xFFDAB9);
	public final static RGBColor PERU=new RGBColor(0xCD853F);
	public final static RGBColor PINK=new RGBColor(0xFFC0CB);
	public final static RGBColor PLUM=new RGBColor(0xDDA0DD);
	public final static RGBColor POWDER_BLUE=new RGBColor(0xB0E0E6);
	public final static RGBColor PURPLE=new RGBColor(0x800080);
	public final static RGBColor RED=new RGBColor(0xFF0000);
	public final static RGBColor ROSY_BROWN=new RGBColor(0xBC8F8F);
	public final static RGBColor ROYAL_BLUE=new RGBColor(0x4169E1);
	public final static RGBColor SADDLE_BROWN=new RGBColor(0x8B4513);
	public final static RGBColor SALMON=new RGBColor(0xFA8072);
	public final static RGBColor SANDY_BROWN=new RGBColor(0xF4A460);
	public final static RGBColor SEA_GREEN=new RGBColor(0x2E8B57);
	public final static RGBColor SEA_SHELL=new RGBColor(0xFFF5EE);
	public final static RGBColor SIENNA=new RGBColor(0xA0522D);
	public final static RGBColor SILVER=new RGBColor(0xC0C0C0);
	public final static RGBColor SKY_BLUE=new RGBColor(0x87CEEB);
	public final static RGBColor SLATE_BLUE=new RGBColor(0x6A5ACD);
	public final static RGBColor SLATE_GRAY=new RGBColor(0x708090);
	public final static RGBColor SNOW=new RGBColor(0xFFFAFA);
	public final static RGBColor SPRING_GREEN=new RGBColor(0x00FF7F);
	public final static RGBColor STEEL_BLUE=new RGBColor(0x4682B4);
	public final static RGBColor TAN=new RGBColor(0xD2B48C);
	public final static RGBColor TEAL=new RGBColor(0x008080);
	public final static RGBColor THISTLE=new RGBColor(0xD8BFD8);
	public final static RGBColor TOMATO=new RGBColor(0xFF6347);
	public final static RGBColor TURQUOISE=new RGBColor(0x40E0D0);
	public final static RGBColor VIOLET=new RGBColor(0xEE82EE);
	public final static RGBColor VIOLET_RED=new RGBColor(0xD02090);
	public final static RGBColor WHEAT=new RGBColor(0xF5DEB3);
	public final static RGBColor WHITE=new RGBColor(0xFFFFFF);
	public final static RGBColor WHITE_SMOKE=new RGBColor(0xF5F5F5);
	public final static RGBColor YELLOW=new RGBColor(0xFFFF00);
	public final static RGBColor YELLOW_GREEN=new RGBColor(0x9ACD32);

	static
	{
			//store the standard colors in the map, keyed to the lowercase name with no underscore
		final Pattern underscorePattern=Pattern.compile("_");	//a pattern to match an underscore, so that it can be removed
		namedColorMap=new HashMap<String, RGBColor>(144);	//create the map of colors
		namedColorMap.put(underscorePattern.matcher("TRANSPARENT").replaceAll("").toLowerCase(), TRANSPARENT);

		namedColorMap.put(underscorePattern.matcher("ALICE_BLUE").replaceAll("").toLowerCase(), ALICE_BLUE);
		namedColorMap.put(underscorePattern.matcher("ANTIQUE_WHITE").replaceAll("").toLowerCase(), ANTIQUE_WHITE);
		namedColorMap.put(underscorePattern.matcher("AQUA").replaceAll("").toLowerCase(), AQUA);
		namedColorMap.put(underscorePattern.matcher("AQUA_MARINE").replaceAll("").toLowerCase(), AQUA_MARINE);
		namedColorMap.put(underscorePattern.matcher("AZURE").replaceAll("").toLowerCase(), AZURE);
		namedColorMap.put(underscorePattern.matcher("BEIGE").replaceAll("").toLowerCase(), BEIGE);
		namedColorMap.put(underscorePattern.matcher("BISQUE").replaceAll("").toLowerCase(), BISQUE);
		namedColorMap.put(underscorePattern.matcher("BLACK").replaceAll("").toLowerCase(), BLACK);
		namedColorMap.put(underscorePattern.matcher("BLANCHE_DALMOND").replaceAll("").toLowerCase(), BLANCHE_DALMOND);
		namedColorMap.put(underscorePattern.matcher("BLUE").replaceAll("").toLowerCase(), BLUE);
		namedColorMap.put(underscorePattern.matcher("BLUE_VIOLET").replaceAll("").toLowerCase(), BLUE_VIOLET);
		namedColorMap.put(underscorePattern.matcher("BROWN").replaceAll("").toLowerCase(), BROWN);
		namedColorMap.put(underscorePattern.matcher("BURLY_WOOD").replaceAll("").toLowerCase(), BURLY_WOOD);
		namedColorMap.put(underscorePattern.matcher("CADET_BLUE").replaceAll("").toLowerCase(), CADET_BLUE);
		namedColorMap.put(underscorePattern.matcher("CHARTREUSE").replaceAll("").toLowerCase(), CHARTREUSE);
		namedColorMap.put(underscorePattern.matcher("CHOCOLATE").replaceAll("").toLowerCase(), CHOCOLATE);
		namedColorMap.put(underscorePattern.matcher("CORAL").replaceAll("").toLowerCase(), CORAL);
		namedColorMap.put(underscorePattern.matcher("CORNFLOWER_BLUE").replaceAll("").toLowerCase(), CORNFLOWER_BLUE);
		namedColorMap.put(underscorePattern.matcher("CORNSILK").replaceAll("").toLowerCase(), CORNSILK);
		namedColorMap.put(underscorePattern.matcher("CRIMSON").replaceAll("").toLowerCase(), CRIMSON);
		namedColorMap.put(underscorePattern.matcher("CYAN").replaceAll("").toLowerCase(), CYAN);
		namedColorMap.put(underscorePattern.matcher("DARK_BLUE").replaceAll("").toLowerCase(), DARK_BLUE);
		namedColorMap.put(underscorePattern.matcher("DARK_CYAN").replaceAll("").toLowerCase(), DARK_CYAN);
		namedColorMap.put(underscorePattern.matcher("DARK_GOLDEN_ROD").replaceAll("").toLowerCase(), DARK_GOLDEN_ROD);
		namedColorMap.put(underscorePattern.matcher("DARK_GRAY").replaceAll("").toLowerCase(), DARK_GRAY);
		namedColorMap.put(underscorePattern.matcher("DARK_GREEN").replaceAll("").toLowerCase(), DARK_GREEN);
		namedColorMap.put(underscorePattern.matcher("DARK_KHAKI").replaceAll("").toLowerCase(), DARK_KHAKI);
		namedColorMap.put(underscorePattern.matcher("DARK_MAGENTA").replaceAll("").toLowerCase(), DARK_MAGENTA);
		namedColorMap.put(underscorePattern.matcher("DARK_OLIVE_GREEN").replaceAll("").toLowerCase(), DARK_OLIVE_GREEN);
		namedColorMap.put(underscorePattern.matcher("DARK_ORANGE").replaceAll("").toLowerCase(), DARK_ORANGE);
		namedColorMap.put(underscorePattern.matcher("DARK_ORCHID").replaceAll("").toLowerCase(), DARK_ORCHID);
		namedColorMap.put(underscorePattern.matcher("DARK_RED").replaceAll("").toLowerCase(), DARK_RED);
		namedColorMap.put(underscorePattern.matcher("DARK_SALMON").replaceAll("").toLowerCase(), DARK_SALMON);
		namedColorMap.put(underscorePattern.matcher("DARK_SEA_GREEN").replaceAll("").toLowerCase(), DARK_SEA_GREEN);
		namedColorMap.put(underscorePattern.matcher("DARK_SLATE_BLUE").replaceAll("").toLowerCase(), DARK_SLATE_BLUE);
		namedColorMap.put(underscorePattern.matcher("DARK_SLATE_GRAY").replaceAll("").toLowerCase(), DARK_SLATE_GRAY);
		namedColorMap.put(underscorePattern.matcher("DARK_TURQUOISE").replaceAll("").toLowerCase(), DARK_TURQUOISE);
		namedColorMap.put(underscorePattern.matcher("DARK_VIOLET").replaceAll("").toLowerCase(), DARK_VIOLET);
		namedColorMap.put(underscorePattern.matcher("DEEP_PINK").replaceAll("").toLowerCase(), DEEP_PINK);
		namedColorMap.put(underscorePattern.matcher("DEEP_SKY_BLUE").replaceAll("").toLowerCase(), DEEP_SKY_BLUE);
		namedColorMap.put(underscorePattern.matcher("DIM_GRAY").replaceAll("").toLowerCase(), DIM_GRAY);
		namedColorMap.put(underscorePattern.matcher("DODGER_BLUE").replaceAll("").toLowerCase(), DODGER_BLUE);
		namedColorMap.put(underscorePattern.matcher("FELDSPAR").replaceAll("").toLowerCase(), FELDSPAR);
		namedColorMap.put(underscorePattern.matcher("FIRE_BRICK").replaceAll("").toLowerCase(), FIRE_BRICK);
		namedColorMap.put(underscorePattern.matcher("FLORAL_WHITE").replaceAll("").toLowerCase(), FLORAL_WHITE);
		namedColorMap.put(underscorePattern.matcher("FOREST_GREEN").replaceAll("").toLowerCase(), FOREST_GREEN);
		namedColorMap.put(underscorePattern.matcher("FUCHSIA").replaceAll("").toLowerCase(), FUCHSIA);
		namedColorMap.put(underscorePattern.matcher("GAINSBORO").replaceAll("").toLowerCase(), GAINSBORO);
		namedColorMap.put(underscorePattern.matcher("GHOST_WHITE").replaceAll("").toLowerCase(), GHOST_WHITE);
		namedColorMap.put(underscorePattern.matcher("GOLD").replaceAll("").toLowerCase(), GOLD);
		namedColorMap.put(underscorePattern.matcher("GOLDEN_ROD").replaceAll("").toLowerCase(), GOLDEN_ROD);
		namedColorMap.put(underscorePattern.matcher("GRAY").replaceAll("").toLowerCase(), GRAY);
		namedColorMap.put(underscorePattern.matcher("GREEN").replaceAll("").toLowerCase(), GREEN);
		namedColorMap.put(underscorePattern.matcher("GREEN_YELLOW").replaceAll("").toLowerCase(), GREEN_YELLOW);
		namedColorMap.put(underscorePattern.matcher("HONEY_DEW").replaceAll("").toLowerCase(), HONEY_DEW);
		namedColorMap.put(underscorePattern.matcher("HOT_PINK").replaceAll("").toLowerCase(), HOT_PINK);
		namedColorMap.put(underscorePattern.matcher("INDIAN_RED").replaceAll("").toLowerCase(), INDIAN_RED);
		namedColorMap.put(underscorePattern.matcher("INDIGO").replaceAll("").toLowerCase(), INDIGO);
		namedColorMap.put(underscorePattern.matcher("IVORY").replaceAll("").toLowerCase(), IVORY);
		namedColorMap.put(underscorePattern.matcher("KHAKI").replaceAll("").toLowerCase(), KHAKI);
		namedColorMap.put(underscorePattern.matcher("LAVENDER").replaceAll("").toLowerCase(), LAVENDER);
		namedColorMap.put(underscorePattern.matcher("LAVENDER_BLUSH").replaceAll("").toLowerCase(), LAVENDER_BLUSH);
		namedColorMap.put(underscorePattern.matcher("LAWN_GREEN").replaceAll("").toLowerCase(), LAWN_GREEN);
		namedColorMap.put(underscorePattern.matcher("LEMON_CHIFFON").replaceAll("").toLowerCase(), LEMON_CHIFFON);
		namedColorMap.put(underscorePattern.matcher("LIGHT_BLUE").replaceAll("").toLowerCase(), LIGHT_BLUE);
		namedColorMap.put(underscorePattern.matcher("LIGHT_CORAL").replaceAll("").toLowerCase(), LIGHT_CORAL);
		namedColorMap.put(underscorePattern.matcher("LIGHT_CYAN").replaceAll("").toLowerCase(), LIGHT_CYAN);
		namedColorMap.put(underscorePattern.matcher("LIGHT_GOLDEN_ROD_YELLOW").replaceAll("").toLowerCase(), LIGHT_GOLDEN_ROD_YELLOW);
		namedColorMap.put(underscorePattern.matcher("LIGHT_GREY").replaceAll("").toLowerCase(), LIGHT_GREY);
		namedColorMap.put(underscorePattern.matcher("LIGHT_GREEN").replaceAll("").toLowerCase(), LIGHT_GREEN);
		namedColorMap.put(underscorePattern.matcher("LIGHT_PINK").replaceAll("").toLowerCase(), LIGHT_PINK);
		namedColorMap.put(underscorePattern.matcher("LIGHT_SALMON").replaceAll("").toLowerCase(), LIGHT_SALMON);
		namedColorMap.put(underscorePattern.matcher("LIGHT_SEA_GREEN").replaceAll("").toLowerCase(), LIGHT_SEA_GREEN);
		namedColorMap.put(underscorePattern.matcher("LIGHT_SKY_BLUE").replaceAll("").toLowerCase(), LIGHT_SKY_BLUE);
		namedColorMap.put(underscorePattern.matcher("LIGHT_SLATE_BLUE").replaceAll("").toLowerCase(), LIGHT_SLATE_BLUE);
		namedColorMap.put(underscorePattern.matcher("LIGHT_SLATE_GRAY").replaceAll("").toLowerCase(), LIGHT_SLATE_GRAY);
		namedColorMap.put(underscorePattern.matcher("LIGHT_STEEL_BLUE").replaceAll("").toLowerCase(), LIGHT_STEEL_BLUE);
		namedColorMap.put(underscorePattern.matcher("LIGHT_YELLOW").replaceAll("").toLowerCase(), LIGHT_YELLOW);
		namedColorMap.put(underscorePattern.matcher("LIME").replaceAll("").toLowerCase(), LIME);
		namedColorMap.put(underscorePattern.matcher("LIME_GREEN").replaceAll("").toLowerCase(), LIME_GREEN);
		namedColorMap.put(underscorePattern.matcher("LINEN").replaceAll("").toLowerCase(), LINEN);
		namedColorMap.put(underscorePattern.matcher("MAGENTA").replaceAll("").toLowerCase(), MAGENTA);
		namedColorMap.put(underscorePattern.matcher("MAROON").replaceAll("").toLowerCase(), MAROON);
		namedColorMap.put(underscorePattern.matcher("MEDIUM_AQUA_MARINE").replaceAll("").toLowerCase(), MEDIUM_AQUA_MARINE);
		namedColorMap.put(underscorePattern.matcher("MEDIU_MBLUE").replaceAll("").toLowerCase(), MEDIU_MBLUE);
		namedColorMap.put(underscorePattern.matcher("MEDIUM_ORCHID").replaceAll("").toLowerCase(), MEDIUM_ORCHID);
		namedColorMap.put(underscorePattern.matcher("MEDIUM_PURPLE").replaceAll("").toLowerCase(), MEDIUM_PURPLE);
		namedColorMap.put(underscorePattern.matcher("MEDIUM_SEA_GREEN").replaceAll("").toLowerCase(), MEDIUM_SEA_GREEN);
		namedColorMap.put(underscorePattern.matcher("MEDIUM_SLATE_BLUE").replaceAll("").toLowerCase(), MEDIUM_SLATE_BLUE);
		namedColorMap.put(underscorePattern.matcher("MEDIUM_SPRING_GREEN").replaceAll("").toLowerCase(), MEDIUM_SPRING_GREEN);
		namedColorMap.put(underscorePattern.matcher("MEDIUM_TURQUOISE").replaceAll("").toLowerCase(), MEDIUM_TURQUOISE);
		namedColorMap.put(underscorePattern.matcher("MEDIUM_VIOLET_RED").replaceAll("").toLowerCase(), MEDIUM_VIOLET_RED);
		namedColorMap.put(underscorePattern.matcher("MIDNIGHT_BLUE").replaceAll("").toLowerCase(), MIDNIGHT_BLUE);
		namedColorMap.put(underscorePattern.matcher("MINT_CREAM").replaceAll("").toLowerCase(), MINT_CREAM);
		namedColorMap.put(underscorePattern.matcher("MISTY_ROSE").replaceAll("").toLowerCase(), MISTY_ROSE);
		namedColorMap.put(underscorePattern.matcher("MOCCASIN").replaceAll("").toLowerCase(), MOCCASIN);
		namedColorMap.put(underscorePattern.matcher("NAVAJO_WHITE").replaceAll("").toLowerCase(), NAVAJO_WHITE);
		namedColorMap.put(underscorePattern.matcher("NAVY").replaceAll("").toLowerCase(), NAVY);
		namedColorMap.put(underscorePattern.matcher("OLD_LACE").replaceAll("").toLowerCase(), OLD_LACE);
		namedColorMap.put(underscorePattern.matcher("OLIVE").replaceAll("").toLowerCase(), OLIVE);
		namedColorMap.put(underscorePattern.matcher("OLIVE_DRAB").replaceAll("").toLowerCase(), OLIVE_DRAB);
		namedColorMap.put(underscorePattern.matcher("ORANGE").replaceAll("").toLowerCase(), ORANGE);
		namedColorMap.put(underscorePattern.matcher("ORANGE_RED").replaceAll("").toLowerCase(), ORANGE_RED);
		namedColorMap.put(underscorePattern.matcher("ORCHID").replaceAll("").toLowerCase(), ORCHID);
		namedColorMap.put(underscorePattern.matcher("PALE_GOLDEN_ROD").replaceAll("").toLowerCase(), PALE_GOLDEN_ROD);
		namedColorMap.put(underscorePattern.matcher("PALE_GREEN").replaceAll("").toLowerCase(), PALE_GREEN);
		namedColorMap.put(underscorePattern.matcher("PALE_TURQUOISE").replaceAll("").toLowerCase(), PALE_TURQUOISE);
		namedColorMap.put(underscorePattern.matcher("PALE_VIOLET_RED").replaceAll("").toLowerCase(), PALE_VIOLET_RED);
		namedColorMap.put(underscorePattern.matcher("PAPAYA_WHIP").replaceAll("").toLowerCase(), PAPAYA_WHIP);
		namedColorMap.put(underscorePattern.matcher("PEACH_PUFF").replaceAll("").toLowerCase(), PEACH_PUFF);
		namedColorMap.put(underscorePattern.matcher("PERU").replaceAll("").toLowerCase(), PERU);
		namedColorMap.put(underscorePattern.matcher("PINK").replaceAll("").toLowerCase(), PINK);
		namedColorMap.put(underscorePattern.matcher("PLUM").replaceAll("").toLowerCase(), PLUM);
		namedColorMap.put(underscorePattern.matcher("POWDER_BLUE").replaceAll("").toLowerCase(), POWDER_BLUE);
		namedColorMap.put(underscorePattern.matcher("PURPLE").replaceAll("").toLowerCase(), PURPLE);
		namedColorMap.put(underscorePattern.matcher("RED").replaceAll("").toLowerCase(), RED);
		namedColorMap.put(underscorePattern.matcher("ROSY_BROWN").replaceAll("").toLowerCase(), ROSY_BROWN);
		namedColorMap.put(underscorePattern.matcher("ROYAL_BLUE").replaceAll("").toLowerCase(), ROYAL_BLUE);
		namedColorMap.put(underscorePattern.matcher("SADDLE_BROWN").replaceAll("").toLowerCase(), SADDLE_BROWN);
		namedColorMap.put(underscorePattern.matcher("SALMON").replaceAll("").toLowerCase(), SALMON);
		namedColorMap.put(underscorePattern.matcher("SANDY_BROWN").replaceAll("").toLowerCase(), SANDY_BROWN);
		namedColorMap.put(underscorePattern.matcher("SEA_GREEN").replaceAll("").toLowerCase(), SEA_GREEN);
		namedColorMap.put(underscorePattern.matcher("SEA_SHELL").replaceAll("").toLowerCase(), SEA_SHELL);
		namedColorMap.put(underscorePattern.matcher("SIENNA").replaceAll("").toLowerCase(), SIENNA);
		namedColorMap.put(underscorePattern.matcher("SILVER").replaceAll("").toLowerCase(), SILVER);
		namedColorMap.put(underscorePattern.matcher("SKY_BLUE").replaceAll("").toLowerCase(), SKY_BLUE);
		namedColorMap.put(underscorePattern.matcher("SLATE_BLUE").replaceAll("").toLowerCase(), SLATE_BLUE);
		namedColorMap.put(underscorePattern.matcher("SLATE_GRAY").replaceAll("").toLowerCase(), SLATE_GRAY);
		namedColorMap.put(underscorePattern.matcher("SNOW").replaceAll("").toLowerCase(), SNOW);
		namedColorMap.put(underscorePattern.matcher("SPRING_GREEN").replaceAll("").toLowerCase(), SPRING_GREEN);
		namedColorMap.put(underscorePattern.matcher("STEEL_BLUE").replaceAll("").toLowerCase(), STEEL_BLUE);
		namedColorMap.put(underscorePattern.matcher("TAN").replaceAll("").toLowerCase(), TAN);
		namedColorMap.put(underscorePattern.matcher("TEAL").replaceAll("").toLowerCase(), TEAL);
		namedColorMap.put(underscorePattern.matcher("THISTLE").replaceAll("").toLowerCase(), THISTLE);
		namedColorMap.put(underscorePattern.matcher("TOMATO").replaceAll("").toLowerCase(), TOMATO);
		namedColorMap.put(underscorePattern.matcher("TURQUOISE").replaceAll("").toLowerCase(), TURQUOISE);
		namedColorMap.put(underscorePattern.matcher("VIOLET").replaceAll("").toLowerCase(), VIOLET);
		namedColorMap.put(underscorePattern.matcher("VIOLET_RED").replaceAll("").toLowerCase(), VIOLET_RED);
		namedColorMap.put(underscorePattern.matcher("WHEAT").replaceAll("").toLowerCase(), WHEAT);
		namedColorMap.put(underscorePattern.matcher("WHITE").replaceAll("").toLowerCase(), WHITE);
		namedColorMap.put(underscorePattern.matcher("WHITE_SMOKE").replaceAll("").toLowerCase(), WHITE_SMOKE);
		namedColorMap.put(underscorePattern.matcher("YELLOW").replaceAll("").toLowerCase(), YELLOW);
		namedColorMap.put(underscorePattern.matcher("YELLOW_GREEN").replaceAll("").toLowerCase(), YELLOW_GREEN);
	}

}
