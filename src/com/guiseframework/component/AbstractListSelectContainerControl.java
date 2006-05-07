package com.guiseframework.component;

import static com.garretwilson.lang.ObjectUtilities.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.*;

import com.guiseframework.component.layout.*;
import com.guiseframework.event.*;
import com.guiseframework.model.*;
import com.guiseframework.validator.*;

/**An abstract list select control that is also a container.
The component valid status is updated before a change in the {@link #VALUE_PROPERTY} or the {@link #VALIDATOR_PROPERTY} is fired.
<p>This implementation installs a default value representation strategy that simply passes through the represented component.</p> 
@author Garret Wilson
*/
public abstract class AbstractListSelectContainerControl<C extends ContainerControl<C> & ListSelectControl<Component<?>, C>> extends AbstractContainerControl<C> implements ListSelectControl<Component<?>, C>
{

	/**The static representation strategy to represent component values as themselves.*/
	public final static ComponentRepresentationStrategy COMPONENT_REPRESENTATION_STRATEGY=new ComponentRepresentationStrategy();
	

		//TODO make sure we listen for enabled status changing on the layout and send an index enabled property change, maybe

	/**The list select model used by this component.*/
//TODO del	private final ListSelectModel<Component<?>> listSelectModel;

		/**@return The list select model used by this component.*/
//TODO del		protected ListSelectModel<Component<?>> getListSelectModel() {return listSelectModel;}
	
	/**@return The layout definition for the container.*/
	public AbstractValueLayout<? extends ControlConstraints> getLayout() {return (AbstractValueLayout<? extends ControlConstraints>)super.getLayout();}

	/**Whether the value is editable and the control will allow the the user to change the value.*/
	private boolean editable=true;

		/**@return Whether the value is editable and the control will allow the the user to change the value.*/
		public boolean isEditable() {return editable;}

		/**Sets whether the value is editable and the control will allow the the user to change the value.
		This is a bound property of type <code>Boolean</code>.
		@param newEditable <code>true</code> if the control should allow the user to change the value.
		@see #EDITABLE_PROPERTY
		*/
		public void setEditable(final boolean newEditable)
		{
			if(editable!=newEditable)	//if the value is really changing
			{
				final boolean oldEditable=editable;	//get the old value
				editable=newEditable;	//actually change the value
				firePropertyChange(EDITABLE_PROPERTY, Boolean.valueOf(oldEditable), Boolean.valueOf(newEditable));	//indicate that the value changed
			}			
		}

	/**The strategy used to generate a component to represent each value in the model.*/
	private ValueRepresentationStrategy<Component<?>> valueRepresentationStrategy;

		/**@return The strategy used to generate a component to represent each value in the model.*/
		public ValueRepresentationStrategy<Component<?>> getValueRepresentationStrategy() {return valueRepresentationStrategy;}

		/**Sets the strategy used to generate a component to represent each value in the model.
		This is a bound property
		@param newValueRepresentationStrategy The new strategy to create components to represent this model's values.
		@exception NullPointerException if the provided value representation strategy is <code>null</code>.
		@see SelectControl#VALUE_REPRESENTATION_STRATEGY_PROPERTY
		*/
		public void setValueRepresentationStrategy(final ValueRepresentationStrategy<Component<?>> newValueRepresentationStrategy)
		{
			if(valueRepresentationStrategy!=newValueRepresentationStrategy)	//if the value is really changing
			{
				final ValueRepresentationStrategy<Component<?>> oldValueRepresentationStrategy=valueRepresentationStrategy;	//get the old value
				valueRepresentationStrategy=checkInstance(newValueRepresentationStrategy, "Value representation strategy cannot be null.");	//actually change the value
				firePropertyChange(VALUE_REPRESENTATION_STRATEGY_PROPERTY, oldValueRepresentationStrategy, newValueRepresentationStrategy);	//indicate that the value changed
			}
		}

