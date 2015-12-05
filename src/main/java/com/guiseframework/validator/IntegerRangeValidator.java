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

/**
 * A range validator for integers. The step value is considered relative either to the minimum value, if available, the maximum value, if available, or zero, in
 * that order or priority.
 * @author Garret Wilson
 */
public class IntegerRangeValidator extends AbstractComparableRangeValidator<Integer> {

	/** Default constructor with no value required and a step of one. */
	public IntegerRangeValidator() {
		this(false); //construct the class and don't required non-null values
	}

	/**
	 * Value required constructor with a step of one.
	 * @param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	 */
	public IntegerRangeValidator(final boolean valueRequired) {
		this(null, null, valueRequired); //construct the class with no minimum or maximum value
	}

	/**
	 * Maximum constructor with a minimum value of zero and a step of one.
	 * @param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	 */
	public IntegerRangeValidator(final Integer maximum) {
		this(Integer.valueOf(0), maximum); //construct the class with a minimum value of 0
	}

	/**
	 * Minimum and maximum constructor with a step of one.
	 * @param minimum The minimum value, inclusive, or <code>null</code> if the range has no lower bound.
	 * @param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	 */
	public IntegerRangeValidator(final Integer minimum, final Integer maximum) {
		this(minimum, maximum, Integer.valueOf(1)); //construct the class with a step of 1
	}

	/**
	 * Minimum, maximum, and step constructor.
	 * @param minimum The minimum value, inclusive, or <code>null</code> if the range has no lower bound.
	 * @param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	 * @param step The step amount, or <code>null</code> if the range has no increment value specified.
	 */
	public IntegerRangeValidator(final Integer minimum, final Integer maximum, final Integer step) {
		this(minimum, maximum, step, false); //construct the class and don't required non-null values
	}

	/**
	 * Minimum and value required constructor with a minimum value of zero and a step of one.
	 * @param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	 * @param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	 */
	public IntegerRangeValidator(final Integer maximum, final boolean valueRequired) {
		this(Integer.valueOf(0), maximum, valueRequired); //construct the class with a minimum value of 0
	}

	/**
	 * Minimum, maximum, and value required constructor with a step of one.
	 * @param minimum The minimum value, inclusive, or <code>null</code> if the range has no lower bound.
	 * @param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	 * @param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	 */
	public IntegerRangeValidator(final Integer minimum, final Integer maximum, final boolean valueRequired) {
		this(minimum, maximum, Integer.valueOf(1), valueRequired); //construct the class with no step
	}

	/**
	 * Minimum, maximum, step, and value required constructor.
	 * @param minimum The minimum value, inclusive, or <code>null</code> if the range has no lower bound.
	 * @param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	 * @param step The step amount, or <code>null</code> if the range has no increment value specified.
	 * @param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	 */
	public IntegerRangeValidator(final Integer minimum, final Integer maximum, final Integer step, final boolean valueRequired) {
		super(minimum, maximum, step, valueRequired); //construct the parent class
	}

	/**
	 * Determines whether the given value falls on the correct step amount relative to the base value.
	 * @param value The value to validate.
	 * @param step The step value.
	 * @param base The base (either the minimum or maximum value), or <code>null</code> if zero should be used as a base.
	 * @return <code>true</code> if the value is a valid step away from the given base.
	 */
	protected boolean isValidStep(final Integer value, final Integer step, final Integer base) {
		final int baseInt = base != null ? base.intValue() : 0; //get the primitive base value
		return (value.intValue() - baseInt) % step.intValue() == 0; //normalize the value to the base and see if the step divides the result evenly
	}

}
