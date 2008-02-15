package com.guiseframework.platform.web;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import com.garretwilson.text.xml.stylesheets.css.*;
import com.garretwilson.util.Debug;

import static com.garretwilson.text.xml.stylesheets.css.XMLCSSConstants.*;

import com.globalmentor.java.Integers;
import com.guiseframework.*;
import com.guiseframework.component.layout.Orientation;
import com.guiseframework.geometry.*;
import com.guiseframework.model.ui.PresentationModel;

import static com.globalmentor.java.Characters.*;
import static com.globalmentor.java.Enums.*;
import static com.globalmentor.java.StringBuilders.*;
import static com.guiseframework.model.ui.PresentationModel.*;
import com.guiseframework.platform.AbstractXHTMLDepictContext;
import com.guiseframework.platform.web.WebPlatform;
import static com.guiseframework.platform.web.WebPlatform.*;
import com.guiseframework.style.*;

/**Abstract implementation of information related to the current depiction on the web platform.
This implementation maps the XHTML namespace {@value WebPlatform#GUISE_ML_NAMESPACE_URI} to the prefix {@value WebPlatform#GUISE_ML_NAMESPACE_PREFIX}.
@author Garret Wilson
*/
public abstract class AbstractWebDepictContext extends AbstractXHTMLDepictContext implements WebDepictContext
{

	/**@return The web platform on which Guise objects are depicted.*/
	public WebPlatform getPlatform() {return (WebPlatform)super.getPlatform();}

	/**Guise session constructor.
	@param session The Guise user session of which this context is a part.
	@param destination The destination with which this context is associated.
	@exception NullPointerException if the given session and/or destination is null.
	@exception IOException If there was an I/O error loading a needed resource.
	*/
	public AbstractWebDepictContext(final GuiseSession session, final Destination destination) throws IOException
	{
		super(session, destination);	//construct the parent class
		getXMLNamespacePrefixManager().registerNamespacePrefix(GUISE_ML_NAMESPACE_URI.toString(), GUISE_ML_NAMESPACE_PREFIX);	//map the Guise namespace to the Guise prefix
	}

