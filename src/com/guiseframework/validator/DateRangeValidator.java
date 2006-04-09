package com.guiseframework.validator;

import java.util.Date;

/**A range validator for dates.
The step value is considered relative either to the minimum value, if available, the maximum value, if available, or zero, in that order or priority.
@author Garret Wilson
*/
public class DateRangeValidator extends AbstractComparableRangeValidator<Date>
{

	/**Default constructor with no value required and a step of one.*/
	public DateRangeValidator()
	{
		this(false);	//construct the class and don't required non-null values
	}

	/**Value required constructor with a step of one.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	*/
	public DateRangeValidator(final boolean valueRequired)
	{
		this(null, null, valueRequired);	//construct the class with no minimum or maximum value
	}

	/**Minimum, and maximum constructor with a step of one.
	@param minimum The minimum value, inclusive, or <code>null</code> if the range has no lower bound.
	@param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	*/
	public DateRangeValidator(final Date minimum, final Date maximum)
	{
		this(minimum, maximum, new Date(1));	//construct the class with a step of 1
	}

	/**Minimum, maximum, and step constructor.
	@param minimum The minimum value, inclusive, or <code>null</code> if the range has no lower bound.
	@param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	@param step The step amount, or <code>null</code> if the range has no increment value specified.
	*/
	public DateRangeValidator(final Date minimum, final Date maximum, final Date step)
	{
		this(minimum, maximum, step, false);	//construct the class and don't required non-null values
	}

	/**Minimum, maximum, and value required constructor.
	@param minimum The minimum value, inclusive, or <code>null</code> if the range has no lower bound.
	@param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	*/
	public DateRangeValidator(final Date minimum, final Date maximum, final boolean valueRequired)
	{
		this(minimum, maximum, null, valueRequired);	//construct the class with no step
	}

	/**Minimum, maximum, step, and value required constructor.
	@param minimum The minimum value, inclusive, or <code>null</code> if the range has no lower bound.
	@param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	@param step The step amount, or <code>null</code> if the range has no increment value specified.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	*/
	public DateRangeValidator(final Date minimum, final Date maximum, final Date step, final boolean valueRequired)
	{
		super(minimum, maximum, step, valueRequired);	//construct the parent class
	}

	/**Determines whether the given value falls on the correct step amount relative to the base value.
	@param value The value to validate.
	@param step The step value.
	@param base The base (either the minimum or maximum value), or <code>null</code> if zero should be used as a base.
	@return <code>true</code> if the value is a valid step away from the given base.
	*/ 
	protected boolean isValidStep(final Date value, final Date step, final Date base)
	{
		final long baseTime=base!=null ? base.getTime() : 0;	//get the primitive base value
		return (value.getTime()-baseTime)%step.getTime()==0;	//normalize the value to the base and see if the step divides the result evenly
	}

}
