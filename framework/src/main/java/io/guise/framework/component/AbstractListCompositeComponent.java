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

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import io.guise.framework.model.InfoModel;

/**
 * Abstract implementation of a composite component that keeps track of its child components in sequence. Iterating over child components is thread safe.
 * @author Garret Wilson
 */
public abstract class AbstractListCompositeComponent extends AbstractMultipleCompositeComponent {

	/** The list of child components. */
	private final List<Component> componentList = new CopyOnWriteArrayList<Component>(); //create a new component list, using a thread-safe array that takes into consideration that adding or removing children usually takes place up-front, and most later access will be only reads

	/** @return The list of child components. */
	protected List<Component> getComponentList() {
		return componentList;
	}

	/** @return The number of child components in this component. */
	protected int size() {
		return getComponentList().size();
	}

	/** @return Whether this component contains no child components. */
	protected boolean isEmpty() {
		return getComponentList().isEmpty();
	}

	/**
	 * Determines whether this component contains the given component.
	 * @param component The component to check.
	 * @return <code>true</code> if this component contains the given component.
	 */
	protected boolean contains(final Object component) {
		return getComponentList().contains(component);
	}

	/**
	 * Returns the index in the component of the first occurrence of the specified component.
	 * @param component The component the index of which should be returned.
	 * @return The index in this component of the first occurrence of the specified component, or -1 if this component does not contain the given component.
	 */
	protected int indexOf(final Object component) {
		return getComponentList().indexOf(component);
	}

	/**
	 * Returns the index in this component of the last occurrence of the specified component.
	 * @param component The component the last index of which should be returned.
	 * @return The index in this component of the last occurrence of the specified component, or -1 if this component does not contain the given component.
	 */
	protected int lastIndexOf(final Object component) {
		return getComponentList().lastIndexOf(component);
	}

	/**
	 * Returns the component at the specified index in the component.
	 * @param index The index of the component to return.
	 * @return The component at the specified position in this component.
	 * @throws IndexOutOfBoundsException if the index is out of range.
	 */
	protected Component get(final int index) {
		return getComponentList().get(index);
	}

	/**
	 * Adds a child component at the specified index. This version adds the component to the component list. Any class that overrides this method must call this
	 * version.
	 * @param index The index at which the component should be added.
	 * @param childComponent The component to add to this component.
	 * @throws IllegalArgumentException if the component already has a parent or if the component is already a child of this composite component.
	 * @throws IndexOutOfBoundsException if the index is less than zero or greater than the number of child components.
	 */
	protected void addComponent(final int index, final Component childComponent) {
		if(childComponent.getParent() != null) { //if this component has already been added to component; do this check before we add the component to the list, because the super class' version of this only comes after the component is added to the list
			throw new IllegalArgumentException("Component " + childComponent + " is already a member of a composite component, " + childComponent.getParent() + ".");
		}
		componentList.add(index, childComponent); //add the component to the list at the specified index
		super.addComponent(childComponent); //do the default adding
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version adds the component to the component list.
	 * </p>
	 */
	@Override
	protected final void addComponent(final Component childComponent) {
		addComponent(componentList.size(), childComponent); //add the component to the end of the list
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version removes the component from the component list.
	 * </p>
	 */
	@Override
	protected void removeComponent(final Component childComponent) {
		if(componentList.remove(childComponent)) { //remove the component from the list
			super.removeComponent(childComponent); //do the default removal
		} else { //if the component list did not change
			throw new IllegalArgumentException("Component " + childComponent + " is not member of composite component " + this + ".");
		}
	}

	@Override
	public Iterable<Component> getChildComponents() {
		return componentList;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation delegates to the component list.
	 * </p>
	 */
	@Override
	public boolean hasChildComponents() {
		return !componentList.isEmpty();
	}

	/**
	 * Info model constructor.
	 * @param infoModel The component info model.
	 * @throws NullPointerException if the given info model is <code>null</code>.
	 */
	public AbstractListCompositeComponent(final InfoModel infoModel) {
		super(infoModel); //construct the parent class
	}

}
