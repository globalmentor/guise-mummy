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

/**
 * A converter that converts a <code>String</code> from and to a string literal.
 * @author Garret Wilson
 */
public class StringStringLiteralConverter extends AbstractStringLiteralConverter<String> {

	/**
	 * Converts a literal representation of a value from the lexical space into a value in the value space. This version returns the literal itself.
	 * @param literal The literal value in the lexical space to convert.
	 * @return The converted value in the value space, or <code>null</code> if the given literal is <code>null</code>.
	 * @throws ConversionException if the literal value cannot be converted.
	 */
	public String convertLiteral(final String literal) throws ConversionException {
		return literal; //a string's value and string literal lexical spaces are identical
	}

	/**
	 * Converts a value from the value space to a literal value in the lexical space. This version returns the value itself.
	 * @param value The value in the value space to convert.
	 * @return The converted value in the lexical space, or <code>null</code> if the given literal is <code>null</code>.
	 * @throws ConversionException if the value cannot be converted.
	 */
	public String convertValue(final String value) throws ConversionException {
		return value; //a string's value and string literal lexical spaces are identical
	}
}
