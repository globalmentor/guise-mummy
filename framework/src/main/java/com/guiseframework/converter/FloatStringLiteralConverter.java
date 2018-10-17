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

import java.util.Currency;

/**
 * A converter that converts a {@link Float} from and to a string literal.
 * @author Garret Wilson
 * @see Float
 */
public class FloatStringLiteralConverter extends AbstractNumberStringLiteralConverter<Float> {

	/** Default constructor with a default number style. */
	public FloatStringLiteralConverter() {
		this(Style.NUMBER); //construct the class with a default number style
	}

	/**
	 * Style constructor. If the currency style is requested, the currency used will dynamically change whenever the locale changes.
	 * @param style The representation style.
	 * @throws NullPointerException if the given style is <code>null</code>.
	 */
	public FloatStringLiteralConverter(final Style style) {
		this(style, null); //construct the class using a dynamic default currency for the locale
	}

	/**
	 * Style, and currency constructor.
	 * @param style The representation style.
	 * @param currency The constant currency type to use, or <code>null</code> if currency representation is not requested or the currency should be dynamically
	 *          determined by the locale.
	 * @throws NullPointerException if the given style is <code>null</code>.
	 * @throws IllegalArgumentException if a currency is provided for a style other than {@link Style#CURRENCY}.
	 */
	public FloatStringLiteralConverter(final Style style, final Currency currency) {
		super(style, currency); //construct the parent class
	}

	@Override
	public Float convertLiteral(final String literal) throws ConversionException {
		final Number number = parseNumber(literal); //parse a number from the literal value
		if(number != null) { //if there is a number
			return number instanceof Float ? (Float)number : Float.valueOf(number.floatValue()); //convert the number to a float object if necessary
		} else { //if there is no number
			return null; //there is no number to return
		}
	}

}
