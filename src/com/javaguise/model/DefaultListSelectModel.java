package com.javaguise.model;

import java.util.*;
import static java.util.Collections.*;

import com.garretwilson.util.ArrayUtilities;
import com.garretwilson.util.Debug;
import com.garretwilson.util.SynchronizedSetDecorator;
import com.javaguise.event.*;
import com.javaguise.session.GuiseSession;
import com.javaguise.validator.ValidationException;
import com.javaguise.validator.Validator;

import static com.garretwilson.lang.IntegerUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.util.ArrayUtilities.createArray;

/**The default implementation of a model for selecting one or more values from a list.
The model is thread-safe, synchronized on itself. Any iteration over values should include synchronization on the instance of this class.
This implementation has a default value of <code>null</code>. 
@param <V> The type of values contained in the model.
@author Garret Wilson
*/
public class DefaultListSelectModel<V> extends AbstractValueModel<V> implements ListSelectModel<V>
{

	/**The thread-safe sorted set of selected indices, synchronized on this model.*/
	private final Set<Integer> selectedIndexSet;

		/**@return The thread-safe sorted set of selected indices, synchronized on this model.*/
		protected Set<Integer> getSelectedIndexSet() {return selectedIndexSet;}

	/**The default value.*/
	private final V defaultValue=null;

		/**@return The default value.*/
		public V getDefaultValue() {return defaultValue;}

	/**@return The selected value, or <code>null</code> if there is no selected value.*/
	public V getValue() {return getSelectedValue();}

	/**Sets the input value.
	This is a bound property that only fires a change event when the new value is different via the <code>equals()</code> method.
	If a validator is installed, the value will first be validated before the current value is changed.
	Validation always occurs if a validator is installed, even if the value is not changing.
	@param newValue The input value of the model.
	@exception ValidationException if the provided value is not valid.
	@see #getValidator()
	@see ValueModel#VALUE_PROPERTY
	*/
	public void setValue(final V newValue) throws ValidationException
	{
		final Validator<V> validator=getValidator();	//get the currently installed validator, if there is one
		if(validator!=null)	//if a validator is installed, always validate the value, even if it isn't changing, so that an initial value that may not be valid will throw an error when it's tried to be set to the same, but invalid, value
		{
			validator.validate(newValue);	//validate the new value, throwing an exception if anything is wrong
		}
		setSelectedValues(newValue);
	}

	/**Clears the value by setting the value to <code>null</code>, which may be invalid according to any installed validators.
	No validation occurs.
	@see ValueModel#VALUE_PROPERTY
	*/
	public void clearValue()
	{
		final V oldSelectedValue, newSelectedValue;
		synchronized(this)	//don't allow the list to be modified while we update the selections
		{
			oldSelectedValue=getSelectedValue();	//get the old selected value
			getSelectedIndexSet().clear();	//remove all selected indices
			newSelectedValue=getSelectedValue();	//find out the new selected value
		}
		firePropertyChange(VALUE_PROPERTY, oldSelectedValue, newSelectedValue);	//indicate that the value changed if needed		
	}

	/**Resets the value to a default value, which may be invalid according to any installed validators.
	No validation occurs.
	This implementation calls {@link #clearValue()}.
	@see ValueModel#VALUE_PROPERTY
	@see #clearValue()
	*/
	public void resetValue()
	{
		clearValue();	//clear the value, as null is the default value
	}

	/**The list of values, all access to which will be synchronized on this.*/
	private final List<V> values=new ArrayList<V>();

	/**@return The number of values in the model.*/
	public synchronized int size() {return values.size();}

	/**@return Whether this model contains no values.*/
	public synchronized boolean isEmpty() {return values.isEmpty();}

	/**Determines whether this model contains the specified value.
	@param value The value the presence of which to test.
	@return <code>true</code> if this model contains the specified value.
	*/
	public synchronized boolean contains(final Object value) {return values.contains(value);}

	/**@return An iterator over the values in this model.*/
	public synchronized Iterator<V> iterator() {return values.iterator();}

	/**@return An array containing all of the values in this model.*/
	public synchronized Object[] toArray() {return values.toArray();}

