package com.guiseframework.component;

import java.beans.PropertyChangeListener;
import java.io.IOException;

import com.garretwilson.beans.AbstractGenericPropertyChangeListener;
import com.garretwilson.beans.GenericPropertyChangeEvent;
import com.guiseframework.GuiseSession;
import com.guiseframework.event.*;
import com.guiseframework.model.LabelModel;
import com.guiseframework.theme.Theme;

/**An abstract implementation of a composite component.
Every child component must be added or removed using {@link #addComponent(Component)} and {@link #removeComponent(Component)}, although other actions may take place.
This version listens for the {@link Component#VALID_PROPERTY} of each child component and updates the valid status of this component in response. 
The lazily-created notification listener to listen for child notifications and refire the {@link NotificationEvent}, retaining the original event source.
@author Garret Wilson
*/
public abstract class AbstractCompositeComponent<C extends CompositeComponent<C>> extends AbstractComponent<C> implements CompositeComponent<C>
{

	/**The lazily-created property change listener to listen for changes in valid status and call {@link #childComponentValidPropertyChanged(Component, boolean, boolean)}.*/
	private PropertyChangeListener validChangeListener=null;

	/**The lazily-created property change listener to listen for changes in valid status and call {@link #childComponentValidPropertyChanged(Component, boolean, boolean)}.*/
	private PropertyChangeListener getValidChangeListener()
	{
		if(validChangeListener==null)	//if there is no valid change listener
		{
			validChangeListener=new AbstractGenericPropertyChangeListener<Boolean>()	//create a new valid change listener
					{
						public void propertyChange(GenericPropertyChangeEvent<Boolean> propertyChangeEvent)	//if the child component's valid status changes
						{
								//TODO do we want to update valid here?
							childComponentValidPropertyChanged((Component<?>)propertyChangeEvent.getSource(), propertyChangeEvent.getOldValue().booleanValue(), propertyChangeEvent.getNewValue().booleanValue());	//notify this component that a child component's valid status changed
						}
					};
		}
		return validChangeListener;	//return the valid change listener
	}

	/**The lazily-created property change listener to listen for changes in visible and display status and update the valid status in response.*/
	private PropertyChangeListener displayVisibleChangeListener=null;

	/**The lazily-created property change listener to listen for changes in visible and display status and update the valid status in response.*/
	private PropertyChangeListener getDisplayVisibleChangeListener()
	{
		if(displayVisibleChangeListener==null)	//if there is no display/visible change listener
		{
			displayVisibleChangeListener=new AbstractGenericPropertyChangeListener<Boolean>()	//create a new display/visible change listener
					{
						public void propertyChange(GenericPropertyChangeEvent<Boolean> propertyChangeEvent)	//if the child component's display or visible status changes
						{
							//TODO maybe add a flag to prevent infinite loops
							updateValid();	//update this composite component's valid state, because the validity of this component only depends on displayed, visible child components
						}
					};
		}
		return validChangeListener;	//return the valid change listener
	}

	/**The lazily-created notification listener to listen for child notifications and refire the {@link NotificationEvent}, retaining the original event source.*/
	private NotificationListener notificationListener=null;

	/**The lazily-created notification listener to listen for child notifications and refire the {@link NotificationEvent}, retaining the original event source.*/
	private NotificationListener getNotificationListener()
	{
		if(notificationListener==null)	//if there is no notification listener
		{
			notificationListener=new NotificationListener()	//create a new notification listener
					{
						public void notified(final NotificationEvent notificationEvent)	//if the child component sends a notification
						{
							fireNotified(notificationEvent);	//refire the notification event to our listeners						
						}				
					};
		}
		return notificationListener;	//return the notification listener
	}

	/**Adds a child component.
	This version installs a listener for the component's valid status.
	Any class that overrides this method must call this version.
	The return value from this version has no significance.
	@param component The component to add to this component.
	@return <code>true</code> if the child components changed as a result of the operation.
	@exception IllegalArgumentException if the component already has a parent.
	*/
	protected boolean addComponent(final Component<?> component)
	{
		if(component.getParent()!=null)	//if this component has already been added to container
		{
			throw new IllegalArgumentException("Component "+component+" is already a member of a composite component, "+component.getParent()+".");
		}
		component.addPropertyChangeListener(DISPLAYED_PROPERTY, getDisplayVisibleChangeListener());	//listen for changes in the component's displayed status and update this component's valid status in response
		component.addPropertyChangeListener(VALID_PROPERTY, getValidChangeListener());	//listen for changes in the component's valid status and update this component's valid status in response
		component.addPropertyChangeListener(VISIBLE_PROPERTY, getDisplayVisibleChangeListener());	//listen for changes in the component's visible status and update this component's valid status in response
		component.addNotificationListener(getNotificationListener());	//listen for component notifications and refire the notification events in response
		return true;	//return true by default
	}

	/**Removes a child component.
	This version uninstalls a listener for the component's valid status.
	Any class that overrides this method must call this version.
	The return value from this version has no sigificance
	@param component The component to remove from this component.
	@return <code>true</code> if the child components changed as a result of the operation.
	@exception IllegalArgumentException if the component is not a member of this composite component.
	*/
	protected boolean removeComponent(final Component<?> component)
	{
		if(component.getParent()!=this)	//if this component is not a member of this container
		{
			throw new IllegalArgumentException("Component "+component+" is not member of composite component "+this+".");
		}
		component.removePropertyChangeListener(DISPLAYED_PROPERTY, getDisplayVisibleChangeListener());	//stop listening for changes in the component's displayed status
		component.removePropertyChangeListener(VALID_PROPERTY, getValidChangeListener());	//stop listening for changes in the component's valid status
		component.removePropertyChangeListener(VISIBLE_PROPERTY, getDisplayVisibleChangeListener());	//stop listening for changes in the component's visible status
		component.removeNotificationListener(getNotificationListener());	//stop listening for component notifications
		return true;	//return true by default
	}

