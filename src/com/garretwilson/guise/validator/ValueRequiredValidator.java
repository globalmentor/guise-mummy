package com.garretwilson.guise.validator;

/**An abstract implementation of an object that can determine whether a value is valid.
@author Garret Wilson
*/
public class ValueRequiredValidator<V> extends AbstractValidator<V>
{
	/**Determines whether the provided value is not <code>null</code>.
	@param value The value to validate.
	@return <code>true</code> if the value is not <code>null</code>.
	*/
	public boolean isValid(final V value)
	{
		return value!=null;	//a required value is invalid if it is null
	}
}
