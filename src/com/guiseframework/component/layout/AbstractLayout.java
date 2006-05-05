package com.guiseframework.component.layout;

import java.beans.PropertyChangeEvent;

import com.garretwilson.beans.*;
import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.component.Component;
import com.guiseframework.component.Container;
import com.guiseframework.event.*;

/**Abstract implementation of layout information for a container.
@param <T> The type of layout constraints associated with each component.
This class and subclasses represent layout definitions, not layout implementations.
@author Garret Wilson
*/
public abstract class AbstractLayout<T extends Constraints> extends GuiseBoundPropertyObject implements Layout<T>
{

	/**The lazily-created listener of constraint property changes.*/
	private ConstraintsPropertyChangeListener constraintsPropertyChangeListener=null;

		/**@return The lazily-created listener of constraint property changes.*/
		protected ConstraintsPropertyChangeListener getConstraintsPropertyChangeListener()
		{
			if(constraintsPropertyChangeListener==null)	//if we haven't yet created a property change listener for constraints
			{
				constraintsPropertyChangeListener=new ConstraintsPropertyChangeListener();	//create a new constraints property change listener
			}
			return constraintsPropertyChangeListener;	//return the listener of constraints properties
		}

	/**The lazily-created listener of component constraints changes.*/
	private GenericPropertyChangeListener<Constraints> componentConstraintsChangeListener=null;

		/**@return The lazily-created listener of component constraints changes.*/
		protected GenericPropertyChangeListener<Constraints> getComponentConstraintsChangeListener()
		{
			if(componentConstraintsChangeListener==null)	//if we haven't yet created a property change listener for the component constraints property
			{
				componentConstraintsChangeListener=new AbstractGenericPropertyChangeListener<Constraints>()	//create a property change listener to listen for the component constraints property changing
						{
							public void propertyChange(final GenericPropertyChangeEvent<Constraints> propertyChangeEvent)	//if a component's constraints property changes
							{
									//indicate that the component's constraints changed, and update the constraints property listener
								componentConstraintsChanged((Component<?>)propertyChangeEvent.getSource(), propertyChangeEvent.getOldValue(), propertyChangeEvent.getNewValue());
							}					
						};
			}
			return componentConstraintsChangeListener;	//return the listener of component constraints
		}

	/**Indicates that the constraints for a component have changed.
	This method is also called when the component is first added to the layout.
	This version removes and installs property change listeners to and from the constraints objects as appropriate.
	@param component The component for which constraints have changed.
	@param oldConstraints The old component constraints, or <code>null</code> if there were no constraints previously.
	@param newConstraints The new component constraints, or <code>null</code> if the component now has no constraints.
	*/
	protected void componentConstraintsChanged(final Component<?> component, final Constraints oldConstraints, final Constraints newConstraints)
	{
		final ConstraintsPropertyChangeListener constraintsPropertyChangeListener=getConstraintsPropertyChangeListener();	//get the constraints property change listener
		if(oldConstraints!=null)	//if there were old constraints
		{
			oldConstraints.removePropertyChangeListener(constraintsPropertyChangeListener);	//stop listening for the constraints properties changing
		}
		if(newConstraints!=null)	//if there are new constraints
		{
			newConstraints.addPropertyChangeListener(constraintsPropertyChangeListener);	//listn for the constraints properties changing
		}		
	}

	/**The container that owns this layout, or <code>null</code> if this layout has not been installed into a container.*/
	private Container<?> container=null;

		/**@return The container that owns this layout, or <code>null</code> if this layout has not been installed into a container.*/
		public Container<?> getContainer() {return container;}