	/**Retrieves the component for the given object.
	This version returns the given component to fulfill the interface contract of {@link ListSelectControl}.
	@param object The object for which a representation component should be returned.
	@return The child component representing the given object, or <code>null</code> if there is no component representing the given object.
	*/
	public Component<?> getComponent(final Component<?> object)
	{
		return object;	//return the given component
	}

	/**Layout constructor.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given layout is <code>null</code>.
	*/
	protected AbstractListSelectContainerControl(final AbstractValueLayout layout)
	{
		super(layout);	//construct the parent class
		this.valueRepresentationStrategy=COMPONENT_REPRESENTATION_STRATEGY;	//use the shared component representation strategy
		layout.addPropertyChangeListener(getRepeatPropertyChangeListener());	//listen an repeat all property changes of the card layout value model TODO make sure the card constraint values are passed on, too---right now they probably aren't as the property change event subclass isn't recognized in the repeater listener class
		layout.addVetoableChangeListener(getRepeatVetoableChangeListener());	//listen and repeat all vetoable changes of the card layout value model
		addPropertyChangeListener(VALUE_PROPERTY, new PropertyChangeListener()	//listen for the value changing
				{
					public void propertyChange(final PropertyChangeEvent propertyChangeEvent)	//if the value changes
					{
						fireSelectionChanged(null, null);	//indicate that the selection changed
					}			
				});
/*TODO del; this isn't needed, and doesn't do what we want
		layout.addPropertyChangeListener(CardLayout.Constraints.ENABLED_PROPERTY, new PropertyChangeListener()	//listen for the constraints enabled value 
				{
					public void propertyChange(final PropertyChangeEvent propertyChangeEvent)	//if the value changes
					{
						if(propertyChangeEvent instanceof LayoutConstraintsPropertyChangeEvent)
						{
							final LayoutConstraintsPropertyChangeEvent<CardLayout.Constraints, Boolean> layoutConstraintsPropertyChangeEvent=(LayoutConstraintsPropertyChangeEvent<CardLayout.Constraints, Boolean>)propertyChangeEvent;	//get the layout constraints change
							setValueEnabled(layoutConstraintsPropertyChangeEvent.getComponent(), layoutConstraintsPropertyChangeEvent.getNewValue());	//update the enabled status of the index
						}
					}			
				});
*/
//TODO del if not needed		this.selectModel=checkNull(layout.getModel(), "Select model cannot be null.");	//save the card layout's value model
/*TODO del if not needed
		this.listSelectModel=checkNull(layout.getListSelectModel(), "List select model cannot be null.");	//save the list select model
		this.listSelectModel.addPropertyChangeListener(getRepeaterPropertyChangeListener());	//listen and repeat all property changes of the value model
		this.listSelectModel.addListListener(new ListListener<Component<?>>()	//install a repeater list listener to listen to the decorated model
				{
					public void listModified(final ListEvent<Component<?>> listEvent)	//if the list is modified
					{
						fireListModified(listEvent.getIndex(), listEvent.getAddedElement(), listEvent.getRemovedElement());	//repeat the event, indicating the component as the source of the event
					}
				});
		this.listSelectModel.addListSelectionListener(new ListSelectionListener<Component<?>>()	//install a repeater list selection listener to listen to the decorated model
				{
					public void listSelectionChanged(final ListSelectionEvent<Component<?>> selectionEvent)	//if the list selection changes
					{
						fireSelectionChanged(selectionEvent.getAddedElement(), selectionEvent.getRemovedElement());	//repeat the event, indicating the component as the source of the event
					}		
				});
*/
	}

	/**Reports that a bound property has changed.
	This version first updates the valid status if the value is reported as being changed.
	@param propertyName The name of the property being changed.
	@param oldValue The old property value.
	@param newValue The new property value.
	*/
	protected <VV> void firePropertyChange(final String propertyName, final VV oldValue, final VV newValue)
	{
		if(VALUE_PROPERTY.equals(propertyName) || VALIDATOR_PROPERTY.equals(propertyName))	//if the value property or the validator property is being reported as changed
		{
			updateValid();	//update the valid status based upon the new property, so that any listeners will know whether the new property is valid
		}
		super.firePropertyChange(propertyName, oldValue, newValue);	//fire the property change event normally
	}

