/*
 * Copyright © 2005-2012 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package com.guiseframework.platform.web;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import com.globalmentor.facebook.*;
import com.globalmentor.java.Integers;
import com.globalmentor.text.xml.stylesheets.css.*;

import com.guiseframework.*;
import com.guiseframework.component.layout.Orientation;
import com.guiseframework.geometry.*;

import static com.globalmentor.java.Characters.*;
import static com.globalmentor.java.Enums.*;
import static com.globalmentor.java.StringBuilders.*;
import static com.globalmentor.text.xml.stylesheets.css.XMLCSS.*;
import static com.guiseframework.model.ui.PresentationModel.*;
import com.guiseframework.platform.AbstractXHTMLDepictContext;
import com.guiseframework.platform.web.WebPlatform;
import static com.guiseframework.platform.web.WebPlatform.*;
import com.guiseframework.style.*;

/**
 * Abstract implementation of information related to the current depiction on the web platform.
 * <p>
 * This implementation maps the XHTML namespace {@value WebPlatform#GUISE_ML_NAMESPACE_URI} to the prefix {@value WebPlatform#GUISE_ML_NAMESPACE_PREFIX}.
 * </p>
 * <p>
 * This implementation maps the XHTML namespace {@value OpenGraph#NAMESPACE_URI} to the prefix {@value OpenGraph#NAMESPACE_PREFIX}.
 * </p>
 * <p>
 * This implementation defaults to not using quirks mode.
 * </p>
 * @author Garret Wilson
 */
public abstract class AbstractWebDepictContext extends AbstractXHTMLDepictContext implements WebDepictContext {

	/** {@inheritDoc} This implementation always returns <code>false</code>. */
	@Override
	public boolean isQuirksMode() {
		return false;
	}

	@Override
	public WebPlatform getPlatform() {
		return (WebPlatform)super.getPlatform();
	}

	/**
	 * Guise session constructor.
	 * @param session The Guise user session of which this context is a part.
	 * @param destination The destination with which this context is associated.
	 * @throws NullPointerException if the given session and/or destination is null.
	 * @throws IOException If there was an I/O error loading a needed resource.
	 */
	public AbstractWebDepictContext(final GuiseSession session, final Destination destination) throws IOException {
		super(session, destination); //construct the parent class
		getXMLNamespacePrefixManager().registerNamespacePrefix(GUISE_ML_NAMESPACE_URI.toString(), GUISE_ML_NAMESPACE_PREFIX); //map the Guise namespace to the Guise prefix
		getXMLNamespacePrefixManager().registerNamespacePrefix(OpenGraph.NAMESPACE_URI.toString(), OpenGraph.NAMESPACE_PREFIX); //map the Open Graph namespace to the Open Graph prefix
		getXMLNamespacePrefixManager().registerNamespacePrefix(Facebook.NAMESPACE_URI.toString(), Facebook.NAMESPACE_PREFIX); //map the Facebook namespace to the Facebook prefix
	}

	@Override
	public String getCSSStyleString(final Map<String, Object> styles, final Orientation orientation) {
		final StringBuilder stringBuilder = new StringBuilder(); //create a new string builder for style
		for(final Map.Entry<String, Object> entry : styles.entrySet()) { //for each style entry
			String property = entry.getKey(); //get the property
			Object value = entry.getValue(); //get the value
			if(CSS_PROP_COLOR.equals(property) && value instanceof Color) { //if this is the color property with a color value, append an opacity if the color's alpha is not 100%
				final RGBColor rgbColor = ((Color)value).asRGB(); //get the color as RGB
				final double alpha = rgbColor.getComponent(RGBColor.Component.ALPHA); //get the alpha value
				if(alpha < 1.0 && !styles.containsKey(CSS_PROP_OPACITY)) { //if the alpha value isn't 100% and some opacity has not explicitly been set
					stringBuilder.append(CSS_PROP_OPACITY).append(PROPERTY_DIVIDER_CHAR).append(Double.toString(alpha)).append(DECLARATION_SEPARATOR_CHAR); //append an opacity that matches the color opacity
				}
			} else if(CSS_PROP_CURSOR.equals(property) && value instanceof URI) { //if this is a cursor property with a URI value
				final URI cursorURI = getSession().dereferenceURI((URI)value); //get the dereferenced cursor URI
				final Cursor cursor = Cursor.getCursor(cursorURI); //get the predefined cursor that corresponds to this URI, if any
				if(cursor != null) { //if this is a predefined cursor
					value = cursor; //use the cursor as the value, which will get serialized later; otherwise, we'll use the custom URI as any other URI
				}
			} else if(CSS_PROP_FONT_WEIGHT.equals(property) && value instanceof Number) { //if this is a font weight as a number
				final double fontWeight = ((Number)value).doubleValue(); //get the font weight
				if(fontWeight >= FONT_WEIGHT_BOLD) { //if the weight is bold or above
					value = CSS_FONT_WEIGHT_BOLD; //indicate a bold font
				} else if(fontWeight >= FONT_WEIGHT_NORMAL) { //if the weight is normal or above
					value = CSS_FONT_WEIGHT_NORMAL; //indicate a bold font			
				} else { //if the font is lighter than normal
					value = CSS_FONT_WEIGHT_NORMAL; //everything else is normal in this implementation
				}
			}
			stringBuilder.append(property).append(PROPERTY_DIVIDER_CHAR); //property:
			appendCSSValue(stringBuilder, value, orientation); //value
			stringBuilder.append(DECLARATION_SEPARATOR_CHAR); //;
		}
		return stringBuilder.toString(); //return the string we constructed
	}

