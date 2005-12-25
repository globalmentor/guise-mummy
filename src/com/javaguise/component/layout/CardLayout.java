package com.javaguise.component.layout;

import static java.text.MessageFormat.*;

import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.MissingResourceException;

import javax.mail.internet.ContentType;

import com.garretwilson.lang.ObjectUtilities;

import static com.garretwilson.lang.ClassUtilities.getPropertyName;
import static com.garretwilson.lang.ObjectUtilities.*;

import static com.javaguise.GuiseResourceConstants.*;

import com.javaguise.GuiseSession;
import com.javaguise.component.Component;
import com.javaguise.component.Container;
import com.javaguise.component.layout.AbstractLayout.ConstraintsPropertyChangeListener;
import com.javaguise.event.AbstractGuisePropertyChangeListener;
import com.javaguise.event.GuisePropertyChangeEvent;
import com.javaguise.event.ListListener;
import com.javaguise.event.ListSelectionListener;
import com.javaguise.model.*;
import com.javaguise.validator.*;

/**A layout that manages child components as an ordered stack of cards.
Only one child comoponent is visible at a time.
The card layout maintains its own value model that maintains the current selected card.
@author Garret Wilson
*/
public class CardLayout extends AbstractLayout<CardLayout.Constraints>
{

	/**The decorated model maintaining the selected component.*/
	private final CardModel model;

		/**@return The decorated model maintaining the selected component.*/
		public ListSelectModel<Component<?>> getModel() {return model;}

	/**The lazily-created listener of constraint property changes.*/
	private CardConstraintsPropertyChangeListener cardConstraintsPropertyChangeListener=null;

		/**@return The lazily-created listener of card constraint property changes.*/
		protected CardConstraintsPropertyChangeListener getConstraintsPropertyChangeListener()
		{
			if(cardConstraintsPropertyChangeListener==null)	//if we haven't yet created a property change listener for constraints
			{
				cardConstraintsPropertyChangeListener=new CardConstraintsPropertyChangeListener();	//create a new constraints property change listener
			}
			return cardConstraintsPropertyChangeListener;	//return the listener of constraints properties
		}

	/**The index of the selected component, or -1 if the index is not known and should be recalculated.*/
	private int selectedIndex=-1;

		/**@return The index of the selected component, or -1 if no component is selected.*/
		public int getSelectedIndex()
		{
			if(selectedIndex<0)	//if there is no valid selected index, make sure the index is up-to-date
			{
				final Component<?> selectedComponent=getValue();	//get the selected component
				if(selectedComponent!=null)	//if a component is selected, we'll need to update the selected index
				{
					selectedIndex=getContainer().indexOf(selectedComponent);	//update the selected index with the index in the container of the selected component
					assert selectedIndex>=0 : "Selected component "+selectedComponent+" is not in the container.";
				}
			}
			return selectedIndex;	//return the selected index, which we've verified is up-to-date
		}

		/**Sets the index of the selected component.
		@param newIndex The index of the selected component, or -1 if no component is selected.
		@exception IllegalStateException if this layout has not yet been installed into a container.
		@exception IndexOutOfBoundsException if the index is out of range.
		@exception ValidationException if the component at the given index is not a valid component to select.
		*/
		public void setSelectedIndex(final int newIndex) throws ValidationException
		{
			final Container<?> container=getContainer();	//get the layout's container
			if(container==null)	//if we haven't been installed into a container
			{
				throw new IllegalStateException("Layout does not have container.");
			}
			final Component<?> component=newIndex>=0 ? container.get(newIndex) : null;	//get the component at the given index, if a valid index was given
			if(newIndex!=getSelectedIndex() && component!=getValue())	//if we're really changing either the selected index or the component
			{
				selectedIndex=-1;	//uncache the selected index (don't actually change it yet---we want to make sure the value model allows the value to be changed)
				setValue(component);		//update the component value, throwing a validation exception if this index can't be selected
			}
		}