	/**Returns an array containing all of the values in this model.
	@param array The array into which the value of this collection are to be stored, if it is big enough; otherwise, a new array of the same runtime type is allocated for this purpose.
	@return An array containing the values of this model.
	@exception ArrayStoreException if the runtime type of the specified array is not a supertype of the runtime type of every value in this model.
	@exception NullPointerException if the specified array is <code>null</code>.
	*/
	public synchronized <T> T[] toArray(final T[] array) {return values.toArray(array);}

	/**Appends the specified value to the end of this model.
	This version delegates to {@link #add(int, Object)}.
	@param value The value to be appended to this model.
	@return <code>true</code>, indicating that the model changed as a result of the operation.
	*/
	public synchronized boolean add(final V value)
	{
		add(values.size(), value);	//add the value to the end of the list
		return true;	//this operation always modifies the list
	}

	/**Removes the first occurrence in this model of the specified value. 
	@param value The value to be removed from this model, if present.
	@return <code>true</code> if this model contained the specified value.
	*/
	@SuppressWarnings("unchecked")	//we only cast the value if the list was modified, which implies the value was in the list, implying that that list is of the appropriate type or it wouldn't have been in the list to begin with
	public synchronized boolean remove(final Object value)
	{
		final V oldSelectedValue=getSelectedValue();	//get the old selected value
		final boolean modified=values.remove(value);	//remove the value from the list
		if(modified)	//if the list was modified
		{
			listModified(-1, null, (V)value);	//indicate the value was removed from an unknown index
			firePropertyChange(VALUE_PROPERTY, oldSelectedValue, getSelectedValue());	//indicate that the value changed if needed		
		}
		return modified;	//indicate whether the list was modified
	}

	/**Determines if this model contains all of the values of the specified collection.
	@param collection The collection to be checked for containment in this model.
	@return <code>true</code> if this model contains all of the values of the specified collection.
	@exception NullPointerException if the specified collection is <code>null</code>.
	@see #contains(Object)
	*/
	public synchronized boolean containsAll(final Collection<?> collection) {return values.containsAll(collection);}

	/**Appends all of the values in the specified collection to the end of this model, in the order that they are returned by the specified collection's iterator.
	@param collection The collection the values of which are to be added to this model.
	@return <code>true</code> if this model changed as a result of the call.
	@exception NullPointerException if the specified collection is <code>null</code>.
	@see #add(Object)
	*/
	public synchronized boolean addAll(final Collection<? extends V> collection)
	{
		final V oldSelectedValue=getSelectedValue();	//get the old selected value
		final boolean modified=values.addAll(collection);	//add all the values
		if(modified)	//if the list was modified
		{
			listModified(-1, null, null);	//indicate a general list modification
			firePropertyChange(VALUE_PROPERTY, oldSelectedValue, getSelectedValue());	//indicate that the value changed if needed		
		}
		return modified;	//indicate whether the list was modified
	}

	/**Inserts all of the values in the specified collection into this model at the specified position.
	@param index The index at which to insert first value from the specified collection.
	@param collection The values to be inserted into this model.
	@return <code>true</code> if this model changed as a result of the call.
	@exception NullPointerException if the specified collection is <code>null</code>.
	@exception IndexOutOfBoundsException if the index is out of range (<var>index</var> &lt; 0 || <var>index</var> &gt; <code>size()</code>).
	*/
	public synchronized boolean addAll(final int index, final Collection<? extends V> collection)
	{
		final V oldSelectedValue=getSelectedValue();	//get the old selected value
		final boolean modified=values.addAll(index, collection);	//add the values
		if(modified)	//if the list was modified
		{
			listModified(-1, null, null);	//indicate a general list modification
			firePropertyChange(VALUE_PROPERTY, oldSelectedValue, getSelectedValue());	//indicate that the value changed if needed		
		}
		return modified;	//indicate whether the list was modified
}

	/**Removes from this model all the values that are contained in the specified collection.
	@param collection The collection that defines which values will be removed from this model.
	@return <code>true</code> if this model changed as a result of the call.
	@exception NullPointerException if the specified collection is <code>null</code>.
	@see #remove(Object)
	@see #contains(Object)
	*/
	public synchronized boolean removeAll(final Collection<?> collection)
	{
		final V oldSelectedValue=getSelectedValue();	//get the old selected value
		final boolean modified=values.removeAll(collection);	//remove the values
		if(modified)	//if the list was modified
		{
			listModified(-1, null, null);	//indicate a general list modification
			firePropertyChange(VALUE_PROPERTY, oldSelectedValue, getSelectedValue());	//indicate that the value changed if needed		
		}
		return modified;	//indicate whether the list was modified
	}