	/**Checks the state of child components for validity.
	This version only checks the validity of the selected card.
	@return <code>true</code> if the relevant children pass all validity tests.
	*/ 
	protected boolean determineChildrenValid()
	{
		final Component selectedComponent=getValue();	//get the selected card
		return selectedComponent==null || selectedComponent.isValid();	//the children will only be invalid if the selected card is invalid
	}

	/**Validates the user input of child components.
	This version only validates the selected card.
	@return <code>true</code> if all child validations return <code>true</code>.
	*/
	public boolean validateChildren()
	{
		final Component selectedComponent=getValue();	//get the selected card
		return selectedComponent!=null ? selectedComponent.validate() : false;	//only validate the selected card if there is one
	}
	
		//ValueModel delegations

	/**@return The default value.*/
	public Component<?> getDefaultValue() {return getLayout().getDefaultValue();}

	/**@return The input value, or <code>null</code> if there is no input value.*/
	public Component<?> getValue() {return getLayout().getValue();}

	/**Sets the value.
	This is a bound property that only fires a change event when the new value is different via the <code>equals()</code> method.
	If a validator is installed, the value will first be validated before the current value is changed.
	Validation always occurs if a validator is installed, even if the value is not changing.
	If the value change is vetoed by the installed validator, the validation exception will be accessible via {@link PropertyVetoException#getCause()}.
	@param newValue The new value.
	@exception PropertyVetoException if the provided value is not valid or the change has otherwise been vetoed.
	@see #getValidator()
	@see #VALUE_PROPERTY
	*/
	public void setValue(final Component<?> newValue) throws PropertyVetoException {getLayout().setValue(newValue);}

	/**Clears the value by setting the value to <code>null</code>, which may be invalid according to any installed validators.
	No validation occurs.
	@see ValueModel#VALUE_PROPERTY
	*/
	public void clearValue() {getLayout().clearValue();}

	/**Resets the value to a default value, which may be invalid according to any installed validators.
	No validation occurs.
	@see #VALUE_PROPERTY
	*/
	public void resetValue() {getLayout().resetValue();}

	/**@return The validator for this model, or <code>null</code> if no validator is installed.*/
	public Validator<Component<?>> getValidator() {return getLayout().getValidator();}

	/**Sets the validator.
	This is a bound property
	@param newValidator The validator for this model, or <code>null</code> if no validator should be used.
	@see #VALIDATOR_PROPERTY
	*/
	public void setValidator(final Validator<Component<?>> newValidator) {getLayout().setValidator(newValidator);}

	/**Determines whether the value of this model is valid.
	@return Whether the value of this model is valid.
	*/
	public boolean isValidValue() {return getLayout().isValidValue();}

	/**Validates the value of this model, throwing an exception if the model is not valid.
	@exception ValidationException if the value of this model is not valid.	
	*/
	public void validateValue() throws ValidationException {getLayout().validateValue();}

	/**@return The class representing the type of value this model can hold.*/
	public Class<Component<?>> getValueClass() {return getLayout().getValueClass();}

		//SelectModel delegations
	
	/**Replaces the first occurrence in the of the given value with its replacement.
	This method ensures that another thread does not change the model while the search and replace operation occurs.
	@param oldValue The value for which to search.
	@param newValue The replacement value.
	@return Whether the operation resulted in a modification of the model.
	*/
	public boolean replace(final Component<?> oldValue, final Component<?> newValue) {throw new UnsupportedOperationException("replace() not yet supported");}

	/**Determines the selected value.
	If more than one value is selected, the lead selected value will be returned.
	@return The value currently selected, or <code>null</code> if no value is currently selected.
	*/
	public Component<?> getSelectedValue() {return getValue();}

