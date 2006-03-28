package com.guiseframework.validator;

import static com.garretwilson.lang.ClassUtilities.*;

import java.util.MissingResourceException;

import com.garretwilson.beans.PropertyBindable;
import com.guiseframework.GuiseResourceConstants;
import com.guiseframework.GuiseSession;

/**Indicates an object that can determine whether a value is valid.
The invalid value message should be in the form "Invalid value: '{0}'.", where "{0}" represents the invalid value.
@param <V> The value type this validator supports.
@author Garret Wilson
@see GuiseResourceConstants#VALIDATOR_INVALID_VALUE_MESSAGE_RESOURCE_KEY
@see GuiseResourceConstants#VALIDATOR_VALUE_REQUIRED_MESSAGE_RESOURCE_KEY
*/
public interface Validator<V> extends PropertyBindable
{

	/**The invalid value message bound property.*/
	public final static String INVALID_VALUE_MESSAGE_PROPERTY=getPropertyName(Validator.class, "invalidValueMessage");
	/**The invalid value message resource key bound property.*/
	public final static String INVALID_VALUE_MESSAGE_RESOURCE_KEY_PROPERTY=getPropertyName(Validator.class, "invalidValueMessageResourceKey");
	/**The value required message bound property.*/
	public final static String VALUE_REQUIRED_MESSAGE_PROPERTY=getPropertyName(Validator.class, "valueRequiredMessage");
	/**The value requiretd message resource key bound property.*/
	public final static String VALUE_REQUIRED_MESSAGE_RESOURCE_KEY_PROPERTY=getPropertyName(Validator.class, "valueRequiredMessageResourceKey");
	/**The value required bound property.*/
	public final static String VALUE_REQUIRED_PROPERTY=getPropertyName(Validator.class, "valueRequired");

	/**Determines the text of the invalid value message.
	If a message is specified, it will be used; otherwise, a value will be loaded from the resources if possible.
	@return The invalid value message text, or <code>null</code> if there is no invalid value message text.
	@exception MissingResourceException if there was an error loading the value from the resources.
	@see #getInvalidValueMessageResourceKey()
	*/
	public String getInvalidValueMessage() throws MissingResourceException;

	/**Sets the text of the invalid value message.
	This is a bound property.
	@param newInvalidValueMessage The new text of the invalid value message.
	@see Validator#INVALID_VALUE_MESSAGE_PROPERTY
	*/
	public void setInvalidValueMessage(final String newInvalidValueMessage);

	/**@return The invalid value message text resource key, or <code>null</code> if there is no invalid value message text resource specified.*/
	public String getInvalidValueMessageResourceKey();

	/**Sets the key identifying the text of the invalid value message in the resources.
	This property defaults to {@link GuiseResourceConstants#VALIDATOR_INVALID_VALUE_MESSAGE_RESOURCE_KEY}.
	This is a bound property.
	@param newInvalidValueMessageResourceKey The new invalid value message text resource key.
	@see #INVALID_VALUE_MESSAGE_RESOURCE_KEY_PROPERTY
	*/
	public void setInvalidValueMessageResourceKey(final String newInvalidValueMessageResourceKey);

	/**@return The value required message text, or <code>null</code> if there is no value required message text.*/
	public String getValueRequiredMessage();

	/**Sets the text of the value required message.
	This is a bound property.
	@param newValueRequiredMessage The new text of the value required message.
	@see #VALUE_REQUIRED_VALUE_MESSAGE_PROPERTY
	*/
	public void setValueRequiredMessage(final String newValueRequiredMessage);

	/**@return The value required message text resource key, or <code>null</code> if there is no value required message text resource specified.*/
	public String getValueRequiredMessageResourceKey();

	/**Sets the key identifying the text of the value required message in the resources.
	This property defaults to {@link VALIDATOR_VALUE_REQUIRED_MESSAGE_RESOURCE_KEY}.
	This is a bound property.
	@param newValueRequiredMessageResourceKey The new value required message text resource key.
	@see #VALUE_REQUIRED_MESSAGE_RESOURCE_KEY_PROPERTY
	*/
	public void setValueRequiredMessageResourceKey(final String newValueRequiredMessageResourceKey);
	
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