	/**Retains only the values in this model that are contained in the specified collection.
	@param collection The collection that defines which values this model will retain.
	@return <code>true</code> if this model changed as a result of the call.
	@exception NullPointerException if the specified collection is <code>null</code>.
	@see #remove(Object)
	@see #contains(Object)
	*/
	public synchronized boolean retainAll(final Collection<?> collection)
	{
		final V oldSelectedValue=getSelectedValue();	//get the old selected value
		final boolean modified=values.retainAll(collection);	//remove values if needed
		if(modified)	//if the list was modified
		{
			listModified(-1, null, null);	//indicate a general list modification
			firePropertyChange(VALUE_PROPERTY, oldSelectedValue, getSelectedValue());	//indicate that the value changed if needed		
		}
		return modified;	//indicate whether the list was modified		
	}

	/**Removes all of the values from this model.*/
	public synchronized void clear()
	{
		final V oldSelectedValue=getSelectedValue();	//get the old selected value
		values.clear();	//clear the list
		listModified(-1, null, null);	//indicate a general list modification (without more intricate synchornization, we can't know for sure if the list was modified, even checking the size beforehand, because of thread race conditions)
		firePropertyChange(VALUE_PROPERTY, oldSelectedValue, getSelectedValue());	//indicate that the value changed if needed		
	}

	/**Returns the value at the specified position in this model.
	@param index The index of the value to return.
	@return The value at the specified position in this model.
	@throws IndexOutOfBoundsException if the index is out of range (<var>index</var> &lt; 0 || <var>index</var> &gt;= <code>size()</code>).
	*/
	public synchronized V get(final int index) {return values.get(index);}

	/**Replaces the value at the specified position in this model with the specified value.
	@param index The index of the value to replace.
	@param value The value to be stored at the specified position.
	@return The value at the specified position.
	@exception IndexOutOfBoundsException if the index is out of range (<var>index<var> &lt; 0 || <var>index</var> &gt;= <code>size()</code>).
	*/
	public synchronized V set(final int index, final V value)
	{
		final V oldSelectedValue=getSelectedValue();	//get the old selected value
		final V oldValue=values.set(index, value);	//set the value at the given index
		listModified(index, oldValue, value);	//indicate that the value at the given index was replaced
		firePropertyChange(VALUE_PROPERTY, oldSelectedValue, getSelectedValue());	//indicate that the value changed if needed		
		return oldValue;	//return the old value
	}

	/**Inserts the specified value at the specified position in this model.
	@param index The index at which the specified value is to be inserted.
	@param value The value to be inserted.
	@throws IndexOutOfBoundsException if the index is out of range (<var>index</var> &lt; 0 || <var>index</var> &gt; <code>size()</code>).
	*/
	public synchronized void add(final int index, final V value)
	{
		final V oldSelectedValue=getSelectedValue();	//get the old selected value
		values.add(index, value);	//add the value at the requested index
		listModified(index, value, null);	//indicate the value was added at the given index
		firePropertyChange(VALUE_PROPERTY, oldSelectedValue, getSelectedValue());	//indicate that the value changed if needed		
	}

	/**Removes the value at the specified position in this model.
	@param index The index of the value to removed.
	@return The value previously at the specified position.
	@exception IndexOutOfBoundsException if the index is out of range (<var>index</var> &lt; 0 || <var>index</var> &gt;= <code>size()</code>).
	*/
	public synchronized V remove(final int index)
	{
		final V oldSelectedValue=getSelectedValue();	//get the old selected value
		final V value=values.remove(index);	//remove the value at this index	
		listModified(index, null, value);	//indicate the value was removed from the given index
		firePropertyChange(VALUE_PROPERTY, oldSelectedValue, getSelectedValue());	//indicate that the value changed if needed		
		return value;	//return the value that was removed
	}

