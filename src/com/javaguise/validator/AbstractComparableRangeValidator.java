package com.javaguise.validator;

import com.javaguise.GuiseSession;

/**An abstract implementation of a range validator that handles comparable values.
The step value is considered relative either to the minimum value, if available, the maximum value, if available, or zero, in that order or priority.
@param <V> The value type this validator supports.
@author Garret Wilson
@see Comparable
*/
public abstract class AbstractComparableRangeValidator<V extends Comparable<V>> extends AbstractRangeValidator<V>
{
	
	/**Session, minimum, maximum, step, and value required constructor.
	@param session The Guise session that owns this validator.
	@param minimum The minimum value, inclusive, or <code>null</code> if the range has no lower bound.
	@param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	@param step The step amount, or <code>null</code> if the range has no increment value specified.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public AbstractComparableRangeValidator(final GuiseSession session, final V minimum, final V maximum, final V step, final boolean valueRequired)
	{
		super(session, minimum, maximum, step, valueRequired);	//construct the parent class
	}

	/**Determines whether a given value is within the specified range.
	This version delgates to the super class version to determine whether <code>null</code> values are allowed.
	This version checks for minimum and maximum compliance, and delegates to {@link #isValidStep(Number, Number, Number)} for checking step compliance.
	Child classes will normally not override this class and instead merely implement {@link #isValidStep(Number, Number, Number)}.
	@param value The value to validate.
	@return <code>true</code> if the value falls meets the range requirements and fulfills the required requirement, if any, else <code>false</code>.
	*/
	public boolean isValid(final V value)
	{
		if(!super.isValid(value))	//if the value doesn't pass the default tests
		{
			return false;	//the value isn't valid
		}
		if(value!=null)	//if there is a value (the super class has already checked for null compliance)
		{
			final V minimum=getMinimum();	//get the minimum value
			if(minimum!=null && minimum.compareTo(value)>0)	//if the value is too small
			{
				return false;	//the value is too low
			}
			final V maximum=getMaximum();	//get the maximum value
			if(maximum!=null && maximum.compareTo(value)<0)	//if the value is too large
			{
				return false;	//the value is too high
			}
			final V step=getStep();	//get the step value
			if(step!=null)	//if a step is provided
			{
				final V base=minimum!=null ? minimum : (maximum!=null ? maximum : null);	//determine the base
				if(!isValidStep(value, step, base))	//if the value is not a valid step away from the base
				{
					return false;	//the value is off step
				}
			}
		}
		return true;	//the value passed all tests
	}

	/**Determines whether the given value falls on the correct step amount relative to the base value.
	@param value The value to validate.
	@param step The step value.
	@param base The base (either the minimum or maximum value), or <code>null</code> if zero should be used as a base.
	@return <code>true</code> if the value is a valid step away from the given base.
	*/ 
	protected abstract boolean isValidStep(final V value, final V step, final V base);

}
