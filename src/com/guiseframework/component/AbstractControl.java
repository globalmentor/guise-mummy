package com.guiseframework.component;

import com.guiseframework.GuiseSession;
import com.guiseframework.model.DefaultLabelModel;
import com.guiseframework.model.LabelModel;

/**An abstract implementation of a model component that allows user interaction to modify the model.
@author Garret Wilson
*/
public abstract class AbstractControl<C extends Control<C>> extends AbstractComponent<C> implements Control<C>
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
		This version returns <code>null</code>.
		@return The current user input status of the control.
		*/ 
		protected Status determineStatus()
		{
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

	/**Session and ID constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractControl(final GuiseSession session, final String id)
	{
		this(session, id, new DefaultLabelModel(session));	//construct the class with a default label model
	}

	/**Session and ID constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param labelModel The component label model.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractControl(final GuiseSession session, final String id, final LabelModel labelModel)
	{
		super(session, id, labelModel);	//construct the parent class
	}

}
