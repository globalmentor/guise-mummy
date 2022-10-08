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

/**
 * An abstract implementation of a range validator that handles comparable values. The step value is considered relative either to the minimum value, if
 * available, the maximum value, if available, or zero, in that order or priority.
 * @param <V> The value type this validator supports.
 * @author Garret Wilson
 * @see Comparable
 */
public abstract class AbstractComparableRangeValidator<V extends Comparable<V>> extends AbstractRangeValidator<V> {

	/**
	 * Minimum, maximum, step, and value required constructor.
	 * @param minimum The minimum value, inclusive, or <code>null</code> if the range has no lower bound.
	 * @param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	 * @param step The step amount, or <code>null</code> if the range has no increment value specified.
	 * @param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	 */
	public AbstractComparableRangeValidator(final V minimum, final V maximum, final V step, final boolean valueRequired) {
		super(minimum, maximum, step, valueRequired); //construct the parent class
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version checks for minimum and maximum compliance, and delegates to {@link #isValidStep(Comparable, Comparable, Comparable)} for checking step
	 * compliance. Child classes will normally not override this class and instead merely implement {@link #isValidStep(Comparable, Comparable, Comparable)}.
	 * </p>
	 */
	@Override
	public void validate(final V value) throws ValidationException {
		super.validate(value); //do the default validation
		if(value != null) { //if there is a value (the super class has already checked for null compliance)
			final V minimum = getMinimum(); //get the minimum value
			if(minimum != null && minimum.compareTo(value) > 0) { //if the value is too small
				throwInvalidValueValidationException(value); //the value is too low TODO add a custom error message, now that we can
			}
			final V maximum = getMaximum(); //get the maximum value
			if(maximum != null && maximum.compareTo(value) < 0) { //if the value is too large
				throwInvalidValueValidationException(value); //the value is too high TODO add a custom error message, now that we can
			}
			final V step = getStep(); //get the step value
			if(step != null) { //if a step is provided
				final V base = minimum != null ? minimum : (maximum != null ? maximum : null); //determine the base
				if(!isValidStep(value, step, base)) { //if the value is not a valid step away from the base
					throwInvalidValueValidationException(value); //the value is off step TODO add a custom error message, now that we can
				}
			}
		}
	}

	/**
	 * Determines whether the given value falls on the correct step amount relative to the base value.
	 * @param value The value to validate.
	 * @param step The step value.
	 * @param base The base (either the minimum or maximum value), or <code>null</code> if zero should be used as a base.
	 * @return <code>true</code> if the value is a valid step away from the given base.
	 */
	protected abstract boolean isValidStep(final V value, final V step, final V base);

}
