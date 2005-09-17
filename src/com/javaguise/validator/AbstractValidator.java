package com.javaguise.validator;

import static java.text.MessageFormat.*;
import static com.javaguise.GuiseResourceConstants.*;
import com.javaguise.session.GuiseSession;
import com.garretwilson.util.Debug;

import static com.garretwilson.lang.ObjectUtilities.*;

/**An abstract implementation of an object that can determine whether a value is valid.
@param <V> The value type this validator supports.
@author Garret Wilson
*/
public abstract class AbstractValidator<V> implements Validator<V>
{

	/**The Guise session that owns this validator.*/
	private final GuiseSession session;

		/**@return The Guise session that owns this validator.*/
		public GuiseSession getSession() {return session;}

	/**Whether the value must be non-<code>null</code> in order to be considered valid.*/
	private boolean valueRequired; 

		/**@return Whether the value must be non-<code>null</code> in order to be considered valid.*/
		public boolean isValueRequired() {return valueRequired;} 

	/**Session constructor with no value required.
	@param session The Guise session that owns this validator.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public AbstractValidator(final GuiseSession session)
	{
		this(session, false);	//construct the class and don't required non-null values
	}

	/**Session and value required constructor.
	@param session The Guise session that owns this validator.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public AbstractValidator(final GuiseSession session, final boolean valueRequired)
	{
		this.session=checkNull(session, "Session cannot be null");	//save the session
		this.valueRequired=valueRequired;	//save the value required specification
	}

	/**Checks whether a given value is valid, and throws an exception if not
	@param value The value to validate.
	@exception ValidationException if the provided value is not valid.
	*/
	public void validate(final V value) throws ValidationException
	{
		if(!isValid(value))	//if the given value is not valid
		{
			if(value==null && isValueRequired())	//if the value is invalid because it didn't mean the required requirement
			{
				throw new ValidationException(getSession().getStringResource(VALIDATOR_VALUE_REQUIRED_MESSAGE_RESOURCE), value);				
			}
			else	//for all other invalid values
			{
				throw new ValidationException(format(getSession().getStringResource(VALIDATOR_INVALID_VALUE_MESSAGE_RESOURCE), toString(value)), value);
			}
		}
	}

	/**Determines whether a given value is valid.
	This version checks whether a value is provided if values are required.
	Child classes should call this version as a convenience for checking non-<code>null</code> and required status.
	@param value The value to validate.
	@return <code>true</code> if a value is given or a value is not required, else <code>false</code>.
	*/
	public boolean isValid(final V value)
	{
		return value!=null || !isValueRequired();	//if the value is not present, it's still valid if we don't require a value
	}

	/**Retrieves a string representation of the given value appropriate for error messages.
	This implementation returns the {@link Object#toString()} string representation of the value.
	@param value The value for which a string representation should be returned.
	@return A string representation of the given value.
	*/
	protected String toString(final V value)
	{
		return value.toString();	//return to toString() version by default
	}
}