	/**Returns a string representation of the provided style declarations.
	This method performs special processing on the following properties, including generating user-agent-specific styles to allow proper display on certain browsers:
	<ul>
		<li>{@value XMLCSSConstants#CSS_PROP_COLOR} with a value of {@link Color} and an alpha less than 1.0.</li>
		<li>{@value XMLCSSConstants#CSS_PROP_CURSOR} with a value of {@link URI}, interpreted as a predefined cursor (one of {@link Cursor#getURI()}) or as a URI to a custom cursor; URI references are allowed in either.</li>
		<li>{@value XMLCSSConstants#CSS_PROP_DISPLAY} with a value of {@value XMLCSSConstants#CSS_DISPLAY_INLINE_BLOCK}.</li>
		<li>{@value XMLCSSConstants#CSS_PROP_FONT_WEIGHT} with a value of {@link Number}, interpreted in terms of {@link PresentationModel#FONT_WEIGHT_NORMAL} and {@link PresentationModel#FONT_WEIGHT_BOLD}.</li>
		<li>{@value XMLCSSConstants#CSS_PROP_MAX_WIDTH} or {@value XMLCSSConstants#CSS_PROP_MAX_HEIGHT} with a pixel value of {@link Extent}.</li>
		<li>{@value XMLCSSConstants#CSS_PROP_OPACITY} with a value of {@link Number}.</li>
	</ul>
	These styles include the CSS property {@value XMLCSSConstants#CSS_PROP_DISPLAY} with a value of {@value XMLCSSConstants#CSS_DISPLAY_INLINE_BLOCK}.
	This implementation supports values of the following types:
	<ul>
		<li>{@link Color}</li>
		<li>{@link Cursor}</li>
		<li>{@link Extent}</li>
		<li>{@link FontStyle}</li>
		<li>{@link LineStyle}</li>
		<li>{@link List}</li>
		<li>{@link URI} with URI references allowed</li>
	</ul>
	All other values will be added using {@link Object#toString()}.
	@param styles The map of styles to write, each keyed to a CSS style property.
	@param orientation The orientation of the component for which the style is being produced.
	@return A string containing the given CSS properties and styles.
	*/
	public String getCSSStyleString(final Map<String, Object> styles, final Orientation orientation)
	{
		final WebPlatform platform=getPlatform();	//get the platform
		final StringBuilder stringBuilder=new StringBuilder();	//creat a new string builder for style
		for(final Map.Entry<String, Object> entry:styles.entrySet())	//for each style entry
		{
			String property=entry.getKey();	//get the property
			Object value=entry.getValue();	//get the value
			if(CSS_PROP_COLOR.equals(property) && value instanceof Color)	//if this is the color property with a color value, append an opacity if the color's alpha is not 100%
			{
				final RGBColor rgbColor=((Color)value).asRGB();	//get the color as RGB
				final double alpha=rgbColor.getComponent(RGBColor.Component.ALPHA);	//get the alpha value
				if(alpha<1.0 && !styles.containsKey(CSS_PROP_OPACITY))	//if the alpha value isn't 100% and some opacity has not explicitly been set
				{
					appendCSSOpacityProperty(stringBuilder, alpha);	//append an opacity that matches the color opacity
				}
			}
			else if(CSS_PROP_CURSOR.equals(property) && value instanceof URI)	//if this is a cursor property with a URI value
			{
				final URI cursorURI=getSession().dereferenceURI((URI)value);	//get the dereferenced cursor URI
				final Cursor cursor=Cursor.getCursor(cursorURI);	//get the predefined cursor that corresponds to this URI, if any
				if(cursor!=null)	//if this is a predefined cursor
				{
					value=cursor;	//use the cursor as the value, which will get serialized later; otherwise, we'll use the custom URI as any other URI
				}
			}			
				//display property; see http://www.w3.org/TR/CSS21/visuren.html#display-prop
					//see also: http://www.webmasterworld.com/css/3271607.htm
					//see also for table-based method: http://godlikenerd.com/weblog/2005/03/24/firefox-inline-block-frustration/
					//see also for support by other browsers: http://www.quirksmode.org/css/display.html#inlineblock
			else if(CSS_DISPLAY_INLINE_BLOCK.equals(value) && CSS_PROP_DISPLAY.equals(property))	//if this is display:inline-block (check the value first, because there will be few inline-block values but a substantial number of display properties that do not have the inline-block property)
			{
				final WebUserAgentProduct userAgent=platform.getClientProduct();	//get the user agent
				if(userAgent.getBrand()==WebUserAgentProduct.Brand.FIREFOX && userAgent.getVersionNumber()<3)	//if this is Firefox <3, a value of "-moz-inline-box" is required (see http://www.quirksmode.org/css/display.html#inlineblock); Firefox 3 brings inline-block support; see http://developer.mozilla.org/en/docs/CSS:display and http://developer.mozilla.org/en/docs/Firefox_3_for_developers and https://bugzilla.mozilla.org/show_bug.cgi?id=9458
				{
					value="-moz-inline-box";	//switch to a display value of "-moz-inline-box" TODO use a constant					
				}
				else if(userAgent.getBrand()==WebUserAgentProduct.Brand.INTERNET_EXPLORER)	//if this is IE, an hasLayout must be given to an inline element, in any order (see http://www.brunildo.org/test/InlineBlockLayout.html)
				{
					stringBuilder.append(CSS_PROP_DISPLAY).append(PROPERTY_DIVIDER_CHAR).append(CSS_DISPLAY_INLINE).append(DECLARATION_SEPARATOR_CHAR);	//make the element inline: display: inline;					
					stringBuilder.append("zoom").append(PROPERTY_DIVIDER_CHAR).append("1").append(DECLARATION_SEPARATOR_CHAR);	//give the element hasLayout: zoom: 1;
					continue;	//we did custom appending; don't do the default appending
				}
			}
			else if(CSS_PROP_FONT_WEIGHT.equals(property) && value instanceof Number)	//if this is a font weight as a number
			{
				final double fontWeight=((Number)value).doubleValue();	//get the font weight
				if(fontWeight>=FONT_WEIGHT_BOLD)	//if the weight is bold or above
				{
					value=CSS_FONT_WEIGHT_BOLD;	//indicate a bold font
				}
				else if(fontWeight>=FONT_WEIGHT_NORMAL)	//if the weight is normal or above
				{
					value=CSS_FONT_WEIGHT_NORMAL;	//indicate a bold font			
				}
				else	//if the font is lighter than normal
				{
					value=CSS_FONT_WEIGHT_NORMAL;	//everything else is normal in this implementation
				}
			}
			else if(CSS_PROP_OPACITY.equals(property) && value instanceof Number)	//if this is the opacity property with a number
			{
				appendCSSOpacityProperty(stringBuilder, ((Number)value).doubleValue());	//append the opacity
				continue;	//we did custom appending; don't do the default appending
			}
			else if((CSS_PROP_MAX_WIDTH.equals(property) || CSS_PROP_MAX_HEIGHT.equals(property)) && value instanceof Extent)	//if this is the max-width or max-height property with an extent value
			{
				final Extent extent=(Extent)value;	//get the extent
				final WebUserAgentProduct userAgent=platform.getClientProduct();	//get the user agent
				if(userAgent.getBrand()==WebUserAgentProduct.Brand.INTERNET_EXPLORER && userAgent.getVersionNumber()<7 && extent.getUnit()==Unit.PIXEL)	//if a pixel max-width or max-height is being used on IE6 (which doesn't support max-width or max-height)
				{
						//see http://msdn2.microsoft.com/en-us/library/ms537634.aspx
						//see http://www.svendtofte.com/code/max_width_in_ie/
						//see http://www.gunlaug.no/contents/wd_additions_14.html
						//http://www.sanctifiedstudios.com/internet-explorer-min-and-max-width-bug-fix/
					if(CSS_PROP_MAX_WIDTH.equals(property))	//if this is a max-width request
					{
						property=CSS_PROP_WIDTH;	//use a width instead
						value="expression(this.clientWidth>"+extent.getValue()+"-1 ? \""+appendCSSValue(new StringBuilder(), extent)+"\" : \"auto\")";	//expression(this:clientWidth>value-1 ? "valuepx" : "auto"); (don't use the same value twice or IE6 will freeze)
					}
					else	//if this is a max-height request
					{
						property=CSS_PROP_HEIGHT;	//use a height instead
						value="expression(this.clientHeight>"+extent.getValue()+"-1 ? \""+appendCSSValue(new StringBuilder(), extent)+"\" : \"auto\")";	//expression(this:clientHeight>value-1 ? "valuepx" : "auto"); (don't use the same value twice or IE6 will freeze)
					}
				}
			}
			stringBuilder.append(property).append(PROPERTY_DIVIDER_CHAR);	//property:
			if(value instanceof Color)	//if the value is a color
			{
				appendCSSValue(stringBuilder, (Color)value);	//append the color value
			}
			else if(value instanceof Cursor)	//if the value is a cursor
			{
				appendCSSValue(stringBuilder, (Cursor)value, orientation);	//append the cursor value
			}
			else if(value instanceof Extent)	//if the value is an extent
			{
				appendCSSValue(stringBuilder, (Extent)value);	//append the extent value
			}
			else if(value instanceof FontStyle)	//if the value is a font style
			{
				appendCSSValue(stringBuilder, (FontStyle)value);	//append the font style
			}
			else if(value instanceof LineStyle)	//if the value is a line style
			{
				appendCSSValue(stringBuilder, (LineStyle)value);	//append the font style
			}
			else if(value instanceof List)	//if the value is a list
			{
				appendCSSValue(stringBuilder, (List<?>)value);	//append the list
			}
			else if(value instanceof URI)	//if the value is a URI
			{
				appendCSSValue(stringBuilder, (URI)value);	//append the URI
			}
			else	//if the value is any other type of object
			{
				stringBuilder.append(value);	//append the normal string value of the object
			}
			stringBuilder.append(DECLARATION_SEPARATOR_CHAR);	//;
		}
		return stringBuilder.toString();	//return the string we constructed
	}