	/**Associates layout metadata with a component.
	This version selects a component if none is selected.
	@param component The component for which layout metadata is being specified.
	@param constraints Layout information specifically for the component.
	@return The layout information previously associated with the component, or <code>null</code> if the component did not previously have metadata specified.
	@exception NullPointerException if the given constraints object is <code>null</code>.
	@exception IllegalStateException if this layout has not yet been installed into a container.
	*/
	public Constraints setConstraints(final Component<?> component, final Constraints constraints)
	{
		final Constraints oldConstraints=super.setConstraints(component, constraints);	//set the constraints normally
		if(getValue()==null)	//if there is no component selected
		{
			try
			{
				setSelectedIndex(0);	//select the first component
			}
			catch(final ValidationException validationException)	//if we can't select the first component, don't do anything
			{
			}
		}
		return oldConstraints;	//return the previously associated constraints, if any
	}

	/**Removes any layout metadata associated with a component.
	This implementation updates the selected component if necessary.
	@param component The component for which layout metadata is being removed.
	@return The layout information previously associated with the component, or <code>null</code> if the component did not previously have metadata specified.
	@exception IllegalStateException if this layout has not yet been installed into a container.
	*/
	public Constraints removeConstraints(final Component<?> component)
	{
		final Constraints oldConstraints=super.removeConstraints(component);	//remove the constraints normally
		if(component==getValue())	//if the selected component was removed
		{
			final Container<?> container=getContainer();	//get our container
			final int selectedIndex=container.indexOf(component);	//find the current index of the component that is being removed
			final int containerSize=container.size();	//find out how many components are in the container
			final int newSelectedComponentIndex;	//we'll determine the new selected index (that is, the index of the new selected component in this current state; it won't be the new selected index after removal)
			if(selectedIndex<containerSize-1)	//if this component wasn't the last component
			{
				newSelectedComponentIndex=selectedIndex+1;	//get the subsequent component
			}
			else	//if this was the last component tha twas removed
			{
				newSelectedComponentIndex=containerSize-2;	//get the second-to last component
			}
			try
			{
				setValue(container.get(newSelectedComponentIndex));		//update the component value, throwing a validation exception if this index can't be selected
			}				
			catch(final ValidationException validationException)	//if we can't select the next component
			{
				getModel().resetValue();	//reset the selected component value
			}
		}
		this.selectedIndex=-1;	//always uncache the selected index, because the index of the selected component might have changed
		return oldConstraints;	//return the previous constraints
	}

	/**Creates default constraints for the container.
	@return New default constraints for the container.
	*/
	public Constraints createDefaultConstraints()
	{
		return new Constraints(new DefaultLabelModel(getSession()));	//create constraints with a default label model
	}

	/**Session constructor.
	@param session The Guise session that owns this layout.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public CardLayout(final GuiseSession session)
	{
		super(session);	//construct the parent class
		model=new CardModel(session);	//create a new decorated value model
	}

	/**@return The current value, or <code>null</code> if there is no input value.*/
	public Component<?> getValue() {return getModel().getValue();}

	/**Sets the input value.
	This is a bound property that only fires a change event when the new value is different via the <code>equals()</code> method.
	If a validator is installed, the value will first be validated before the current value is changed.
	Validation always occurs if a validator is installed, even if the value is not changing.
	@param newValue The input value of the model.
	@exception ValidationException if the provided value is not valid.
	@see #getValidator()
	@see #VALUE_PROPERTY
	*/
	public void setValue(final Component<?> newValue) throws ValidationException {getModel().setValue(newValue);}

	/**Metadata about individual component layout.
	@author Garret Wilson
	*/
	public static class Constraints extends AbstractLayout.AbstractConstraints
	{

		/**The enabled bound property.*/
		public final static String ENABLED_PROPERTY=getPropertyName(ControlModel.class, "enabled");

		/**The label associated with an individual component.*/
		private final LabelModel labelModel;
		
			/**@return The label associated with an individual component.*/
			public LabelModel getLabel() {return labelModel;}

		/**Whether the card is enabled for selection.*/
		private boolean enabled=true;

			/**@return Whether the card is enabled for selection.*/
			public boolean isEnabled() {return enabled;}

			/**Sets whether the the card is enabled for selection.
			This is a bound property of type <code>Boolean</code>.
			@param newEnabled <code>true</code> if the corresponding card can be selected.
			@see #ENABLED_PROPERTY
			*/
			public void setEnabled(final boolean newEnabled)
			{
				if(enabled!=newEnabled)	//if the value is really changing
				{
					final boolean oldEnabled=enabled;	//get the old value
					enabled=newEnabled;	//actually change the value
					firePropertyChange(ENABLED_PROPERTY, Boolean.valueOf(oldEnabled), Boolean.valueOf(newEnabled));	//indicate that the value changed
				}			
			}

