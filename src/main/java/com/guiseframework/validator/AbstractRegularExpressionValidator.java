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

package com.guiseframework.validator;

import java.util.regex.Pattern;

import static com.globalmentor.java.Objects.*;

/**
 * An abstract validator that can validate against regular expressions.
 * @param <V> The value type this validator supports.
 * @author Garret Wilson
 */
public abstract class AbstractRegularExpressionValidator<V> extends AbstractValidator<V> {

	/** The regular expression pattern against which to validate string values. */
	private final Pattern pattern;

	/** @return The regular expression pattern against which to validate string values. */
	public Pattern getPattern() {
		return pattern;
	}

	/**
	 * Constructs a string regular expression validator from a regular expression string, without requiring a non-<code>null</code> value..
	 * @param regularExpression The regular expression against which to validate string values.
	 * @throws NullPointerException if the given regular expression is <code>null</code>.
	 */
	public AbstractRegularExpressionValidator(final String regularExpression) {
		this(regularExpression, false); //construct the class without requiring a value
	}

	/**
	 * Constructs a string regular expression validator from a regular expression string.
	 * @param regularExpression The regular expression against which to validate string values.
	 * @param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	 * @throws NullPointerException if the given regular expression is <code>null</code>.
	 */
	public AbstractRegularExpressionValidator(final String regularExpression, final boolean valueRequired) {
		this(Pattern.compile(checkInstance(regularExpression, "Regular expression cannot be null.")), valueRequired); //compile the regular expression into a pattern and construct the class
	}

	/**
	 * Constructs a string regular expression validator from a regular expression pattern, without requiring a non-<code>null</code> value.
	 * @param pattern The regular expression pattern against which to validate string values.
	 * @throws NullPointerException if the given regular expression pattern is <code>null</code>.
	 */
	public AbstractRegularExpressionValidator(final Pattern pattern) {
		this(pattern, false); //construct the class without requiring a value
	}

	/**
	 * Constructs a string regular expression validator from a regular expression pattern.
	 * @param pattern The regular expression pattern against which to validate string values.
	 * @param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	 * @throws NullPointerException if the given regular expression pattern is <code>null</code>.
	 */
	public AbstractRegularExpressionValidator(final Pattern pattern, final boolean valueRequired) {
		super(valueRequired); //construct the parent class
		this.pattern = checkInstance(pattern, "Regular expression pattern cannot be null."); //save the pattern
	}

	/**
	 * Returns a string representation of the given value so that it may be validated against the regular expression.
	 * @param value The value being validated.
	 * @return A string representation of the given value, or <code>null</code> if the value is <code>null</code>.
	 */
	protected abstract String toString(final V value);

	/**
	 * Checks whether a given string matches the regular expression.
	 * @param value The value to validate.
	 * @throws ValidationException if the provided value is not valid.
	 * @see #getPattern()
	 */
	public void validate(final V value) throws ValidationException {
		super.validate(value); //do the default validation
		if(value != null && !pattern.matcher(toString(value)).matches()) { //if there is a non-null value being checked, and it doesn't match the pattern
			throwInvalidValueValidationException(value); //indicate that the value is invalid
		}
	}

}
