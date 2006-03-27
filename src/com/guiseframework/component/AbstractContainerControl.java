package com.guiseframework.component;

import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.GuiseSession;
import com.guiseframework.component.layout.Layout;
import com.guiseframework.model.Notification;

/**An abstract implementation of a container that is also a control.
@author Garret Wilson
*/
public abstract class AbstractContainerControl<C extends ContainerControl<C>> extends AbstractContainer<C> implements ContainerControl<C>
{

	/**Whether the control is enabled and can receive user input.*/
	private boolean enabled=true;

		/**@return Whether the control is enabled and can receive user input.*/
		public boolean isEnabled() {return enabled;}

		/**Sets whether the control is enabled and and can receive user input.
		This is a bound property of type <code>Boolean</code>.
		@param newEnabled <code>true</code> if the control should indicate and accept user input.
		@see #ENABLED_PROPERTY
		*/
		public void setEnabled(final boolean newEnabled)
		{
			if(enabled!=newEnabled)	//if the value is really changing
			{
				final boolean oldEnabled=enabled;	//get the old value
				enabled=newEnabled;	//actually change the value
				firePropertyChange(ENABLED_PROPERTY, Boolean.valueOf(oldEnabled), Boolean.valueOf(newEnabled));	//indicate that the value changed
			}			
		}

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

	/**Session, ID, and layout constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given session, and/or layout is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractContainerControl(final GuiseSession session, final String id, final Layout layout)
	{
		super(session, id, layout);	//construct the parent class
	}
}
