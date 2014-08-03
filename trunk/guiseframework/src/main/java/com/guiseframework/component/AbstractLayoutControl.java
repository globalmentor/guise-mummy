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

import com.globalmentor.beans.*;
import com.globalmentor.java.Objects;

import com.guiseframework.component.layout.Layout;
import com.guiseframework.model.*;

/**
 * An abstract implementation of a layout component that is also a control.
 * @author Garret Wilson
 */
public abstract class AbstractLayoutControl extends AbstractLayoutComponent implements LayoutControl {

	/** The enableable object decorated by this component. */
	private final Enableable enableable;

	/** @return The enableable object decorated by this component. */
	protected Enableable getEnableable() {
		return enableable;
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
	 * Resets the control to its default value. This version clears any notification.
	 * @see #setNotification(Notification)
	 */
	public void reset() {
		setNotification(null); //clear any notification
	}

	/**
	 * Layout constructor with a default info model and enableable.
	 * @param layout The layout definition for the layout component.
	 * @throws NullPointerException if the given layout is <code>null</code>.
	 */
	public AbstractLayoutControl(final Layout<?> layout) {
		this(new DefaultInfoModel(), new DefaultEnableable(), layout); //construct the class with a default info model and enableable
	}

	/**
	 * Info model, enableable, and layout constructor.
	 * @param infoModel The component info model.
	 * @param enableable The enableable object in which to store enabled status.
	 * @param layout The layout definition for the layout component.
	 * @throws NullPointerException if the given info model, enableable, and/or layout is <code>null</code>.
	 */
	public AbstractLayoutControl(final InfoModel infoModel, final Enableable enableable, final Layout<?> layout) {
		super(infoModel, layout); //construct the parent class
		this.enableable = checkInstance(enableable, "Enableable object cannot be null."); //save the enableable object
		if(enableable != infoModel) { //if the enableable and the info model are two different objects (we don't want to repeat property change events twice) TODO eventually just listen to specific events for each object
			this.enableable.addPropertyChangeListener(getRepeatPropertyChangeListener()); //listen and repeat all property changes of the enableable object
		}
		addPropertyChangeListener(ENABLED_PROPERTY, new AbstractGenericPropertyChangeListener<Boolean>() { //listen for the "enabled" property changing

					public void propertyChange(final GenericPropertyChangeEvent<Boolean> genericPropertyChangeEvent) { //if the "enabled" property changes
						assert genericPropertyChangeEvent.getOldValue() != null && genericPropertyChangeEvent.getNewValue() != null : "The enabled property does not support null.";
						enabledPropertyChange(genericPropertyChangeEvent.getOldValue().booleanValue(), genericPropertyChangeEvent.getNewValue().booleanValue()); //delegate to our enabled property change method
					}
				});
	}

	/**
	 * Called when the enabled property changes. Child versions should call this version. This version clears any notifications and updates the valid status.
	 * @param oldValue The old value of the property.
	 * @param newValue The new value of the property.
	 * @see #setNotification(Notification)
	 * @see #updateValid()
	 */
	protected void enabledPropertyChange(final boolean oldValue, final boolean newValue) {
		setNotification(null); //clear any notification
		updateValid(); //update the valid status, which depends on the enabled status					
	}

	//Enableable delegations

	/** @return Whether the control is enabled and can receive user input. */
	public boolean isEnabled() {
		return enableable.isEnabled();
	}

	/**
	 * Sets whether the control is enabled and and can receive user input. This is a bound property of type <code>Boolean</code>.
	 * @param newEnabled <code>true</code> if the control should indicate and accept user input.
	 * @see #ENABLED_PROPERTY
	 */
	public void setEnabled(final boolean newEnabled) {
		enableable.setEnabled(newEnabled);
	}
}
