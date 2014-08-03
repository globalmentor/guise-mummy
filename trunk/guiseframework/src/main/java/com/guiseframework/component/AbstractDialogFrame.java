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

import java.beans.PropertyVetoException;

import static com.globalmentor.java.Objects.*;

import com.globalmentor.java.Objects;
import com.guiseframework.input.*;
import com.guiseframework.model.*;
import com.guiseframework.validator.*;

/**
 * Abstract implementation of a frame meant for communication of a value. A dialog frame by default is modal and movable but not resizable. The component valid
 * status is updated before a change in the {@link #VALUE_PROPERTY} or the {@link #VALIDATOR_PROPERTY} is fired.
 * <p>
 * This implementation binds the action {@link #getCloseActionPrototype()} to the command {@link ProcessCommand#CONTINUE}.
 * </p>
 * @param <V> The value to be communicated.
 * @author Garret Wilson
 */
public abstract class AbstractDialogFrame<V> extends AbstractFrame implements DialogFrame<V> {

	/** The value model used by this component. */
	private final ValueModel<V> valueModel;

	/** @return The value model used by this component. */
	protected ValueModel<V> getValueModel() {
		return valueModel;
	}

	/** Whether the control is enabled and can receive user input. */
	private boolean enabled = true;

	/** @return Whether the control is enabled and can receive user input. */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets whether the control is enabled and and can receive user input. This is a bound property of type <code>Boolean</code>.
	 * @param newEnabled <code>true</code> if the control should indicate and accept user input.
	 * @see #ENABLED_PROPERTY
	 */
	public void setEnabled(final boolean newEnabled) {
		if(enabled != newEnabled) { //if the value is really changing
			final boolean oldEnabled = enabled; //get the old value
			enabled = newEnabled; //actually change the value
			setNotification(null); //clear any notification
			updateValid(); //update the valid status, which depends on the enabled status					
			firePropertyChange(ENABLED_PROPERTY, Boolean.valueOf(oldEnabled), Boolean.valueOf(newEnabled)); //indicate that the value changed
		}
	}

	/** The status of the current user input, or <code>null</code> if there is no status to report. */
	private Status status = null;

	/** @return The status of the current user input, or <code>null</code> if there is no status to report. */
	public Status getStatus() {
		return status;
	}

	/**
	 * Sets the status of the current user input. This is a bound property.
	 * @param newStatus The new status of the current user input, or <code>null</code> if there is no status to report.
	 * @see #STATUS_PROPERTY
	 */
	protected void setStatus(final Status newStatus) {
		if(status != newStatus) { //if the value is really changing
			final Status oldStatus = status; //get the current value
			status = newStatus; //update the value
			firePropertyChange(STATUS_PROPERTY, oldStatus, newStatus);
		}
	}

	/**
	 * Rechecks user input status of this component, and updates the status.
	 * @see #setStatus(Control.Status)
	 */
	protected void updateStatus() {
		setStatus(determineStatus()); //update the status after rechecking it
	}

	/**
	 * Checks the user input status of the control. If the component has a notification of {@link Notification.Severity#WARNING}, the status is determined to be
	 * {@link Status#WARNING}. If the component has a notification of {@link Notification.Severity#ERROR}, the status is determined to be {@link Status#ERROR}.
	 * Otherwise, this version returns <code>null</code>.
	 * @return The current user input status of the control.
	 */
	protected Status determineStatus() {
		final Notification notification = getNotification(); //get the current notification
		if(notification != null) { //if there is a notification
			switch(notification.getSeverity()) { //see how severe the notification is
				case WARN:
					return Status.WARNING;
				case ERROR:
					return Status.ERROR;
			}
		}
		return null; //default to no status to report
	}

	/**
	 * Rechecks user input validity of this component and all child components, and updates the valid state. This version also updates the status.
	 * @see #setValid(boolean)
	 * @see #updateStatus()
	 */
	protected void updateValid() {
		super.updateValid(); //update validity normally
		updateStatus(); //update user input status
	}

	/**
	 * Sets the component notification. This version updates the component status if the notification changes. This is a bound property.
	 * @param newNotification The notification for the component, or <code>null</code> if no notification is associated with this component.
	 * @see #NOTIFICATION_PROPERTY
	 */
	public void setNotification(final Notification newNotification) {
		final Notification oldNotification = getNotification(); //get the old notification
		super.setNotification(newNotification); //update the old notification normally
		if(!Objects.equals(oldNotification, newNotification)) { //if the notification changed
			updateStatus(); //update the status			
		}
	}

	/**
	 * Resets the control to its default value. This version clears any notification and resets the control value.
	 * @see #setNotification(Notification)
	 * @see #resetValue()
	 */
	public void reset() {
		setNotification(null); //clear any notification
		resetValue(); //reset the control value
	}

	/**
	 * Value model, and component constructor.
	 * @param valueModel The frame value model.
	 * @param component The single child component, or <code>null</code> if this frame should have no child component.
	 * @throws NullPointerException if the given value model is <code>null</code>.
	 */
	public AbstractDialogFrame(final ValueModel<V> valueModel, final Component component) {
		super(component); //construct the parent class
		this.valueModel = checkInstance(valueModel, "Value model cannot be null."); //save the table model
		this.valueModel.addPropertyChangeListener(getRepeatPropertyChangeListener()); //listen an repeat all property changes of the value model
		this.valueModel.addVetoableChangeListener(getRepeatVetoableChangeListener()); //listen and repeat all vetoable changes of the value model
		setModal(true); //default to being a modal frame
		setMovable(true); //default to being movable
		setResizable(false); //default to not allowing resizing
		final BindingInputStrategy bindingInputStrategy = new BindingInputStrategy(getInputStrategy()); //create a new input strategy based upon the current input strategy (if any)
		bindingInputStrategy.bind(new CommandInput(ProcessCommand.ABORT), getCloseActionPrototype()); //map the "abort" command to the close action
		setInputStrategy(bindingInputStrategy); //switch to our new input strategy
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
	 * Validates the user input of this component and all child components. The component will be updated with error information. This version validates the
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
	 * @see #VALUE_PROPERTY
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
