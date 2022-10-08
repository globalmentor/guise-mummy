/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.model;

import static com.globalmentor.java.Classes.*;

import java.beans.PropertyVetoException;

import com.globalmentor.model.MutableValued;

import io.guise.framework.validator.*;

/**
 * A model for user input of a value.
 * @param <V> The type of value contained in the model.
 * @author Garret Wilson
 */
public interface ValueModel<V> extends Model, MutableValued<V> {

	/** The validator bound property. */
	public static final String VALIDATOR_PROPERTY = getPropertyName(ValueModel.class, "validator");
	/** The value bound property. */
	public static final String VALUE_PROPERTY = getPropertyName(ValueModel.class, "value");

	/** @return The default value. */
	public V getDefaultValue();

	/** @return The input value, or <code>null</code> if there is no input value. */
	public V getValue();

	/**
	 * Sets the new value. This is a bound property that only fires a change event when the new value is different via the <code>equals()</code> method. If a
	 * validator is installed, the value will first be validated before the current value is changed. Validation always occurs if a validator is installed, even
	 * if the value is not changing. If the value change is vetoed by the installed validator, the validation exception will be accessible via
	 * {@link PropertyVetoException#getCause()}.
	 * @param newValue The new value.
	 * @throws PropertyVetoException if the provided value is not valid or the change has otherwise been vetoed.
	 * @see #getValidator()
	 * @see #VALUE_PROPERTY
	 */
	public void setValue(final V newValue) throws PropertyVetoException;

	/**
	 * Clears the value by setting the value to <code>null</code>, which may be invalid according to any installed validators. No validation occurs.
	 * @see #VALUE_PROPERTY
	 */
	public void clearValue();

	/**
	 * Resets the value to a default value, which may be invalid according to any installed validators. No validation occurs.
	 * @see #VALUE_PROPERTY
	 */
	public void resetValue();

	/** @return The validator for this model, or <code>null</code> if no validator is installed. */
	public Validator<V> getValidator();

	/**
	 * Sets the validator. This is a bound property
	 * @param newValidator The validator for this model, or <code>null</code> if no validator should be used.
	 * @see #VALIDATOR_PROPERTY
	 */
	public void setValidator(final Validator<V> newValidator);

	/**
	 * Determines whether the value of this model is valid.
	 * @return Whether the value of this model is valid.
	 */
	public boolean isValidValue();

	/**
	 * Validates the value of this model, throwing an exception if the model is not valid.
	 * @throws ValidationException if the value of this model is not valid.
	 */
	public void validateValue() throws ValidationException;

	/** @return The class representing the type of value this model can hold. */
	public Class<V> getValueClass();

}