	/**Appends a CSS {@value XMLCSSConstants#CSS_PROP_OPACITY} property designation.
	If the user agent is IE 6, the appropriate {@value XMLCSSConstants#CSS_PROP_FILTER} property will also be added.
	@param stringBuilder The string builder to which the style will be added
	@param opacity The opacity value to add.
	@return The provided string builder.
	*/
	protected StringBuilder appendCSSOpacityProperty(final StringBuilder stringBuilder, final double opacity)
	{
		stringBuilder.append(CSS_PROP_OPACITY).append(PROPERTY_DIVIDER_CHAR).append(Double.toString(opacity)).append(DECLARATION_SEPARATOR_CHAR);	//add the opacity
		final WebUserAgentProduct userAgent=getPlatform().getClientProduct();	//get the user agent
		if(userAgent.getBrand()==WebUserAgentProduct.Brand.INTERNET_EXPLORER && userAgent.getVersionNumber()<7)	//if this is IE6 (which doesn't support opacity), we'll have to use a filter
		{
			stringBuilder.append(CSS_PROP_FILTER).append(PROPERTY_DIVIDER_CHAR).append("alpha(opacity=").append(Math.round(opacity*100)).append(')').append(DECLARATION_SEPARATOR_CHAR);	//add a filter for IE that gives the same opacity
		}
		return stringBuilder;	//return the string builder
	}