	/**Label model constructor.
	@param labelModel The component label model.
	@exception NullPointerException if the given label model is <code>null</code>.
	*/
	public AbstractCompositeComponent(final LabelModel labelModel)
	{
		super(labelModel);	//construct the parent class
	}

	/**Called when the {@link Component#VALID_PROPERTY} of a child component changes.
	Every child version should call this version.
	This version updates the composite component's valid state by calling {@link #updateValid()}.
	@param childComponent The child component the valid property of which changed.
	@param oldValid The old valid property.
	@param newValid The new valid property.
	*/
	protected void childComponentValidPropertyChanged(final Component<?> childComponent, final boolean oldValid, final boolean newValid)
	{
		updateValid();	//update this composite component's valid state		
	}

	/**Called when a child fires a NotificationEvent {@link Component#VALID_PROPERTY} of a child component changes.
	Every child version should call this version.
	This version updates the composite component's valid state by calling {@link #updateValid()}.
	@param childComponent The child component the valid property of which changed.
	@param oldValid The old valid property.
	@param newValid The new valid property.
	*/
/*TODO fix
	protected void childComponentValidPropertyChanged(final Component<?> childComponent, final boolean oldValid, final boolean newValid)
	{
		updateValid();	//update this composite component's valid state		
	}
*/

	/**Checks the state of the component for validity.
	This version calls {@link #determineChildrenValid()}.
	@return <code>true</code> if the component and all relevant children passes all validity tests, else <code>false</code>.
	*/ 
	protected boolean determineValid()
	{
		return super.determineValid() && determineChildrenValid();	//determine if the super class and children are valid
	}

	/**Checks the state of child components for validity.
	This version checks all child components for validity using the current {@link Component#isValid()}; child component are not asked to update their valid state.
	Children that are not visible and/or not displayed are not taken into account.
	@return <code>true</code> if the relevant children pass all validity tests.
	*/ 
	protected boolean determineChildrenValid()
	{
//TODO fix Debug.trace("ready to determine children valid in", this);
		for(final Component<?> childComponent:getChildren())	//for each child component
		{
//TODO del Debug.trace("in", this, "child", childComponent, "is valid", childComponent.isValid());			
			if(childComponent.isDisplayed() && childComponent.isVisible() && !childComponent.isValid())	//if this child component is displayed and visible, but not valid
			{
//TODO del Debug.trace("in", this, "found non-valid child", childComponent);
				return false;	//the composite component should not be considered valid
			}
		}
//	TODO fix Debug.trace("in", this, "child", "all children are valid");
		return true;	//all children are valid
	}

	/**Validates the user input of this component and all child components.
	The component will be updated with error information.
	This version first calls {@link #validateChildren()} so that all children will be validated before checks are performed on this component.
	@return The current state of {@link #isValid()} as a convenience.
	*/
	public boolean validate()
	{
		validateChildren();	//validate all children
		super.validate();	//validate the component normally
//TODO del Debug.trace("in panel", this, "ready to return", isValid(), "for isValid()");
		return isValid();	//return the current valid state
	}

	/**Validates the user input of child components.
	Children that are not visible and/or not displayed are not taken into account.
	@return <code>true</code> if all child validations return <code>true</code>.
	*/
	protected boolean validateChildren()
	{
		boolean result=true;	//start by assuming all child components will validate 
		for(final Component<?> childComponent:getChildren())	//for each child component
		{
			if(childComponent.isDisplayed() && childComponent.isVisible() && !childComponent.validate())	//if this child component is displayed and visible, but doesn't validate
			{
				result=false;	//the result will be false
			}
		}
		return result;	//return whether all child components validated
	}

	/**Retrieves the first component in the hierarchy with the given name.
	This method checks this component and all descendant components.
	@return The first component with the given name, or <code>null</code> if this component and all descendant components do not have the given name. 
	*/
	public Component<?> getComponentByName(final String name)
	{
		return getComponentByName(this, name);	//search the component hierarchy for a component with the given name
	}

	/**Update's this component's theme.
	This method checks whether a theme has been applied to this component.
	If no theme has been applied to the component, the current session theme will be applied by delegating to {@link #applyTheme(Theme)}.
	This method is called recursively for any child components before applying any theme on the component itself,
	to assure that child theme updates have already occured before theme updates occur for this component.
	There is normally no need to override this method or to call this method directly by applications.
	This version recursively calls the {@link #updateTheme()} method of all child components before updating this component's theme.
	@exception IOException if there was an error loading or applying the theme.
	@see #applyTheme(Theme)
	@see #isThemeApplied()
	@see GuiseSession#getTheme()
	*/
	public void updateTheme() throws IOException
	{
		for(final Component<?> childComponent:getChildren())	//for each child component
		{
			childComponent.updateTheme();	//tell the child component to update its theme
		}
		super.updateTheme();	//update the theme for this component
	}

}
