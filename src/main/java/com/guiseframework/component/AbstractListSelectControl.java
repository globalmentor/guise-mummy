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

import java.beans.PropertyVetoException;
import java.lang.reflect.*;
import java.util.*;

import static com.globalmentor.java.Classes.*;
import static com.globalmentor.java.Objects.*;

import com.globalmentor.event.EventListenerManager;
import com.guiseframework.converter.*;
import com.guiseframework.event.*;
import com.guiseframework.model.*;
import com.guiseframework.validator.*;

/**
 * Abstract implementation of a control to allow selection by the user of a value from a list. The component valid status is updated before a change in the
 * {@link #VALUE_PROPERTY} or the {@link #VALIDATOR_PROPERTY} is fired. This implementation does not yet fully support elements that appear more than once in
 * the model.
 * @param <V> The type of values to select.
 * @author Garret Wilson
 */
public abstract class AbstractListSelectControl<V> extends AbstractCompositeStateControl<V, AbstractListSelectControl.ValueComponentState> implements
		ListSelectControl<V> {

	/** The list select model used by this component. */
	private final ListSelectModel<V> listSelectModel;

	/** @return The list select model used by this component. */
	protected ListSelectModel<V> getListSelectModel() {
		return listSelectModel;
	}

	/** The strategy used to generate a component to represent each value in the model. */
	private ValueRepresentationStrategy<V> valueRepresentationStrategy;

	/** @return The strategy used to generate a component to represent each value in the model. */
	public ValueRepresentationStrategy<V> getValueRepresentationStrategy() {
		return valueRepresentationStrategy;
	}

	/**
	 * Sets the strategy used to generate a component to represent each value in the model. This is a bound property
	 * @param newValueRepresentationStrategy The new strategy to create components to represent this model's values.
	 * @throws NullPointerException if the provided value representation strategy is <code>null</code>.
	 * @see SelectControl#VALUE_REPRESENTATION_STRATEGY_PROPERTY
	 */
	public void setValueRepresentationStrategy(final ValueRepresentationStrategy<V> newValueRepresentationStrategy) {
		if(valueRepresentationStrategy != newValueRepresentationStrategy) { //if the value is really changing
			final ValueRepresentationStrategy<V> oldValueRepresentationStrategy = valueRepresentationStrategy; //get the old value
			valueRepresentationStrategy = checkInstance(newValueRepresentationStrategy, "Value representation strategy cannot be null."); //actually change the value
			firePropertyChange(VALUE_REPRESENTATION_STRATEGY_PROPERTY, oldValueRepresentationStrategy, newValueRepresentationStrategy); //indicate that the value changed
		}
	}

	/**
	 * Retrieves the component for the given object. If no component yet exists for the given object, one will be created. This version is provided to allow
	 * public access.
	 * @param value The object for which a representation component should be returned.
	 * @return The child component representing the given object.
	 * @throws IllegalArgumentException if the given object is not an appropriate object for a component to be created.
	 */
	public Component getComponent(final V value) {
		return super.getComponent(value); //delegate to the parent version
	}

	/**
	 * Creates a component state to represent the given object.
	 * @param value The object with which the component state is to be associated.
	 * @return The component state to represent the given object.
	 * @throws IllegalArgumentException if the given object is not an appropriate object for a component state to be created.
	 */
	protected ValueComponentState createComponentState(final V value) {
		//TODO assert that there is a representation strategy, or otherwise check
		//TODO improve and/or change parameters
		final Component valueComponent = getValueRepresentationStrategy().createComponent(this, value, -1, false, false); //create a new component for the value
		return new ValueComponentState(valueComponent); //create a new component state for the value's component and metadata
	}

	/**
	 * List select model and value representation strategy constructor.
	 * @param listSelectModel The component list select model.
	 * @param valueRepresentationStrategy The strategy to create controls to represent this model's values.
	 * @throws NullPointerException if the given list select model and/or value representation strategy is <code>null</code>.
	 */
	public AbstractListSelectControl(final ListSelectModel<V> listSelectModel, final ValueRepresentationStrategy<V> valueRepresentationStrategy) {
		this.valueRepresentationStrategy = checkInstance(valueRepresentationStrategy, "Value representation strategy cannot be null.");
		this.listSelectModel = checkInstance(listSelectModel, "List select model cannot be null."); //save the list select model
		this.listSelectModel.addPropertyChangeListener(getRepeatPropertyChangeListener()); //listen and repeat all property changes of the list select model
		this.listSelectModel.addVetoableChangeListener(getRepeatVetoableChangeListener()); //listen and repeat all vetoable changes of the list select model
		this.listSelectModel.addListListener(new ListListener<V>() { //install a repeater list listener to listen to the decorated model

					public void listModified(final ListEvent<V> listEvent) { //if the list is modified
						fireListModified(listEvent.getIndex(), listEvent.getAddedElement(), listEvent.getRemovedElement()); //repeat the event, indicating the component as the source of the event
					}
				});
		this.listSelectModel.addListSelectionListener(new ListSelectionListener<V>() { //install a repeater list selection listener to listen to the decorated model

					public void listSelectionChanged(final ListSelectionEvent<V> selectionEvent) { //if the list selection changes
						fireSelectionChanged(selectionEvent.getAddedElement(), selectionEvent.getRemovedElement()); //repeat the event, indicating the component as the source of the event
					}
				});
		addListListener(new ListListener<V>() { //listen for list changes

			public void listModified(final ListEvent<V> listEvent) { //if list is modified
				clearComponentStates(); //clear all the components and component states TODO probably do this on a component-by-component basis
			};
		});
	}

	/**
	 * Reports that a bound property has changed. This version first updates the valid status if the value is reported as being changed.
	 * @param propertyName The name of the property being changed.
	 * @param oldValue The old property value.
	 * @param newValue The new property value.
	 */
	protected <VV> void firePropertyChange(final String propertyName, final VV oldValue, final VV newValue) {
		if(VALUE_PROPERTY.equals(propertyName) || VALIDATOR_PROPERTY.equals(propertyName)) { //if the value property or the validator property is being reported as changed
			updateValid(); //update the valid status based upon the new property, so that any listeners will know whether the new property is valid
		}
		super.firePropertyChange(propertyName, oldValue, newValue); //fire the property change event normally
	}

	/**
	 * Checks the state of the component for validity. This version checks the validity of the list select model. This version performs no additional checks if
	 * the control is disabled.
	 * @return <code>true</code> if the component and all children passes all validity tests, else <code>false</code>.
	 */
	protected boolean determineValid() {
		if(!super.determineValid()) { //if we don't pass the default validity checks
			return false; //the component isn't valid
		}
		return !isEnabled() || getListSelectModel().isValidValue(); //the component is valid if the list select model has a valid value (don't check the list select model if the control is not enabled)
	}

	/**
	 * Validates the user input of this component and all child components. The component will be updated with error information. This version validates the
	 * associated list select model. This version performs no additional checks if the control is disabled.
	 * @return The current state of {@link #isValid()} as a convenience.
	 */
	public boolean validate() {
		super.validate(); //validate the parent class
		if(isEnabled()) { //if the control is enabled
			try {
				getListSelectModel().validateValue(); //validate the value model
			} catch(final ValidationException validationException) { //if there is a validation error
				//TODO del			componentException.setComponent(this);	//make sure the exception knows to which component it relates
				setNotification(new Notification(validationException)); //add notification of this error to the component
			}
		}
		return isValid(); //return the current valid state
	}

	/**
	 * Resets the control to its default value. This version resets the control value.
	 * @see #resetValue()
	 */
	public void reset() {
		super.reset(); //reset normally
		resetValue(); //reset the control value
	}

	//ValueModel delegations

	/** @return The default value. */
	public V getDefaultValue() {
		return getListSelectModel().getDefaultValue();
	}

	/** @return The input value, or <code>null</code> if there is no input value. */
	public V getValue() {
		return getListSelectModel().getValue();
	}

	/**
	 * Sets the input value. This is a bound property that only fires a change event when the new value is different via the <code>equals()</code> method. If a
	 * validator is installed, the value will first be validated before the current value is changed. Validation always occurs if a validator is installed, even
	 * if the value is not changing. If the value change is vetoed by the installed validator, the validation exception will be accessible via
	 * {@link PropertyVetoException#getCause()}.
	 * @param newValue The input value of the model.
	 * @throws PropertyVetoException if the provided value is not valid or the change has otherwise been vetoed.
	 * @see #getValidator()
	 * @see #VALUE_PROPERTY
	 */
	public void setValue(final V newValue) throws PropertyVetoException {
		getListSelectModel().setValue(newValue);
	}

	/**
	 * Clears the value by setting the value to <code>null</code>, which may be invalid according to any installed validators. No validation occurs.
	 * @see ValueModel#VALUE_PROPERTY
	 */
	public void clearValue() {
		getListSelectModel().clearValue();
	}

	/**
	 * Resets the value to a default value, which may be invalid according to any installed validators. No validation occurs.
	 * @see #VALUE_PROPERTY
	 */
	public void resetValue() {
		getListSelectModel().resetValue();
	}

	/** @return The validator for this model, or <code>null</code> if no validator is installed. */
	public Validator<V> getValidator() {
		return getListSelectModel().getValidator();
	}

	/**
	 * Sets the validator. This is a bound property
	 * @param newValidator The validator for this model, or <code>null</code> if no validator should be used.
	 * @see #VALIDATOR_PROPERTY
	 */
	public void setValidator(final Validator<V> newValidator) {
		getListSelectModel().setValidator(newValidator);
	}

	/**
	 * Determines whether the value of this model is valid.
	 * @return Whether the value of this model is valid.
	 */
	public boolean isValidValue() {
		return getListSelectModel().isValidValue();
	}

	/**
	 * Validates the value of this model, throwing an exception if the model is not valid.
	 * @throws ValidationException if the value of this model is not valid.
	 */
	public void validateValue() throws ValidationException {
		getListSelectModel().validateValue();
	}

	/** @return The class representing the type of value this model can hold. */
	public Class<V> getValueClass() {
		return getListSelectModel().getValueClass();
	}

	//SelectModel delegations

	/**
	 * Replaces the first occurrence in the of the given value with its replacement. This method ensures that another thread does not change the model while the
	 * search and replace operation occurs.
	 * @param oldValue The value for which to search.
	 * @param newValue The replacement value.
	 * @return Whether the operation resulted in a modification of the model.
	 */
	public boolean replace(final V oldValue, final V newValue) {
		return getListSelectModel().replace(oldValue, newValue);
	}

	/**
	 * Determines the selected value. This method delegates to the selection strategy. If more than one value is selected, the lead selected value will be
	 * returned.
	 * @return The value currently selected, or <code>null</code> if no value is currently selected.
	 */
	public V getSelectedValue() {
		return getListSelectModel().getSelectedValue();
	}

	/**
	 * Determines the selected values. This method delegates to the selection strategy.
	 * @return The values currently selected.
	 */
	public V[] getSelectedValues() {
		return getListSelectModel().getSelectedValues();
	}

	/**
	 * Sets the selected values. If a value occurs more than one time in the model, all occurrences of the value will be selected. Values that do not occur in the
	 * select model will be ignored. If the value change is vetoed by the installed validator, the validation exception will be accessible via
	 * {@link PropertyVetoException#getCause()}. This method delegates to the selection strategy.
	 * @param values The values to select.
	 * @throws PropertyVetoException if the provided value is not valid or the change has otherwise been vetoed.
	 */
	public void setSelectedValues(final V... values) throws PropertyVetoException {
		getListSelectModel().setSelectedValues(values);
	}

	//ListSelectModel delegations

	/** @return The selection policy for this model. */
	public ListSelectionPolicy<V> getSelectionPolicy() {
		return getListSelectModel().getSelectionPolicy();
	}

	/**
	 * Determines the selected index. If more than one index is selected, the lead selected index will be returned.
	 * @return The index currently selected, or -1 if no index is selected.
	 * @see #getSelectedValue()
	 */
	public int getSelectedIndex() {
		return getListSelectModel().getSelectedIndex();
	}

	/**
	 * Determines the selected indices.
	 * @return The indices currently selected.
	 * @see #getSelectedValues()
	 */
	public int[] getSelectedIndexes() {
		return getListSelectModel().getSelectedIndexes();
	}

	/**
	 * Sets the selected indices. Invalid and duplicate indices will be ignored. If the value change is vetoed by the installed validator, the validation
	 * exception will be accessible via {@link PropertyVetoException#getCause()}.
	 * @param indexes The indices to select.
	 * @throws PropertyVetoException if the provided value is not valid or the change has otherwise been vetoed.
	 * @see ListSelectionPolicy#getSetSelectedIndices(ListSelectModel, int[])
	 * @see #setSelectedValues(V[])
	 * @see #addSelectedIndexes(int...)
	 */
	public void setSelectedIndexes(int... indexes) throws PropertyVetoException {
		getListSelectModel().setSelectedIndexes(indexes);
	}

	/**
	 * Adds a selection at the given indices. Any invalid indices will be ignored. If the value change is vetoed by the installed validator, the validation
	 * exception will be accessible via {@link PropertyVetoException#getCause()}.
	 * @param indexes The indices to add to the selection.
	 * @throws PropertyVetoException if the provided value is not valid or the change has otherwise been vetoed.
	 * @see ListSelectionPolicy#getAddSelectedIndices(ListSelectModel, int[])
	 * @see #setSelectedIndexes(int[])
	 */
	public void addSelectedIndexes(int... indexes) throws PropertyVetoException {
		getListSelectModel().addSelectedIndexes(indexes);
	}

	/**
	 * Removes a selection at the given indices. Any invalid indices will be ignored. If the value change is vetoed by the installed validator, the validation
	 * exception will be accessible via {@link PropertyVetoException#getCause()}.
	 * @param indexes The indices to remove from the selection.
	 * @throws PropertyVetoException if the provided value is not valid or the change has otherwise been vetoed.
	 * @see ListSelectionPolicy#getRemoveSelectedIndices(ListSelectModel, int[])
	 * @see #setSelectedIndexes(int[])
	 */
	public void removeSelectedIndexes(int... indexes) throws PropertyVetoException {
		getListSelectModel().removeSelectedIndexes(indexes);
	}

	/**
	 * Determines the displayed status of the first occurrence of a given value.
	 * @param value The value for which the displayed status is to be determined.
	 * @return <code>true</code> if the value is displayed, else <code>false</code>.
	 * @throws IndexOutOfBoundsException if the given value does not occur in the model.
	 */
	public boolean isValueDisplayed(final V value) {
		return getListSelectModel().isValueDisplayed(value);
	}

	/**
	 * Sets the displayed status of the first occurrence of a given value. This is a bound value state property.
	 * @param value The value to enable or disable.
	 * @param newDisplayed Whether the value should be displayed.
	 * @see #DISPLAYED_PROPERTY
	 */
	public void setValueDisplayed(final V value, final boolean newDisplayed) {
		getListSelectModel().setValueDisplayed(value, newDisplayed);
	} //TODO fix property change event

	/**
	 * Determines the displayed status of a given index.
	 * @param index The index of the value for which the displayed status is to be determined.
	 * @return <code>true</code> if the value at the given index is displayed, else <code>false</code>.
	 */
	public boolean isIndexDisplayed(final int index) {
		return getListSelectModel().isIndexDisplayed(index);
	}

	/**
	 * Sets the displayed status of a given index. This is a bound value state property.
	 * @param index The index of the value to enable or disable.
	 * @param newDisplayed Whether the value at the given index should be displayed.
	 * @see #DISPLAYED_PROPERTY
	 * @throws IndexOutOfBoundsException if the given index is not within the range of the list.
	 */
	public void setIndexDisplayed(final int index, final boolean newDisplayed) {
		getListSelectModel().setIndexDisplayed(index, newDisplayed);
	} //TODO fix property change event

	/**
	 * Determines the enabled status of the first occurrence of a given value.
	 * @param value The value for which the enabled status is to be determined.
	 * @return <code>true</code> if the value is enabled, else <code>false</code>.
	 * @throws IndexOutOfBoundsException if the given value does not occur in the model.
	 */
	public boolean isValueEnabled(final V value) {
		return getListSelectModel().isValueEnabled(value);
	}

	/**
	 * Sets the enabled status of the first occurrence of a given value. This is a bound value state property.
	 * @param value The value to enable or disable.
	 * @param newEnabled Whether the value should be enabled.
	 * @see #ENABLED_PROPERTY
	 */
	public void setValueEnabled(final V value, final boolean newEnabled) {
		getListSelectModel().setValueEnabled(value, newEnabled);
	} //TODO fix property change event

	/**
	 * Determines the enabled status of a given index.
	 * @param index The index of the value for which the enabled status is to be determined.
	 * @return <code>true</code> if the value at the given index is enabled, else <code>false</code>.
	 */
	public boolean isIndexEnabled(final int index) {
		return getListSelectModel().isIndexEnabled(index);
	}

	/**
	 * Sets the enabled status of a given index. This is a bound value state property.
	 * @param index The index of the value to enable or disable.
	 * @param newEnabled Whether the value at the given index should be enabled.
	 * @see #ENABLED_PROPERTY
	 * @throws IndexOutOfBoundsException if the given index is not within the range of the list.
	 */
	public void setIndexEnabled(final int index, final boolean newEnabled) {
		getListSelectModel().setIndexEnabled(index, newEnabled);
	} //TODO fix property change event

	/**
	 * Adds a list listener.
	 * @param listListener The list listener to add.
	 */
	public void addListListener(final ListListener<V> listListener) {
		getEventListenerManager().add(ListListener.class, listListener); //add the listener
	}

	/**
	 * Removes a list listener.
	 * @param listListener The list listener to remove.
	 */
	public void removeListListener(final ListListener<V> listListener) {
		getEventListenerManager().remove(ListListener.class, listListener); //remove the listener
	}

	/**
	 * Adds a list selection listener.
	 * @param selectionListener The selection listener to add.
	 */
	public void addListSelectionListener(final ListSelectionListener<V> selectionListener) {
		getEventListenerManager().add(ListSelectionListener.class, selectionListener); //add the listener
	}

	/**
	 * Removes a list selection listener.
	 * @param selectionListener The selection listener to remove.
	 */
	public void removeListSelectionListener(final ListSelectionListener<V> selectionListener) {
		getEventListenerManager().remove(ListSelectionListener.class, selectionListener); //remove the listener
	}

	/**
	 * Fires an event to all registered list listeners indicating the list was modified.
	 * @param index The index at which an element was added and/or removed, or -1 if the index is unknown.
	 * @param addedElement The element that was added to the list, or <code>null</code> if no element was added or it is unknown whether or which elements were
	 *          added.
	 * @param removedElement The element that was removed from the list, or <code>null</code> if no element was removed or it is unknown whether or which elements
	 *          were removed.
	 * @see ListListener
	 * @see ListEvent
	 */
	protected void fireListModified(final int index, final V addedElement, final V removedElement) {
		final EventListenerManager eventListenerManager = getEventListenerManager(); //get the event listener manager
		if(eventListenerManager.hasListeners(ListListener.class)) { //if there are appropriate listeners registered
			final ListEvent<V> listEvent = new ListEvent<V>(this, index, addedElement, removedElement); //create a new event
			for(final ListListener<V> listener : eventListenerManager.getListeners(ListListener.class)) { //for each registered event listeners
				listener.listModified(listEvent); //dispatch the event
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
			final ListSelectionEvent<V> selectionEvent = new ListSelectionEvent<V>(this, addedIndex, removedIndex); //create a new event
			for(final ListSelectionListener<V> listener : eventListenerManager.getListeners(ListSelectionListener.class)) { //for each registered event listeners
				listener.listSelectionChanged(selectionEvent); //dispatch the event
			}
		}
	}

	//List delegations

	/** @return The number of values in the model. */
	public int size() {
		return getListSelectModel().size();
	}

	/** @return Whether this model contains no values. */
	public boolean isEmpty() {
		return getListSelectModel().isEmpty();
	}

	/**
	 * Determines whether this model contains the specified value.
	 * @param value The value the presence of which to test.
	 * @return <code>true</code> if this model contains the specified value.
	 */
	public boolean contains(final Object value) {
		return getListSelectModel().contains(value);
	}

	/** @return An iterator over the values in this model. */
	public Iterator<V> iterator() {
		return getListSelectModel().iterator();
	}

	/** @return An array containing all of the values in this model. */
	public Object[] toArray() {
		return getListSelectModel().toArray();
	}

	/**
	 * Returns an array containing all of the values in this model.
	 * @param array The array into which the value of this collection are to be stored, if it is big enough; otherwise, a new array of the same runtime type is
	 *          allocated for this purpose.
	 * @return An array containing the values of this model.
	 * @throws ArrayStoreException if the runtime type of the specified array is not a supertype of the runtime type of every value in this model.
	 * @throws NullPointerException if the specified array is <code>null</code>.
	 */
	public <T> T[] toArray(final T[] array) {
		return getListSelectModel().toArray(array);
	}

	/**
	 * Appends the specified value to the end of this model. This version delegates to {@link #add(int, Object)}.
	 * @param value The value to be appended to this model.
	 * @return <code>true</code>, indicating that the model changed as a result of the operation.
	 */
	public boolean add(final V value) {
		return getListSelectModel().add(value);
	}

	/**
	 * Removes the first occurrence in this model of the specified value.
	 * @param value The value to be removed from this model, if present.
	 * @return <code>true</code> if this model contained the specified value.
	 */
	public boolean remove(final Object value) {
		return getListSelectModel().remove(value);
	}

	/**
	 * Determines if this model contains all of the values of the specified collection.
	 * @param collection The collection to be checked for containment in this model.
	 * @return <code>true</code> if this model contains all of the values of the specified collection.
	 * @throws NullPointerException if the specified collection is <code>null</code>.
	 * @see #contains(Object)
	 */
	public boolean containsAll(final Collection<?> collection) {
		return getListSelectModel().containsAll(collection);
	}

	/**
	 * Appends all of the values in the specified collection to the end of this model, in the order that they are returned by the specified collection's iterator.
	 * @param collection The collection the values of which are to be added to this model.
	 * @return <code>true</code> if this model changed as a result of the call.
	 * @throws NullPointerException if the specified collection is <code>null</code>.
	 * @see #add(Object)
	 */
	public boolean addAll(final Collection<? extends V> collection) {
		return getListSelectModel().addAll(collection);
	}

	/**
	 * Inserts all of the values in the specified collection into this model at the specified position.
	 * @param index The index at which to insert first value from the specified collection.
	 * @param collection The values to be inserted into this model.
	 * @return <code>true</code> if this model changed as a result of the call.
	 * @throws NullPointerException if the specified collection is <code>null</code>.
	 * @throws IndexOutOfBoundsException if the index is out of range (<var>index</var> &lt; 0 || <var>index</var> &gt; <code>size()</code>).
	 */
	public synchronized boolean addAll(final int index, final Collection<? extends V> collection) {
		return getListSelectModel().addAll(index, collection);
	}

	/**
	 * Removes from this model all the values that are contained in the specified collection.
	 * @param collection The collection that defines which values will be removed from this model.
	 * @return <code>true</code> if this model changed as a result of the call.
	 * @throws NullPointerException if the specified collection is <code>null</code>.
	 * @see #remove(Object)
	 * @see #contains(Object)
	 */
	public boolean removeAll(final Collection<?> collection) {
		return getListSelectModel().removeAll(collection);
	}

	/**
	 * Retains only the values in this model that are contained in the specified collection.
	 * @param collection The collection that defines which values this model will retain.
	 * @return <code>true</code> if this model changed as a result of the call.
	 * @throws NullPointerException if the specified collection is <code>null</code>.
	 * @see #remove(Object)
	 * @see #contains(Object)
	 */
	public boolean retainAll(final Collection<?> collection) {
		return getListSelectModel().retainAll(collection);
	}

	/** Removes all of the values from this model. */
	public void clear() {
		getListSelectModel().clear();
	}

	/**
	 * Returns the value at the specified position in this model.
	 * @param index The index of the value to return.
	 * @return The value at the specified position in this model.
	 * @throws IndexOutOfBoundsException if the index is out of range (<var>index</var> &lt; 0 || <var>index</var> &gt;= <code>size()</code>).
	 */
	public V get(final int index) {
		return getListSelectModel().get(index);
	}

	/**
	 * Replaces the value at the specified position in this model with the specified value.
	 * @param index The index of the value to replace.
	 * @param value The value to be stored at the specified position.
	 * @return The value at the specified position.
	 * @throws IndexOutOfBoundsException if the index is out of range (<var>index<var> &lt; 0 || <var>index</var> &gt;= <code>size()</code>).
	 */
	public V set(final int index, final V value) {
		return getListSelectModel().set(index, value);
	}

	/**
	 * Inserts the specified value at the specified position in this model.
	 * @param index The index at which the specified value is to be inserted.
	 * @param value The value to be inserted.
	 * @throws IndexOutOfBoundsException if the index is out of range (<var>index</var> &lt; 0 || <var>index</var> &gt; <code>size()</code>).
	 */
	public void add(final int index, final V value) {
		getListSelectModel().add(index, value);
	}

	/**
	 * Removes the value at the specified position in this model.
	 * @param index The index of the value to removed.
	 * @return The value previously at the specified position.
	 * @throws IndexOutOfBoundsException if the index is out of range (<var>index</var> &lt; 0 || <var>index</var> &gt;= <code>size()</code>).
	 */
	public V remove(final int index) {
		return getListSelectModel().remove(index);
	}

	/**
	 * Returns the index in this model of the first occurrence of the specified value, or -1 if this model does not contain this value.
	 * @param value The value for which to search.
	 * @return The index in this model of the first occurrence of the specified value, or -1 if this model does not contain this value.
	 */
	public int indexOf(final Object value) {
		return getListSelectModel().indexOf(value);
	}

	/**
	 * Returns the index in this model of the last occurrence of the specified value, or -1 if this model does not contain this value.
	 * @param value The value for which to search.
	 * @return The index in this model of the last occurrence of the specified value, or -1 if this model does not contain this value.
	 */
	public int lastIndexOf(final Object value) {
		return getListSelectModel().lastIndexOf(value);
	}

	/** @return A read-only list iterator of the values in this model (in proper sequence). */
	public ListIterator<V> listIterator() {
		return getListSelectModel().listIterator();
	}

	/**
	 * Returns a list iterator of the values in this model (in proper sequence), starting at the specified position in this model.
	 * @param index The index of first value to be returned from the list iterator (by a call to the <code>next()</code> method).
	 * @return A list iterator of the values in this model (in proper sequence), starting at the specified position in this model.
	 * @throws IndexOutOfBoundsException if the index is out of range (<var>index</var> &lt; 0 || <var>index</var> &gt; <code>size()</code>).
	 */
	public ListIterator<V> listIterator(final int index) {
		return getListSelectModel().listIterator(index);
	}

	/**
	 * Returns a read-only view of the portion of this model between the specified <var>fromIndex</var>, inclusive, and <var>toIndex</var>, exclusive.
	 * @param fromIndex The low endpoint (inclusive) of the sub-list.
	 * @param toIndex The high endpoint (exclusive) of the sub-list.
	 * @return A view of the specified range within this model.
	 * @throws IndexOutOfBoundsException for an illegal endpoint index value (<var>fromIndex</var> &lt; 0 || <var>toIndex</var> &gt; <code>size()</code> ||
	 *           <var>fromIndex</var> &gt; <var>toIndex</var>).
	 */
	public List<V> subList(final int fromIndex, final int toIndex) {
		return getListSelectModel().subList(fromIndex, toIndex);
	}

	/**
	 * An encapsulation of a component for a tree node along with other metadata, such as whether the component was editable when created.
	 * @author Garret Wilson
	 */
	protected static class ValueComponentState extends AbstractCompositeStateComponent.ComponentState {

		/**
		 * Constructor
		 * @param component The component for a tree node.
		 * @throws NullPointerException if the given component is <code>null</code>.
		 */
		public ValueComponentState(final Component component) {
			super(component); //construct the parent class
		}
	}

	/**
	 * A list select value representation strategy that creates a component by converting the value to a info model. The specified component class must have a
	 * constructor that takes a single {@link InfoModel} as an argument.
	 * @param <VV> The type of value the strategy is to represent.
	 * @author Garret Wilson
	 */
	public static class ConverterInfoModelValueRepresentationStrategy<VV> implements ValueRepresentationStrategy<VV> {

		/** The converter to use for displaying the value as a string. */
		private final Converter<VV, String> converter;

		/** @return The converter to use for displaying the value as a string. */
		public Converter<VV, String> getConverter() {
			return converter;
		}

		/** The component constructor that takes a single {@link InfoModel} argument. */
		private final Constructor<? extends Component> componentInfoModelConstructor;

		/**
		 * Value class constructor with a default converter. This implementation uses a {@link DefaultStringLiteralConverter}.
		 * @param valueClass The class indicating the type of value to convert.
		 * @param componentClass The class of component to create.
		 * @throws NullPointerException if the given value class and/or component class is <code>null</code>.
		 * @throws IllegalArgumentException if the given component class does not have a constructor with a single {@link InfoModel} constructor.
		 */
		public ConverterInfoModelValueRepresentationStrategy(final Class<VV> valueClass, final Class<? extends Component> componentClass) {
			this(componentClass, AbstractStringLiteralConverter.getInstance(valueClass)); //construct the class with the appropriate string literal converter
		}

		/**
		 * Converter constructor.
		 * @param converter The converter to use for displaying the value as a string.
		 * @param componentClass The class of component to create.
		 * @throws NullPointerException if the given converter is <code>null</code>.
		 * @throws IllegalArgumentException if the given component class does not have a constructor with a single {@link InfoModel} constructor.
		 */
		public ConverterInfoModelValueRepresentationStrategy(final Class<? extends Component> componentClass, final Converter<VV, String> converter) {
			this.converter = checkInstance(converter, "Converter cannot be null."); //save the converter
			componentInfoModelConstructor = getCompatiblePublicConstructor(checkInstance(componentClass, "Component class cannot be null."), InfoModel.class); //get the constructor for the component
			if(componentInfoModelConstructor == null) { //if there is no appropriate constructor
				throw new IllegalArgumentException("Component class " + componentClass + " has no constructor with a single info model parameter.");
			}
		}

		/**
		 * Creates a component for the given list value. This implementation constructs a component from a info model that converts the value using the saved
		 * converter.
		 * @param model The model containing the value.
		 * @param value The value for which a component should be created.
		 * @param index The index of the value within the list, or -1 if the value is not in the list (e.g. for representing no selection).
		 * @param selected <code>true</code> if the value is selected.
		 * @param focused <code>true</code> if the value has the focus.
		 * @return A new component to represent the given value.
		 * @see #getConverter()
		 */
		public Component createComponent(final ListSelectModel<VV> model, final VV value, final int index, final boolean selected, final boolean focused) {
			try {
				return componentInfoModelConstructor.newInstance(new ValueConverterInfoModel<VV>(value, getConverter())); //create a component that will convert the value to a string in a info model
			} catch(final InstantiationException instantiationException) {
				throw new AssertionError(instantiationException);
			} catch(final IllegalAccessException illegalAccessException) {
				throw new AssertionError(illegalAccessException);
			} catch(final InvocationTargetException invocationTargetException) {
				throw new AssertionError(invocationTargetException);
			}
		}
	}

	/**
	 * A default list select value representation strategy that creates a {@link Label}. A label component will be generated containing the default string
	 * representation of a value.
	 * @param <VV> The type of value the strategy is to represent.
	 * @see Label
	 * @author Garret Wilson
	 */
	public static class DefaultValueRepresentationStrategy<VV> extends ConverterInfoModelValueRepresentationStrategy<VV> {

		/**
		 * Value class constructor with a default converter. This implementation uses a {@link DefaultStringLiteralConverter}.
		 * @param valueClass The class indicating the type of value to convert.
		 * @throws NullPointerException if the given value class is <code>null</code>.
		 */
		public DefaultValueRepresentationStrategy(final Class<VV> valueClass) {
			this(AbstractStringLiteralConverter.getInstance(valueClass)); //construct the class with the appropriate string literal converter
		}

		/**
		 * Converter constructor.
		 * @param converter The converter to use for displaying the value as a string.
		 * @throws NullPointerException if the given converter is <code>null</code>.
		 */
		public DefaultValueRepresentationStrategy(final Converter<VV, String> converter) {
			super(Label.class, converter); //use a label to represent values
		}

		/**
		 * Creates a component for the given list value. This version uses covariant types to specify that a {@link Label} is returned.
		 * @param model The model containing the value.
		 * @param value The value for which a component should be created.
		 * @param index The index of the value within the list, or -1 if the value is not in the list (e.g. for representing no selection).
		 * @param selected <code>true</code> if the value is selected.
		 * @param focused <code>true</code> if the value has the focus.
		 * @return A new component to represent the given value.
		 * @see #getConverter()
		 */
		public Label createComponent(final ListSelectModel<VV> model, final VV value, final int index, final boolean selected, final boolean focused) {
			return (Label)super.createComponent(model, value, index, selected, focused); //construct the component normally and cast it to a Label
		}

	}

}