	/**Determines the selected values.
	@return The values currently selected.
	*/
	public Component<?>[] getSelectedValues()
	{
		final Component<?> selectedValue=getValue();	//get the selected value, if any
		return selectedValue!=null ? new Component<?>[]{selectedValue} : new Component<?>[]{};	//return an array with the component, if there is one selected
	}

	/**Sets the selected values.
	If a value occurs more than one time in the model, the first occurrence of the value will be selected.
	Values that do not occur in the select model will be ignored.
	If the value change is vetoed by the installed validator, the validation exception will be accessible via {@link PropertyVetoException#getCause()}.
	This method delegates to the selection strategy.
	@param values The values to select.
	@exception PropertyVetoException if the provided value is not valid or the change has otherwise been vetoed.
	*/
	public void setSelectedValues(final Component<?>... values) throws PropertyVetoException
	{
		setValue(values.length>0 ? values[0] : null);	//select the first or no value
	}
	
	/**The shared single component selection policy.*/
	private final static ListSelectionPolicy<Component<?>> SINGLE_COMPONENT_SELECTION_POLICY=new SingleListSelectionPolicy<Component<?>>();
	
		//ListSelectModel delegations

	/**@return The selection policy for this model.*/
	public ListSelectionPolicy<Component<?>> getSelectionPolicy() {return SINGLE_COMPONENT_SELECTION_POLICY;}

	/**Determines the selected index.
	If more than one index is selected, the lead selected index will be returned.
	@return The index currently selected, or -1 if no index is selected.
	@see #getSelectedValue()
	*/
	public int getSelectedIndex() {return getLayout().getSelectedIndex();}
	
	/**Determines the selected indices.
	@return The indices currently selected.
	@see #getSelectedValues()
	*/
	public int[] getSelectedIndexes()
	{
		final int selectedIndex=getLayout().getSelectedIndex();	//get the selected index
		return selectedIndex>=0 ? new int[]{selectedIndex} : new int[]{};	//return the selected index in an array, if there is a selected index
	}
	
	/**Sets the selected indices.
	Invalid and duplicate indices will be ignored.
	If the value change is vetoed by the installed validator, the validation exception will be accessible via {@link PropertyVetoException#getCause()}.
	@param indexes The indices to select.
	@exception PropertyVetoException if the provided value is not valid or the change has otherwise been vetoed.
	@see ListSelectionPolicy#getSetSelectedIndices(ListSelectModel, int[])
	@see #setSelectedValues(V[])
	@see #addSelectedIndexes(int...)
	*/
	public void setSelectedIndexes(final int... indexes) throws PropertyVetoException
	{
		getLayout().setSelectedIndex(indexes.length>0 ? indexes[0] : -1);	//select the first index if there are indexes to select
	}
	
	/**Adds a selection at the given indices.
	Any invalid indices will be ignored.
	If the value change is vetoed by the installed validator, the validation exception will be accessible via {@link PropertyVetoException#getCause()}.
	@param indexes The indices to add to the selection.
	@exception PropertyVetoException if the provided value is not valid or the change has otherwise been vetoed.
	@see ListSelectionPolicy#getAddSelectedIndices(ListSelectModel, int[])
	@see #setSelectedIndexes(int[])
	*/
	public void addSelectedIndexes(int... indexes) throws PropertyVetoException
	{
		if(getSelectedIndex()<0)	//only if there are no selected indexes
		{
			setSelectedIndexes(indexes);	//set the new index
		}
	}
	
	/**Removes a selection at the given indices.
	Any invalid indices will be ignored.
	If the value change is vetoed by the installed validator, the validation exception will be accessible via {@link PropertyVetoException#getCause()}.
	@param indexes The indices to remove from the selection.
	@exception PropertyVetoException if the provided value is not valid or the change has otherwise been vetoed.
	@see ListSelectionPolicy#getRemoveSelectedIndices(ListSelectModel, int[])
	@see #setSelectedIndexes(int[])
	*/
	public void removeSelectedIndexes(int... indexes) throws PropertyVetoException
	{
		final int selectedIndex=getLayout().getSelectedIndex();	//get the selected index
		for(int i=indexes.length-1; i>=0; --i)	//for each index index
		{
			final int index=indexes[i];	//get this index
			if(index==selectedIndex)	//if this index is selected
			{
				getLayout().setSelectedIndex(-1);	//clear the selected index
				break;	//there can only have been one selected index
			}
		}
	}