  /**Returns the index in this model of the first occurrence of the specified value, or -1 if this model does not contain this value.
	@param value The value for which to search.
	@return The index in this model of the first occurrence of the specified value, or -1 if this model does not contain this value.
	*/
	public synchronized int indexOf(final Object value) {return values.indexOf(value);}

	/**Returns the index in this model of the last occurrence of the specified value, or -1 if this model does not contain this value.
	@param value The value for which to search.
	@return The index in this model of the last occurrence of the specified vale, or -1 if this model does not contain this value.
	*/
	public synchronized int lastIndexOf(final Object value) {return values.lastIndexOf(value);}

	/**@return A list iterator of the values in this model (in proper sequence).*/
	public synchronized ListIterator<V> listIterator() {return values.listIterator();}

	/**Returns a list iterator of the values in this model (in proper sequence), starting at the specified position in this model.
	@param index The index of first value to be returned from the list iterator (by a call to the <code>next()</code> method).
	@return A list iterator of the values in this model (in proper sequence), starting at the specified position in this model.
	@exception IndexOutOfBoundsException if the index is out of range (<var>index</var> &lt; 0 || <var>index</var> &gt; <code>size()</code>).
	*/
	public synchronized ListIterator<V> listIterator(final int index) {return values.listIterator(index);}

	/**Returns a view of the portion of this model between the specified <var>fromIndex</var>, inclusive, and <var>toIndex</var>, exclusive.
	@param fromIndex The low endpoint (inclusive) of the sub-list.
	@param toIndex The high endpoint (exclusive) of the sub-list.
	@return A view of the specified range within this model.
	@throws IndexOutOfBoundsException for an illegal endpoint index value (<var>fromIndex</var> &lt; 0 || <var>toIndex</var> &gt; <code>size()</code> || <var>fromIndex</var> &gt; <var>toIndex</var>).
	*/
	public synchronized List<V> subList(final int fromIndex, final int toIndex) {return values.subList(fromIndex, toIndex);}

	/**Replaces the first occurrence of the given value with its replacement.
	This method ensures that another thread does not change the model while the search and replace operation occurs.
	@param oldValue The value for which to search.
	@param newValue The replacement value.
	@return Whether the operation resulted in a modification of the model.
	*/
	public synchronized boolean replace(final V oldValue, final V newValue)
	{
		final int index=indexOf(oldValue);	//get the index of the old value
		if(index>=0)	//if the value is in the model
		{
			final V oldSelectedValue=getSelectedValue();	//get the old selected value
			set(index, newValue);	//change the value at the given index, which will fire the appropriate event
			firePropertyChange(VALUE_PROPERTY, oldSelectedValue, getSelectedValue());	//indicate that the value changed if needed		
			return true;	//indicate that we modified the model
		}
		else	//if the value is not in the model
		{
			return false;	//report that the old value could not be found
		}
	}

	/**The selection policy for this model.*/
	private ListSelectionPolicy<V> selectionPolicy;

		/**@return The selection policy for this model.*/
		public ListSelectionPolicy<V> getSelectionPolicy() {return selectionPolicy;}

	/**Determines the selected index.
	If more than one index is selected, the lead selected index will be returned.
	@return The index currently selected, or -1 if no index is selected.
	@see #getSelectedValue()
	*/
	public int getSelectedIndex()
	{
		final int[] selectedIndices=getSelectedIndices();	//get the selected indices
		return selectedIndices.length>0 ? selectedIndices[0] : -1;	//if there are indices, return the first one					
	}

	/**Determines the selected indices.
	@return The indices currently selected.
	@see #getSelectedValues()
	*/
	public int[] getSelectedIndices()
	{
		final Set<Integer> selectedIndexSet=getSelectedIndexSet();	//get the set of selected indices
		final Integer[] integerIndices;	//we'll initially get the selected indices as integers
		synchronized(selectedIndexSet)	//don't allow the selection set to be changed while we calculate how many indices to allocate 
		{
			integerIndices=selectedIndexSet.toArray(new Integer[selectedIndexSet.size()]);	//create an array of integer selected indices
		}
		return toIntArray(integerIndices);	//return the integers as an array of ints
	}

