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

import static java.text.MessageFormat.*;

/**
 * A converter that converts a {@link Long} from and to a string literal with no delimiters.
 * @author Garret Wilson
 * @see Long
 */
public class PlainLongStringLiteralConverter extends AbstractStringLiteralConverter<Long> {

	/**
	 * Converts a literal representation of a value from the lexical space into a value in the value space.
	 * @param literal The literal value in the lexical space to convert.
	 * @return The converted value in the value space, or <code>null</code> if the given literal is <code>null</code>.
	 * @throws ConversionException if the literal value cannot be converted.
	 */
	public Long convertLiteral(final String literal) throws ConversionException {
		try {
			return literal != null && literal.length() > 0 ? Long.valueOf(Long.parseLong(literal)) : null; //if there is a literal, convert it to a Long			
		} catch(final NumberFormatException numberFormatException) { //if the string does not contain a valid Long
			throw new ConversionException(format(getSession().dereferenceString(getInvalidValueMessage()), literal), literal);
		}
	}
}