	/**Determines the displayed status of the first occurrence of a given value.
	@param value The value for which the displayed status is to be determined.
	@return <code>true</code> if the value is displayed, else <code>false</code>.
	@exception IndexOutOfBoundsException if the given value does not occur in the model.
	*/
	public boolean isValueDisplayed(final Component<?> value) {return getLayout().getConstraints(value).isDisplayed();}

	/**Sets the displayed status of the first occurrence of a given value.
	This is a bound value state property.
	@param value The value to enable or disable.
	@param newDisplayed Whether the value should be displayed.
	@see #DISPLAYED_PROPERTY
	*/
	public void setValueDisplayed(final Component<?> value, final boolean newDisplayed) {getLayout().getConstraints(value).setDisplayed(newDisplayed);}	//TODO fix property change event

	/**Determines the displayed status of a given index.
	@param index The index of the value for which the displayed status is to be determined.
	@return <code>true</code> if the value at the given index is displayed, else <code>false</code>.
	*/
	public boolean isIndexDisplayed(final int index) {return isValueDisplayed(get(index));}
	
	/**Sets the displayed status of a given index.
	This is a bound value state property.
	@param index The index of the value to enable or disable.
	@param newDisplayed Whether the value at the given index should be displayed.
	@see #DISPLAYED_PROPERTY
	@exception IndexOutOfBoundsException if the given index is not within the range of the list.
	*/
	public void setIndexDisplayed(final int index, final boolean newDisplayed) {setValueDisplayed(get(index), newDisplayed);}	//TODO fix property change event

	/**Determines the enabled status of the first occurrence of a given value.
	@param value The value for which the enabled status is to be determined.
	@return <code>true</code> if the value is enabled, else <code>false</code>.
	@exception IndexOutOfBoundsException if the given value does not occur in the model.
	*/
	public boolean isValueEnabled(final Component<?> value) {return getLayout().getConstraints(value).isEnabled();}

	/**Sets the enabled status of the first occurrence of a given value.
	This is a bound value state property.
	@param value The value to enable or disable.
	@param newEnabled Whether the value should be enabled.
	@see #ENABLED_PROPERTY
	*/
	public void setValueEnabled(final Component<?> value, final boolean newEnabled) {getLayout().getConstraints(value).setEnabled(newEnabled);}	//TODO fix property change event

	/**Determines the enabled status of a given index.
	@param index The index of the value for which the enabled status is to be determined.
	@return <code>true</code> if the value at the given index is enabled, else <code>false</code>.
	*/
	public boolean isIndexEnabled(final int index) {return isValueEnabled(get(index));}
	
	/**Sets the enabled status of a given index.
	This is a bound value state property.
	@param index The index of the value to enable or disable.
	@param newEnabled Whether the value at the given index should be enabled.
	@see #ENABLED_PROPERTY
	@exception IndexOutOfBoundsException if the given index is not within the range of the list.
	*/
	public void setIndexEnabled(final int index, final boolean newEnabled) {setValueEnabled(get(index), newEnabled);}	//TODO fix property change event

	/**Adds a list listener.
	@param listListener The list listener to add.
	*/
	public void addListListener(final ListListener<Component<?>> listListener)
	{
		getEventListenerManager().add(ListListener.class, listListener);	//add the listener
	}

	/**Removes a list listener.
	@param listListener The list listener to remove.
	*/
	public void removeListListener(final ListListener<Component<?>> listListener)
	{
		getEventListenerManager().remove(ListListener.class, listListener);	//remove the listener
	}