	/**Sets the selected indices.
	Invalid and duplicate indices will be ignored.
	@param indices The indices to select.
	@exception ValidationException if the provided value is not valid.
	@see ListSelectionPolicy#getSetSelectedIndices(ListSelectModel, int[])
	@see #setSelectedValues(V[])
	@see #addSelectedIndices(int...)
	*/
	public void setSelectedIndices(int... indices) throws ValidationException
	{
int validIndexCount=0;	//TODO fix validation hack
for(int i=indices.length-1; i>=0; --i)
{
	if(indices[i]>=0)
	{
		++validIndexCount;
	}
}
if(validIndexCount==0)	//TODO add more thorough validation throughout; right now we only check for null not being valid; also take into consideration that some of the indices may be invalid and therefore ignored
{
	final Validator<V> validator=getValidator();	//get the currently installed validator, if there is one
	if(validator!=null)	//if a validator is installed, always validate the value, even if it isn't changing, so that an initial value that may not be valid will throw an error when it's tried to be set to the same, but invalid, value
	{
		validator.validate(null);	//validate the new value, throwing an exception if anything is wrong
	}
}
			//TODO this method, along with the add and remove methods, need to collect added and/or removed indices and report them after all changes are done; better event classes should be created as well
		final V oldSelectedValue, newSelectedValue;
		synchronized(this)	//don't allow the list to be modified while we update the selections
		{
			indices=getSelectionPolicy().getSetSelectedIndices(this, indices);	//get the indices to set
			oldSelectedValue=getSelectedValue();	//get the old selected value
			final Set<Integer> selectedIndexSet=getSelectedIndexSet();	//get the set of selected indices
			final Iterator<Integer> oldSelectedIndexIterator=selectedIndexSet.iterator();	//get an iterator to the old selected indices
			while(oldSelectedIndexIterator.hasNext())	//while there are more old selected indices
			{
				final Integer oldSelectedIndex=oldSelectedIndexIterator.next();	//get the next old selected index
				if(ArrayUtilities.indexOf(indices, oldSelectedIndex.intValue())<0)	//if the new set of indices doesn't have this old index
				{
					oldSelectedIndexIterator.remove();	//remove this old selected index
				}
			}
			final int itemCount=size();	//find out how many items there are
			for(final Integer index:indices)	//for each index
			{
				if(index>=0 && index<itemCount)	//if the index is within the allowed range
				{
					if(selectedIndexSet.add(index))	//add this selection to the set; if the index was added
					{
						fireSelectionChanged(index, null);	//notify listeners that an index was added						
					}
				}
			}
			newSelectedValue=getSelectedValue();	//find out the new selected value
		}
		firePropertyChange(VALUE_PROPERTY, oldSelectedValue, newSelectedValue);	//indicate that the value changed if needed		
	}

	/**Adds a selection at the given indices.
	Any invalid indices will be ignored.
	@param indices The indices to add to the selection.
	@exception ValidationException if the provided value is not valid.
	@see ListSelectionPolicy#getAddSelectedIndices(ListSelectModel, int[])
	@see #setSelectedIndices(int[])
	*/
	public void addSelectedIndices(int... indices) throws ValidationException
	{
		final V oldSelectedValue, newSelectedValue;
		synchronized(this)	//don't allow the list to be modified while we update the selections
		{
			indices=getSelectionPolicy().getAddSelectedIndices(this, indices);	//get the indices to add
			oldSelectedValue=getSelectedValue();	//get the old selected value
			final Set<Integer> selectedIndexSet=getSelectedIndexSet();	//get the set of selected indices
			final int itemCount=size();	//find out how many items there are
			for(final Integer index:indices)	//for each index
			{
				if(index>=0 && index<itemCount)	//if the index is within the allowed range
				{
					if(selectedIndexSet.add(index))	//add this selection to the set; if the index was added
					{
						fireSelectionChanged(index, null);	//notify listeners that an index was added						
					}
				}
			}
			newSelectedValue=getSelectedValue();	//find out the new selected value
		}
		firePropertyChange(VALUE_PROPERTY, oldSelectedValue, newSelectedValue);	//indicate that the value changed if needed		
	}

