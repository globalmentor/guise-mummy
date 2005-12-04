package com.javaguise.validator;

import com.javaguise.GuiseSession;

/**A range validator for integers.
The step value is considered relative either to the minimum value, if available, the maximum value, if available, or zero, in that order or priority.
@author Garret Wilson
*/
public class IntegerRangeValidator extends AbstractComparableRangeValidator<Integer>
{

	/**Session constructor with no value required and a step of one.
	@param session The Guise session that owns this validator.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public IntegerRangeValidator(final GuiseSession session)
	{
		this(session, false);	//construct the class and don't required non-null values
	}

	/**Session and value required constructor with a step of one.
	@param session The Guise session that owns this validator.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public IntegerRangeValidator(final GuiseSession session, final boolean valueRequired)
	{
		this(session, null, null, valueRequired);	//construct the class with no minimum or maximum value
	}

	/**Session, minimum, and maximum constructor with a step of one.
	@param session The Guise session that owns this validator.
	@param minimum The minimum value, inclusive, or <code>null</code> if the range has no lower bound.
	@param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public IntegerRangeValidator(final GuiseSession session, final Integer minimum, final Integer maximum)
	{
		this(session, minimum, maximum, new Integer(1));	//construct the class with a step of 1
	}

	/**Session, minimum, maximum, and step constructor.
	@param session The Guise session that owns this validator.
	@param minimum The minimum value, inclusive, or <code>null</code> if the range has no lower bound.
	@param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	@param step The step amount, or <code>null</code> if the range has no increment value specified.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public IntegerRangeValidator(final GuiseSession session, final Integer minimum, final Integer maximum, final Integer step)
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
	public IntegerRangeValidator(final GuiseSession session, final Integer minimum, final Integer maximum, final boolean valueRequired)
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
	public IntegerRangeValidator(final GuiseSession session, final Integer minimum, final Integer maximum, final Integer step, final boolean valueRequired)
	{
		super(session, minimum, maximum, step, valueRequired);	//construct the parent class
	}

	/**Determines whether the given value falls on the correct step amount relative to the base value.
	@param value The value to validate.
	@param step The step value.
	@param base The base (either the minimum or maximum value), or <code>null</code> if zero should be used as a base.
	@return <code>true</code> if the value is a valid step away from the given base.
	*/ 
	protected boolean isValidStep(final Integer value, final Integer step, final Integer base)
	{
		final int baseInt=base!=null ? base.intValue() : 0;	//get the primitive base value
		return (value.intValue()-baseInt)%step.intValue()==0;	//normalize the value to the base and see if the step divides the result evenly
	}

}
