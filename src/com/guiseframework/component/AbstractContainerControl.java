package com.guiseframework.component;

import com.garretwilson.beans.AbstractGenericPropertyChangeListener;
import com.garretwilson.beans.GenericPropertyChangeEvent;
import com.garretwilson.lang.ObjectUtilities;
import static com.garretwilson.lang.ObjectUtilities.*;
import com.guiseframework.component.layout.Layout;
import com.guiseframework.model.DefaultEnableable;
import com.guiseframework.model.DefaultLabelModel;
import com.guiseframework.model.Enableable;
import com.guiseframework.model.LabelModel;
import com.guiseframework.model.Notification;

/**An abstract implementation of a container that is also a control.
@author Garret Wilson
*/
public abstract class AbstractContainerControl<C extends ContainerControl<C>> extends AbstractContainer<C> implements ContainerControl<C>
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

	/**Resets the control to its default value.
	This version clears any notification.
	@see #setNotification(Notification)
	*/
	public void reset()
	{
		setNotification(null);	//clear any notification
	}

	/**Layout constructor with a default label model and enableable.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given layout is <code>null</code>.
	*/
	public AbstractContainerControl(final Layout<?> layout)
	{
		this(new DefaultLabelModel(), new DefaultEnableable(), layout);	//construct the class with a default label model and enableable
	}

	/**Label model, enableable, and layout constructor.
	@param labelModel The component label model.
	@param enableable The enableable object in which to store enabled status.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given label model, enableable, and/or layout is <code>null</code>.
	*/
	public AbstractContainerControl(final LabelModel labelModel, final Enableable enableable, final Layout<?> layout)
	{
		super(labelModel, layout);	//construct the parent class
		this.enableable=checkInstance(enableable, "Enableable object cannot be null.");	//save the enableable object
		if(enableable!=labelModel)	//if the enableable and the label model are two different objects (we don't want to repeat property change events twice) TODO eventually just listen to specific events for each object
		{
			this.enableable.addPropertyChangeListener(getRepeatPropertyChangeListener());	//listen and repeat all property changes of the enableable object
		}
		addPropertyChangeListener(ENABLED_PROPERTY, new AbstractGenericPropertyChangeListener<Boolean>()	//listen for the "enabled" property changing
				{
					public void propertyChange(GenericPropertyChangeEvent<Boolean> genericPropertyChangeEvent)	//if the "enabled" property changes
					{
						setNotification(null);	//clear any notification
						updateValid();	//update the valid status, which depends on the enabled status					
					}
				});
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