		/**Sets the container that owns this layout
		This method is managed by containers, and normally should not be called by applications.
//TODO del		A layout cannot be given a container if it is already installed in another container. Once a layout is installed in a container, it cannot be uninstalled.
		A layout cannot be given a container if it is already installed in another container.
		A layout cannot be given a container unless that container already recognizes this layout as its layout.
		If a layout is given the same container it already has, no action occurs.
		@param newContainer The new container for this layout.
//TODO del		@exception NullPointerException if the given container is <code>null</code>.
		@exception IllegalStateException if a different container is provided and this layout already has a container.
		@exception IllegalArgumentException if a different container is provided and the given container does not already recognize this layout as its layout.
		*/
		public void setContainer(final Container<?> newContainer)
		{
			final Container<?> oldContainer=container;	//get the old component
			if(oldContainer!=newContainer)	//if the component is really changing
			{
/*TODO fix
				checkNull(newContainer, "Container cannot be null.");
				if(oldContainer!=null)	//if we already have a parent
				{
					throw new IllegalStateException("Layout "+this+" already has container: "+oldContainer);
				}
*/
				if(newContainer!=null && newContainer.getLayout()!=this)	//if the container that is not really our owner
				{
					throw new IllegalArgumentException("Provided container "+newContainer+" is not really owner of layout "+this);
				}
				if(oldContainer!=null)	//if there was an old container
				{
					for(final Component<?> childComponent:oldContainer)	//for each child component in the old container
					{
						removeComponent(childComponent);	//remove the old component from the layout
					}
				}
				container=newContainer;	//this is really our component; make a note of it
				if(newContainer!=null)	//if there is a new container
				{
					for(final Component<?> childComponent:newContainer)	//for each child component in the new container
					{
						addComponent(childComponent);	//add the new component to the layout
					}
				}
			}
		}

	/**Lays out the associated container.
	This version does nothing.
	@exception IllegalStateException if this layout has not yet been installed into a container.
	*/
/*TODO del if not needed
	public void layout()
	{		
	}
*/

	/**Adds a component to the layout.
	Called immediately after a component is added to the associated container.
	This method is called by the associated container, and should not be called directly by application code.
	@param component The component to add to the layout.
	@exception IllegalStateException if this layout has not yet been installed into a container.
	*/
	public void addComponent(final Component<?> component)
	{
		final Container<?> container=getContainer();	//get the layout's container
		if(container==null)	//if we haven't been installed into a container
		{
			throw new IllegalStateException("Layout does not have container.");
		}
//TODO del		final Constraints constraints=getConstraints(component);	//get the component constraints, installing appropriate constraints if necessary
		final Constraints constraints=component.getConstraints();	//get the component constraints, ignoring whether they are appropriate for this layout, because the components may be added with constraints for another layout before that layout is installed
		if(constraints!=null)	//if there are constraints
		{
			componentConstraintsChanged(component, null, constraints);	//act as if the component changed from no constraints to its current constraints, adding a listener to those constraints
		}
		component.addPropertyChangeListener(Component.CONSTRAINTS_PROPERTY, getComponentConstraintsChangeListener());	//listen for the component's constraints property changing so that we can listen for properties of the new constraints changing
	}

	/**Removes a component from the layout.
	Called immediately before a component is removed from the associated container.
	This method is called by the associated container, and should not be called directly by application code.
	@param component The component to remove from the layout.
	*/
	public void removeComponent(final Component<?> component)
	{
		final Container<?> container=getContainer();	//get the layout's container
		if(container==null)	//if we haven't been installed into a container
		{
			throw new IllegalStateException("Layout does not have container.");
		}		
		component.removePropertyChangeListener(Component.CONSTRAINTS_PROPERTY, getComponentConstraintsChangeListener());	//stop listening for the component's constraints property changing
		final Constraints constraints=component.getConstraints();	//get the component constraints
		if(constraints!=null)	//if there are constraints
		{
			componentConstraintsChanged(component, constraints, null);	//act as if the component changed from its current constraints to no constraints, removing the listener from those constraints		
		}
	}
		
	/**Retreives layout constraints associated with a component.
	If the constraints currently associated with the component are not compatible with this layout,
		or if no constraints are associated with the given component,
		default constraints are created and associated with the component.
	@param component The component for which layout metadata is being requested.
	@return The constraints associated with the component.
	@exception IllegalStateException if this layout has not yet been installed into a container.
	@exception IllegalStateException if no constraints are associated with the given component and this layout does not support default constraints.
	@see #getConstraintsClass()
	@see Component#getConstraints()
	@see Component#setConstraints(Constraints)
	*/
	public T getConstraints(final Component<?> component)
	{
		final Container<?> container=getContainer();	//get the layout's container
		if(container==null)	//if we haven't been installed into a container
		{
			throw new IllegalStateException("Layout does not have container.");
		}
		final Class<? extends T> constraintsClass=getConstraintsClass();	//get the type of constraints we expect
		Constraints constraints=component.getConstraints();	//get the constraints associated with the component
		if(!constraintsClass.isInstance(constraints))	//if the current constraints are not compatible with the type of constraints we expect, or there are no constraints
		{
			constraints=createDefaultConstraints();	//create default constraints for this component
			component.setConstraints(constraints);	//associate the new constraints with this component			
		}
		return constraintsClass.cast(constraints);	//return the constraints, casting them to the correct type, because we've verified that they are of the correct type
	}