		/**Label constructor.
		@param labelModel The label associated with an individual component.
		@exception NullPointerException if the given label is <code>null</code>.
		*/
		public Constraints(final LabelModel labelModel)
		{
			this(labelModel, true);	//default to enabling the card
		}

		/**Label and enabled constructor.
		@param labelModel The label associated with an individual component.
		@param enabled Whether the card is enabled.
		@exception NullPointerException if the given label is <code>null</code>.
		*/
		public Constraints(final LabelModel labelModel, final boolean enabled)
		{
			this.labelModel=checkNull(labelModel, "Label cannot be null.");
			this.enabled=enabled;
		}
	}

	/**The list select model that maintains the current selected tab.
	@author Garret Wilson
	*/
	protected class CardModel extends DefaultValueModel<Component<?>> implements ListSelectModel<Component<?>>	//TODO make sure the list select events are fired when a constraint value (e.g. enabled) changes directly, rather than going through the list select interface
	{
		/**Session constructor.
		@param session The Guise session that owns this model.
		@exception NullPointerException if the given session is <code>null</code>.
		*/
		@SuppressWarnings("unchecked")	//classes don't support generics multilevel, so we have to cast Component.class to the correct generic type
		public CardModel(final GuiseSession session)
		{
			super(session, (Class<Component<?>>)(Object)Component.class);	//construct the parent class TODO find out why we have to do the double-cast for JDK 1.5 to work
		}

			//ValueModel methods

		/**Sets the input value.
		This version makes sure that the given component is contained in the container, and resets the cached selected index so that it can be recalculated.
		@param newValue The input value of the model.
		@exception IllegalStateException if this layout has not yet been installed into a container.
		@exception ValidationException if the provided value is not valid, including if the given component is not a member of the layout container.
		*/
		public void setValue(final Component<?> newValue) throws ValidationException
		{
			if(!ObjectUtilities.equals(getValue(), newValue))	//if a new component is given
			{
				final Container<?> container=getContainer();	//get the layout's container
				if(container==null)	//if we haven't been installed into a container
				{
					throw new IllegalStateException("Layout does not have container.");
				}
				if(newValue!=null && !container.contains(newValue))	//if there is a new component that isn't contained in the container
				{
					throw new ValidationException(format(getSession().getStringResource(VALIDATOR_INVALID_VALUE_MESSAGE_RESOURCE), newValue.toString()), newValue);						
				}
				selectedIndex=-1;	//uncache the selected index
			}
			super.setValue(newValue);	//set the new value normally
		}

		/**Resets the value to a default value, which may be invalid according to any installed validators.
		No validation occurs.
		This version resets the cached selected index so that it can be recalculated.
		*/
		public void resetValue()
		{
			selectedIndex=-1;	//uncache the selected index
		}

			//ListSelectModel methods
	
		/**Replaces the first occurrence of the given value with its replacement.
		This method ensures that another thread does not change the model while the search and replace operation occurs.
		@param oldValue The value for which to search.
		@param newValue The replacement value.
		@return Whether the operation resulted in a modification of the model.
		*/
		public boolean replace(final Component<?> oldValue, final Component<?> newValue) {return false;}	//TODO fix

		/**The selection policy for this model.*/
		private final ListSelectionPolicy<Component<?>> selectionPolicy=new SingleListSelectionPolicy<Component<?>>();	//TODO use common version

			/**@return The selection policy for this model.*/
			public ListSelectionPolicy<Component<?>> getSelectionPolicy() {return selectionPolicy;}

		/**Determines the selected index.
		If more than one index is selected, the lead selected index will be returned.
		@return The index currently selected, or -1 if no index is selected.
		@see #getSelectedValue()
		*/
		public int getSelectedIndex()
		{
			return CardLayout.this.getSelectedIndex();
		}
		
		/**Determines the selected indices.
		@return The indices currently selected.
		@see #getSelectedValues()
		*/
		public int[] getSelectedIndexes()
		{
			final int selectedIndex=getSelectedIndex();	//get the selected index
			return selectedIndex>=0 ? new int[]{selectedIndex} : new int[0];	//if there is a selected index, return it in an array
		}
		
