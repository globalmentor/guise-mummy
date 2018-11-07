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

import io.guise.framework.component.layout.*;
import io.guise.framework.model.*;
import io.guise.framework.prototype.*;
import io.guise.framework.validator.RangeValidator;

/**
 * Abstract implementation of a container component. Iterating over child components is thread safe.
 * @author Garret Wilson
 */
public abstract class AbstractContainer extends AbstractLayoutComponent implements Container {

	@Override
	public int size() {
		return super.size();
	}

	@Override
	public boolean isEmpty() {
		return super.isEmpty();
	}

	@Override
	public boolean contains(final Object component) {
		return super.contains(component);
	}

	@Override
	public int indexOf(final Object component) {
		return super.indexOf(component);
	}

	@Override
	public int lastIndexOf(final Object component) {
		return super.lastIndexOf(component);
	}

	@Override
	public Component get(final int index) {
		return super.get(index);
	}

	/**
	 * Adds a child component with default constraints to the container at the specified index.
	 * @param index The index at which the component should be added.
	 * @param component The component to add to this container.
	 * @throws IllegalArgumentException if the component already has a parent.
	 * @throws IllegalStateException if the installed layout does not support default constraints.
	 * @throws IndexOutOfBoundsException if the index is less than zero or greater than the number of child components.
	 */
	public void add(final int index, final Component component) {
		addComponent(index, component); //add the component normally
	}

	@Override
	public boolean add(final Component component) {
		add(size(), component); //add the component at the last index
		return true; //indicate that the container was modified
	}

	@Override
	public void add(final int index, final Component component, final Constraints constraints) {
		component.setConstraints(constraints); //set the constraints in the component
		add(index, component); //add the component, now that its constraints have been set		
	}

