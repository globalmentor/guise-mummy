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

package com.guiseframework.converter;

import java.util.*;

import static com.globalmentor.java.Objects.*;
import static com.globalmentor.model.Locales.*;

/**
 * An object that can convert a locale to a string using the current locale. This implementation does not support conversion of a literal value to a locale.
 * @author Garret Wilson
 */
public class LocaleStringLiteralConverter extends AbstractConverter<Locale, String> {

	/** The locale representation style. */
	private final LocaleStringLiteralStyle style;

	/** @return The locale representation style. */
	public LocaleStringLiteralStyle getStyle() {
		return style;
	}

	/**
	 * Locale style constructor.
	 * @param style The locale representation style.
	 * @throws NullPointerException if the given locale style is <code>null</code>.
	 */
	public LocaleStringLiteralConverter(final LocaleStringLiteralStyle style) {
		this.style = checkInstance(style, "Locale style cannot be null.");
	}

	@Override
	public String convertValue(final Locale value) throws ConversionException {
		if(value != null) { //if a value is given
			try {
				final String literalValue; //we'll determine a literal value
				final Locale locale = getSession().getLocale(); //get the current locale
				final LocaleStringLiteralStyle style = getStyle(); //get the style
				switch(style) { //see with which style we should convert the locale
					case COUNTRY:
						literalValue = value.getDisplayCountry(locale);
						break;
					case COUNTRY_CODE_2:
						literalValue = value.getCountry();
						break;
					case COUNTRY_CODE_3:
						literalValue = value.getISO3Country();
						break;
					case LANGUAGE:
						literalValue = value.getDisplayLanguage(locale);
						break;
					case LANGUAGE_CODE_2:
						literalValue = value.getLanguage();
						break;
					case LANGUAGE_CODE_3:
						literalValue = value.getISO3Language();
						break;
					case LANGUAGE_TAG:
						literalValue = getLanguageTag(value);
						break;
					case NAME:
						literalValue = value.getDisplayName(locale);
						break;
					case VARIANT:
						literalValue = value.getDisplayVariant(locale);
						break;
					case VARIANT_CODE:
						literalValue = value.getVariant();
						break;
					default:
						throw new AssertionError("Unrecognized locale style: " + style);
				}
				return literalValue != null ? literalValue : ""; //if there is no literal value, return the empty string
			} catch(final MissingResourceException missingResourceException) { //if we can't find a particular literal representation 
				return ""; //return the empty string
			}
		} else { //if no value is given
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version throws a conversion exception if the literal is not <code>null</code>.
	 * </p>
	 */
	@Override
	public Locale convertLiteral(final String literal) throws ConversionException {
		if(literal == null) { //if the literal is null
			return null; //the value is null
		} else { //if the literal is not null
			throw new ConversionException("This class does not support literal to value conversions.", literal);
		}
	}
}
