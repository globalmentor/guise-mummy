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

import static com.globalmentor.java.Classes.*;

import com.globalmentor.beans.PropertyBindable;
import com.guiseframework.GuiseSession;
import com.guiseframework.Resources;

/**
 * Indicates an object that can determine whether a value is valid. The invalid value message should be in the form "Invalid value: '{0}'.", where "{0}"
 * represents the invalid value.
 * @param <V> The value type this validator supports.
 * @author Garret Wilson
 * @see Resources#VALIDATOR_INVALID_VALUE_MESSAGE_RESOURCE_REFERENCE
 * @see Resources#VALIDATOR_VALUE_REQUIRED_MESSAGE_RESOURCE_REFERENCE
 */
public interface Validator<V> extends PropertyBindable {

	/** The invalid value message bound property. */
	public static final String INVALID_VALUE_MESSAGE_PROPERTY = getPropertyName(Validator.class, "invalidValueMessage");
	/** The value required message bound property. */
	public static final String VALUE_REQUIRED_MESSAGE_PROPERTY = getPropertyName(Validator.class, "valueRequiredMessage");
	/** The value required bound property. */
	public static final String VALUE_REQUIRED_PROPERTY = getPropertyName(Validator.class, "valueRequired");

	/** @return The invalid value message text, which may include a resource reference. */
	public String getInvalidValueMessage();

	/**
	 * Sets the text of the invalid value message. This is a bound property.
	 * @param newInvalidValueMessage The new text of the invalid value message, which may include a resource reference.
	 * @throws NullPointerException if the given message is <code>null</code>.
	 * @see #INVALID_VALUE_MESSAGE_PROPERTY
	 */
	public void setInvalidValueMessage(final String newInvalidValueMessage);

	/** @return The value required message text, which may include a resource reference. */
	public String getValueRequiredMessage();

	/**
	 * Sets the text of the value required message. This is a bound property.
	 * @param newValueRequiredMessage The new text of the value required message, which may include a resource reference..
	 * @throws NullPointerException if the given message is <code>null</code>.
	 * @see #VALUE_REQUIRED_VALUE_MESSAGE_PROPERTY
	 */
	public void setValueRequiredMessage(final String newValueRequiredMessage);

	/** @return The Guise session that owns this validator. */
	public GuiseSession getSession();

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
	public void validate(final V value) throws ValidationException;

	/**
	 * Determines whether a given value is valid. This convenience version calls {@link #validate(Object)}, returning <code>false</code> only if an exception is
	 * thrown. Although this method may be overridden to provide optimized fast-fail determinations, adding new logic to this method cannot be used in place of
	 * overriding {@link #validate(Object)}.
	 * @param value The value to validate.
	 * @return <code>true</code> if a value is given and the value is valid; or a value is not required, else <code>false</code>.
	 */
	public boolean isValid(final V value);

}
