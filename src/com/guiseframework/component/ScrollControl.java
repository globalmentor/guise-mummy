package com.guiseframework.component;

import static com.garretwilson.lang.Objects.*;

import com.garretwilson.beans.AbstractGenericPropertyChangeListener;
import com.garretwilson.beans.GenericPropertyChangeEvent;
import com.guiseframework.model.*;

/**A control that allows the user to scroll its contents
The control's contents are specified using {@link #setContent(Component)}.
@author Garret Wilson
*/
public class ScrollControl extends AbstractEnumCompositeComponent<ScrollControl.ScrollComponent> implements ContentComponent, Control
{

	/**The enumeration of frame components.*/
	private enum ScrollComponent{CONTENT_COMPONENT};

	/**The enableable object decorated by this component.*/
	private final Enableable enableable;

		/**@return The enableable object decorated by this component.*/
		protected Enableable getEnableable() {return enableable;}

	/**@return The content child component, or <code>null</code> if this frame does not have a content child component.
	@see ScrollComponent#CONTENT_COMPONENT
	*/
	public Component getContent() {return getComponent(ScrollComponent.CONTENT_COMPONENT);}

	/**Sets the content child component.
	This is a bound property.
	@param newContent The content child component, or <code>null</code> if this frame does not have a content child component.
	@see ScrollComponent#CONTENT_COMPONENT
	@see ContentComponent#CONTENT_PROPERTY
	*/
	public void setContent(final Component newContent)
	{
		final Component oldContent=setComponent(ScrollComponent.CONTENT_COMPONENT, newContent);	//set the component
		if(oldContent!=newContent)	//if the component really changed
		{
			firePropertyChange(CONTENT_PROPERTY, oldContent, newContent);	//indicate that the value changed
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

	/**Resets the control to its default value.
	This version clears any notification.
	@see #setNotification(Notification)
	*/
	public void reset()
	{
//TODO check; are we missing notification methods?		setNotification(null);	//clear any notification
	}

	/**Default constructor with no content component.
	@param component The single content child component, or <code>null</code> if this control should have no content child component.
	*/
	public ScrollControl()
	{
		this(null);	//construct the class with no content child component
	}

	/**Component constructor.
	@param component The single child component, or <code>null</code> if this control should have no child component.
	*/
	public ScrollControl(final Component component)
	{
		super(ScrollComponent.values());	//construct the parent class
		setComponent(ScrollComponent.CONTENT_COMPONENT, component);	//set the component directly, because child classes may prevent the setContent() method from changing the component 
		this.enableable=checkInstance(new DefaultEnableable(), "Enableable object cannot be null.");	//save the enableable object TODO later allow this to be passed as an argument
		this.enableable.addPropertyChangeListener(getRepeatPropertyChangeListener());	//listen and repeat all property changes of the enableable object
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
