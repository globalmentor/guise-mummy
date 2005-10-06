package com.javaguise.validator;

import com.javaguise.session.GuiseSession;

/**An abstract implementation of a validator restricted to a range.
The step value is considered relative either to the minimum value, if available, the maximum value, if available, or zero, in that order or priority.
@param <V> The value type this validator supports.
@author Garret Wilson
*/
public abstract class AbstractRangeValidator<V extends Number> extends AbstractValidator<V> implements RangeValidator<V>
{
	
	/**The minimum value, inclusive, or <code>null</code> if the range has no lower bound.*/
	private final V minimum;

		/**@return The minimum value, inclusive, or <code>null</code> if the range has no lower bound.*/
		public V getMinimum() {return minimum;}

	/**The maximum value, inclusive, or <code>null</code> if the range has no upper bound.*/
	private final V maximum;

		/**@return The maximum value, inclusive, or <code>null</code> if the range has no upper bound.*/
		public V getMaximum() {return maximum;}

	/**The step amount, or <code>null</code> if the range has no increment value specified.*/
	private final V step;

		/**@return The step amount, or <code>null</code> if the range has no increment value specified.*/
		public V getStep() {return step;}

	/**Session, minimum, maximum, step, and value required constructor.
	@param session The Guise session that owns this validator.
	@param minimum The minimum value, inclusive, or <code>null</code> if the range has no lower bound.
	@param maximum The maximum value, inclusive, or <code>null</code> if the range has no upper bound.
	@param step The step amount, or <code>null</code> if the range has no increment value specified.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public AbstractRangeValidator(final GuiseSession session, final V minimum, final V maximum, final V step, final boolean valueRequired)
	{
		super(session, valueRequired);	//construct the parent class
		this.minimum=minimum;
		this.maximum=maximum;
		this.step=step;
	}

}
