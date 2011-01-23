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

import java.math.BigDecimal;

import static com.globalmentor.java.Numbers.*;

/**A range validator for decimal numbers such as floating point numbers that uses {@link java.math.BigInteger} for validation accuracy.
The step value is considered relative either to the minimum value, if available, the maximum value, if available, or zero, in that order or priority.
@param <V> The value type this validator supports.
@author Garret Wilson
@see Comparable
*/
public class DecimalRangeValidator<V extends Number & Comparable<V>> extends AbstractComparableRangeValidator<V>
{

	/**Default constructor with no value required and a step of one.*/
	public DecimalRangeValidator()
	{
		this(false);	//construct the class and don't required non-null values
	}

	/**Value required constructor with a step of one.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	*/
	public DecimalRangeValidator(final boolean valueRequired)
	{
		this(null, null, valueRequired);	//construct the class with no minimum or maximum value
	}

	/**Minimum, and maximum constructor with no step.
	@param minimum The minimum value, inclusive, or <code>null</code> if the range has no lower bound.
	@param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	*/
	public DecimalRangeValidator(final V minimum, final V maximum)
	{
		this(minimum, maximum, null);	//construct the class with no step
	}

	/**Minimum, maximum, and step constructor.
	@param minimum The minimum value, inclusive, or <code>null</code> if the range has no lower bound.
	@param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	@param step The step amount, or <code>null</code> if the range has no increment value specified.
	*/
	public DecimalRangeValidator(final V minimum, final V maximum, final V step)
	{
		this(minimum, maximum, step, false);	//construct the class and don't required non-null values
	}

	/**Minimum, maximum, and value required constructor.
	@param minimum The minimum value, inclusive, or <code>null</code> if the range has no lower bound.
	@param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	*/
	public DecimalRangeValidator(final V minimum, final V maximum, final boolean valueRequired)
	{
		this(minimum, maximum, null, valueRequired);	//construct the class with no step
	}

	/**Minimum, maximum, step, and value required constructor.
	@param minimum The minimum value, inclusive, or <code>null</code> if the range has no lower bound.
	@param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	@param step The step amount, or <code>null</code> if the range has no increment value specified.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	*/
	public DecimalRangeValidator(final V minimum, final V maximum, final V step, final boolean valueRequired)
	{
		super(minimum, maximum, step, valueRequired);	//construct the parent class
	}

	/**Determines whether the given value falls on the correct step amount relative to the base value.
	@param value The value to validate.
	@param step The step value.
	@param base The base (either the minimum or maximum value), or <code>null</code> if zero should be used as a base.
	@return <code>true</code> if the value is a valid step away from the given base.
	*/ 
	protected boolean isValidStep(final V value, final V step, final V base)
	{
		final BigDecimal bigBase=base!=null ? toBigDecimal(base) : new BigDecimal(0);	//default to zero for the base
		final double bigFactor=toBigDecimal(value).subtract(bigBase).divide(toBigDecimal(step)).doubleValue();	//get the number of times the step goes into the normalized value
		return bigFactor==Math.floor(bigFactor);	//see if the step goes into the normalized value a whole number of times
	}

}
