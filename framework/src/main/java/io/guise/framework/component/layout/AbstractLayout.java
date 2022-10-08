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

package io.guise.framework.component.layout;

import java.beans.PropertyChangeEvent;

import com.globalmentor.beans.*;
import com.globalmentor.java.Objects;

import io.guise.framework.component.Component;
import io.guise.framework.component.LayoutComponent;
import io.guise.framework.event.*;

/**
 * Abstract implementation of layout information for a layout component.
 * @param <T> The type of layout constraints associated with each component. This class and subclasses represent layout definitions, not layout implementations.
 * @author Garret Wilson
 */
public abstract class AbstractLayout<T extends Constraints> extends GuiseBoundPropertyObject implements Layout<T> {

	/** The lazily-created listener of constraint property changes. */
	private ConstraintsPropertyChangeListener constraintsPropertyChangeListener = null;

	/** @return The lazily-created listener of constraint property changes. */
	protected ConstraintsPropertyChangeListener getConstraintsPropertyChangeListener() {
		if(constraintsPropertyChangeListener == null) { //if we haven't yet created a property change listener for constraints
			constraintsPropertyChangeListener = new ConstraintsPropertyChangeListener(); //create a new constraints property change listener
		}
		return constraintsPropertyChangeListener; //return the listener of constraints properties
	}

	/** The lazily-created listener of component constraints changes. */
	private GenericPropertyChangeListener<Constraints> componentConstraintsChangeListener = null;

	/** @return The lazily-created listener of component constraints changes. */
	protected GenericPropertyChangeListener<Constraints> getComponentConstraintsChangeListener() {
		if(componentConstraintsChangeListener == null) { //if we haven't yet created a property change listener for the component constraints property
			componentConstraintsChangeListener = new AbstractGenericPropertyChangeListener<Constraints>() { //create a property change listener to listen for the component constraints property changing

				@Override
				public void propertyChange(final GenericPropertyChangeEvent<Constraints> propertyChangeEvent) { //if a component's constraints property changes
					//indicate that the component's constraints changed, and update the constraints property listener
					componentConstraintsChanged((Component)propertyChangeEvent.getSource(), propertyChangeEvent.getOldValue(), propertyChangeEvent.getNewValue());
				}

			};
		}
		return componentConstraintsChangeListener; //return the listener of component constraints
	}

	/**
	 * Indicates that the constraints for a component have changed. This method is also called when the component is first added to the layout. This version
	 * removes and installs property change listeners to and from the constraints objects as appropriate.
	 * @param component The component for which constraints have changed.
	 * @param oldConstraints The old component constraints, or <code>null</code> if there were no constraints previously.
	 * @param newConstraints The new component constraints, or <code>null</code> if the component now has no constraints.
	 */
	protected void componentConstraintsChanged(final Component component, final Constraints oldConstraints, final Constraints newConstraints) {
		final ConstraintsPropertyChangeListener constraintsPropertyChangeListener = getConstraintsPropertyChangeListener(); //get the constraints property change listener
		if(oldConstraints != null) { //if there were old constraints
			oldConstraints.removePropertyChangeListener(constraintsPropertyChangeListener); //stop listening for the constraints properties changing
		}
		if(newConstraints != null) { //if there are new constraints
			newConstraints.addPropertyChangeListener(constraintsPropertyChangeListener); //listn for the constraints properties changing
		}
	}

	/** The layout component that owns this layout, or <code>null</code> if this layout has not been installed into a layout component. */
	private LayoutComponent owner = null;

	@Override
	public LayoutComponent getOwner() {
		return owner;
	}

	@Override
	public void setOwner(final LayoutComponent newOwner) {
		final LayoutComponent oldOwner = owner; //get the old component
		if(oldOwner != newOwner) { //if the component is really changing
			/*TODO fix
							checkNull(newContainer, "Layout component cannot be null.");
							if(oldContainer!=null) {	//if we already have a parent
								throw new IllegalStateException("Layout "+this+" already has layout component: "+oldContainer);
							}
			*/
			if(newOwner != null && newOwner.getLayout() != this) { //if the layout component that is not really our owner
				throw new IllegalArgumentException("Provided layout component " + newOwner + " is not really owner of layout " + this);
			}
			if(oldOwner != null) { //if there was an old layout component
				for(final Component childComponent : oldOwner.getChildComponents()) { //for each child component in the old layout component
					removeComponent(childComponent); //remove the old component from the layout
				}
			}
			owner = newOwner; //this is really our component; make a note of it
			if(newOwner != null) { //if there is a new layout component
				for(final Component childComponent : newOwner.getChildComponents()) { //for each child component in the new layout component
					addComponent(childComponent); //add the new component to the layout
				}
			}
		}
	}

	/**
	 * Lays out the associated layout component. This version does nothing.
	 * @throws IllegalStateException if this layout has not yet been installed into a layout component.
	 */
	/*TODO del if not needed
		public void layout()
		{		
		}
	*/

	@Override
	public void addComponent(final Component component) {
		final LayoutComponent owner = getOwner(); //get the layout's owner
		if(owner == null) { //if we haven't been installed into an owner
			throw new IllegalStateException("Layout does not have an owner layout component.");
		}
		//TODO del		final Constraints constraints=getConstraints(component);	//get the component constraints, installing appropriate constraints if necessary
		final Constraints constraints = component.getConstraints(); //get the component constraints, ignoring whether they are appropriate for this layout, because the components may be added with constraints for another layout before that layout is installed
		if(constraints != null) { //if there are constraints
			componentConstraintsChanged(component, null, constraints); //act as if the component changed from no constraints to its current constraints, adding a listener to those constraints
		}
		component.addPropertyChangeListener(Component.CONSTRAINTS_PROPERTY, getComponentConstraintsChangeListener()); //listen for the component's constraints property changing so that we can listen for properties of the new constraints changing
	}