	/**Appends a CSS string representation of the given color.
	@param stringBuilder The string builder to which the style will be added
	@param color The color to represent in CSS.
	@return The provided string builder.
	*/
	protected static StringBuilder appendCSSValue(final StringBuilder stringBuilder, final Color color)
	{
		final RGBColor rgbColor=color.asRGB();	//get the color as RGB
		stringBuilder.append(RGB_NUMBER_CHAR);	//#
		stringBuilder.append(Integers.toHexString(rgbColor.getAbsoluteRed8(), 2));	//red
		stringBuilder.append(Integers.toHexString(rgbColor.getAbsoluteGreen8(), 2));	//green
		stringBuilder.append(Integers.toHexString(rgbColor.getAbsoluteBlue8(), 2));	//blue
		return stringBuilder;	//return the string builder
	}

	/**Appends a CSS string representation of the given cursor.
	@param stringBuilder The string builder to which the style will be added
	@param cursor The cursor to represent in CSS.
	@param orientation The orientation of the component for which the cursor is being set.
	@return The provided string builder.
	@exception NullPointerException if the given cursor is <code>null</code>.
	*/
	protected static StringBuilder appendCSSValue(final StringBuilder stringBuilder, final Cursor cursor, final Orientation orientation)
	{
		return stringBuilder.append(getSerializationName(cursor.getCSSCursor(orientation)));	//append the serialized form of the corresponding XML CSS cursor enum value
	}
	
	/**Appends a CSS string representation of the given extent.
	@param stringBuilder The string builder to which the style will be added
	@param extent The extent to be represented by a CSS length string.
	@return The provided string builder.
	*/
	protected static StringBuilder appendCSSValue(final StringBuilder stringBuilder, final Extent extent)
	{
		double value=extent.getValue();	//get the value of the extent
		final Unit unit=extent.getUnit();	//get the unit of measurement
		final String cssUnit=CSS_UNITS[unit.ordinal()];	//get the corresponding CSS unit
		if(unit==Unit.RELATIVE)	//if this is a pure relative unit, show it as a percent
		{
			value*=100;	//use as a percentage			
		}
		return stringBuilder.append(value).append(cssUnit);	//format the extent and return the string builder
	}

	/**Appends a CSS string representation of the given font style.
	@param stringBuilder The string builder to which the style will be added
	@param fontStyle The font style to represent in CSS.
	@return The provided string builder.
	@exception NullPointerException if the given font style is <code>null</code>.
	*/
	protected static StringBuilder appendCSSValue(final StringBuilder stringBuilder, final FontStyle fontStyle)
	{
		return stringBuilder.append(getSerializationName(fontStyle));	//append the serialized form of the enum
	}

