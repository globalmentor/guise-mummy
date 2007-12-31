package com.guiseframework.converter;

import java.util.*;

import static com.garretwilson.lang.Objects.*;
import static com.garretwilson.util.LocaleUtilities.*;

/**An object that can convert a locale to a string using the current locale.
This implementation does not support conversion of a literal value to a locale.
@author Garret Wilson
*/
public class LocaleStringLiteralConverter extends AbstractConverter<Locale, String>
{
	/**The locale representation style.*/
	private final LocaleStringLiteralStyle style;

		/**@return The locale representation style.*/
		public LocaleStringLiteralStyle getStyle() {return style;}

	/**Locale style constructor.
	@param style The locale representation style.
	@exception NullPointerException if the given locale style is <code>null</code>.
	*/
	public LocaleStringLiteralConverter(final LocaleStringLiteralStyle style)
	{
		this.style=checkInstance(style, "Locale style cannot be null.");
	}

	/**Converts a value from the value space to a literal value in the lexical space.
	@param value The value in the value space to convert.
	@return The converted value in the lexical space, or <code>null</code> if the given literal is <code>null</code>.
	@exception ConversionException if the value cannot be converted.
	*/ 
	public String convertValue(final Locale value) throws ConversionException
	{
		if(value!=null)	//if a value is given
		{
			try
			{
				final String literalValue;	//we'll determine a literal value
				final Locale locale=getSession().getLocale();	//get the current locale
				final LocaleStringLiteralStyle style=getStyle();	//get the style
				switch(style)	//see with which style we should convert the locale
				{
					case COUNTRY:
						literalValue=value.getDisplayCountry(locale);
						break;
					case COUNTRY_CODE_2:
						literalValue=value.getCountry();
						break;
					case COUNTRY_CODE_3:
						literalValue=value.getISO3Country();
						break;
					case LANGUAGE:
						literalValue=value.getDisplayLanguage(locale);
						break;
					case LANGUAGE_CODE_2:
						literalValue=value.getLanguage();
						break;
					case LANGUAGE_CODE_3:
						literalValue=value.getISO3Language();
						break;
					case LANGUAGE_TAG:
						literalValue=getLanguageTag(value);
						break;
					case NAME:
						literalValue=value.getDisplayName(locale);
						break;
					case VARIANT:
						literalValue=value.getDisplayVariant(locale);
						break;
					case VARIANT_CODE:
						literalValue=value.getVariant();
						break;
					default:
						throw new AssertionError("Unrecognized locale style: "+style);
				}
				return literalValue!=null ? literalValue : "";	//if there is no literal value, return the empty string
			}
			catch(final MissingResourceException missingResourceException)	//if we can't find a particular literal representation 
			{
				return "";	//return the empty string
			}			
		}
		else	//if no value is given
		{
			return null;
		}
	}

	/**Converts a literal representation of a value from the lexical space into a value in the value space.
	This version throws a conversion exception if the literal is not <code>null</code>.
	@param literal The literal value in the lexical space to convert.
	@return The converted value in the value space, or <code>null</code> if the given literal is <code>null</code>.
	@exception ConversionException if the literal value cannot be converted.
	*/ 
	public Locale convertLiteral(final String literal) throws ConversionException
	{
		if(literal==null)	//if the literal is null
		{
			return null;	//the value is null
		}
		else	//if the literal is not null
		{
			throw new ConversionException("This class does not support literal to value conversions.", literal);
		}
	}
}
