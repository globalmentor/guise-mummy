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

package io.guise.framework.component;

import java.beans.PropertyVetoException;
import java.lang.reflect.*;
import java.util.*;

import static java.util.Objects.*;

import static com.globalmentor.java.Classes.*;

import com.globalmentor.event.EventListenerManager;

import io.guise.framework.converter.*;
import io.guise.framework.event.*;
import io.guise.framework.model.*;
import io.guise.framework.validator.*;

/**
 * Abstract implementation of a control to allow selection by the user of a value from a list. The component valid status is updated before a change in the
 * {@link #VALUE_PROPERTY} or the {@link #VALIDATOR_PROPERTY} is fired. This implementation does not yet fully support elements that appear more than once in
 * the model.
 * @param <V> The type of values to select.
 * @author Garret Wilson
 */
public abstract class AbstractListSelectControl<V> extends AbstractCompositeStateControl<V, AbstractListSelectControl.ValueComponentState>
		implements ListSelectControl<V> {

	/** The list select model used by this component. */
	private final ListSelectModel<V> listSelectModel;

	/** @return The list select model used by this component. */
	protected ListSelectModel<V> getListSelectModel() {
		return listSelectModel;
	}

	/** The strategy used to generate a component to represent each value in the model. */
	private ValueRepresentationStrategy<V> valueRepresentationStrategy;

	@Override
	public ValueRepresentationStrategy<V> getValueRepresentationStrategy() {
		return valueRepresentationStrategy;
	}

	@Override
	public void setValueRepresentationStrategy(final ValueRepresentationStrategy<V> newValueRepresentationStrategy) {
		if(valueRepresentationStrategy != newValueRepresentationStrategy) { //if the value is really changing
			final ValueRepresentationStrategy<V> oldValueRepresentationStrategy = valueRepresentationStrategy; //get the old value
			valueRepresentationStrategy = requireNonNull(newValueRepresentationStrategy, "Value representation strategy cannot be null."); //actually change the value
			firePropertyChange(VALUE_REPRESENTATION_STRATEGY_PROPERTY, oldValueRepresentationStrategy, newValueRepresentationStrategy); //indicate that the value changed
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version is provided to allow public access.
	 * </p>
	 */
	@Override
	public Component getComponent(final V value) {
		return super.getComponent(value); //delegate to the parent version
	}

	@Override
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
		this.valueRepresentationStrategy = requireNonNull(valueRepresentationStrategy, "Value representation strategy cannot be null.");
		this.listSelectModel = requireNonNull(listSelectModel, "List select model cannot be null."); //save the list select model
		this.listSelectModel.addPropertyChangeListener(getRepeatPropertyChangeListener()); //listen and repeat all property changes of the list select model
		this.listSelectModel.addVetoableChangeListener(getRepeatVetoableChangeListener()); //listen and repeat all vetoable changes of the list select model
		this.listSelectModel.addListListener(new ListListener<V>() { //install a repeater list listener to listen to the decorated model

			@Override
			public void listModified(final ListEvent<V> listEvent) { //if the list is modified
				fireListModified(listEvent.getIndex(), listEvent.getAddedElement(), listEvent.getRemovedElement()); //repeat the event, indicating the component as the source of the event
			}

		});
		this.listSelectModel.addListSelectionListener(new ListSelectionListener<V>() { //install a repeater list selection listener to listen to the decorated model

			@Override
			public void listSelectionChanged(final ListSelectionEvent<V> selectionEvent) { //if the list selection changes
				fireSelectionChanged(selectionEvent.getAddedElement(), selectionEvent.getRemovedElement()); //repeat the event, indicating the component as the source of the event
			}

		});
		addListListener(new ListListener<V>() { //listen for list changes

			@Override
			public void listModified(final ListEvent<V> listEvent) { //if list is modified
				clearComponentStates(); //clear all the components and component states TODO probably do this on a component-by-component basis
			};

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
	 * This version performs no additional checks if the control is disabled.
	 * </p>
	 */
	@Override
	protected boolean determineValid() {
		if(!super.determineValid()) { //if we don't pass the default validity checks
			return false; //the component isn't valid
		}
		return !isEnabled() || getListSelectModel().isValidValue(); //the component is valid if the list select model has a valid value (don't check the list select model if the control is not enabled)
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version validates the associated list select model. This version performs no additional checks if the control is disabled.
	 * </p>
	 */
	@Override
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
	 * {@inheritDoc}
	 * <p>
	 * This version resets the control value.
	 * </p>
	 * @see #resetValue()
	 */
	@Override
	public void reset() {
		super.reset(); //reset normally
		resetValue(); //reset the control value
	}

	//ValueModel delegations

	@Override
	public V getDefaultValue() {
		return getListSelectModel().getDefaultValue();
	}

	@Override
	public V getValue() {
		return getListSelectModel().getValue();
	}

	@Override
	public void setValue(final V newValue) throws PropertyVetoException {
		getListSelectModel().setValue(newValue);
	}

	@Override
	public void clearValue() {
		getListSelectModel().clearValue();
	}

	@Override
	public void resetValue() {
		getListSelectModel().resetValue();
	}

	@Override
	public Validator<V> getValidator() {
		return getListSelectModel().getValidator();
	}

	@Override
	public void setValidator(final Validator<V> newValidator) {
		getListSelectModel().setValidator(newValidator);
	}

	@Override
	public boolean isValidValue() {
		return getListSelectModel().isValidValue();
	}

	@Override
	public void validateValue() throws ValidationException {
		getListSelectModel().validateValue();
	}

	@Override
	public Class<V> getValueClass() {
		return getListSelectModel().getValueClass();
	}

	//SelectModel delegations

	@Override
	public boolean replace(final V oldValue, final V newValue) {
		return getListSelectModel().replace(oldValue, newValue);
	}

	@Override
	public V getSelectedValue() {
		return getListSelectModel().getSelectedValue();
	}

	@Override
	public V[] getSelectedValues() {
		return getListSelectModel().getSelectedValues();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setSelectedValues(final V... values) throws PropertyVetoException {
		getListSelectModel().setSelectedValues(values);
	}

	//ListSelectModel delegations

	@Override
	public ListSelectionPolicy<V> getSelectionPolicy() {
		return getListSelectModel().getSelectionPolicy();
	}

	@Override
	public int getSelectedIndex() {
		return getListSelectModel().getSelectedIndex();
	}

	@Override
	public int[] getSelectedIndexes() {
		return getListSelectModel().getSelectedIndexes();
	}

	@Override
	public void setSelectedIndexes(int... indexes) throws PropertyVetoException {
		getListSelectModel().setSelectedIndexes(indexes);
	}

	@Override
	public void addSelectedIndexes(int... indexes) throws PropertyVetoException {
		getListSelectModel().addSelectedIndexes(indexes);
	}

	@Override
	public void removeSelectedIndexes(int... indexes) throws PropertyVetoException {
		getListSelectModel().removeSelectedIndexes(indexes);
	}

	@Override
	public boolean isValueDisplayed(final V value) {
		return getListSelectModel().isValueDisplayed(value);
	}

	@Override
	public void setValueDisplayed(final V value, final boolean newDisplayed) {
		getListSelectModel().setValueDisplayed(value, newDisplayed);
	} //TODO fix property change event

	@Override
	public boolean isIndexDisplayed(final int index) {
		return getListSelectModel().isIndexDisplayed(index);
	}

	@Override
	public void setIndexDisplayed(final int index, final boolean newDisplayed) {
		getListSelectModel().setIndexDisplayed(index, newDisplayed);
	} //TODO fix property change event

	@Override
	public boolean isValueEnabled(final V value) {
		return getListSelectModel().isValueEnabled(value);
	}

	@Override
	public void setValueEnabled(final V value, final boolean newEnabled) {
		getListSelectModel().setValueEnabled(value, newEnabled);
	} //TODO fix property change event

	@Override
	public boolean isIndexEnabled(final int index) {
		return getListSelectModel().isIndexEnabled(index);
	}

	@Override
	public void setIndexEnabled(final int index, final boolean newEnabled) {
		getListSelectModel().setIndexEnabled(index, newEnabled);
	} //TODO fix property change event

	@Override
	public void addListListener(final ListListener<V> listListener) {
		getEventListenerManager().add(ListListener.class, listListener); //add the listener
	}

	@Override
	public void removeListListener(final ListListener<V> listListener) {
		getEventListenerManager().remove(ListListener.class, listListener); //remove the listener
	}

	@Override
	public void addListSelectionListener(final ListSelectionListener<V> selectionListener) {
		getEventListenerManager().add(ListSelectionListener.class, selectionListener); //add the listener
	}

	@Override
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
	@SuppressWarnings("unchecked")
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
	@SuppressWarnings("unchecked")
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

	@Override
	public int size() {
		return getListSelectModel().size();
	}

	@Override
	public boolean isEmpty() {
		return getListSelectModel().isEmpty();
	}

	@Override
	public boolean contains(final Object value) {
		return getListSelectModel().contains(value);
	}

	@Override
	public Iterator<V> iterator() {
		return getListSelectModel().iterator();
	}

	@Override
	public Object[] toArray() {
		return getListSelectModel().toArray();
	}

	@Override
	public <T> T[] toArray(final T[] array) {
		return getListSelectModel().toArray(array);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version delegates to {@link #add(int, Object)}.
	 * </p>
	 */
	@Override
	public boolean add(final V value) {
		return getListSelectModel().add(value);
	}

	@Override
	public boolean remove(final Object value) {
		return getListSelectModel().remove(value);
	}

	@Override
	public boolean containsAll(final Collection<?> collection) {
		return getListSelectModel().containsAll(collection);
	}

	@Override
	public boolean addAll(final Collection<? extends V> collection) {
		return getListSelectModel().addAll(collection);
	}

	@Override
	public synchronized boolean addAll(final int index, final Collection<? extends V> collection) {
		return getListSelectModel().addAll(index, collection);
	}

	@Override
	public boolean removeAll(final Collection<?> collection) {
		return getListSelectModel().removeAll(collection);
	}

	@Override
	public boolean retainAll(final Collection<?> collection) {
		return getListSelectModel().retainAll(collection);
	}

	@Override
	public void clear() {
		getListSelectModel().clear();
	}

	@Override
	public V get(final int index) {
		return getListSelectModel().get(index);
	}

	@Override
	public V set(final int index, final V value) {
		return getListSelectModel().set(index, value);
	}

	@Override
	public void add(final int index, final V value) {
		getListSelectModel().add(index, value);
	}

	@Override
	public V remove(final int index) {
		return getListSelectModel().remove(index);
	}

	@Override
	public int indexOf(final Object value) {
		return getListSelectModel().indexOf(value);
	}

	@Override
	public int lastIndexOf(final Object value) {
		return getListSelectModel().lastIndexOf(value);
	}

	@Override
	public ListIterator<V> listIterator() {
		return getListSelectModel().listIterator();
	}

	@Override
	public ListIterator<V> listIterator(final int index) {
		return getListSelectModel().listIterator(index);
	}

	@Override
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
			this.converter = requireNonNull(converter, "Converter cannot be null."); //save the converter
			componentInfoModelConstructor = getCompatiblePublicConstructor(requireNonNull(componentClass, "Component class cannot be null."), InfoModel.class); //get the constructor for the component
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
		 * {@inheritDoc}
		 * <p>
		 * This version uses covariant types to specify that a {@link Label} is returned.
		 * </p>
		 */
		@Override
		public Label createComponent(final ListSelectModel<VV> model, final VV value, final int index, final boolean selected, final boolean focused) {
			return (Label)super.createComponent(model, value, index, selected, focused); //construct the component normally and cast it to a Label
		}

	}

}