	@Override
	public boolean add(final Component component, final Constraints constraints) {
		component.setConstraints(constraints); //set the constraints in the component
		return add(component); //add the component, now that its constraints have been set
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation delegates to {@link #add(int, Component)}.
	 * </p>
	 */
	@Override
	public Component add(final int index, final Prototype prototype) {
		final Component component = createComponent(prototype); //create a component from the prototype
		add(index, component); //add the component to the container
		return component; //return the component we created
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation delegates to {@link #add(Component)}.
	 * </p>
	 */
	@Override
	public Component add(final Prototype prototype) {
		final Component component = createComponent(prototype); //create a component from the prototype
		add(component); //add the component to the container
		return component; //return the component we created
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation delegates to {@link #add(int, Component, Constraints)}.
	 * </p>
	 */
	@Override
	public Component add(final int index, final Prototype prototype, final Constraints constraints) {
		final Component component = createComponent(prototype); //create a component from the prototype
		add(index, component, constraints); //add the component to the container
		return component; //return the component we created
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation delegates to {@link #add(Component, Constraints)}.
	 * </p>
	 */
	@Override
	public Component add(final Prototype prototype, final Constraints constraints) {
		final Component component = createComponent(prototype); //create a component from the prototype
		add(component, constraints); //add the component to the container
		return component; //return the component we created
	}

	/**
	 * Creates a component appropriate for the context of this component from the given prototype. This version creates the following components, in order of
	 * priority:
	 * <dl>
	 * <dt>{@link ActionPrototype}</dt>
	 * <dd>{@link Button}</dd>
	 * <dt>{@link LabelPrototype}</dt>
	 * <dd>{@link Label}</dd>
	 * <dt>{@link MenuPrototype}</dt>
	 * <dd>{@link DropMenu}</dd>
	 * <dt>{@link TogglePrototype}</dt>
	 * <dd>{@link BooleanSelectButton}</dd>
	 * <dt>{@link ValuePrototype}&lt;{@link Boolean}&gt;</dt>
	 * <dd>{@link CheckControl}</dd>
	 * <dt>{@link ValuePrototype}&lt;{@link Number}&gt; with installed {@link RangeValidator}</dt>
	 * <dd>{@link SliderControl}</dd>
	 * <dt>{@link ValuePrototype}&lt;?&gt;</dt>
	 * <dd>{@link TextControl}</dd>
	 * </dl>
	 * @param prototype The prototype of the component to create.
	 * @return A new component based upon the given prototype.
	 * @throws IllegalArgumentException if no component can be created from the given prototype
	 */
	public Component createComponent(final Prototype prototype) {
		if(prototype instanceof MenuPrototype) { //menu prototypes
			return new DropMenu((MenuPrototype)prototype, Flow.PAGE);
		} else if(prototype instanceof ActionPrototype) { //all other action prototypes
			return new Button((ActionPrototype)prototype);
		} else if(prototype instanceof LabelPrototype) { //label prototypes
			return new Label((LabelPrototype)prototype);
		} else if(prototype instanceof TogglePrototype) { //toggle prototypes
			final TogglePrototype togglePrototype = (TogglePrototype)prototype; //get the toggle prototype
			final BooleanSelectButton booleanSelectButton = new BooleanSelectButton(togglePrototype); //create a boolean select button
			booleanSelectButton.setToggle(true); //turn on toggling
			return booleanSelectButton; //return the button
		} else if(prototype instanceof ValuePrototype) { //value prototypes
			final ValuePrototype<?> valuePrototype = (ValuePrototype<?>)prototype; //get the value prototype
			final Class<?> valueClass = valuePrototype.getValueClass(); //get the type of value represented
			if(Boolean.class.isAssignableFrom(valueClass)) { //if a boolean value is represented
				return new CheckControl((ValuePrototype<Boolean>)prototype);
			} else if(Number.class.isAssignableFrom(valueClass) && valuePrototype.getValidator() instanceof RangeValidator) { //if a number range is represented
				return new SliderControl<Number>((ValuePrototype<Number>)prototype, Flow.LINE);
			} else { //if the prototype is unrecognized
				throw new IllegalArgumentException("Unrecognized prototype: " + prototype.getClass());
			}
			/*TODO finish
						else {	//for any other value type
							return new TextControl<V>()
						}
			*/
		} else { //if the prototype is unrecognized
			throw new IllegalArgumentException("Unrecognized prototype: " + prototype.getClass());
		}
	}

	/*
	 * Creates a component appropriate for the context of this component from the given prototype. This implementation creates the following components, in order
	 * of priority:
	 * <dl>
	 * <dt>{@link ActionPrototype}</dt>
	 * <dd>{@link Button}</dd>
	 * <dt>{@link LabelPrototype}</dt>
	 * <dd>{@link Label}</dd>
	 * <dt>{@link MenuPrototype}</dt>
	 * <dd>{@link DropMenu}</dd>
	 * <dt>{@link ValuePrototype}&lt;{@link Boolean}&gt;</dt>
	 * <dd>{@link CheckControl}</dd>
	 * <dt>{@link ValuePrototype}&lt;?&gt;</dt>
	 * <dd>{@link TextControl}</dd>
	 * </dl>
	 * @param prototype The prototype of the component to create.
	 * @return A new component based upon the given prototype.
	 * @throws IllegalArgumentException if no component can be created from the given prototype
	 */
	/*TODO del if not needed
		public <V> ValueControl<V, ?> createValueControl(final ValuePrototype<V> valuePrototype)
		{
			final Class<V> valueClass=valuePrototype.getValueClass();	//get the type of value represented
			if(Boolean.class.isAssignableFrom(valueClass)) {	//if a boolean value is represented
				return new CheckControl((ValuePrototype<Boolean>)valuePrototype);
			}
			else {	//for any other value type
				return new TextControl<V>()
			}
			
		}
	*/

	@Override
	public boolean remove(final Object componentObject) {
		final Component component = (Component)componentObject; //cast the object to a component
		final int index = indexOf(component); //get the index of the component
		removeComponent(component); //remove the component normally
		assert index >= 0 : "Component successfully removed from container, yet previous index is negative.";
		return true; //removing a component from a container will always result in container modification
	}

	@Override
	public Component remove(final int index) {
		final Component component = get(index); //get the component at this index
		remove(component); //remove the component
		return component; //return the component that was removed
	}

	@Override
	public void clear() {
		for(final Component component : this) { //for each component in the container
			remove(component); //remove this component
		}
	}

	@Override
	public Iterator<Component> iterator() {
		return getComponentList().iterator();
	}

	@Override
	public List<Component> getChildComponents() {
		return new ArrayList<Component>(getComponentList()); //create and return a copy of the list
	}

	/**
	 * Sets the children in this container. This method along with {@link #getChildComponents()} provides a <code>children</code> property for alternate children
	 * access.
	 * @param children The new children for this container in order.
	 * @see #clear()
	 * @see #add(Component)
	 */
	public void setChildren(final List<Component> children) {
		clear(); //remove all children from the container
		for(final Component child : children) { //for each child
			add(child); //add this child
		}
	}

	@Override
	public <T extends Constraints> void setLayout(final Layout<T> newLayout) {
		super.setLayout(newLayout); //delegate to the parent class
	}

	/**
	 * Layout constructor with a default info model.
	 * @param layout The layout definition for the container.
	 * @throws NullPointerException if the given layout is <code>null</code>.
	 */
	public AbstractContainer(final Layout<? extends Constraints> layout) {
		this(new DefaultInfoModel(), layout); //construct the class with a default info model
	}

	/**
	 * Info model and layout constructor.
	 * @param infoModel The component info model.
	 * @param layout The layout definition for the container.
	 * @throws NullPointerException if the given info model and/or layout is <code>null</code>.
	 */
	public AbstractContainer(final InfoModel infoModel, final Layout<? extends Constraints> layout) {
		super(infoModel, layout); //construct the parent class
	}

}
