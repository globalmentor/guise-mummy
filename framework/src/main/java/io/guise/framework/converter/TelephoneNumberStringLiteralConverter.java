/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.converter;

import static java.text.MessageFormat.format;

import java.util.Locale;

import com.globalmentor.itu.CountryCode;
import com.globalmentor.itu.TelephoneNumber;
import com.globalmentor.text.ArgumentSyntaxException;

import io.guise.framework.GuiseSession;

/**
 * A converter that converts a {@link TelephoneNumber} from and to a string literal. If a default country code is specified, that country code will be used as
 * the default telephone country code. Otherwise, the country code of the current local's country, if available, will be used as the default telephone country
 * code.
 * @author Garret Wilson
 * @see GuiseSession#getLocale()
 * @see CountryCode#getCountryCode(Locale)
 */
public class TelephoneNumberStringLiteralConverter extends AbstractStringLiteralConverter<TelephoneNumber> {

	/** The default country code, or -1 if the country code of the current local's country, if available, should be used as the default telephone country code. */
	private final int defaultCC;

	/**
	 * @return The default country code, or -1 if the country code of the current local's country, if available, should be used as the default telephone country
	 *         code.
	 */
	public int getDefaultCC() {
		return defaultCC;
	}

	/**
	 * Default constructor. The country code of the current local's country, if available, will be used as the default telephone country code when each telephone
	 * number is created.
	 */
	public TelephoneNumberStringLiteralConverter() {
		this(null); //construct the class with no default country code
	}

	/**
	 * Default country code constructor.
	 * @param defaultCC The default country code to use when constructing telephone numbers, or null if the country code of the current local's country, if
	 *          available, should be used as the default telephone country code.
	 */
	public TelephoneNumberStringLiteralConverter(final CountryCode defaultCC) {
		this(defaultCC != null ? defaultCC.getValue() : -1); //construct the converter with the default country code value, if given
	}

	/**
	 * Default country code constructor.
	 * @param defaultCC The default country code to use when constructing telephone numbers, or -1 if the country code of the current local's country, if
	 *          available, should be used as the default telephone country code.
	 */
	public TelephoneNumberStringLiteralConverter(final int defaultCC) {
		this.defaultCC = defaultCC; //save the default country code
	}

	@Override
	public TelephoneNumber convertLiteral(final String literal) throws ConversionException {
		if(literal == null) { //if the literal is null
			return null; //the value is null
		} else { //if the literal is not null
			int defaultCC = getDefaultCC(); //get the default country code
			if(defaultCC < 0) { //if there is no default country code
				final Locale locale = getSession().getLocale(); //get the current session locale
				final CountryCode countryCode = CountryCode.getCountryCode(locale); //try to get a default country code from the current locale
				if(countryCode != null) { //if we can determine a country code from the current locale
					defaultCC = countryCode.getValue(); //get the default country code value
				}
			}
			try {
				return new TelephoneNumber(literal, defaultCC); //construct the telephone number using the default country code, if any
			} catch(final ArgumentSyntaxException argumentSyntaxException) { //if the phone number is not in the correct syntax
				throw new ConversionException(format(getSession().dereferenceString(getInvalidValueMessage()), literal), literal); //indicate that the value was invalid				
			}
		}
	}
}
