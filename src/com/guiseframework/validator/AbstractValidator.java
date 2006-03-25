package com.guiseframework.validator;

import static java.text.MessageFormat.*;
import static com.guiseframework.GuiseResourceConstants.*;

import java.util.MissingResourceException;

import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.GuiseSession;
import com.guiseframework.event.GuiseBoundPropertyObject;

/**An abstract implementation of an object that can determine whether a value is valid.
@param <V> The value type this validator supports.
@author Garret Wilson
*/
public abstract class AbstractValidator<V> extends GuiseBoundPropertyObject implements Validator<V>
{

	/**Whether the value must be non-<code>null</code> in order to be considered valid.*/
	private boolean valueRequired; 

		/**@return Whether the value must be non-<code>null</code> in order to be considered valid.*/
		public boolean isValueRequired() {return valueRequired;} 
		
		/**Sets whether the value must be non-<code>null</code> in order to be considered valid.
		This is a bound property of type <code>Boolean</code>.
		@param newValueRequired <code>true</code> if the value must be non-<code>null</code> in order to be considered valid.
		@see #VALUE_REQUIRED_PROPERTY
		*/
		public void setValueRequired(final boolean newValueRequired)
		{
			if(valueRequired!=newValueRequired)	//if the value is really changing
			{
				final boolean oldValueRequired=valueRequired;	//get the current value
				valueRequired=newValueRequired;	//update the value
				firePropertyChange(VALUE_REQUIRED_PROPERTY, Boolean.valueOf(oldValueRequired), Boolean.valueOf(newValueRequired));
			}
		}
		
	/**The invalid value message text, or <code>null</code> if there is no message text.*/
	private String invalidValueMessage=null;

		/**Determines the text of the invalid value message.
		If a message is specified, it will be used; otherwise, a value will be loaded from the resources if possible.
		@return The invalid value message text, or <code>null</code> if there is no invalid value message text.
		@exception MissingResourceException if there was an error loading the value from the resources.
		@see #getInvalidValueMessageResourceKey()
		*/
		public String getInvalidValueMessage() throws MissingResourceException
		{
			return getSession().determineString(invalidValueMessage, getInvalidValueMessageResourceKey());	//get the value or the resource, if available
		}

		/**Sets the text of the invalid value message.
		This is a bound property.
		@param newInvalidValueMessage The new text of the invalid value message.
		@see Validator#INVALID_VALUE_MESSAGE_PROPERTY
		*/
		public void setInvalidValueMessage(final String newInvalidValueMessage)
		{
			if(!ObjectUtilities.equals(invalidValueMessage, newInvalidValueMessage))	//if the value is really changing
			{
				final String oldInvalidValueMessage=invalidValueMessage;	//get the old value
				invalidValueMessage=newInvalidValueMessage;	//actually change the value
				firePropertyChange(INVALID_VALUE_MESSAGE_PROPERTY, oldInvalidValueMessage, newInvalidValueMessage);	//indicate that the value changed
			}			
		}

	/**The invalid value message text resource key, or <code>null</code> if there is no invalid value message text resource specified.*/
	private String invalidValueMessageResourceKey=VALIDATOR_INVALID_VALUE_MESSAGE_RESOURCE_KEY;

		/**@return The invalid value message text resource key, or <code>null</code> if there is no invalid value message text resource specified.*/
		public String getInvalidValueMessageResourceKey() {return invalidValueMessageResourceKey;}

		/**Sets the key identifying the text of the invalid value message in the resources.
		This property defaults to {@link VALIDATOR_INVALID_VALUE_MESSAGE_RESOURCE}.
		This is a bound property.
		@param newInvalidValueMessageResourceKey The new invalid value message text resource key.
		@see Validator#INVALID_VALUE_MESSAGE_RESOURCE_KEY_PROPERTY
		*/
		public void setInvalidValueMessageResourceKey(final String newInvalidValueMessageResourceKey)
		{
			if(!ObjectUtilities.equals(invalidValueMessageResourceKey, newInvalidValueMessageResourceKey))	//if the value is really changing
			{
				final String oldInvalidValueMessageResourceKey=invalidValueMessageResourceKey;	//get the old value
				invalidValueMessageResourceKey=newInvalidValueMessageResourceKey;	//actually change the value
				firePropertyChange(INVALID_VALUE_MESSAGE_RESOURCE_KEY_PROPERTY, oldInvalidValueMessageResourceKey, newInvalidValueMessageResourceKey);	//indicate that the value changed
			}
		}
		
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
		super(session);	//construct the parent class
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
				throw new ValidationException(getSession().getStringResource(VALIDATOR_VALUE_REQUIRED_MESSAGE_RESOURCE_KEY), value);				
			}
			else	//for all other invalid values
			{
				throw new ValidationException(format(getInvalidValueMessage(), toString(value)), value);
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
