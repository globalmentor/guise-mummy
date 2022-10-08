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

import java.util.Date;

/**
 * A range validator for dates. The step value is considered relative either to the minimum value, if available, the maximum value, if available, or zero, in
 * that order or priority.
 * @author Garret Wilson
 */
public class DateRangeValidator extends AbstractComparableRangeValidator<Date> {

	/** Default constructor with no value required and a step of one. */
	public DateRangeValidator() {
		this(false); //construct the class and don't required non-null values
	}

	/**
	 * Value required constructor with a step of one.
	 * @param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	 */
	public DateRangeValidator(final boolean valueRequired) {
		this(null, null, valueRequired); //construct the class with no minimum or maximum value
	}

	/**
	 * Minimum, and maximum constructor with a step of one.
	 * @param minimum The minimum value, inclusive, or <code>null</code> if the range has no lower bound.
	 * @param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	 */
	public DateRangeValidator(final Date minimum, final Date maximum) {
		this(minimum, maximum, new Date(1)); //construct the class with a step of 1
	}

	/**
	 * Minimum, maximum, and step constructor.
	 * @param minimum The minimum value, inclusive, or <code>null</code> if the range has no lower bound.
	 * @param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	 * @param step The step amount, or <code>null</code> if the range has no increment value specified.
	 */
	public DateRangeValidator(final Date minimum, final Date maximum, final Date step) {
		this(minimum, maximum, step, false); //construct the class and don't required non-null values
	}

	/**
	 * Minimum, maximum, and value required constructor.
	 * @param minimum The minimum value, inclusive, or <code>null</code> if the range has no lower bound.
	 * @param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	 * @param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	 */
	public DateRangeValidator(final Date minimum, final Date maximum, final boolean valueRequired) {
		this(minimum, maximum, null, valueRequired); //construct the class with no step
	}

	/**
	 * Minimum, maximum, step, and value required constructor.
	 * @param minimum The minimum value, inclusive, or <code>null</code> if the range has no lower bound.
	 * @param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	 * @param step The step amount, or <code>null</code> if the range has no increment value specified.
	 * @param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	 */
	public DateRangeValidator(final Date minimum, final Date maximum, final Date step, final boolean valueRequired) {
		super(minimum, maximum, step, valueRequired); //construct the parent class
	}

	@Override
	protected boolean isValidStep(final Date value, final Date step, final Date base) {
		final long baseTime = base != null ? base.getTime() : 0; //get the primitive base value
		return (value.getTime() - baseTime) % step.getTime() == 0; //normalize the value to the base and see if the step divides the result evenly
	}

}