		/**Sets the selected indices.
		Invalid and duplicate indices will be ignored.
		@param indices The indices to select.
		@exception ValidationException if the provided value is not valid.
		@see ListSelectionPolicy#getSetSelectedIndices(ListSelectModel, int[])
		@see #setSelectedValues(V[])
		@see #addSelectedIndexes(int...)
		*/
		public void setSelectedIndexes(int... indices) throws ValidationException
		{
			setSelectedIndex(indices.length>0 ? indices[0] : -1);	//if there is at least one requested index, select the first one
		}
		
		/**Adds a selection at the given indices.
		Any invalid indices will be ignored.
		@param indices The indices to add to the selection.
		@exception ValidationException if the provided value is not valid.
		@see ListSelectionPolicy#getAddSelectedIndices(ListSelectModel, int[])
		@see #setSelectedIndexes(int[])
		*/
		public void addSelectedIndexes(int... indices) throws ValidationException {throw new UnsupportedOperationException();}
		
		/**Removes a selection at the given indices.
		Any invalid indices will be ignored.
		@param indices The indices to remove from the selection.
		@exception ValidationException if the provided value is not valid.
		@see ListSelectionPolicy#getRemoveSelectedIndices(ListSelectModel, int[])
		@see #setSelectedIndexes(int[])
		*/
		public void removeSelectedIndexes(int... indices) throws ValidationException {throw new UnsupportedOperationException();}
		
		/**Determines the selected value.
		If more than one value is selected, the lead selected value will be returned.
		@return The value currently selected, or <code>null</code> if no value is currently selected.
		@see #getSelectedIndex()
		*/
		public Component<?> getSelectedValue()
		{
			return getValue();
		}
		
		/**Determines the selected values.
		This method delegates to the selection strategy.
		@return The values currently selected.
		@see #getSelectedIndexes()
		*/
		public Component<?>[] getSelectedValues()
		{
			final Component<?> selectedValue=getSelectedValue();	//get the selected value
			return selectedValue!=null ? new Component<?>[]{selectedValue} : new Component<?>[0];	//if there is a selected value, return it in an array
		}
		
		/**Sets the selected values.
		If a value occurs more than one time in the model, the first occurrence of the value will be selected.
		Values that do not occur in the select model will be ignored.
		This method delegates to the selection strategy.
		@param values The values to select.
		@exception ValidationException if the provided value is not valid.
		@see #setSelectedIndexes(int[])
		*/
		public void setSelectedValues(final Component<?>... values) throws ValidationException
		{
			if(values.length>0)	//if there is at least one value
			{
				setValue(values[0]);	//use the first one
			}
		}
		
		//TODO make sure the list and selection events are firedd
		
		/**Adds a list listener.
		@param listListener The list listener to add.
		*/
		public void addListListener(final ListListener<ListSelectModel<Component<?>>, Component<?>> listListener)
		{
			getEventListenerManager().add(ListListener.class, listListener);	//add the listener
		}

		/**Removes a list listener.
		@param listListener The list listener to remove.
		*/
		public void removeListListener(final ListListener<ListSelectModel<Component<?>>, Component<?>> listListener)
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
	
			//List methods
		
				//TODO eventually make container implement List and delegate 

		/**@return The number of values in the model.*/
		public synchronized int size() {return getContainer().size();}

		/**@return Whether this model contains no values.*/
		public synchronized boolean isEmpty() {return size()==0;}

		/**Determines whether this model contains the specified value.
		@param value The value the presence of which to test.
		@return <code>true</code> if this model contains the specified value.
		*/
		public synchronized boolean contains(final Object value) {return getContainer().contains(value);}

		/**@return An iterator over the values in this model.*/
		public synchronized Iterator<Component<?>> iterator() {return getContainer().iterator();}

		/**@return An array containing all of the values in this model.*/
		public synchronized Object[] toArray() {throw new UnsupportedOperationException();}