	/**Adds a list selection listener.
	@param selectionListener The selection listener to add.
	*/
	public void addListSelectionListener(final ListSelectionListener<Component<?>> selectionListener)
	{
		getEventListenerManager().add(ListSelectionListener.class, selectionListener);	//add the listener
	}

	/**Removes a list selection listener.
	@param selectionListener The selection listener to remove.
	*/
	public void removeListSelectionListener(final ListSelectionListener<Component<?>> selectionListener)
	{
		getEventListenerManager().remove(ListSelectionListener.class, selectionListener);	//remove the listener
	}

	/**Fires an event to all registered container listeners indicating the components in the container changed.
	This implementation also fires a list modified event to all registered list listeners, if any.
	@param index The index at which a component was added and/or removed, or -1 if the index is unknown.
	@param addedComponent The component that was added to the container, or <code>null</code> if no component was added or it is unknown whether or which components were added.
	@param removedComponent The component that was removed from the container, or <code>null</code> if no component was removed or it is unknown whether or which components were removed.
	@see ContainerListener
	@see ContainerEvent
	*/
	protected void fireContainerModified(final int index, final Component<?> addedComponent, final Component<?> removedComponent)
	{
		super.fireContainerModified(index, addedComponent, removedComponent);	//fire the container modified event normally
		if(getEventListenerManager().hasListeners(ListListener.class))	//if there are appropriate listeners registered
		{
			final ListEvent<Component<?>> listEvent=new ListEvent<Component<?>>(this, index, addedComponent, removedComponent);	//create a new event
			getSession().queueEvent(new PostponedListEvent<Component<?>>(getEventListenerManager(), listEvent));	//tell the Guise session to queue the event
		}
	}

	/**Fires an event to all registered selection listeners indicating the selection changed.
	@param addedIndex The index that was added to the selection, or <code>null</code> if no index was added or it is unknown whether or which indices were added.
	@param removedIndex The index that was removed from the list, or <code>null</code> if no index was removed or it is unknown whether or which indices were removed.
	@see ListSelectionListener
	@see ListSelectionEvent
	*/
	protected void fireSelectionChanged(final Integer addedIndex, final Integer removedIndex)
	{
		if(getEventListenerManager().hasListeners(ListSelectionListener.class))	//if there are appropriate listeners registered
		{
			final ListSelectionEvent<Component<?>> selectionEvent=new ListSelectionEvent<Component<?>>(this, addedIndex, removedIndex);	//create a new event
			getSession().queueEvent(new PostponedListSelectionEvent<Component<?>>(getEventListenerManager(), selectionEvent));	//tell the Guise session to queue the event
		}
	}

		//List delegations
	
	/**@return An array containing all of the values in this model.*/
	public Object[] toArray() {return getComponentList().toArray();}

	/**Returns an array containing all of the values in this model.
	@param array The array into which the value of this collection are to be stored, if it is big enough; otherwise, a new array of the same runtime type is allocated for this purpose.
	@return An array containing the values of this model.
	@exception ArrayStoreException if the runtime type of the specified array is not a supertype of the runtime type of every value in this model.
	@exception NullPointerException if the specified array is <code>null</code>.
	*/
	public <T> T[] toArray(final T[] array) {return getComponentList().toArray(array);}

	/**Determines if this model contains all of the values of the specified collection.
	@param collection The collection to be checked for containment in this model.
	@return <code>true</code> if this model contains all of the values of the specified collection.
	@exception NullPointerException if the specified collection is <code>null</code>.
	@see #contains(Object)
	*/
	public boolean containsAll(final Collection<?> collection) {return getComponentList().containsAll(collection);}

	/**Appends all of the values in the specified collection to the end of this model, in the order that they are returned by the specified collection's iterator.
	@param collection The collection the values of which are to be added to this model.
	@return <code>true</code> if this model changed as a result of the call.
	@exception NullPointerException if the specified collection is <code>null</code>.
	@see #add(Object)
	*/
	public boolean addAll(final Collection<? extends Component<?>> collection) {throw new UnsupportedOperationException("addAll(Collection) not yet supported");}	//TODO add all these to container

