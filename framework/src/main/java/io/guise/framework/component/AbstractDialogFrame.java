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

package io.guise.framework.component;

import java.beans.PropertyVetoException;

import static java.util.Objects.*;

import com.globalmentor.java.Objects;

import io.guise.framework.input.*;
import io.guise.framework.model.*;
import io.guise.framework.validator.*;

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

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
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

	@Override
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
	 * Checks the user input status of the control. If the component has a notification of {@link Notification.Severity#WARN}, the status is determined to be
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
	 * {@inheritDoc}
	 * <p>
	 * This version also updates the status.
	 * </p>
	 * @see #updateStatus()
	 */
	@Override
	protected void updateValid() {
		super.updateValid(); //update validity normally
		updateStatus(); //update user input status
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version updates the component status if the notification changes.
	 * </p>
	 * @see #NOTIFICATION_PROPERTY
	 */
	@Override
	public void setNotification(final Notification newNotification) {
		final Notification oldNotification = getNotification(); //get the old notification
		super.setNotification(newNotification); //update the old notification normally
		if(!Objects.equals(oldNotification, newNotification)) { //if the notification changed
			updateStatus(); //update the status			
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version clears any notification and resets the control value.
	 * </p>
	 * @see #setNotification(Notification)
	 * @see #resetValue()
	 */
	@Override
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
		this.valueModel = requireNonNull(valueModel, "Value model cannot be null."); //save the table model
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
	 * {@inheritDoc}
	 * <p>
	 * This version first updates the valid status if the value is reported as being changed.
	 * </p>
	 */
	@Override
	protected <VV> void firePropertyChange(final String propertyName, final VV oldValue, final VV newValue) {
		if(VALUE_PROPERTY.equals(propertyName) || VALIDATOR_PROPERTY.equals(propertyName)) { //if the value property or the validator property is being reported as changed
			updateValid(); //update the valid status based upon the new property, so that any listeners will know whether the new property is valid
		}
		super.firePropertyChange(propertyName, oldValue, newValue); //fire the property change event normally
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version checks the validity of the value model.
	 * </p>
	 */
	@Override
	protected boolean determineValid() {
		if(!super.determineValid()) { //if we don't pass the default validity checks
			return false; //the component isn't valid
		}
		return getValueModel().isValidValue(); //the component is valid if the value model has a valid value
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version validates the associated value model.
	 * </p>
	 */
	@Override
	public boolean validate() {
		super.validate(); //validate the parent class
		try {
			getValueModel().validateValue(); //validate the value model
		} catch(final ValidationException validationException) { //if there is a validation error
			setNotification(new Notification(validationException)); //add a notification of this error to the component
		}
		return isValid(); //return the current valid state
	}

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
