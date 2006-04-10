package com.guiseframework.component;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.model.*;

/**An abstract implementation of a model component that allows user interaction to modify the model.
@author Garret Wilson
*/
public abstract class AbstractControl<C extends Control<C>> extends AbstractComponent<C> implements Control<C>
{

	/**The enableable object decorated by this component.*/
	private final Enableable enableable;

		/**@return The enableable object decorated by this component.*/
		protected Enableable getEnableable() {return enableable;}

	/**The status of the current user input, or <code>null</code> if there is no status to report.*/
	private Status status=null;

		/**@return The status of the current user input, or <code>null</code> if there is no status to report.*/
		public Status getStatus() {return status;}

		/**Sets the status of the current user input.
		This is a bound property.
		@param newStatus The new status of the current user input, or <code>null</code> if there is no status to report.
		@see #STATUS_PROPERTY
		*/
		protected void setStatus(final Status newStatus)
		{
			if(status!=newStatus)	//if the value is really changing
			{
				final Status oldStatus=status;	//get the current value
				status=newStatus;	//update the value
				firePropertyChange(STATUS_PROPERTY, oldStatus, newStatus);
			}
		}

		/**Rechecks user input status of this component, and updates the status.
		@see #setStatus(Control.Status)
		*/ 
		protected void updateStatus()
		{
			setStatus(determineStatus());	//update the status after rechecking it
		}

		/**Checks the user input status of the control.
		If the component has a notification of {@link Notification.Severity#WARNING}, the status is determined to be {@link Status#WARNING}.
		If the component has a notification of {@link Notification.Severity#ERROR}, the status is determined to be {@link Status#ERROR}.
		Otherwise, this version returns <code>null</code>.
		@return The current user input status of the control.
		*/ 
		protected Status determineStatus()
		{
			final Notification notification=getNotification();	//get the current notification
			if(notification!=null)	//if there is a notification
			{
				switch(notification.getSeverity())	//see how severe the notification is
				{
					case WARN:
						return Status.WARNING;
					case ERROR:
						return Status.ERROR;
				}
			}
			return null;	//default to no status to report
		}

	/**Rechecks user input validity of this component and all child components, and updates the valid state.
	This version also updates the status.
	@see #setValid(boolean)
	@see #updateStatus()
	*/ 
	protected void updateValid()
	{
		super.updateValid();	//update validity normally
		updateStatus();	//update user input status
	}

	/**Sets the component notification.
	This version updates the component status if the notification changes.
	This is a bound property.
	@param newNotification The notification for the component, or <code>null</code> if no notification is associated with this component.
	@see #NOTIFICATION_PROPERTY
	*/
	public void setNotification(final Notification newNotification)
	{
		final Notification oldNotification=getNotification();	//get the old notification
		super.setNotification(newNotification);	//update the old notification normally
		if(!ObjectUtilities.equals(oldNotification, newNotification))	//if the notification changed
		{
			updateStatus();	//update the status			
		}
	}

	/**Default constructor with a default label model.*/
	public AbstractControl()
	{
		this(new DefaultLabelModel());	//construct the class with a default label model
	}

	/**Label model constructor.
	@param labelModel The component label model.
	@exception NullPointerException if the given label model is <code>null</code>.
	*/
	public AbstractControl(final LabelModel labelModel)
	{
		this(labelModel, new DefaultEnableable());	//construct the class with a default enableable
	}

	/**Label model and enableable object constructor.
	@param labelModel The component label model.
	@param enableable The enableable object in which to store enabled status.
	@exception NullPointerException if the given label model and/or enableable object is <code>null</code>.
	*/
	public AbstractControl(final LabelModel labelModel, final Enableable enableable)
	{
		super(labelModel);	//construct the parent class
		this.enableable=checkInstance(enableable, "Enableable object cannot be null.");	//save the enableable object
		if(enableable!=labelModel)	//if the enableable and the label model are two different objects TODO eventually just listen to specific events for each object
		{
			this.enableable.addPropertyChangeListener(getRepeatPropertyChangeListener());	//listen and repeat all property changes of the enableable object
		}
	}

		//Enableable delegations
	
	/**@return Whether the control is enabled and can receive user input.*/
	public boolean isEnabled() {return enableable.isEnabled();}

	/**Sets whether the control is enabled and and can receive user input.
	This is a bound property of type <code>Boolean</code>.
	@param newEnabled <code>true</code> if the control should indicate and accept user input.
	@see #ENABLED_PROPERTY
	*/
	public void setEnabled(final boolean newEnabled) {enableable.setEnabled(newEnabled);}

}
