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

package com.guiseframework.component;

import static com.globalmentor.java.Objects.*;

import java.beans.PropertyVetoException;
import java.net.URI;
import java.util.*;

import com.guiseframework.model.*;
import com.guiseframework.validator.*;

/**
 * Abstract implementation of an action control containing a value. The component valid status is updated before a change in the {@link #VALUE_PROPERTY} or the
 * {@link #VALIDATOR_PROPERTY} is fired.
 * @param <V> The type of value the control represents.
 * @author Garret Wilson
 */
public abstract class AbstractActionValueControl<V> extends AbstractActionControl implements ActionValueControl<V> {

	/** The value model used by this component. */
	private final ValueModel<V> valueModel;

	/** @return The value model used by this component. */
	protected ValueModel<V> getValueModel() {
		return valueModel;
	}

	/** The map of icons keyed to values. */
	private final Map<V, URI> valueGlyphURIMap = new HashMap<V, URI>();

	/**
	 * Retrieves the icon associated with a given value.
	 * @param value The value for which an associated icon should be returned, or <code>null</code> to retrieve the icon associated with the <code>null</code>
	 *          value.
	 * @return The value icon URI, which may be a resource URI, or <code>null</code> if the value has no associated icon URI.
	 */
	public URI getValueGlyphURI(final V value) {
		return valueGlyphURIMap.get(value);
	}

	/**
	 * Sets the URI of the icon associated with a value. This method fires a property change event for the changed icon if its value changes.
	 * @param value The value with which the icon should be associated, or <code>null</code> if the icon should be associated with the <code>null</code> value.
	 * @param newValueIcon The new URI of the value icon, which may be a resource URI.
	 * @see #VALUE_GLYPH_URI_PROPERTY
	 */
	public void setValueGlyphURI(final V value, final URI newValueIcon) {
		final URI oldValueIcon = valueGlyphURIMap.put(value, newValueIcon); //store the new value
		firePropertyChange(VALUE_GLYPH_URI_PROPERTY, oldValueIcon, newValueIcon); //indicate that the value changed (which will only fire the event if the value actually changed)
	}

	/**
	 * Info model, action model, value model, and enableable object constructor.
	 * @param infoModel The component info model.
	 * @param actionModel The component action model.
	 * @param valueModel The component value model.
	 * @param enableable The enableable object in which to store enabled status.
	 * @throws NullPointerException if the given info model, action model, and/or enableable object is <code>null</code>.
	 */
	public AbstractActionValueControl(final InfoModel infoModel, final ActionModel actionModel, final ValueModel<V> valueModel, final Enableable enableable) {
		super(infoModel, actionModel, enableable); //construct the parent class
		this.valueModel = checkInstance(valueModel, "Value model cannot be null."); //save the table model
		this.valueModel.addPropertyChangeListener(getRepeatPropertyChangeListener()); //listen an repeat all property changes of the value model
		this.valueModel.addVetoableChangeListener(getRepeatVetoableChangeListener()); //listen and repeat all vetoable changes of the value model
	}

	/**
	 * Reports that a bound property has changed. This version first updates the valid status if the value is reported as being changed.
	 * @param propertyName The name of the property being changed.
	 * @param oldValue The old property value.
	 * @param newValue The new property value.
	 */
	protected <VV> void firePropertyChange(final String propertyName, final VV oldValue, final VV newValue) {
		if(VALUE_PROPERTY.equals(propertyName) || VALIDATOR_PROPERTY.equals(propertyName)) { //if the value property or the validator property is being reported as changed
			updateValid(); //update the valid status based upon the new property, so that any listeners will know whether the new property is valid
		}
		super.firePropertyChange(propertyName, oldValue, newValue); //fire the property change event normally
	}

	/**
	 * Checks the state of the component for validity. This version checks the validity of the value model.
	 * @return <code>true</code> if the component and all children passes all validity tests, else <code>false</code>.
	 */
	protected boolean determineValid() {
		if(!super.determineValid()) { //if we don't pass the default validity checks
			return false; //the component isn't valid
		}
		return getValueModel().isValidValue(); //the component is valid if the value model has a valid value
	}

	/**
	 * Validates the user interface of this component and all child components. The component will be updated with error information. This version validates the
	 * associated value model.
	 * @return The current state of {@link #isValid()} as a convenience.
	 */
	public boolean validate() {
		super.validate(); //validate the parent class
		try {
			getValueModel().validateValue(); //validate the value model
		} catch(final ValidationException validationException) { //if there is a validation error
			setNotification(new Notification(validationException)); //add a notification of this error to the component
		}
		return isValid(); //return the current valid state
	}

	/**
	 * Resets the control to its default value. This version resets the control value.
	 * @see #resetValue()
	 */
	public void reset() {
		super.reset(); //reset normally
		resetValue(); //reset the control value
	}

	/** @return The default value. */
	public V getDefaultValue() {
		return getValueModel().getDefaultValue();
	}

	/** @return The input value, or <code>null</code> if there is no input value. */
	public V getValue() {
		return getValueModel().getValue();
	}

	/**
	 * Sets the input value. This is a bound property that only fires a change event when the new value is different via the <code>equals()</code> method. If a
	 * validator is installed, the value will first be validated before the current value is changed. Validation always occurs if a validator is installed, even
	 * if the value is not changing. If the value change is vetoed by the installed validator, the validation exception will be accessible via
	 * {@link PropertyVetoException#getCause()}.
	 * @param newValue The input value of the model.
	 * @throws PropertyVetoException if the provided value is not valid or the change has otherwise been vetoed.
	 * @see #getValidator()
	 * @see ValueModel#VALUE_PROPERTY
	 */
	public void setValue(final V newValue) throws PropertyVetoException {
		getValueModel().setValue(newValue);
	}

	/**
	 * Clears the value by setting the value to <code>null</code>, which may be invalid according to any installed validators. No validation occurs.
	 * @see ValueModel#VALUE_PROPERTY
	 */
	public void clearValue() {
		getValueModel().clearValue();
	}

	/**
	 * Resets the value to a default value, which may be invalid according to any installed validators. No validation occurs.
	 * @see #VALUE_PROPERTY
	 */
	public void resetValue() {
		getValueModel().resetValue();
	}

	/** @return The validator for this model, or <code>null</code> if no validator is installed. */
	public Validator<V> getValidator() {
		return getValueModel().getValidator();
	}

	/**
	 * Sets the validator. This is a bound property
	 * @param newValidator The validator for this model, or <code>null</code> if no validator should be used.
	 * @see #VALIDATOR_PROPERTY
	 */
	public void setValidator(final Validator<V> newValidator) {
		getValueModel().setValidator(newValidator);
	}

	/**
	 * Determines whether the value of this model is valid.
	 * @return Whether the value of this model is valid.
	 */
	public boolean isValidValue() {
		return getValueModel().isValidValue();
	}

	/**
	 * Validates the value of this model, throwing an exception if the model is not valid.
	 * @throws ValidationException if the value of this model is not valid.
	 */
	public void validateValue() throws ValidationException {
		getValueModel().validateValue();
	}

	/** @return The class representing the type of value this model can hold. */
	public Class<V> getValueClass() {
		return getValueModel().getValueClass();
	}

}