		/**Returns an array containing all of the values in this model.
		@param array The array into which the value of this collection are to be stored, if it is big enough; otherwise, a new array of the same runtime type is allocated for this purpose.
		@return An array containing the values of this model.
		@exception ArrayStoreException if the runtime type of the specified array is not a supertype of the runtime type of every value in this model.
		@exception NullPointerException if the specified array is <code>null</code>.
		*/
		public synchronized <T> T[] toArray(final T[] array) {throw new UnsupportedOperationException();}

		/**Appends the specified value to the end of this model.
		This version delegates to {@link #add(int, Object)}.
		@param value The value to be appended to this model.
		@return <code>true</code>, indicating that the model changed as a result of the operation.
		*/
		public synchronized boolean add(final Component<?> value) {getContainer().add(value); return true;}	//TODO fix all these methods

		/**Removes the first occurrence in this model of the specified value. 
		@param value The value to be removed from this model, if present.
		@return <code>true</code> if this model contained the specified value.
		*/
		@SuppressWarnings("unchecked")	//we only cast the value if the list was modified, which implies the value was in the list, implying that that list is of the appropriate type or it wouldn't have been in the list to begin with
		public synchronized boolean remove(final Object value)
		{
			if(value instanceof Component)
			{
				final Component<?> oldSelectedValue=getSelectedValue();	//get the old selected value
				final boolean modified=getContainer().contains(value);
				getContainer().remove((Component<?>)value);	//remove the value from the list
				if(modified)	//if the list was modified
				{
//TODO fix					listModified(-1, null, (V)value);	//indicate the value was removed from an unknown index
					firePropertyChange(VALUE_PROPERTY, oldSelectedValue, getSelectedValue());	//indicate that the value changed if needed		
				}
				return modified;	//indicate whether the list was modified
			}
			else
			{
				return false;
			}
		}

		/**Determines if this model contains all of the values of the specified collection.
		@param collection The collection to be checked for containment in this model.
		@return <code>true</code> if this model contains all of the values of the specified collection.
		@exception NullPointerException if the specified collection is <code>null</code>.
		@see #contains(Object)
		*/
		public synchronized boolean containsAll(final Collection<?> collection) {throw new UnsupportedOperationException();}

		/**Appends all of the values in the specified collection to the end of this model, in the order that they are returned by the specified collection's iterator.
		@param collection The collection the values of which are to be added to this model.
		@return <code>true</code> if this model changed as a result of the call.
		@exception NullPointerException if the specified collection is <code>null</code>.
		@see #add(Object)
		*/
		public synchronized boolean addAll(final Collection<? extends Component<?>> collection) {throw new UnsupportedOperationException();}

		/**Inserts all of the values in the specified collection into this model at the specified position.
		@param index The index at which to insert first value from the specified collection.
		@param collection The values to be inserted into this model.
		@return <code>true</code> if this model changed as a result of the call.
		@exception NullPointerException if the specified collection is <code>null</code>.
		@exception IndexOutOfBoundsException if the index is out of range (<var>index</var> &lt; 0 || <var>index</var> &gt; <code>size()</code>).
		*/
		public synchronized boolean addAll(final int index, final Collection<? extends Component<?>> collection) {throw new UnsupportedOperationException();}

		/**Removes from this model all the values that are contained in the specified collection.
		@param collection The collection that defines which values will be removed from this model.
		@return <code>true</code> if this model changed as a result of the call.
		@exception NullPointerException if the specified collection is <code>null</code>.
		@see #remove(Object)
		@see #contains(Object)
		*/
		public synchronized boolean removeAll(final Collection<?> collection) {throw new UnsupportedOperationException();}

		/**Retains only the values in this model that are contained in the specified collection.
		@param collection The collection that defines which values this model will retain.
		@return <code>true</code> if this model changed as a result of the call.
		@exception NullPointerException if the specified collection is <code>null</code>.
		@see #remove(Object)
		@see #contains(Object)
		*/
		public synchronized boolean retainAll(final Collection<?> collection) {throw new UnsupportedOperationException();}

		/**Removes all of the values from this model.*/
		public synchronized void clear()
		{
			clearValue();
		}

		/**Returns the value at the specified position in this model.
		@param index The index of the value to return.
		@return The value at the specified position in this model.
		@throws IndexOutOfBoundsException if the index is out of range (<var>index</var> &lt; 0 || <var>index</var> &gt;= <code>size()</code>).
		*/
		public synchronized Component<?> get(final int index) {return getContainer().get(index);}

