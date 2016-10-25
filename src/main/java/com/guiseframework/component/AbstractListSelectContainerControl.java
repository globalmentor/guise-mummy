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

import static com.globalmentor.java.Objects.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.*;

import com.globalmentor.event.EventListenerManager;
import com.guiseframework.component.layout.*;
import com.guiseframework.event.*;
import com.guiseframework.model.*;
import com.guiseframework.validator.*;

/**
 * An abstract list select control that is also a container. The component valid status is updated before a change in the {@link #VALUE_PROPERTY} or the
 * {@link #VALIDATOR_PROPERTY} is fired.
 * <p>
 * This implementation installs a default value representation strategy that simply passes through the represented component.
 * </p>
 * @author Garret Wilson
 */
public abstract class AbstractListSelectContainerControl extends AbstractContainerControl implements ListSelectControl<Component> {

	/** The static representation strategy to represent component values as themselves. */
	public static final ComponentRepresentationStrategy COMPONENT_REPRESENTATION_STRATEGY = new ComponentRepresentationStrategy();

	//TODO make sure we listen for enabled status changing on the layout and send an index enabled property change, maybe

	@Override
	public AbstractValueLayout<? extends ControlConstraints> getLayout() {
		return (AbstractValueLayout<? extends ControlConstraints>)super.getLayout();
	}

	/** The strategy used to generate a component to represent each value in the model. */
	private ValueRepresentationStrategy<Component> valueRepresentationStrategy;

	@Override
	public ValueRepresentationStrategy<Component> getValueRepresentationStrategy() {
		return valueRepresentationStrategy;
	}