	/**Removes a selection at the given indices.
	Any invalid indices will be ignored.
	@param indices The indices to remove from the selection.
	@exception ValidationException if the provided value is not valid.
	@see ListSelectionPolicy#getRemoveSelectedIndices(ListSelectModel, int[])
	@see #setSelectedIndices(int[])
	*/
	public void removeSelectedIndices(int... indices) throws ValidationException
	{
		final V oldSelectedValue, newSelectedValue;
		synchronized(this)	//don't allow the list to be modified while we update the selections
		{
			indices=getSelectionPolicy().getRemoveSelectedIndices(this, indices);	//get the indices to remove
			oldSelectedValue=getSelectedValue();	//get the old selected value
			final Set<Integer> selectedIndexSet=getSelectedIndexSet();	//get the set of selected indices
			final int itemCount=size();	//find out how many items there are
			for(final Integer index:indices)	//for each index
			{
				if(index>=0 && index<itemCount)	//if the index is within the allowed range
				{
					if(selectedIndexSet.remove(index))	//remove this selection from the set; if the index was removed
					{
						fireSelectionChanged(null, index);	//notify listeners that an index was removed						
					}
				}
			}
			newSelectedValue=getSelectedValue();	//find out the new selected value
		}
		firePropertyChange(VALUE_PROPERTY, oldSelectedValue, newSelectedValue);	//indicate that the value changed if needed		
	}

	/**Determines the selected value.
	If more than one value is selected, the lead selected value will be returned.
	@return The value currently selected, or <code>null</code> if no value is currently selected.
	@see #getSelectedIndex()
	*/
	public V getSelectedValue()
	{
		final V[] selectedValues=getSelectedValues();	//get the selected values
		return selectedValues.length>0 ? selectedValues[0] : null;	//if there are values, return the first one
	}

	/**Determines the selected values.
	This method delegates to the selection strategy.
	@return The values currently selected.
	@see #getSelectedIndices()
	*/
	public V[] getSelectedValues()
	{		
		synchronized(this)	//don't allow the model to be changed while we determine the selections 
		{
			final int[] selectedIndices=getSelectedIndices();	//get the selected indices
			final V[] selectedValues=createArray(getValueClass(), selectedIndices.length);	//create an array of selected objects
			for(int i=selectedIndices.length-1; i>=0; --i)	//for each selected index
			{
				selectedValues[i]=get(selectedIndices[i]);	//get the value from the model at this index
			}
			return selectedValues;	//return the selected values
		}
	}

	/**Sets the selected values.
	If a value occurs more than one time in the model, the first occurrence of the value will be selected.
	Values that do not occur in the select model will be ignored.
	This method delegates to the selection strategy.
	@param values The values to select.
	@exception ValidationException if the provided value is not valid.
	@see #setSelectedIndices(int[])
	*/
	public void setSelectedValues(final V... values) throws ValidationException
	{
if(values.length==0)	//TODO add more thorough validation throughout; right now we only check for null not being valid
{
	final Validator<V> validator=getValidator();	//get the currently installed validator, if there is one
	if(validator!=null)	//if a validator is installed, always validate the value, even if it isn't changing, so that an initial value that may not be valid will throw an error when it's tried to be set to the same, but invalid, value
	{
		validator.validate(null);	//validate the new value, throwing an exception if anything is wrong
	}
}
		synchronized(this)	//don't allow the model to be changed while we determine the indices 
		{
			final int[] indices=new int[values.length];	//create a new array in which to hold the indices to select
			for(int i=values.length-1; i>=0; --i)	//for each value
			{
				indices[i]=indexOf(values[i]);	//get the index of this value, ignoring whether it is valid as its validity will be checked in setSelectedIndices()
			}
			setSelectedIndices(indices);	//select the indices
		}
	}

	/**Adds a list listener.
	@param listListener The list listener to add.
	*/
	public void addListListener(final ListListener<ListSelectModel<V>, V> listListener)
	{
		getEventListenerManager().add(ListListener.class, listListener);	//add the listener
	}

	/**Removes a list listener.
	@param listListener The list listener to remove.
	*/
	public void removeListListener(final ListListener<ListSelectModel<V>, V> listListener)
	{
		getEventListenerManager().remove(ListListener.class, listListener);	//remove the listener
	}

	/**Adds a list selection listener.
	@param selectionListener The selection listener to add.
	*/
	public void addListSelectionListener(final ListSelectionListener<V> selectionListener)
	{
		getEventListenerManager().add(ListSelectionListener.class, selectionListener);	//add the listener
	}

