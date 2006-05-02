package com.guiseframework.validator;

import static com.garretwilson.lang.ClassUtilities.*;

import com.garretwilson.beans.PropertyBindable;
import com.guiseframework.GuiseResourceConstants;
import com.guiseframework.GuiseSession;

/**Indicates an object that can determine whether a value is valid.
The invalid value message should be in the form "Invalid value: '{0}'.", where "{0}" represents the invalid value.
@param <V> The value type this validator supports.
@author Garret Wilson
@see GuiseResourceConstants#VALIDATOR_INVALID_VALUE_MESSAGE_RESOURCE_REFERENCE
@see GuiseResourceConstants#VALIDATOR_VALUE_REQUIRED_MESSAGE_RESOURCE_REFERENCE
*/
public interface Validator<V> extends PropertyBindable
{

	/**The invalid value message bound property.*/
	public final static String INVALID_VALUE_MESSAGE_PROPERTY=getPropertyName(Validator.class, "invalidValueMessage");
	/**The value required message bound property.*/
	public final static String VALUE_REQUIRED_MESSAGE_PROPERTY=getPropertyName(Validator.class, "valueRequiredMessage");
	/**The value required bound property.*/
	public final static String VALUE_REQUIRED_PROPERTY=getPropertyName(Validator.class, "valueRequired");

	/**@return The invalid value message text, which may include a resource reference.*/
	public String getInvalidValueMessage();

	/**Sets the text of the invalid value message.
	This is a bound property.
	@param newInvalidValueMessage The new text of the invalid value message, which may include a resource reference.
	@exception NullPointerException if the given message is <code>null</code>.
	@see #INVALID_VALUE_MESSAGE_PROPERTY
	*/
	public void setInvalidValueMessage(final String newInvalidValueMessage);

	/**@return The value required message text, which may include a resource reference.*/
	public String getValueRequiredMessage();

	/**Sets the text of the value required message.
	This is a bound property.
	@param newValueRequiredMessage The new text of the value required message, which may include a resource reference..
	@exception NullPointerException if the given message is <code>null</code>.
	@see #VALUE_REQUIRED_VALUE_MESSAGE_PROPERTY
	*/
	public void setValueRequiredMessage(final String newValueRequiredMessage);
	
	/**@return The Guise session that owns this validator.*/
	public GuiseSession getSession();

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