	@Override
	public void removeComponent(final Component component) {
		final LayoutComponent owner = getOwner(); //get the layout's owner
		if(owner == null) { //if we haven't been installed into a owner
			throw new IllegalStateException("Layout does not have an owner layout component.");
		}
		component.removePropertyChangeListener(Component.CONSTRAINTS_PROPERTY, getComponentConstraintsChangeListener()); //stop listening for the component's constraints property changing
		final Constraints constraints = component.getConstraints(); //get the component constraints
		if(constraints != null) { //if there are constraints
			componentConstraintsChanged(component, constraints, null); //act as if the component changed from its current constraints to no constraints, removing the listener from those constraints		
		}
	}

	@Override
	public T getConstraints(final Component component) {
		final LayoutComponent layoutComponent = getOwner(); //get the layout's layout component
		if(layoutComponent == null) { //if we haven't been installed into a layout component
			throw new IllegalStateException("Layout does not have an owner layout component.");
		}
		final Class<? extends T> constraintsClass = getConstraintsClass(); //get the type of constraints we expect
		Constraints constraints = component.getConstraints(); //get the constraints associated with the component
		if(!constraintsClass.isInstance(constraints)) { //if the current constraints are not compatible with the type of constraints we expect, or there are no constraints
			constraints = createDefaultConstraints(); //create default constraints for this component
			component.setConstraints(constraints); //associate the new constraints with this component			
		}
		return constraintsClass.cast(constraints); //return the constraints, casting them to the correct type, because we've verified that they are of the correct type
	}

	/**
	 * Reports that the bound property of a component's constraints has changed. No event is fired if old and new are both <code>null</code> or are both non-
	 * <code>null</code> and equal according to the {@link Object#equals(java.lang.Object)} method. No event is fired if no listeners are registered for the given
	 * property. This method delegates actual firing of the event to {@link #firePropertyChange(PropertyChangeEvent)}.
	 * @param <V> The type of the values.
	 * @param component The component for which a constraint value changed.
	 * @param constraints The constraints for which a value changed.
	 * @param propertyName The name of the property being changed.
	 * @param oldValue The old property value.
	 * @param newValue The new property value.
	 * @see LayoutConstraintsPropertyChangeEvent
	 */
	protected <V> void fireConstraintsPropertyChange(final Component component, final T constraints, final String propertyName, final V oldValue,
			final V newValue) {
		if(hasPropertyChangeListeners(propertyName)) { //if we have listeners registered for this property
			if(!Objects.equals(oldValue, newValue)) { //if the values are different					
				firePropertyChange(new LayoutConstraintsPropertyChangeEvent<T, V>(this, component, constraints, propertyName, oldValue, newValue)); //create and fire a layout constraints property change event
			}
		}
	}

	/**
	 * A property change listener that listens for changes in a constraint object's properties and fires a layout constraints property change event in response. A
	 * {@link LayoutConstraintsPropertyChangeEvent} will be fired for each component associated with the constraints for which a property changed. Events are only
	 * fired for constraints of a type recognized by this layout.
	 * @author Garret Wilson
	 * @see LayoutConstraintsPropertyChangeEvent
	 */
	protected class ConstraintsPropertyChangeListener extends AbstractGenericPropertyChangeListener<Object> { //TODO important fix; when a LayoutConstraintsPropertyChangeEvent is refired by the component, it won't be the correct type 

		/**
		 * {@inheritDoc}
		 * <p>
		 * This implementation fires a {@link LayoutConstraintsPropertyChangeEvent} indicating the constraints and associated component.
		 * </p>
		 */
		@Override
		public void propertyChange(final GenericPropertyChangeEvent<Object> propertyChangeEvent) {
			final Class<? extends T> constraintsClass = getConstraintsClass(); //get the type of constraints we expect			
			final Constraints constraints = (Constraints)propertyChangeEvent.getSource(); //get the constraints for which a property changed
			if(constraintsClass.isInstance(constraints)) { //if the current constraints are compatible with the type of constraints we expect
				final T layoutConstraints = constraintsClass.cast(constraints); //cast the constraints to this layout's type
				//find the component for these constraints
				for(final Component childComponent : getOwner().getChildComponents()) { //for each child component in the layout component
					if(childComponent.getConstraints() == constraints) { //if this component was associated with the constraints
						refirePropertyChange(childComponent, layoutConstraints, propertyChangeEvent.getPropertyName(), propertyChangeEvent.getOldValue(),
								propertyChangeEvent.getNewValue()); //refire the event
					}
				}
			}
		}

		/**
		 * Refires a constraint property change event for the layout in the form of a {@link LayoutConstraintsPropertyChangeEvent}.
		 * @param <V> The type of the values.
		 * @param component The component for which a constraint value changed.
		 * @param constraints The constraints for which a value changed.
		 * @param propertyName The name of the property being changed.
		 * @param oldValue The old property value.
		 * @param newValue The new property value.
		 */
		protected <V> void refirePropertyChange(final Component component, final T constraints, final String propertyName, final V oldValue, final V newValue) {
			//TODO del Log.trace("Ready to fire an event indicating that component", componentConstraintsEntry.getKey(), "changed property", propertyChangeEvent.getPropertyName(), "to value", propertyChangeEvent.getNewValue());	//TODO del
			//fire an event indicating that the constraints for this component changed one if its properties
			fireConstraintsPropertyChange(component, constraints, propertyName, oldValue, newValue);

		}
	}
}
