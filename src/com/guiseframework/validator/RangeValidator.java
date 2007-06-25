package com.guiseframework.validator;

/**A validator restricting a value to a range.
The step value is considered relative either to the minimum value, if available, the maximum value, if available, or zero, in that order or priority.
@param <V> The value type this validator supports.
@author Garret Wilson
*/
public interface RangeValidator<V> extends Validator<V>
{

	/**@return The minimum value, inclusive, or <code>null</code> if the range has no lower bound.*/
	public V getMinimum();

	/**@return The maximum value, inclusive, or <code>null</code> if the range has no upper bound.*/
	public V getMaximum();

	/**@return The step amount, or <code>null</code> if the range has no increment value specified.*/
	public V getStep();

}