	/**Removes a list selection listener.
	@param selectionListener The selection listener to remove.
	*/
	public void removeListSelectionListener(final ListSelectionListener<V> selectionListener)
	{
		getEventListenerManager().remove(ListSelectionListener.class, selectionListener);	//remove the listener
	}

	/**Called when the list is modified.
	This method calls the method for notifying listeners that the list was modified.
	@param index The index at which an element was added and/or removed, or -1 if the index is unknown.
	@param addedElement The element that was added to the list, or <code>null</code> if no element was added or it is unknown whether or which elements were added.
	@param removedElement The element that was removed from the list, or <code>null</code> if no element was removed or it is unknown whether or which elements were removed.
	@see #fireListModified(int, Object, Object)
	*/
	protected void listModified(final int index, final V addedElement, final V removedElement)	//TODO fire selection change events if we need to
	{
		try
		{
			if(index>=0 && addedElement==null && removedElement!=null)	//if a single element was removed and not replaced
			{
				removeSelectedIndices(index);	//make sure the removed index is not selected, as there's a different (or no) value there, now
					//TODO go through the other indices and adjust them
			}
			else if(index<0 || (addedElement==null && removedElement==null))	//if we don't have enough information about what exactly happened
			{
				setSelectedIndices();	//clear all selections, as we don't know which values were added or removed
			}
		}
		catch(final ValidationException validationException)
		{
			Debug.warn(validationException);	//TODO improve error handling
		}
		fireListModified(index, addedElement, removedElement);	//fire an event indicating that the list changed
	}

	
	/**Fires an event to all registered list listeners indicating the list was modified.
	This method first manually notifies its selection strategy that the list has changed.
	@param index The index at which an element was added and/or removed, or -1 if the index is unknown.
	@param addedElement The element that was added to the list, or <code>null</code> if no element was added or it is unknown whether or which elements were added.
	@param removedElement The element that was removed from the list, or <code>null</code> if no element was removed or it is unknown whether or which elements were removed.
	@see ListListener
	@see ListEvent
	*/
	protected void fireListModified(final int index, final V addedElement, final V removedElement)
	{
		if(getEventListenerManager().hasListeners(ListListener.class))	//if there are appropriate listeners registered
		{
			final ListEvent<ListSelectModel<V>, V> listEvent=new ListEvent<ListSelectModel<V>, V>(getSession(), this, index, addedElement, removedElement);	//create a new event
			getSession().queueEvent(new PostponedListEvent<ListSelectModel<V>, V>(getEventListenerManager(), listEvent));	//tell the Guise session to queue the event
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
			final ListSelectionEvent<V> selectionEvent=new ListSelectionEvent<V>(getSession(), this, addedIndex, removedIndex);	//create a new event
			getSession().queueEvent(new PostponedListSelectionEvent<V>(getEventListenerManager(), selectionEvent));	//tell the Guise session to queue the event
		}
	}

	/**Constructs a list select model indicating the type of values it can hold, using a default multiple selection strategy.
	@param session The Guise session that owns this model.
	@param valueClass The class indicating the type of values held in the model.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	*/
	public DefaultListSelectModel(final GuiseSession session, final Class<V> valueClass)
	{
		this(session, valueClass, new MultipleListSelectionPolicy<V>());	//construct the class with a multiple selection strategy
	}

	/**Constructs a list select model indicating the type of values it can hold.
	The selection strategy is not added as a listener to this model but is rather notified manually so that the event won't be delayed and/or sent out of order
	@param session The Guise session that owns this model.
	@param valueClass The class indicating the type of values held in the model.
	@param listSelectionStrategy The strategy for selecting values in the model.
	@exception NullPointerException if the given session, class object, and/or selection strategy is <code>null</code>.
	*/
	public DefaultListSelectModel(final GuiseSession session, final Class<V> valueClass, final ListSelectionPolicy<V> listSelectionStrategy)
	{
		super(session, valueClass);	//construct the parent class
		selectedIndexSet=new SynchronizedSetDecorator<Integer>(new TreeSet<Integer>(), this);	//create a sorted set synchronized on this object
		this.selectionPolicy=checkNull(listSelectionStrategy, "Selection strategy cannot be null.");
	}
}
