/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guiseframework.validator;

import static java.text.MessageFormat.*;

import static com.globalmentor.java.Objects.*;

import static com.guiseframework.Resources.*;
import com.guiseframework.event.GuiseBoundPropertyObject;

/**
 * An abstract implementation of an object that can determine whether a value is valid.
 * @param <V> The value type this validator supports.
 * @author Garret Wilson
 */
public abstract class AbstractValidator<V> extends GuiseBoundPropertyObject implements Validator<V> {

	/** Whether the value must be non-<code>null</code> in order to be considered valid. */
	private boolean valueRequired;

	/** @return Whether the value must be non-<code>null</code> in order to be considered valid. */
	public boolean isValueRequired() {
		return valueRequired;
	}

	/**
	 * Sets whether the value must be non-<code>null</code> in order to be considered valid. This is a bound property of type <code>Boolean</code>.
	 * @param newValueRequired <code>true</code> if the value must be non-<code>null</code> in order to be considered valid.
	 * @see #VALUE_REQUIRED_PROPERTY
	 */
	public void setValueRequired(final boolean newValueRequired) {
		if(valueRequired != newValueRequired) { //if the value is really changing
			final boolean oldValueRequired = valueRequired; //get the current value
			valueRequired = newValueRequired; //update the value
			firePropertyChange(VALUE_REQUIRED_PROPERTY, Boolean.valueOf(oldValueRequired), Boolean.valueOf(newValueRequired));
		}
	}

	/** The invalid value message text, which may include a resource reference. */
	private String invalidValueMessage = VALIDATOR_INVALID_VALUE_MESSAGE_RESOURCE_REFERENCE;

	/** @return The invalid value message text, which may include a resource reference. */
	public String getInvalidValueMessage() {
		return invalidValueMessage;
	}

	/**
	 * Sets the text of the invalid value message. This is a bound property.
	 * @param newInvalidValueMessage The new text of the invalid value message, which may include a resource reference.
	 * @throws NullPointerException if the given message is <code>null</code>.
	 * @see #INVALID_VALUE_MESSAGE_PROPERTY
	 */
	public void setInvalidValueMessage(final String newInvalidValueMessage) {
		if(!invalidValueMessage.equals(checkInstance(newInvalidValueMessage, "Invalid value message cannot be null."))) { //if the value is really changing
			final String oldInvalidValueMessage = invalidValueMessage; //get the old value
			invalidValueMessage = newInvalidValueMessage; //actually change the value
			firePropertyChange(INVALID_VALUE_MESSAGE_PROPERTY, oldInvalidValueMessage, newInvalidValueMessage); //indicate that the value changed
		}
	}

	/** The value required message text, which may include a resource reference. */
	private String valueRequiredMessage = VALIDATOR_VALUE_REQUIRED_MESSAGE_RESOURCE_REFERENCE;

	/** @return The value required message text, which may include a resource reference. */
	public String getValueRequiredMessage() {
		return valueRequiredMessage;
	}

	/**
	 * Sets the text of the value required message. This is a bound property.
	 * @param newValueRequiredMessage The new text of the value required message, which may include a resource reference.
	 * @throws NullPointerException if the given message is <code>null</code>.
	 * @see Validator#VALUE_REQUIRED_MESSAGE_PROPERTY
	 */
	public void setValueRequiredMessage(final String newValueRequiredMessage) {
		if(!valueRequiredMessage.equals(checkInstance(newValueRequiredMessage, "Value required message cannot be null."))) { //if the value is really changing
			final String oldValueRequiredMessage = valueRequiredMessage; //get the old value
			valueRequiredMessage = newValueRequiredMessage; //actually change the value
			firePropertyChange(VALUE_REQUIRED_MESSAGE_PROPERTY, oldValueRequiredMessage, newValueRequiredMessage); //indicate that the value changed
		}
	}

	/**
	 * Throws a validation exception with a message indicating that the given value is invalid.
	 * @param value The value being validated.
	 * @throws ValidationException to indicate that the given value is invalid.
	 * @see #getInvalidValueMessage()
	 */
	public void throwInvalidValueValidationException(final V value) throws ValidationException {
		final String invalidValueMessage = getSession().dereferenceString(getInvalidValueMessage()); //get the invalid value message to use
		throw new ValidationException(format(invalidValueMessage, toString(value)), value); //format the message based upon the value			
	}

	/**
	 * Throws a validation exception with a message indicating that a valid is required.
	 * @param value The value being validated.
	 * @throws ValidationException to indicate that a value is required.
	 * @see #getValueRequiredMessage()
	 */
	public void throwValueRequiredValidationException(final V value) throws ValidationException {
		final String valueRequiredMessage = getSession().dereferenceString(getValueRequiredMessage()); //get the value required message to use
		throw new ValidationException(valueRequiredMessage, value);
	}

	/** Default constructor with no value required. */
	public AbstractValidator() {
		this(false); //construct the class and don't required non-null values
	}

	/**
	 * Value required constructor.
	 * @param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	 */
	public AbstractValidator(final boolean valueRequired) {
		this.valueRequired = valueRequired; //save the value required specification
	}

	/**
	 * Checks whether a given value is valid, and throws an exception if not.
	 * <p>
	 * The message of the thrown exception should be appropriate for display to the user, although it may include string resource references. If a child class has
	 * no specific message to return, that class may call {@link #throwInvalidValueValidationException(Object)} as a convenience. A child class may also call
	 * {@link #throwValueRequiredValidationException(Object)} as a convenience, but this is usually not required if this version of the method, which provides a
	 * missing value check, is called first.
	 * </p>
	 * <p>
	 * This version checks whether a value is provided if values are required. Child classes should call this version as a convenience for checking non-
	 * <code>null</code> and required status.
	 * </p>
	 * <p>
	 * Adding new validation logic always requires overriding this method. Although {@link #isValid(Object)} may be overridden to provide optimized fast-fail
	 * determinations, adding new logic to {@link #isValid(Object)} cannot be used in place of overriding this method.
	 * </p>
	 * @param value The value to validate, which may be <code>null</code>.
	 * @throws ValidationException if the provided value is not valid.
	 * @see #throwInvalidValueValidationException(Object)
	 * @see #throwValueRequiredValidationException(Object)
	 */
	public void validate(final V value) throws ValidationException {
		if(value == null && isValueRequired()) { //if there is no value but a value is required
			throwValueRequiredValidationException(value); //indicate that a value is required
		}
	}

	/**
	 * Determines whether a given value is valid. This convenience version calls {@link #validate(Object)}, returning <code>false</code> only if an exception is
	 * thrown. Although this method may be overridden to provide optimized fast-fail determinations, adding new logic to this method cannot be used in place of
	 * overriding {@link #validate(Object)}.
	 * @param value The value to validate.
	 * @return <code>true</code> if a value is given and the value is valid; or a value is not required, else <code>false</code>.
	 */
	public boolean isValid(final V value) {
		try {
			validate(value); //validate the value
			return true; //indicate that the value validated
		} catch(final ValidationException validationException) { //if the value didn't validate
			return false; //indicate that the value is not valid
		}
	}

	/**
	 * Retrieves a string representation of the given value appropriate for error messages. This implementation returns the {@link Object#toString()} string
	 * representation of the value.
	 * @param value The value for which a string representation should be returned.
	 * @return A string representation of the given value.
	 */
	protected String toString(final V value) {
		return value.toString(); //return to toString() version by default
	}
}
