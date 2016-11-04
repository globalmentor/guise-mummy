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
 * A converter that converts a <code>char[]</code> from and to a string literal.
 * @author Garret Wilson
 */
public class CharArrayStringLiteralConverter extends AbstractStringLiteralConverter<char[]> {

	@Override
	public char[] convertLiteral(final String literal) throws ConversionException {
		return literal != null ? literal.toCharArray() : null; //if there is a literal, convert it to a character array			
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version creates a new string based upon the character array value.
	 * </p>
	 */
	@Override
	public String convertValue(final char[] value) throws ConversionException {
		return value != null ? new String(value) : null; //construct a string from the characters, if there is a character array
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation converts the value to a string and compares that string with the literal value.
	 * </p>
	 */
	@Override
	public boolean isEquivalent(final char[] value, final String literal) {
		if(value != null) { //if there is a value
			return new String(value).equals(literal); //convert the value to a string and compare it with the literal
		} else { //if there is no value
			return literal == null; //the values are equivalent if there also is no literal
		}
	}
}