	/**Appends a CSS string representation of the given line style, such as used for a border style.
	@param stringBuilder The string builder to which the style will be added
	@param lineStyle The line style to be represented in CSS.
	@return The provided string builder.
	@exception NullPointerException if the given line style is <code>null</code>.
	*/
	protected static StringBuilder appendCSSValue(final StringBuilder stringBuilder, final LineStyle lineStyle)
	{
		return stringBuilder.append(getSerializationName(lineStyle));	//append the serialized form of the enum
	}

	/**Appends a CSS string containing the given list of items.
	Strings containing spaces will be quoted.
	Items that are <code>null</code> will be represented by a missing item in the list.
	@param stringBuilder The string builder to which the style will be added
	@param list The list of items to be converted to a string.
	@return The provided string builder.
	*/
	protected static StringBuilder appendCSSValue(final StringBuilder stringBuilder, final List<?> items)
	{
		if(!items.isEmpty())	//if there are items
		{
			for(final Object item:items)	//for each item
			{
				if(item!=null)	//if we have an item
				{
					final String string=item.toString();	//get the string version of the item
					final boolean needsQuotes=WHITESPACE_PATTERN.matcher(string).find();	//this item needs quotes if it contains one or more whitespace characters
					if(needsQuotes)	//if we need quotes
					{
						stringBuilder.append(DOUBLE_QUOTE_CHAR);	//"
					}
					stringBuilder.append(string);	//append the item
					if(needsQuotes)	//if we need quotes
					{
						stringBuilder.append(DOUBLE_QUOTE_CHAR);	//"
					}
				}
				stringBuilder.append(LIST_DELIMITER_CHAR);	//append the delimiter
			}
			deleteEnd(stringBuilder);	//remove the last delimiter
		}
		return stringBuilder;	//return the string builder
	}

	/**Appends a CSS string representation of the given URI.
	The URI will be dereferenced and resolved to the application as well as converted to a depict URI.
	@param stringBuilder The string builder to which the style will be added
	@param uri The URI to be represented in CSS.
	@return The provided string builder.
	@see #getDepictionURI(URI, String...)
	*/
	protected StringBuilder appendCSSValue(final StringBuilder stringBuilder, final URI uri)
	{
		return stringBuilder.append("url(").append(getDepictionURI(uri)).append(')');	//append the CSS form of the URI
	}

	/**The pre-filled CSS unit strings mapped to extent unit ordinal indices.
	@see Unit
	*/
	private final static String[] CSS_UNITS;

	/**Initializes the CSS unit lookup array.*/
	static
	{
		final Unit[] units=Unit.values();	//get the available unit values
		CSS_UNITS=new String[units.length];	//create an array of corresponding CSS unit strings
		for(int i=CSS_UNITS.length-1; i>=0; --i)	//for each CSS unit
		{
			final String cssUnit;	//we'll determine a CSS unit string to represent the extent's unit of measurement
			final Unit unit=units[i];	//get this unit
			switch(unit)	//see which unit to use
			{
				case EM:
					cssUnit=EM_UNITS;
					break;
				case EX:
					cssUnit=EX_UNITS;
					break;
				case PIXEL:
					cssUnit=PX_UNITS;
					break;
				case INCH:
					cssUnit=IN_UNITS;
					break;
				case CENTIMETER:
					cssUnit=CM_UNITS;
					break;
				case MILLIMETER:
					cssUnit=MM_UNITS;
					break;
				case POINT:
					cssUnit=PT_UNITS;
					break;
				case PICA:
					cssUnit=PC_UNITS;
					break;
				case RELATIVE:
					cssUnit=String.valueOf(PERCENT_SIGN_CHAR);
					break;
				default:	//if we've left out a unit by mistake
					throw new AssertionError("Unrecognized unit: "+unit);
			}
			CSS_UNITS[i]=cssUnit;	//save the CSS unit string at the correct position in the array
		}
	}
}