		/**Replaces the value at the specified position in this model with the specified value.
		@param index The index of the value to replace.
		@param value The value to be stored at the specified position.
		@return The value at the specified position.
		@exception IndexOutOfBoundsException if the index is out of range (<var>index<var> &lt; 0 || <var>index</var> &gt;= <code>size()</code>).
		*/
		public synchronized Component<?> set(final int index, final Component<?> value)  {throw new UnsupportedOperationException();}

		/**Inserts the specified value at the specified position in this model.
		@param index The index at which the specified value is to be inserted.
		@param value The value to be inserted.
		@throws IndexOutOfBoundsException if the index is out of range (<var>index</var> &lt; 0 || <var>index</var> &gt; <code>size()</code>).
		*/
		public synchronized void add(final int index, final Component<?> value) {throw new UnsupportedOperationException();}

		/**Removes the value at the specified position in this model.
		@param index The index of the value to removed.
		@return The value previously at the specified position.
		@exception IndexOutOfBoundsException if the index is out of range (<var>index</var> &lt; 0 || <var>index</var> &gt;= <code>size()</code>).
		*/
		public synchronized Component<?> remove(final int index)  {throw new UnsupportedOperationException();}

	  /**Returns the index in this model of the first occurrence of the specified value, or -1 if this model does not contain this value.
		@param value The value for which to search.
		@return The index in this model of the first occurrence of the specified value, or -1 if this model does not contain this value.
		*/
		public synchronized int indexOf(final Object value) {throw new UnsupportedOperationException();}

		/**Returns the index in this model of the last occurrence of the specified value, or -1 if this model does not contain this value.
		@param value The value for which to search.
		@return The index in this model of the last occurrence of the specified vale, or -1 if this model does not contain this value.
		*/
		public synchronized int lastIndexOf(final Object value) {throw new UnsupportedOperationException();}

		/**@return A list iterator of the values in this model (in proper sequence).*/
		public synchronized ListIterator<Component<?>> listIterator() {throw new UnsupportedOperationException();}

		/**Returns a list iterator of the values in this model (in proper sequence), starting at the specified position in this model.
		@param index The index of first value to be returned from the list iterator (by a call to the <code>next()</code> method).
		@return A list iterator of the values in this model (in proper sequence), starting at the specified position in this model.
		@exception IndexOutOfBoundsException if the index is out of range (<var>index</var> &lt; 0 || <var>index</var> &gt; <code>size()</code>).
		*/
		public synchronized ListIterator<Component<?>> listIterator(final int index) {throw new UnsupportedOperationException();}

		/**Returns a view of the portion of this model between the specified <var>fromIndex</var>, inclusive, and <var>toIndex</var>, exclusive.
		@param fromIndex The low endpoint (inclusive) of the sub-list.
		@param toIndex The high endpoint (exclusive) of the sub-list.
		@return A view of the specified range within this model.
		@throws IndexOutOfBoundsException for an illegal endpoint index value (<var>fromIndex</var> &lt; 0 || <var>toIndex</var> &gt; <code>size()</code> || <var>fromIndex</var> &gt; <var>toIndex</var>).
		*/
		public synchronized List<Component<?>> subList(final int fromIndex, final int toIndex) {throw new UnsupportedOperationException();}

		/**Determines the enabled status of the first occurrence of a given value.
		@param value The value for which the enabled status is to be determined.
		@return <code>true</code> if the value is enabled, else <code>false</code>.
		@exception IndexOutOfBoundsException if the given value does not occur in the model.
		*/
		public boolean isValueEnabled(final Component<?> value)
		{
			synchronized(this)	//don't allow the model to be changed
			{
				final Constraints constraints=getConstraints(value);	//get the constraints for this component
				return constraints.isEnabled();	//return whether the constraints are enabled for this component
			}
		}

