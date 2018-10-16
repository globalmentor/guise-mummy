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

import static java.util.Objects.*;

import java.beans.*;

import com.guiseframework.model.*;
import com.guiseframework.validator.*;

/**
 * Abstract implementation of a control to accept input from the user. The component valid status is updated before a change in the
 * {@link ValueModel#VALUE_PROPERTY} or the {@link #VALIDATOR_PROPERTY} is fired.
 * @param <V> The type of value to represent.
 * @author Garret Wilson
 */
public abstract class AbstractValueControl<V> extends AbstractControl implements ValueControl<V> {

	/** The value model used by this component. */
	private final ValueModel<V> valueModel;

	/** @return The value model used by this component. */
	protected ValueModel<V> getValueModel() {
		return valueModel;
	}

	/**
	 * The property change listener that updates validity and removes any notification in response to a property changing.
	 * @see #setNotification(Notification)
	 * @see #updateValid()
	 */
	private final PropertyChangeListener updateValidPropertyChangeListener = new PropertyChangeListener() {

		@Override
		public void propertyChange(final PropertyChangeEvent propertyChangeEvent) { //if the property changes
			setNotification(null); //clear the notification
			updateValid(); //update the valid status based upon the new property, so that any listeners will know whether the new property is valid
		}

	};

	/**
	 * Info model, value model, and enableable constructor.
	 * @param infoModel The component info model.
	 * @param valueModel The component value model.
	 * @param enableable The enableable object in which to store enabled status.
	 * @throws NullPointerException if the given info model, value model, and/or enableable object is <code>null</code>.
	 */
	public AbstractValueControl(final InfoModel infoModel, final ValueModel<V> valueModel, final Enableable enableable) {
		super(infoModel, enableable); //construct the parent class
		this.valueModel = requireNonNull(valueModel, "Value model cannot be null."); //save the value model
		if(valueModel != infoModel && valueModel != enableable) { //if the value model is not the same as the enableable object and the info model (we don't want to repeat property change events twice) TODO eventually just listen to specific events for each object
			this.valueModel.addPropertyChangeListener(getRepeatPropertyChangeListener()); //listen and repeat all property changes of the value model
		}
		this.valueModel.addVetoableChangeListener(getRepeatVetoableChangeListener()); //listen and repeat all vetoable changes of the value model
		addPropertyChangeListener(VALUE_PROPERTY, updateValidPropertyChangeListener); //listen for the value changing, and clear the notification and update the validity in response TODO this needs to be put in other value controls as well
		addPropertyChangeListener(VALIDATOR_PROPERTY, updateValidPropertyChangeListener); //listen for the validator changing, and clear the notification and update the validity in response TODO this needs to be put in other value controls as well
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version performs no additional checks if the control is disabled.
	 * </p>
	 */
	@Override
	protected boolean determineValid() {
		if(!super.determineValid()) { //if we don't pass the default validity checks
			return false; //the component isn't valid
		}
		return !isEnabled() || getValueModel().isValidValue(); //the component is valid if the value model has a valid value (don't check the value model if the control is not enabled)
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version validates the associated value model. This version performs no additional checks if the control is disabled.
	 * </p>
	 */
	@Override
	public boolean validate() {
		super.validate(); //validate the parent class
		if(isEnabled()) { //if the control is enabled
			try {
				getValueModel().validateValue(); //validate the value model
			} catch(final ValidationException validationException) { //if there is a validation error
				//TODO del			componentException.setComponent(this);	//make sure the exception knows to which component it relates
				setNotification(new Notification(validationException)); //add notification of this error to the component
			}
		}
		return isValid(); //return the current valid state
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version resets the control value.
	 * </p>
	 * @see #resetValue()
	 */
	@Override
	public void reset() {
		super.reset(); //reset normally
		resetValue(); //reset the control value
	}

	//ValueModel delegations

	@Override
	public V getDefaultValue() {
		return getValueModel().getDefaultValue();
	}

	@Override
	public V getValue() {
		return getValueModel().getValue();
	}

	@Override
	public void setValue(final V newValue) throws PropertyVetoException {
		getValueModel().setValue(newValue);
	}

	@Override
	public void clearValue() {
		getValueModel().clearValue();
	}

	@Override
	public void resetValue() {
		getValueModel().resetValue();
	}

	@Override
	public Validator<V> getValidator() {
		return getValueModel().getValidator();
	}

	@Override
	public void setValidator(final Validator<V> newValidator) {
		getValueModel().setValidator(newValidator);
	}

	@Override
	public boolean isValidValue() {
		return getValueModel().isValidValue();
	}

	@Override
	public void validateValue() throws ValidationException {
		getValueModel().validateValue();
	}

	@Override
	public Class<V> getValueClass() {
		return getValueModel().getValueClass();
	}

}
