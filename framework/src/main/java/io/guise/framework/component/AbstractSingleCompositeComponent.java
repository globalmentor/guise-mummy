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

package io.guise.framework.component;

import java.util.ArrayList;
import java.util.List;

import io.guise.framework.model.InfoModel;

/**
 * An abstract implementation of a composite component that can contain a single component.
 * @author Garret Wilson
 */
public abstract class AbstractSingleCompositeComponent extends AbstractCompositeComponent //TODO del class if we don't need
{

	/** The child component, or <code>null</code> if this component does not contain a child component. */
	private Component component = null;

	/** @return The child component, or <code>null</code> if this component does not contain a child component. */
	protected Component getComponent() {
		return component;
	}

	/**
	 * Sets the child component. This is a bound property.
	 * @param newComponent The child component, or <code>null</code> if this component does not contain a child component. //TODO fix if needed @see
	 *          #CHECK_TYPE_PROPERTY
	 * @throws IllegalArgumentException if the component already has a parent.
	 */
	public void setComponent(final Component newComponent) {
		if(component != newComponent) { //if the value is really changing
			final Component oldComponent = component; //get the old value
			component = newComponent; //actually change the value
			if(oldComponent != null) { //if there was an old component
				super.uninitializeChildComponent(component); //uninitialize the old component as needed
				oldComponent.setParent(null); //tell the old component it no longer has a parent
				fireChildComponentRemoved(component); //inform listeners that the child component was removed
			}
			if(newComponent != null) { //if there is a new component
				super.initializeChildComponent(component); //initialize the new component as needed
				newComponent.setParent(this); //tell the component who its parent is					
				fireChildComponentAdded(component); //inform listeners that the child component was added
			}
			//TODO fix				firePropertyChange(CHECK_TYPE_PROPERTY, oldComponent, newComponent);	//indicate that the value changed
		}
	}

	@Override
	public boolean hasChildComponents() {
		return component != null;
	}

	/** @return An iterable to child components. */
	public Iterable<Component> getChldren() { //TODO make this more efficient
		final List<Component> components = new ArrayList<Component>(); //create a list of components
		if(component != null) { //if we have a component
			components.add(component); //add this component to the list
		}
		return components; //return the components
		//TODO del		return component!=null ? new ObjectIterator<Component>(component) : new EmptyIterator<Component>();
	}

	/**
	 * Info model constructor.
	 * @param infoModel The component info model.
	 * @throws NullPointerException if the given info model is <code>null</code>.
	 */
	public AbstractSingleCompositeComponent(final InfoModel infoModel) {
		super(infoModel); //construct the parent class
	}

}
