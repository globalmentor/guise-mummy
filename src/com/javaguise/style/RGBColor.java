package com.javaguise.style;

/**Encapsulates a color value of the sRGB color space.
@author Garret Wilson
@see http://www.w3.org/pub/WWW/Graphics/Color/sRGB.html
@see http://msdn.microsoft.com/library/default.asp?url=/library/en-us/dnwebgen/html/X11_names.asp
@see http://www.w3schools.com/html/html_colornames.asp
*/
public class RGBColor extends AbstractColor<RGBColor.Component>
{

	/**A color component of sRGB.*/
	public enum Component {ALPHA, RED, GREEN, BLUE;}

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
	@param red The red component on an absolute scale in the range (0x00-0xFF).
	@param green The green component on an absolute scale in the range (0x00-0xFF).
	@param blue The blue component on an absolute scale in the range (0x00-0xFF).
	@exception IllegalArgumentException if one of the values is outside the range (0x00-0xFF).
	*/
	public RGBColor(final int red, final int green, final int blue)
	{
		this(red, green, blue, 1.0f);	//construct the color with full alpha
	}

	/**Creates an sRGB color with the specified absolute red, green, blue, and alpha component values.
	@param red The red component on an absolute scale in the range (0x00-0xFF).
	@param green The green component on an absolute scale in the range (0x00-0xFF).
	@param blue The blue component on an absolute scale in the range (0x00-0xFF).
	@param alpha The alpha component on an absolute scale in the range (0x00-0xFF).
	@exception IllegalArgumentException if one of the values is outside the range (0x00-0xFF).
	*/
	public RGBColor(final int red, final int green, final int blue, final int alpha)
	{
		this((float)red/0xFF, (float)green/0xFF, (float)blue/0xFF, (float)alpha/0xFF);	//convert the components into relative amounts
	}

	/**Creates an opaque sRGB color with the specified red, green, and blue component values.
	@param red The red component.
	@param green The green component.
	@param blue The blue component.
	@exception IllegalArgumentException if one of the values is outside the range (0.0-1.0).
	*/
	public RGBColor(final float red, final float green, final float blue)
	{
		this(red, green, blue, 1.0f);	//construct the color with full alpha
	}

	/**Creates an sRGB color with the specified red, green, blue, and alpha component values.
	@param red The red component.
	@param green The green component.
	@param blue The blue component.
	@param alpha The alpha component.
	@exception IllegalArgumentException if one of the values is outside the range (0.0-1.0).
	*/
	public RGBColor(final float red, final float green, final float blue, final float alpha)
	{
		super(alpha, red, green, blue);	//construct the parent class
	}

	/**@return The absolute red value at a depth of 8 bits.*/
	public int getAbsoluteRed()
	{
		return getAbsoluteComponent(Component.RED, 8);	//return the absolute red component at 8 bits
	}

	/**@return The absolute green value at a depth of 8 bits.*/
	public int getAbsoluteGreen()
	{
		return getAbsoluteComponent(Component.GREEN, 8);	//return the absolute green component at 8 bits
	}

	/**@return The absolute blue value at a depth of 8 bits.*/
	public int getAbsoluteBlue()
	{
		return getAbsoluteComponent(Component.BLUE, 8);	//return the absolute blue component at 8 bits
	}

	/**@return The absolute alpha value at a depth of 8 bits.*/
	public int getAbsoluteAlpha()
	{
		return getAbsoluteComponent(Component.ALPHA, 8);	//return the absolute alpha component at 8 bits
	}

	/**@return The color in the RGB color space.*/
	public RGBColor asRGB()
	{
		return this;
	}

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
	public final static RGBColor DARK_SEAGREEN=new RGBColor(0x8FBC8F);
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
	public final static RGBColor INDIAN_RED =new RGBColor(0xCD5C5C);
	public final static RGBColor INDIGO =new RGBColor(0x4B0082);
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
	public final static RGBColor PAPAY_AWHIP=new RGBColor(0xFFEFD5);
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
	public final static RGBColor SEAG_REEN=new RGBColor(0x2E8B57);
	public final static RGBColor SEAS_HELL=new RGBColor(0xFFF5EE);
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
	public final static RGBColor VIOLETRED=new RGBColor(0xD02090);
	public final static RGBColor WHEAT=new RGBColor(0xF5DEB3);
	public final static RGBColor WHITE=new RGBColor(0xFFFFFF);
	public final static RGBColor WHITES_MOKE=new RGBColor(0xF5F5F5);
	public final static RGBColor YELLOW=new RGBColor(0xFFFF00);
	public final static RGBColor YELLOW_GREEN=new RGBColor(0x9ACD32);
}