		/**Sets the enabled status of the first occurrence of a given value.
		This is a bound value state property.
		@param value The value to enable or disable.
		@param newEnabled Whether the value should be enabled.
		@see ValuePropertyChangeEvent
		@see ControlModel#ENABLED_PROPERTY
		*/
		public void setValueEnabled(final Component<?> value, final boolean newEnabled) 
		{
			synchronized(this)	//don't allow the model to be changed
			{
				final Constraints constraints=getConstraints(value);	//get the constraints for this component
				final boolean oldEnabled=constraints.isEnabled();	//get the old enabled state
				if(oldEnabled!=newEnabled)	//if the value is really changing
				{
					constraints.setEnabled(newEnabled);	//update the enabled state
					fireValuePropertyChange(value, ControlModel.ENABLED_PROPERTY, Boolean.valueOf(oldEnabled), Boolean.valueOf(newEnabled));	//indicate that the value state changed
				}
			}
		}

		/**Determines the enabled status of a given index.
		@param index The index of the value for which the enabled status is to be determined.
		@return <code>true</code> if the value at the given index is enabled, else <code>false</code>.
		*/
		public boolean isIndexEnabled(final int index)
		{
			synchronized(this)	//don't allow the model to be changed
			{
				final Component<?> component=getContainer().get(index);	//get the component at this index
				return isValueEnabled(component);	//determine whether this component is enabled
			}
		}

		/**Sets the enabled status of a given index.
		This is a bound value state property.
		@param index The index of the value to enable or disable.
		@param newEnabled Whether the value at the given index should be enabled.
		@see ValuePropertyChangeEvent
		@see ControlModel#ENABLED_PROPERTY
		@exception IndexOutOfBoundsException if the given index is not within the range of the list.
		*/
		public void setIndexEnabled(final int index, final boolean newEnabled) 
		{
			synchronized(this)	//don't allow the the model to change while we update the enabled status
			{
				final Component<?> component=getContainer().get(index);	//get the component at this index
				setValueEnabled(component, newEnabled);	//change the constraints for the component at the index
			}
		}

		/**Reports that an associated property of a value, such as enabled status, has changed.
		No event is fired if old and new are both <code>null</code> or are both non-<code>null</code> and equal according to the {@link Object#equals(java.lang.Object)} method.
		No event is fired if no listeners are registered for the given property.
		This method delegates actual firing of the event to {@link #firePropertyChange(PropertyChangeEvent)}.
		@param value The value for which a property value changed.
		@param propertyName The name of the property being changed.
		@param oldValue The old property value.
		@param newValue The new property value.
		@see ValuePropertyChangeEvent
		*/
		protected <P> void fireValuePropertyChange(final Component<?> value, final String propertyName, final P oldValue, final P newValue)
		{
			if(hasListeners(propertyName)) //if we have listeners registered for this property
			{
				if(!ObjectUtilities.equals(oldValue, newValue))	//if the values are different
				{					
					firePropertyChange(new ValuePropertyChangeEvent<ListSelectModel<Component<?>>, Component<?>, P>(this, value, propertyName, oldValue, newValue));	//create and fire a value property change event
				}
			}
		}
	}

	/**A property change listener that listens for changes in a constraint object's properties and fires a layout constraints property change event in response.
	This version also fires model {@link ValuePropertyChangeEvent}s if appropriate.
	A {@link LayoutConstraintsPropertyChangeEvent} will be fired for each component associated with the constraints for which a property changed
	@author Garret Wilson
	@see ValuePropertyChangeEvent
	*/
	protected class CardConstraintsPropertyChangeListener extends ConstraintsPropertyChangeListener
	{

		/**Refires a constraint property change event for the layout in the form of a {@link LayoutConstraintsPropertyChangeEvent}.
		This version also fires a model {@link ValuePropertyChangeEvent} if appropriate to satisfy the list select model contract for value state changes.
		@param component The component for which a constraint value changed.
		@param constraints The constraints for which a value changed.
		@param propertyName The name of the property being changed.
		@param oldValue The old property value.
		@param newValue The new property value.
		*/
		protected <V> void refirePropertyChange(final Component<?> component, final Constraints constraints, final String propertyName, final V oldValue, final V newValue)
		{
			super.refirePropertyChange(component, constraints, propertyName, oldValue, newValue);	//refire the event normally
			if(Constraints.ENABLED_PROPERTY.equals(propertyName))	//if the enabled constraint changed
			{
				model.fireValuePropertyChange(component, propertyName, oldValue, newValue);	//tell the model to fire its own event to satisfy the model's contract
			}			
		}
	}

}
