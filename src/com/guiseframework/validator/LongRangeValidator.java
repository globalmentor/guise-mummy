package com.guiseframework.validator;

/**A range validator for longs.
The step value is considered relative either to the minimum value, if available, the maximum value, if available, or zero, in that order or priority.
@author Garret Wilson
*/
public class LongRangeValidator extends AbstractComparableRangeValidator<Long>
{

	/**Default constructor with no value required and a step of one.*/
	public LongRangeValidator()
	{
		this(false);	//construct the class and don't required non-null values
	}

	/**Value required constructor with a step of one.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	*/
	public LongRangeValidator(final boolean valueRequired)
	{
		this(null, null, valueRequired);	//construct the class with no minimum or maximum value
	}

	/**Maximum constructor with a minimum value of zero and a step of one.
	@param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	*/
	public LongRangeValidator(final Long maximum)
	{
		this(Long.valueOf(0), maximum);	//construct the class with a minimum value of 0
	}

	/**Minimum and maximum constructor with a step of one.
	@param minimum The minimum value, inclusive, or <code>null</code> if the range has no lower bound.
	@param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	*/
	public LongRangeValidator(final Long minimum, final Long maximum)
	{
		this(minimum, maximum, Long.valueOf(1));	//construct the class with a step of 1
	}

	/**Minimum, maximum, and step constructor.
	@param minimum The minimum value, inclusive, or <code>null</code> if the range has no lower bound.
	@param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	@param step The step amount, or <code>null</code> if the range has no increment value specified.
	*/
	public LongRangeValidator(final Long minimum, final Long maximum, final Long step)
	{
		this(minimum, maximum, step, false);	//construct the class and don't required non-null values
	}

	/**Minimum and value required constructor with a minimum value of zero and a step of one.
	@param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	*/
	public LongRangeValidator(final Long maximum, final boolean valueRequired)
	{
		this(Long.valueOf(0), maximum, valueRequired);	//construct the class with a minimum value of 0
	}

	/**Minimum, maximum, and value required constructor with a step of one.
	@param minimum The minimum value, inclusive, or <code>null</code> if the range has no lower bound.
	@param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	*/
	public LongRangeValidator(final Long minimum, final Long maximum, final boolean valueRequired)
	{
		this(minimum, maximum, Long.valueOf(1), valueRequired);	//construct the class with no step
	}

	/**Minimum, maximum, step, and value required constructor.
	@param minimum The minimum value, inclusive, or <code>null</code> if the range has no lower bound.
	@param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	@param step The step amount, or <code>null</code> if the range has no increment value specified.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	*/
	public LongRangeValidator(final Long minimum, final Long maximum, final Long step, final boolean valueRequired)
	{
		super(minimum, maximum, step, valueRequired);	//construct the parent class
	}

	/**Determines whether the given value falls on the correct step amount relative to the base value.
	@param value The value to validate.
	@param step The step value.
	@param base The base (either the minimum or maximum value), or <code>null</code> if zero should be used as a base.
	@return <code>true</code> if the value is a valid step away from the given base.
	*/ 
	protected boolean isValidStep(final Long value, final Long step, final Long base)
	{
		final int baseInt=base!=null ? base.intValue() : 0;	//get the primitive base value
		return (value.intValue()-baseInt)%step.intValue()==0;	//normalize the value to the base and see if the step divides the result evenly
	}

}
