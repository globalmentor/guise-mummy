package com.guiseframework.validator;

import static java.text.MessageFormat.*;
import static com.guiseframework.GuiseResourceConstants.*;

import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.GuiseResourceConstants;
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
		
	/**The invalid value message text, or <code>null</code> if there is no invalid value message text.*/
	private String invalidValueMessage=null;

		/**@return The invalid value message text, or <code>null</code> if there is no invalid value message text.*/
		public String getInvalidValueMessage() {return invalidValueMessage;}

		/**Sets the text of the invalid value message.
		This is a bound property.
		@param newInvalidValueMessage The new text of the invalid value message.
		@see #INVALID_VALUE_MESSAGE_PROPERTY
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
		This property defaults to {@link GuiseResourceConstants#VALIDATOR_INVALID_VALUE_MESSAGE_RESOURCE_KEY}.
		This is a bound property.
		@param newInvalidValueMessageResourceKey The new invalid value message text resource key.
		@see #INVALID_VALUE_MESSAGE_RESOURCE_KEY_PROPERTY
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

	/**The value required message text, or <code>null</code> if there is no value required message text.*/
	private String valueRequiredMessage=null;

		/**@return The value required message text, or <code>null</code> if there is no value required message text.*/
		public String getValueRequiredMessage() {return valueRequiredMessage;}

		/**Sets the text of the value required message.
		This is a bound property.
		@param newValueRequiredMessage The new text of the value required message.
		@see #VALUE_REQUIRED_VALUE_MESSAGE_PROPERTY
		*/
		public void setValueRequiredMessage(final String newValueRequiredMessage)
		{
			if(!ObjectUtilities.equals(valueRequiredMessage, newValueRequiredMessage))	//if the value is really changing
			{
				final String oldValueRequiredMessage=valueRequiredMessage;	//get the old value
				valueRequiredMessage=newValueRequiredMessage;	//actually change the value
				firePropertyChange(VALUE_REQUIRED_MESSAGE_PROPERTY, oldValueRequiredMessage, newValueRequiredMessage);	//indicate that the value changed
			}			
		}

	/**The value required message text resource key, or <code>null</code> if there is no value required message text resource specified.*/
	private String valueRequiredMessageResourceKey=VALIDATOR_VALUE_REQUIRED_MESSAGE_RESOURCE_KEY;

		/**@return The value required message text resource key, or <code>null</code> if there is no value required message text resource specified.*/
		public String getValueRequiredMessageResourceKey() {return valueRequiredMessageResourceKey;}

		/**Sets the key identifying the text of the value required message in the resources.
		This property defaults to {@link GuiseResourceConstants#VALIDATOR_VALUE_REQUIRED_MESSAGE_RESOURCE_KEY}.
		This is a bound property.
		@param newValueRequiredMessageResourceKey The new value required message text resource key.
		@see #VALUE_REQUIRED_MESSAGE_RESOURCE_KEY_PROPERTY
		*/
		public void setValueRequiredMessageResourceKey(final String newValueRequiredMessageResourceKey)
		{
			if(!ObjectUtilities.equals(valueRequiredMessageResourceKey, newValueRequiredMessageResourceKey))	//if the value is really changing
			{
				final String oldValueRequiredMessageResourceKey=valueRequiredMessageResourceKey;	//get the old value
				valueRequiredMessageResourceKey=newValueRequiredMessageResourceKey;	//actually change the value
				firePropertyChange(VALUE_REQUIRED_MESSAGE_RESOURCE_KEY_PROPERTY, oldValueRequiredMessageResourceKey, newValueRequiredMessageResourceKey);	//indicate that the value changed
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
				final String valueRequiredMessage=getSession().determineString(getValueRequiredMessage(), getValueRequiredMessageResourceKey());	//get the value required message to use
				throw new ValidationException(valueRequiredMessage, value);				
			}
			else	//for all other invalid values
			{
				final String invalidValueMessage=getSession().determineString(getInvalidValueMessage(), getInvalidValueMessageResourceKey());	//get the invalid value message to use
				throw new ValidationException(format(invalidValueMessage, toString(value)), value);	//format the message based upon the value
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
