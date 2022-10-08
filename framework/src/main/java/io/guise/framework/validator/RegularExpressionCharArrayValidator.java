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

package io.guise.framework.validator;

import java.util.regex.Pattern;

import static java.util.Objects.*;

/**
 * A character array validator that can validate against regular expressions.
 * @author Garret Wilson
 */
public class RegularExpressionCharArrayValidator extends AbstractRegularExpressionValidator<char[]> {

	/**
	 * Constructs a string regular expression validator from a regular expression string, without requiring a non-<code>null</code> value..
	 * @param regularExpression The regular expression against which to validate string values.
	 * @throws NullPointerException if the given regular expression is <code>null</code>.
	 */
	public RegularExpressionCharArrayValidator(final String regularExpression) {
		this(regularExpression, false); //construct the class without requiring a value
	}

	/**
	 * Constructs a string regular expression validator from a regular expression string.
	 * @param regularExpression The regular expression against which to validate string values.
	 * @param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	 * @throws NullPointerException if the given regular expression is <code>null</code>.
	 */
	public RegularExpressionCharArrayValidator(final String regularExpression, final boolean valueRequired) {
		this(Pattern.compile(requireNonNull(regularExpression, "Regular expression cannot be null.")), valueRequired); //compile the regular expression into a pattern and construct the class
	}

	/**
	 * Constructs a string regular expression validator from a regular expression pattern, without requiring a non-<code>null</code> value.
	 * @param pattern The regular expression pattern against which to validate string values.
	 * @throws NullPointerException if the given regular expression pattern is <code>null</code>.
	 */
	public RegularExpressionCharArrayValidator(final Pattern pattern) {
		this(pattern, false); //construct the class without requiring a value
	}

	/**
	 * Constructs a string regular expression validator from a regular expression pattern.
	 * @param pattern The regular expression pattern against which to validate string values.
	 * @param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	 * @throws NullPointerException if the given regular expression pattern is <code>null</code>.
	 */
	public RegularExpressionCharArrayValidator(final Pattern pattern, final boolean valueRequired) {
		super(pattern, valueRequired); //construct the parent class
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version a string with the given characters or <code>null</code> if the character array is <code>null</code>.
	 * </p>
	 */
	@Override
	protected String toString(final char[] value) {
		return value != null ? new String(value) : null; //return a new string constructed from the character array
	}

}