	/**Inserts all of the values in the specified collection into this model at the specified position.
	@param index The index at which to insert first value from the specified collection.
	@param collection The values to be inserted into this model.
	@return <code>true</code> if this model changed as a result of the call.
	@exception NullPointerException if the specified collection is <code>null</code>.
	@exception IndexOutOfBoundsException if the index is out of range (<var>index</var> &lt; 0 || <var>index</var> &gt; <code>size()</code>).
	*/
	public synchronized boolean addAll(final int index, final Collection<? extends Component<?>> collection) {throw new UnsupportedOperationException("addAll(index, Collection) not yet supported");}

	/**Removes from this model all the values that are contained in the specified collection.
	@param collection The collection that defines which values will be removed from this model.
	@return <code>true</code> if this model changed as a result of the call.
	@exception NullPointerException if the specified collection is <code>null</code>.
	@see #remove(Object)
	@see #contains(Object)
	*/
	public boolean removeAll(final Collection<?> collection) {throw new UnsupportedOperationException("removeAll(Collection) not yet supported");}

	/**Retains only the values in this model that are contained in the specified collection.
	@param collection The collection that defines which values this model will retain.
	@return <code>true</code> if this model changed as a result of the call.
	@exception NullPointerException if the specified collection is <code>null</code>.
	@see #remove(Object)
	@see #contains(Object)
	*/
	public boolean retainAll(final Collection<?> collection) {throw new UnsupportedOperationException("retainAll(Collection) not yet supported");}

	/**Replaces the value at the specified position in this model with the specified value.
	@param index The index of the value to replace.
	@param value The value to be stored at the specified position.
	@return The value at the specified position.
	@exception IndexOutOfBoundsException if the index is out of range (<var>index<var> &lt; 0 || <var>index</var> &gt;= <code>size()</code>).
	*/
	public Component<?> set(final int index, final Component<?> value) {throw new UnsupportedOperationException("set(index, value) not yet supported");}

	/**Inserts the specified value at the specified position in this model.
	@param index The index at which the specified value is to be inserted.
	@param value The value to be inserted.
	@throws IndexOutOfBoundsException if the index is out of range (<var>index</var> &lt; 0 || <var>index</var> &gt; <code>size()</code>).
	*/
	public void add(final int index, final Component<?> value) {throw new UnsupportedOperationException("addAll(index, value) not yet supported");}

	/**@return A read-only list iterator of the values in this model (in proper sequence).*/
	public ListIterator<Component<?>> listIterator() {return getComponentList().listIterator();}

	/**Returns a list iterator of the values in this model (in proper sequence), starting at the specified position in this model.
	@param index The index of first value to be returned from the list iterator (by a call to the <code>next()</code> method).
	@return A list iterator of the values in this model (in proper sequence), starting at the specified position in this model.
	@exception IndexOutOfBoundsException if the index is out of range (<var>index</var> &lt; 0 || <var>index</var> &gt; <code>size()</code>).
	*/
	public ListIterator<Component<?>> listIterator(final int index) {return getComponentList().listIterator(index);}

	/**Returns a read-only view of the portion of this model between the specified <var>fromIndex</var>, inclusive, and <var>toIndex</var>, exclusive.
	@param fromIndex The low endpoint (inclusive) of the sub-list.
	@param toIndex The high endpoint (exclusive) of the sub-list.
	@return A view of the specified range within this model.
	@throws IndexOutOfBoundsException for an illegal endpoint index value (<var>fromIndex</var> &lt; 0 || <var>toIndex</var> &gt; <code>size()</code> || <var>fromIndex</var> &gt; <var>toIndex</var>).
	*/
	public List<Component<?>> subList(final int fromIndex, final int toIndex) {return getComponentList().subList(fromIndex, toIndex);}