	/**
	 * Appends a CSS value to the given string builder. If the value is an array of a non-primitive type, each element in the array will be appended separated by
	 * spaces.
	 * @param stringBuilder The string builder to which the style value will be added.
	 * @param value The value to append.
	 * @param orientation The orientation of the component for which the style is being produced.
	 * @return The provided string builder.
	 */
	protected StringBuilder appendCSSValue(final StringBuilder stringBuilder, final Object value, final Orientation orientation) {
		if(value instanceof Object[]) { //if the value is an array
			final Object[] array = (Object[])value;
			final int arrayLength = array.length;
			for(int i = 0; i < arrayLength; ++i) { //look at each element in the array
				if(i > 0) { //append a separator if needed
					stringBuilder.append(' ');
				}
				appendCSSValue(stringBuilder, array[i], orientation); //append this individual value element
			}
		} else if(value instanceof Color) { //if the value is a color
			appendCSSValue(stringBuilder, (Color)value); //append the color value
		} else if(value instanceof Cursor) { //if the value is a cursor
			appendCSSValue(stringBuilder, (Cursor)value, orientation); //append the cursor value
		} else if(value instanceof Extent) { //if the value is an extent
			appendCSSValue(stringBuilder, (Extent)value); //append the extent value
		} else if(value instanceof FontStyle) { //if the value is a font style
			appendCSSValue(stringBuilder, (FontStyle)value); //append the font style
		} else if(value instanceof LineStyle) { //if the value is a line style
			appendCSSValue(stringBuilder, (LineStyle)value); //append the font style
		} else if(value instanceof List) { //if the value is a list
			appendCSSValue(stringBuilder, (List<?>)value); //append the list
		} else if(value instanceof URI) { //if the value is a URI
			appendCSSValue(stringBuilder, (URI)value); //append the URI
		} else { //if the value is any other type of object
			stringBuilder.append(value); //append the normal string value of the object
		}
		return stringBuilder;
	}

	/**
	 * Appends a CSS string representation of the given color. This method correctly handles transparent colors with the special CSS keyword
	 * {@value XMLCSS#CSS_COLOR_TRANSPARENT}.
	 * @param stringBuilder The string builder to which the style will be added.
	 * @param color The color to represent in CSS.
	 * @return The provided string builder.
	 */
	protected static StringBuilder appendCSSValue(final StringBuilder stringBuilder, final Color color) {
		final RGBColor rgbColor = color.asRGB(); //get the color as RGB
		if(rgbColor.getAlpha() > 0) { //if there is an alpha value
			stringBuilder.append(RGB_NUMBER_CHAR); //#
			stringBuilder.append(Integers.toHexString(rgbColor.getAbsoluteRed8(), 2)); //red
			stringBuilder.append(Integers.toHexString(rgbColor.getAbsoluteGreen8(), 2)); //green
			stringBuilder.append(Integers.toHexString(rgbColor.getAbsoluteBlue8(), 2)); //blue
		} else { //if there is no alpha, the color is transparent
			stringBuilder.append(CSS_COLOR_TRANSPARENT);
		}
		return stringBuilder; //return the string builder
	}

	/**
	 * Appends a CSS string representation of the given cursor.
	 * @param stringBuilder The string builder to which the style will be added
	 * @param cursor The cursor to represent in CSS.
	 * @param orientation The orientation of the component for which the cursor is being set.
	 * @return The provided string builder.
	 * @throws NullPointerException if the given cursor is <code>null</code>.
	 */
	protected static StringBuilder appendCSSValue(final StringBuilder stringBuilder, final Cursor cursor, final Orientation orientation) {
		return stringBuilder.append(getSerializationName(cursor.getCSSCursor(orientation))); //append the serialized form of the corresponding XML CSS cursor enum value
	}

