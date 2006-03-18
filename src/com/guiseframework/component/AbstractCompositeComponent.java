package com.guiseframework.component;

import java.beans.PropertyChangeListener;

import com.guiseframework.GuiseSession;
import com.guiseframework.event.AbstractGuisePropertyChangeListener;
import com.guiseframework.event.GuisePropertyChangeEvent;

/**An abstract implementation of a composite component.
Every child component must be added or removed using {@link #addComponent(Component)} and {@link #removeComponent(Component)}, although other actions may take place.
@author Garret Wilson
*/
public abstract class AbstractCompositeComponent<C extends CompositeComponent<C>> extends AbstractComponent<C> implements CompositeComponent<C>
{

	/**The lazily-created property change listener to listen for changes in valid status and call {@link #childComponentValidPropertyChanged(Component, boolean, boolean)}.*/
	private PropertyChangeListener validChangeListener=null;

	/**The lazily-created property change listener to listen for changes in valid status and cal {@link #childComponentValidPropertyChanged(Component, boolean, boolean)}.*/
	private PropertyChangeListener getValidChangeListener()
	{
		if(validChangeListener==null)	//if there is no valid change listener
		{
			validChangeListener=new AbstractGuisePropertyChangeListener<Boolean>()	//create a new valid change listener
					{
						public void propertyChange(GuisePropertyChangeEvent<Boolean> propertyChangeEvent)	//if the component's valid status changes
						{
							childComponentValidPropertyChanged((Component<?>)propertyChangeEvent.getSource(), propertyChangeEvent.getOldValue().booleanValue(), propertyChangeEvent.getNewValue().booleanValue());	//notify this component that a child component's valid status changed
						}
					};
		}
		return validChangeListener;	//return the valid change listener
	}

	/**Session and ID constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	@exception IllegalStateException if no controller is registered for this component type.
	@exception IllegalStateException if no view is registered for this component type.
	*/
	public AbstractCompositeComponent(final GuiseSession session, final String id)
	{
		super(session, id);	//construct the parent class
	}

	/**Adds a child component.
	This version installs a listener for the component's valid status.
	Any class that overrides this method must call this version.
	@param component The component to add to this component.
	*/
	protected void addComponent(final Component<?> component)
	{
		component.addPropertyChangeListener(Component.VALID_PROPERTY, getValidChangeListener());	//listen for changes in the component's valid status and update this component's valid status in response
	}

	/**Removes a child component.
	This version uninstalls a listener for the component's valid status.
	Any class that overrides this method must call this version.
	@param component The component to remove from this component.
	*/
	protected void removeComponent(final Component<?> component)
	{
		component.removePropertyChangeListener(Component.VALID_PROPERTY, getValidChangeListener());	//stop listening for changes in the component's valid status
	}

	/**Called when the {@link Component#VALID_PROPERTY} of a child compoenent changes.
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

	/**Checks the state of the component for validity.
	This version calls {@link #determineChildrenValid()}.
	@return <code>true</code> if the component and all relevant children passes all validity tests, else <code>false</code>.
	*/ 
	protected boolean determineValid()
	{
		return super.determineValid() && determineChildrenValid();	//determine if the super class and children are valid 
	}

	/**Checks the state of the component for validity.
	This version checks all child components for validity.
	@return <code>true</code> if the relevant children pass all validity tests.
	*/ 
	protected boolean determineChildrenValid()
	{
		for(final Component<?> childComponent:this)	//for each child component
		{
			if(!childComponent.isValid())	//if this child component is not valid
			{
				return false;	//the composite component should not be considered valid
			}
		}
		return true;	//all children are valid
	}

	/**Validates the this component and all child components.
	The component will be updated with error information.
	This version validates the this component and all child components.
	@exception ComponentExceptions if there was one or more validation error.
	*/
	public void validate() throws ComponentExceptions
	{
		ComponentExceptions componentExceptions=null;	//we'll store any component exceptions here and keep going
		try
		{
			super.validate();	//validate the component normally
		}
		catch(final ComponentExceptions superComponentExceptions)	//if the super version returns an error
		{
			if(componentExceptions==null)	//if this is our first component exception
			{
				componentExceptions=superComponentExceptions;	//store the exception and continue processing events with other child components
			}
			else	//if we already have component exceptions
			{
				componentExceptions.addAll(superComponentExceptions);	//add all the exceptions to the exception we already have
			}
		}
		for(final Component<?> childComponent:this)	//for each child component
		{
			try
			{
				childComponent.validate();	//validate the child
			}
			catch(final ComponentExceptions childComponentExceptions)	//if a child returns an error
			{
				if(componentExceptions==null)	//if this is our first component exception
				{
					componentExceptions=childComponentExceptions;	//store the exception and continue processing events with other child components
				}
				else	//if we already have component exceptions
				{
					componentExceptions.addAll(childComponentExceptions);	//add all the child component exceptions to the exception we already have
				}
			}
		}
		if(componentExceptions!=null)	//if we encountered one or more component exceptions
		{
			throw componentExceptions;	//throw the exception, which may contain multiple exceptions
		}
	}

}
