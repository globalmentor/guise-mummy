package com.guiseframework.validator;

import java.math.BigDecimal;

import static com.garretwilson.lang.NumberUtilities.*;

import com.guiseframework.GuiseSession;

/**A range validator for decimal numbers such as floating point numbers that uses {@link java.math.BigInteger} for validation accuracy.
The step value is considered relative either to the minimum value, if available, the maximum value, if available, or zero, in that order or priority.
@param <V> The value type this validator supports.
@author Garret Wilson
@see Comparable
*/
public class DecimalRangeValidator<V extends Number & Comparable<V>> extends AbstractComparableRangeValidator<V>
{

	/**Session constructor with no value required and a step of one.
	@param session The Guise session that owns this validator.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public DecimalRangeValidator(final GuiseSession session)
	{
		this(session, false);	//construct the class and don't required non-null values
	}

	/**Session and value required constructor with a step of one.
	@param session The Guise session that owns this validator.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public DecimalRangeValidator(final GuiseSession session, final boolean valueRequired)
	{
		this(session, null, null, valueRequired);	//construct the class with no minimum or maximum value
	}

	/**Session, minimum, and maximum constructor with no step.
	@param session The Guise session that owns this validator.
	@param minimum The minimum value, inclusive, or <code>null</code> if the range has no lower bound.
	@param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public DecimalRangeValidator(final GuiseSession session, final V minimum, final V maximum)
	{
		this(session, minimum, maximum, null);	//construct the class with no step
	}

	/**Session, minimum, maximum, and step constructor.
	@param session The Guise session that owns this validator.
	@param minimum The minimum value, inclusive, or <code>null</code> if the range has no lower bound.
	@param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	@param step The step amount, or <code>null</code> if the range has no increment value specified.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public DecimalRangeValidator(final GuiseSession session, final V minimum, final V maximum, final V step)
	{
		this(session, minimum, maximum, step, false);	//construct the class and don't required non-null values
	}

	/**Session, minimum, maximum, and value required constructor.
	@param session The Guise session that owns this validator.
	@param minimum The minimum value, inclusive, or <code>null</code> if the range has no lower bound.
	@param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public DecimalRangeValidator(final GuiseSession session, final V minimum, final V maximum, final boolean valueRequired)
	{
		this(session, minimum, maximum, null, valueRequired);	//construct the class with no step
	}

	/**Session, minimum, maximum, step, and value required constructor.
	@param session The Guise session that owns this validator.
	@param minimum The minimum value, inclusive, or <code>null</code> if the range has no lower bound.
	@param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	@param step The step amount, or <code>null</code> if the range has no increment value specified.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public DecimalRangeValidator(final GuiseSession session, final V minimum, final V maximum, final V step, final boolean valueRequired)
	{
		super(session, minimum, maximum, step, valueRequired);	//construct the parent class
	}

	/**Determines whether the given value falls on the correct step amount relative to the base value.
	@param value The value to validate.
	@param step The step value.
	@param base The base (either the minimum or maximum value), or <code>null</code> if zero should be used as a base.
	@return <code>true</code> if the value is a valid step away from the given base.
	*/ 
	protected boolean isValidStep(final V value, final V step, final V base)
	{
		final BigDecimal bigBase=base!=null ? asBigDecimal(base) : new BigDecimal(0);	//default to zero for the base
		final double bigFactor=asBigDecimal(value).subtract(bigBase).divide(asBigDecimal(step)).doubleValue();	//get the number of times the step goes into the normalized value
		return bigFactor==Math.floor(bigFactor);	//see if the step goes into the normalized value a whole number of times
	}

}