	/**
	 * Appends a CSS string representation of the given extent.
	 * @param stringBuilder The string builder to which the style will be added
	 * @param extent The extent to be represented by a CSS length string.
	 * @return The provided string builder.
	 */
	protected static StringBuilder appendCSSValue(final StringBuilder stringBuilder, final Extent extent) {
		double value = extent.getValue(); //get the value of the extent
		final Unit unit = extent.getUnit(); //get the unit of measurement
		final String cssUnit = CSS_UNITS[unit.ordinal()]; //get the corresponding CSS unit
		if(unit == Unit.RELATIVE) { //if this is a pure relative unit, show it as a percent
			value *= 100; //use as a percentage			
		}
		return stringBuilder.append(value).append(cssUnit); //format the extent and return the string builder
	}

	/**
	 * Appends a CSS string representation of the given font style.
	 * @param stringBuilder The string builder to which the style will be added
	 * @param fontStyle The font style to represent in CSS.
	 * @return The provided string builder.
	 * @throws NullPointerException if the given font style is <code>null</code>.
	 */
	protected static StringBuilder appendCSSValue(final StringBuilder stringBuilder, final FontStyle fontStyle) {
		return stringBuilder.append(getSerializationName(fontStyle)); //append the serialized form of the enum
	}

	/**
	 * Appends a CSS string representation of the given line style, such as used for a border style.
	 * @param stringBuilder The string builder to which the style will be added
	 * @param lineStyle The line style to be represented in CSS.
	 * @return The provided string builder.
	 * @throws NullPointerException if the given line style is <code>null</code>.
	 */
	protected static StringBuilder appendCSSValue(final StringBuilder stringBuilder, final LineStyle lineStyle) {
		return stringBuilder.append(getSerializationName(lineStyle)); //append the serialized form of the enum
	}

	/**
	 * Appends a CSS string containing the given list of items. Strings containing spaces will be quoted. Items that are <code>null</code> will be represented by
	 * a missing item in the list.
	 * @param stringBuilder The string builder to which the style will be added
	 * @param list The list of items to be converted to a string.
	 * @return The provided string builder.
	 */
	protected static StringBuilder appendCSSValue(final StringBuilder stringBuilder, final List<?> items) {
		if(!items.isEmpty()) { //if there are items
			for(final Object item : items) { //for each item
				if(item != null) { //if we have an item
					final String string = item.toString(); //get the string version of the item
					final boolean needsQuotes = WHITESPACE_PATTERN.matcher(string).find(); //this item needs quotes if it contains one or more whitespace characters
					if(needsQuotes) { //if we need quotes
						stringBuilder.append(DOUBLE_QUOTE_CHAR); //"
					}
					stringBuilder.append(string); //append the item
					if(needsQuotes) { //if we need quotes
						stringBuilder.append(DOUBLE_QUOTE_CHAR); //"
					}
				}
				stringBuilder.append(LIST_DELIMITER_CHAR); //append the delimiter
			}
			deleteEnd(stringBuilder); //remove the last delimiter
		}
		return stringBuilder; //return the string builder
	}

	/**
	 * Appends a CSS string representation of the given URI. The URI will be dereferenced and resolved to the application as well as converted to a depict URI.
	 * @param stringBuilder The string builder to which the style will be added
	 * @param uri The URI to be represented in CSS.
	 * @return The provided string builder.
	 * @see #getDepictionURI(URI, String...)
	 */
	protected StringBuilder appendCSSValue(final StringBuilder stringBuilder, final URI uri) {
		return stringBuilder.append("url(").append(getDepictionURI(uri)).append(')'); //append the CSS form of the URI
	}

	/**
	 * The pre-filled CSS unit strings mapped to extent unit ordinal indices.
	 * @see Unit
	 */
	private static final String[] CSS_UNITS;

	/** Initializes the CSS unit lookup array. */
	static {
		final Unit[] units = Unit.values(); //get the available unit values
		CSS_UNITS = new String[units.length]; //create an array of corresponding CSS unit strings
		for(int i = CSS_UNITS.length - 1; i >= 0; --i) { //for each CSS unit
			final String cssUnit; //we'll determine a CSS unit string to represent the extent's unit of measurement
			final Unit unit = units[i]; //get this unit
			switch(unit) { //see which unit to use
				case EM:
					cssUnit = EM_UNITS;
					break;
				case EX:
					cssUnit = EX_UNITS;
					break;
				case PIXEL:
					cssUnit = PX_UNITS;
					break;
				case INCH:
					cssUnit = IN_UNITS;
					break;
				case CENTIMETER:
					cssUnit = CM_UNITS;
					break;
				case MILLIMETER:
					cssUnit = MM_UNITS;
					break;
				case POINT:
					cssUnit = PT_UNITS;
					break;
				case PICA:
					cssUnit = PC_UNITS;
					break;
				case RELATIVE:
					cssUnit = String.valueOf(PERCENT_SIGN_CHAR);
					break;
				default: //if we've left out a unit by mistake
					throw new AssertionError("Unrecognized unit: " + unit);
			}
			CSS_UNITS[i] = cssUnit; //save the CSS unit string at the correct position in the array
		}
	}
}