	/**Adds a component to the container along with a label.
	This convenience method creates new card layout constraints from the given label model and adds the component.
	@param component The component to add.
	@param labelModel The label associated with an individual component.
	@exception NullPointerException if the given label is <code>null</code>.
	@exception IllegalArgumentException if the component already has a parent.
	*/
/*TODO del if not wanted
	public void add(final Component<?> component, final LabelModel labelModel)
	{
		add(component, new CardLayout.Constraints(labelModel));	//create card layout constraints for the label and add the component to the container
	}
*/

	/**Convenience method to determine whether a card is displayed based upon its associated constraints.
	@return Whether the card is displayed or has no representation, taking up no space.
	@exception IllegalStateException if the given component has no associated constraints.
	@see ControlConstraints#isDisplayed()
	*/
	public boolean isDisplayed(final Component<?> component)
	{
		final ControlConstraints cardConstraints=getLayout().getConstraints(component);	//get constraints of the component
		if(cardConstraints==null)	//if there are no constraints
		{
			throw new IllegalStateException("Component "+component+" has no associated constraints.");
		}
		return cardConstraints.isDisplayed();	//return the displayed status of the constraints
	}

	/**Sets a card displayed or not displayed.
	This convenience method changes the displayed status of the component's associated constraints.
	@param component The component for which the card should be displayed or not displayed.
	@param newDisplayed <code>true</code> if the card should be displayed.
	@exception IllegalStateException if the given component has no associated constraints.
	@see ControlConstraints#setDisplayed(boolean)
	*/
	public void setDisplayed(final Component<?> component, final boolean newDisplayed)
	{
		final ControlConstraints cardConstraints=getLayout().getConstraints(component);	//get constraints of the component
		if(cardConstraints==null)	//if there are no constraints
		{
			throw new IllegalStateException("Component "+component+" has no associated constraints.");
		}
		cardConstraints.setDisplayed(newDisplayed);	//change the displayed status of the constraints
	}

	/**Convenience method to determine whether a card is enabled based upon its associated constraints.
	@return Whether the card is enabled and can receive user input.
	@exception IllegalStateException if the given component has no associated constraints.
	@see CardConstraints#isEnabled()
	*/
	public boolean isEnabled(final Component<?> component)
	{
		final ControlConstraints cardConstraints=getLayout().getConstraints(component);	//get constraints of the component
		if(cardConstraints==null)	//if there are no constraints
		{
			throw new IllegalStateException("Component "+component+" has no associated constraints.");
		}
		return cardConstraints.isEnabled();	//return the enabled status of the constraints
	}

	/**Enables or disables a card.
	This convenience method changes the enabled status of the component's associated constraints.
	@param component The component for which the card should be enabled or disabled.
	@param newEnabled <code>true</code> if the card can be selected.
	@exception IllegalStateException if the given component has no associated constraints.
	@see CardConstraints#setEnabled(boolean)
	*/
	public void setEnabled(final Component<?> component, final boolean newEnabled)
	{
		final ControlConstraints cardConstraints=getLayout().getConstraints(component);	//get constraints of the component
		if(cardConstraints==null)	//if there are no constraints
		{
			throw new IllegalStateException("Component "+component+" has no associated constraints.");
		}
		cardConstraints.setEnabled(newEnabled);	//change the enabled status of the constraints
	}

	/**A strategy for to represent components in a list select model as themselves.
	@author Garret Wilson
	*/
	public static class ComponentRepresentationStrategy implements ValueRepresentationStrategy<Component<?>>
	{
		/**Creates a component for the given list value.
		This implementation returns the component value itself.
		@param model The model containing the value.
		@param value The value for which a component should be created.
		@param index The index of the value within the list, or -1 if the value is not in the list (e.g. for representing no selection).
		@param selected <code>true</code> if the value is selected.
		@param focused <code>true</code> if the value has the focus.
		@return A new component to represent the given value.
		*/
		public Component<?> createComponent(final ListSelectModel<Component<?>> model, final Component<?> value, final int index, final boolean selected, final boolean focused)
		{
			return value;	//return the component to represent itself
		}
	}
	
}