	@Override
	public void setValueRepresentationStrategy(final ValueRepresentationStrategy<Component> newValueRepresentationStrategy) {
		if(valueRepresentationStrategy != newValueRepresentationStrategy) { //if the value is really changing
			final ValueRepresentationStrategy<Component> oldValueRepresentationStrategy = valueRepresentationStrategy; //get the old value
			valueRepresentationStrategy = checkInstance(newValueRepresentationStrategy, "Value representation strategy cannot be null."); //actually change the value
			firePropertyChange(VALUE_REPRESENTATION_STRATEGY_PROPERTY, oldValueRepresentationStrategy, newValueRepresentationStrategy); //indicate that the value changed
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version returns the given component to fulfill the interface contract of {@link ListSelectControl}.
	 * </p>
	 */
	@Override
	public Component getComponent(final Component object) {
		return object; //return the given component
	}

	/**
	 * Layout constructor.
	 * @param layout The layout definition for the container.
	 * @throws NullPointerException if the given layout is <code>null</code>.
	 */
	protected AbstractListSelectContainerControl(final AbstractValueLayout<?> layout) {
		super(layout); //construct the parent class
		this.valueRepresentationStrategy = COMPONENT_REPRESENTATION_STRATEGY; //use the shared component representation strategy
		layout.addPropertyChangeListener(getRepeatPropertyChangeListener()); //listen an repeat all property changes of the card layout value model TODO make sure the card constraint values are passed on, too---right now they probably aren't as the property change event subclass isn't recognized in the repeater listener class
		layout.addVetoableChangeListener(getRepeatVetoableChangeListener()); //listen and repeat all vetoable changes of the card layout value model
		addPropertyChangeListener(VALUE_PROPERTY, new PropertyChangeListener() { //listen for the value changing

			@Override
			public void propertyChange(final PropertyChangeEvent propertyChangeEvent) { //if the value changes
				fireSelectionChanged(null, null); //indicate that the selection changed
			}

		});
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version first updates the valid status if the value is reported as being changed.
	 * </p>
	 */
	@Override
	protected <VV> void firePropertyChange(final String propertyName, final VV oldValue, final VV newValue) {
		if(VALUE_PROPERTY.equals(propertyName) || VALIDATOR_PROPERTY.equals(propertyName)) { //if the value property or the validator property is being reported as changed
			updateValid(); //update the valid status based upon the new property, so that any listeners will know whether the new property is valid
		}
		super.firePropertyChange(propertyName, oldValue, newValue); //fire the property change event normally
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version only checks the validity of the selected card.
	 * </p>
	 */
	@Override
	protected boolean determineChildrenValid() {
		final Component selectedComponent = getValue(); //get the selected card
		return selectedComponent == null || selectedComponent.isValid(); //the children will only be invalid if the selected card is invalid
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version only validates the selected card.
	 * </p>
	 */
	@Override
	public boolean validateChildren() {
		final Component selectedComponent = getValue(); //get the selected card
		return selectedComponent != null ? selectedComponent.validate() : false; //only validate the selected card if there is one
	}

	//ValueModel delegations

	@Override
	public Component getDefaultValue() {
		return getLayout().getDefaultValue();
	}

	@Override
	public Component getValue() {
		return getLayout().getValue();
	}

	@Override
	public void setValue(final Component newValue) throws PropertyVetoException {
		getLayout().setValue(newValue);
	}

	@Override
	public void clearValue() {
		getLayout().clearValue();
	}

	@Override
	public void resetValue() {
		getLayout().resetValue();
	}

	@Override
	public Validator<Component> getValidator() {
		return getLayout().getValidator();
	}

	@Override
	public void setValidator(final Validator<Component> newValidator) {
		getLayout().setValidator(newValidator);
	}

	@Override
	public boolean isValidValue() {
		return getLayout().isValidValue();
	}

	@Override
	public void validateValue() throws ValidationException {
		getLayout().validateValue();
	}

	@Override
	public Class<Component> getValueClass() {
		return getLayout().getValueClass();
	}

	//SelectModel delegations

	@Override
	public boolean replace(final Component oldValue, final Component newValue) {
		throw new UnsupportedOperationException("replace() not yet supported");
	}

	@Override
	public Component getSelectedValue() {
		return getValue();
	}

	@Override
	public Component[] getSelectedValues() {
		final Component selectedValue = getValue(); //get the selected value, if any
		return selectedValue != null ? new Component[] {selectedValue} : new Component[] {}; //return an array with the component, if there is one selected
	}

	@Override
	public void setSelectedValues(final Component... values) throws PropertyVetoException {
		setValue(values.length > 0 ? values[0] : null); //select the first or no value
	}

	/** The shared single component selection policy. */
	private static final ListSelectionPolicy<Component> SINGLE_COMPONENT_SELECTION_POLICY = new SingleListSelectionPolicy<Component>();

	//ListSelectModel delegations

	@Override
	public ListSelectionPolicy<Component> getSelectionPolicy() {
		return SINGLE_COMPONENT_SELECTION_POLICY;
	}

	@Override
	public int getSelectedIndex() {
		return getLayout().getSelectedIndex();
	}

	@Override
	public int[] getSelectedIndexes() {
		final int selectedIndex = getLayout().getSelectedIndex(); //get the selected index
		return selectedIndex >= 0 ? new int[] {selectedIndex} : new int[] {}; //return the selected index in an array, if there is a selected index
	}

	@Override
	public void setSelectedIndexes(final int... indexes) throws PropertyVetoException {
		getLayout().setSelectedIndex(indexes.length > 0 ? indexes[0] : -1); //select the first index if there are indexes to select
	}

	@Override
	public void addSelectedIndexes(int... indexes) throws PropertyVetoException {
		if(getSelectedIndex() < 0) { //only if there are no selected indexes
			setSelectedIndexes(indexes); //set the new index
		}
	}

	@Override
	public void removeSelectedIndexes(int... indexes) throws PropertyVetoException {
		final int selectedIndex = getLayout().getSelectedIndex(); //get the selected index
		for(int i = indexes.length - 1; i >= 0; --i) { //for each index index
			final int index = indexes[i]; //get this index
			if(index == selectedIndex) { //if this index is selected
				getLayout().setSelectedIndex(-1); //clear the selected index
				break; //there can only have been one selected index
			}
		}
	}

	@Override
	public boolean isValueDisplayed(final Component value) {
		return getLayout().getConstraints(value).isDisplayed();
	}

	@Override
	public void setValueDisplayed(final Component value, final boolean newDisplayed) {
		getLayout().getConstraints(value).setDisplayed(newDisplayed);
	} //TODO fix property change event

	@Override
	public boolean isIndexDisplayed(final int index) {
		return isValueDisplayed(get(index));
	}

	@Override
	public void setIndexDisplayed(final int index, final boolean newDisplayed) {
		setValueDisplayed(get(index), newDisplayed);
	} //TODO fix property change event

	@Override
	public boolean isValueEnabled(final Component value) {
		return getLayout().getConstraints(value).isEnabled();
	}

	@Override
	public void setValueEnabled(final Component value, final boolean newEnabled) {
		getLayout().getConstraints(value).setEnabled(newEnabled);
	} //TODO fix property change event

	@Override
	public boolean isIndexEnabled(final int index) {
		return isValueEnabled(get(index));
	}

	@Override
	public void setIndexEnabled(final int index, final boolean newEnabled) {
		setValueEnabled(get(index), newEnabled);
	} //TODO fix property change event

	@Override
	public void addListListener(final ListListener<Component> listListener) {
		getEventListenerManager().add(ListListener.class, listListener); //add the listener
	}

	@Override
	public void removeListListener(final ListListener<Component> listListener) {
		getEventListenerManager().remove(ListListener.class, listListener); //remove the listener
	}

	@Override
	public void addListSelectionListener(final ListSelectionListener<Component> selectionListener) {
		getEventListenerManager().add(ListSelectionListener.class, selectionListener); //add the listener
	}

	@Override
	public void removeListSelectionListener(final ListSelectionListener<Component> selectionListener) {
		getEventListenerManager().remove(ListSelectionListener.class, selectionListener); //remove the listener
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation also fires a list modified event to all registered list listeners, if any.
	 * </p>
	 */
	@Override
	protected void fireChildComponentAdded(final ComponentEvent childComponentEvent) { //TODO it might be better to listen for the composite component events and act accordingly
		super.fireChildComponentAdded(childComponentEvent); //fire the component added event normally
		if(getEventListenerManager().hasListeners(ListListener.class)) { //if there are appropriate listeners registered
			final Component childComponent = childComponentEvent.getComponent(); //get the added child component
			final ListEvent<Component> listEvent = new ListEvent<Component>(this, indexOf(childComponent), childComponent, null); //create a new list event
			for(final ListListener<Component> listListener : getEventListenerManager().getListeners(ListListener.class)) { //for each list listener
				listListener.listModified(listEvent); //dispatch the list event to the listener
			}
		}
	}

	/**
	 * Fires a given component removed event to all registered composite component listeners. This implementation also fires a list modified event to all
	 * registered list listeners, if any.
	 * @param childComponentEvent The child component event to fire.
	 */
	protected void fireChildComponentRemoved(final ComponentEvent childComponentEvent) { //TODO it might be better to listen for the composite component events and act accordingly
		super.fireChildComponentRemoved(childComponentEvent); //fire the component removed event normally
		if(getEventListenerManager().hasListeners(ListListener.class)) { //if there are appropriate listeners registered
			final Component childComponent = childComponentEvent.getComponent(); //get the removed child component
			final ListEvent<Component> listEvent = new ListEvent<Component>(this, -1, null, childComponent); //create a new list event
			for(final ListListener<Component> listListener : getEventListenerManager().getListeners(ListListener.class)) { //for each list listener
				listListener.listModified(listEvent); //dispatch the list event to the listener
			}
		}
	}

	/**
	 * Fires an event to all registered selection listeners indicating the selection changed.
	 * @param addedIndex The index that was added to the selection, or <code>null</code> if no index was added or it is unknown whether or which indices were
	 *          added.
	 * @param removedIndex The index that was removed from the list, or <code>null</code> if no index was removed or it is unknown whether or which indices were
	 *          removed.
	 * @see ListSelectionListener
	 * @see ListSelectionEvent
	 */
	protected void fireSelectionChanged(final Integer addedIndex, final Integer removedIndex) {
		final EventListenerManager eventListenerManager = getEventListenerManager(); //get the event listener manager
		if(eventListenerManager.hasListeners(ListSelectionListener.class)) { //if there are appropriate listeners registered
			final ListSelectionEvent<Component> selectionEvent = new ListSelectionEvent<Component>(this, addedIndex, removedIndex); //create a new event
			for(final ListSelectionListener<Component> listener : eventListenerManager.getListeners(ListSelectionListener.class)) { //for each registered event listeners
				listener.listSelectionChanged(selectionEvent); //dispatch the event
			}
		}
	}

	//List delegations

	@Override
	public Object[] toArray() {
		return getComponentList().toArray();
	}

	@Override
	public <T> T[] toArray(final T[] array) {
		return getComponentList().toArray(array);
	}

	@Override
	public boolean containsAll(final Collection<?> collection) {
		return getComponentList().containsAll(collection);
	}

	@Override
	public boolean addAll(final Collection<? extends Component> collection) {
		throw new UnsupportedOperationException("addAll(Collection) not yet supported");
	} //TODO add all these to container

	@Override
	public synchronized boolean addAll(final int index, final Collection<? extends Component> collection) {
		throw new UnsupportedOperationException("addAll(index, Collection) not yet supported");
	}

	@Override
	public boolean removeAll(final Collection<?> collection) {
		throw new UnsupportedOperationException("removeAll(Collection) not yet supported");
	}

	@Override
	public boolean retainAll(final Collection<?> collection) {
		throw new UnsupportedOperationException("retainAll(Collection) not yet supported");
	}

	@Override
	public Component set(final int index, final Component value) {
		throw new UnsupportedOperationException("set(index, value) not yet supported");
	}

	@Override
	public ListIterator<Component> listIterator() {
		return getComponentList().listIterator();
	}

	@Override
	public ListIterator<Component> listIterator(final int index) {
		return getComponentList().listIterator(index);
	}

	@Override
	public List<Component> subList(final int fromIndex, final int toIndex) {
		return getComponentList().subList(fromIndex, toIndex);
	}

	/**
	 * Adds a component to the container along with a label. This convenience method creates new card layout constraints from the given label model and adds the
	 * component.
	 * @param component The component to add.
	 * @param labelModel The label associated with an individual component.
	 * @throws NullPointerException if the given label is <code>null</code>.
	 * @throws IllegalArgumentException if the component already has a parent.
	 */
	/*TODO del if not wanted
		public void add(final Component component, final LabelModel labelModel)
		{
			add(component, new CardLayout.Constraints(labelModel));	//create card layout constraints for the label and add the component to the container
		}
	*/

	/**
	 * Convenience method to determine whether a card is displayed based upon its associated constraints.
	 * @param component The component for which the card should be displayed or not displayed.
	 * @return Whether the card is displayed or has no representation, taking up no space.
	 * @throws IllegalStateException if the given component has no associated constraints.
	 * @see ControlConstraints#isDisplayed()
	 */
	public boolean isDisplayed(final Component component) {
		final ControlConstraints cardConstraints = getLayout().getConstraints(component); //get constraints of the component
		if(cardConstraints == null) { //if there are no constraints
			throw new IllegalStateException("Component " + component + " has no associated constraints.");
		}
		return cardConstraints.isDisplayed(); //return the displayed status of the constraints
	}

	/**
	 * Sets a card displayed or not displayed. This convenience method changes the displayed status of the component's associated constraints.
	 * @param component The component for which the card should be displayed or not displayed.
	 * @param newDisplayed <code>true</code> if the card should be displayed.
	 * @throws IllegalStateException if the given component has no associated constraints.
	 * @see ControlConstraints#setDisplayed(boolean)
	 */
	public void setDisplayed(final Component component, final boolean newDisplayed) {
		final ControlConstraints cardConstraints = getLayout().getConstraints(component); //get constraints of the component
		if(cardConstraints == null) { //if there are no constraints
			throw new IllegalStateException("Component " + component + " has no associated constraints.");
		}
		cardConstraints.setDisplayed(newDisplayed); //change the displayed status of the constraints
	}

	/**
	 * Convenience method to determine whether a card is enabled based upon its associated constraints.
	 * @param component The component for which the card should be enabled or disabled.
	 * @return Whether the card is enabled and can receive user input.
	 * @throws IllegalStateException if the given component has no associated constraints.
	 * @see CardConstraints#isEnabled()
	 */
	public boolean isEnabled(final Component component) {
		final ControlConstraints cardConstraints = getLayout().getConstraints(component); //get constraints of the component
		if(cardConstraints == null) { //if there are no constraints
			throw new IllegalStateException("Component " + component + " has no associated constraints.");
		}
		return cardConstraints.isEnabled(); //return the enabled status of the constraints
	}

	/**
	 * Enables or disables a card. This convenience method changes the enabled status of the component's associated constraints.
	 * @param component The component for which the card should be enabled or disabled.
	 * @param newEnabled <code>true</code> if the card can be selected.
	 * @throws IllegalStateException if the given component has no associated constraints.
	 * @see CardConstraints#setEnabled(boolean)
	 */
	public void setEnabled(final Component component, final boolean newEnabled) {
		final ControlConstraints cardConstraints = getLayout().getConstraints(component); //get constraints of the component
		if(cardConstraints == null) { //if there are no constraints
			throw new IllegalStateException("Component " + component + " has no associated constraints.");
		}
		cardConstraints.setEnabled(newEnabled); //change the enabled status of the constraints
	}

	/**
	 * A strategy for to represent components in a list select model as themselves.
	 * @author Garret Wilson
	 */
	public static class ComponentRepresentationStrategy implements ValueRepresentationStrategy<Component> {

		/**
		 * {@inheritDoc}
		 * <p>
		 * This implementation returns the component value itself.
		 * </p>
		 */
		@Override
		public Component createComponent(final ListSelectModel<Component> model, final Component value, final int index, final boolean selected,
				final boolean focused) {
			return value; //return the component to represent itself
		}
	}

}