	/**Reports that the bound property of a component's constraints has changed.
	No event is fired if old and new are both <code>null</code> or are both non-<code>null</code> and equal according to the {@link Object#equals(java.lang.Object)} method.
	No event is fired if no listeners are registered for the given property.
	This method delegates actual firing of the event to {@link #firePropertyChange(PropertyChangeEvent)}.
	@param component The component for which a constraint value changed.
	@param constraints The constraints for which a value changed.
	@param propertyName The name of the property being changed.
	@param oldValue The old property value.
	@param newValue The new property value.
	@see LayoutConstraintsPropertyChangeEvent
	*/
	protected <V> void fireConstraintsPropertyChange(final Component<?> component, final T constraints, final String propertyName, final V oldValue, final V newValue)
	{
		if(hasPropertyChangeListeners(propertyName)) //if we have listeners registered for this property
		{
			if(!ObjectUtilities.equals(oldValue, newValue))	//if the values are different
			{					
				firePropertyChange(new LayoutConstraintsPropertyChangeEvent<T, V>(this, component, constraints, propertyName, oldValue, newValue));	//create and fire a layout constraints property change event
			}
		}
	}

	/**A property change listener that listens for changes in a constraint object's properties and fires a layout constraints property change event in response.
	A {@link LayoutConstraintsPropertyChangeEvent} will be fired for each component associated with the constraints for which a property changed.
	Events are only fired for constraints of a type recognized by this layout.
	@author Garret Wilson
	@see LayoutConstraintsPropertyChangeEvent
	*/
	protected class ConstraintsPropertyChangeListener extends AbstractGenericPropertyChangeListener<Object>	//TODO important fix; when a LayoutConstraintsPropertyChangeEvent is refired by the component, it won't be the correct type 
	{

		/**Called when a bound property is changed.
		This implementation fires a {@link LayoutConstraintsPropertyChangeEvent} indicating the constraints and associated component. 
		@param propertyChangeEvent An event object describing the event source, the property that has changed, and its old and new values.
		*/
		public void propertyChange(final GenericPropertyChangeEvent<Object> propertyChangeEvent)
		{
			final Class<? extends T> constraintsClass=getConstraintsClass();	//get the type of constraints we expect			
			final Constraints constraints=(Constraints)propertyChangeEvent.getSource();	//get the constraints for which a property changed
			if(constraintsClass.isInstance(constraints))	//if the current constraints are compatible with the type of constraints we expect
			{
				final T layoutConstraints=constraintsClass.cast(constraints);	//cast the constraints to this layout's type
					//find the component for these constraints
				for(final Component<?> childComponent:getContainer())	//for each child component in the container
				{
					if(childComponent.getConstraints()==constraints)	//if this component was associated with the constraints
					{
						refirePropertyChange(childComponent, layoutConstraints, propertyChangeEvent.getPropertyName(), propertyChangeEvent.getOldValue(), propertyChangeEvent.getNewValue());	//refire the event
					}
				}
			}
		}
	
		/**Refires a constraint property change event for the layout in the form of a {@link LayoutConstraintsPropertyChangeEvent}.
		@param component The component for which a constraint value changed.
		@param constraints The constraints for which a value changed.
		@param propertyName The name of the property being changed.
		@param oldValue The old property value.
		@param newValue The new property value.
		*/
		protected <V> void refirePropertyChange(final Component<?> component, final T constraints, final String propertyName, final V oldValue, final V newValue)
		{
//TODO del Debug.trace("Ready to fire an event indicating that component", componentConstraintsEntry.getKey(), "changed property", propertyChangeEvent.getPropertyName(), "to value", propertyChangeEvent.getNewValue());	//TODO del
			//fire an event indicating that the constraints for this component changed one if its properties
			fireConstraintsPropertyChange(component, constraints, propertyName, oldValue, newValue);								
			
		}
	}
}
