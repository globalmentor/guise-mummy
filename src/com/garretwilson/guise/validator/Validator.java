package com.garretwilson.guise.validator;

/**Indicates an object that can determine whether a value is valid.
@param <V> The value type this validator supports.
@author Garret Wilson
*/
public interface Validator<V>
{
	/**Determines whether a given value is valid.
	@param value The value to validate.
	@return <code>true</code> if the value is valid, else <code>false</code>.
	*/
	public boolean isValid(final V value);

	/**Checks whether a given value is valid, and throws an exception if not
	@param value The value to validate.
	@exception ValidationException if the provided value is not valid.
	*/
	public void validate(final V value) throws ValidationException;

}
