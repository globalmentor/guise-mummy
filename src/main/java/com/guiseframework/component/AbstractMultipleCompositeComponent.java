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

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

import com.guiseframework.model.InfoModel;

/**
 * An abstract implementation of a composite component that can contain multiple components. Every child component must be added or removed using
 * {@link #addComponent(Component)} and {@link #removeComponent(Component)}, although other actions may take place. The component's validity is updated whenever
 * a child comonent is added or removed from the component.
 * @author Garret Wilson
 */
public abstract class AbstractMultipleCompositeComponent extends AbstractCompositeComponent {

	/** The set of child components. */
	private final Set<Component> childComponents = new CopyOnWriteArraySet<Component>(); //TODO change to a read write lock set and add thread-safety to the entire hierarchy

	@Override
	public boolean hasChildComponents() {
		return !childComponents.isEmpty();
	}

	@Override
	public Iterable<Component> getChildComponents() {
		return childComponents;
	} //TODO make sure this is unmodifiable after we switch to thread-safety

	/**
	 * Adds a child component. This version adds the component to the component set. Any class that overrides this method must call this version.
	 * @param childComponent The component to add to this component.
	 * @throws IllegalArgumentException if the component already has a parent or if the component is already a child of this composite component.
	 */
	protected void addComponent(final Component childComponent) {
		if(childComponent.getParent() != null) { //if this component has already been added to container; do this check before we try to add the component to the map, because setting the same mapping won't result in an error
			throw new IllegalArgumentException("Component " + childComponent + " is already a member of a composite component, " + childComponent.getParent() + ".");
		}
		if(childComponents.add(childComponent)) { //add this component to the set; if that resulted in a map change
			initializeChildComponent(childComponent); //initialize the child component as needed
			childComponent.setParent(this); //tell the component who its parent is
			updateValid(); //update the valid status
			fireChildComponentAdded(childComponent); //inform listeners that the child component was added
		} else { //if the component was already in the set
			throw new IllegalArgumentException("Component " + childComponent + " is already a member of a composite component, " + this + ".");
		}
	}

	/**
	 * Removes a child component. This version removes the component from the component set. Any class that overrides this method must call this version.
	 * @param childComponent The component to remove from this component.
	 * @throws IllegalArgumentException if the component does not recognize this composite component as its parent or the component is not a member of this
	 *           composite component.
	 */
	protected void removeComponent(final Component childComponent) {
		if(childComponent.getParent() != this) { //if this component is not a member of this container
			throw new IllegalArgumentException("Component " + childComponent + " does not recognize composite component " + this + " as its parent.");
		}
		if(childComponents.remove(childComponent)) { //remove this component from the set; if the component was in the set
			uninitializeChildComponent(childComponent); //uninitialize the child component as needed
			childComponent.setParent(null); //tell the component it no longer has a parent
			updateValid(); //update the valid status
			fireChildComponentRemoved(childComponent); //inform listeners that the child component was removed
		} else { //if no component was removed
			throw new IllegalArgumentException("Component " + childComponent + " is not member of composite component " + this + ".");
		}
	}

	/**
	 * Info model constructor.
	 * @param infoModel The component info model.
	 * @throws NullPointerException if the given info model is <code>null</code>.
	 */
	public AbstractMultipleCompositeComponent(final InfoModel infoModel) {
		super(infoModel); //construct the parent class
	}
}
