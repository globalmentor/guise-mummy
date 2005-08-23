package com.javaguise.model;

import java.util.*;

import com.garretwilson.event.EventListenerManager;
import com.javaguise.event.*;

import static java.util.Collections.*;
import static com.garretwilson.util.ArrayUtilities.*;

/**An abstract implementation of a list selection strategy for a list select model.
This class is thread-safe, and assumes that the corresponding select model is thread-safe, synchronized on itself.
@param <V> The type of values contained in the select model.
@author Garret Wilson
@see ListSelectModel
*/
public abstract class AbstractListSelectionStrategy<V> implements ListSelectionStrategy<V>
{

	/**The object managing event listeners.*/
	private final EventListenerManager eventListenerManager=new EventListenerManager();

		/**@return The object managing event listeners.*/
		protected EventListenerManager getEventListenerManager() {return eventListenerManager;}

	/**The thread-safe sorted set of selected indices on which synchronization can be performed.*/
	private final Set<Integer> selectedIndices=synchronizedSortedSet(new TreeSet<Integer>());

		/**@return The thread-safe sorted set of selected indices on which synchronization can be performed.*/
		protected Set<Integer> getSelectedIndices() {return selectedIndices;}

	/**Determines the selected index.
	If more than one index is selected, the lead selected index will be returned.
	@param selectModel The model containing the selected values.
	@return The index currently selected, or -1 if no index is selected.
	@see #getSelectedValue(ListSelectModel)
	*/
	public int getSelectedIndex(final ListSelectModel<V> selectModel)
	{
		final int[] selectedIndices=getSelectedIndices(selectModel);	//get the selected indices
		return selectedIndices.length>0 ? selectedIndices[0] : -1;	//if there are indices, return the first one		
	}

	/**Determines the selected indices.
	@param selectModel The model containing the selected values.
	@return The indices currently selected.
	@see #getSelectedValues(ListSelectModel)
	*/
	public int[] getSelectedIndices(final ListSelectModel<V> selectModel)
	{
		final Integer[] integerIndices;	//we'll initially get the selected indices as integers
		synchronized(selectedIndices)	//don't allow the selection set to be changed while we calculate how many indices to allocate 
		{
			integerIndices=selectedIndices.toArray(new Integer[selectedIndices.size()]);	//create an array of integer selected indices
		}
		final int[] intIndices=new int[integerIndices.length];	//create an array of int indices
		for(int i=integerIndices.length-1; i>=0; --i)	//for each index
		{
			intIndices[i]=integerIndices[i].intValue();	//copy the index to the primitive value array
		}
		return intIndices;	//return the selected indexes
	}

	/**Determines the selected value.
	If more than one value is selected, the lead selected value will be returned.
	@param selectModel The model containing the selected values.
	@return The value currently selected, or <code>null</code> if no value is currently selected.
	@see #getSelectedIndex(ListSelectModel)
	*/
	public V getSelectedValue(final ListSelectModel<V> selectModel)
	{
		final V[] selectedValues=getSelectedValues(selectModel);	//get the selected values
		return selectedValues.length>0 ? selectedValues[0] : null;	//if there are values, return the first one
	}

	/**Determines the selected values.
	@param selectModel The model containing the selected values.
	@return The values currently selected.
	@see #getSelectedIndices(ListSelectModel)
	*/
	public V[] getSelectedValues(final ListSelectModel<V> selectModel)
	{
		synchronized(selectModel)	//don't allow the model to be changed while we determine the selections 
		{
			final int[] selectedIndices=getSelectedIndices(selectModel);	//get the selected indices
			final V[] selectedValues=createArray(selectModel.getValueClass(), selectedIndices.length);	//create an array of selected objects
			for(int i=selectedIndices.length-1; i>=0; --i)	//for each selected index
			{
				selectedValues[i]=selectModel.get(selectedIndices[i]);	//get the value from the model at this index
			}
			return selectedValues;	//return the selected values
		}
	}

	/**Sets the selected indices.
	Invalid and duplicate indices will be ignored.
	@param selectModel The model containing the values to select.
	@param indices The indices to select.
	@see #setSelectedValues(ListSelectModel, V[])
	@see #canSelectIndex(ListSelectModel, int)
	@see #addSelectedIndex(ListSelectModel, int)
	*/
	public void setSelectedIndices(final ListSelectModel<V> selectModel, final int... indices)
	{
		synchronized(selectedIndices)	//don't allow the selection set to be changed while we modify it 
		{
			selectedIndices.clear();	//clear all the values in the selection set
			synchronized(selectModel)	//don't allow the model to be changed while we select the indices, just in case canSelectIndex() wants to look the value up in the model 
			{
				for(int index:indices)	//for each requested index
				{
					addSelectedIndex(selectModel, index);	//add this index to the selection
				}
			}
		}
		fireSelectionChanged(selectModel, null, null);	//indicate that a complex selection change occurred
	}

	/**Sets the selected values.
	If a value occurs more than one time in the model, the first occurrence of the value will be selected.
	Values that do not occur in the select model will be ignored.
	This version calls {@link #setSelectedIndices(ListSelectModel, int[])}
	@param selectModel The model containing the values to select.
	@param values The values to select.
	@see #setSelectedIndices(ListSelectModel, int[])
	@see #canSelectIndex(ListSelectModel, int)
	*/
	public void setSelectedValues(final ListSelectModel<V> selectModel, final V... values)
	{
		synchronized(selectModel)	//don't allow the model to be changed while we determine the indices 
		{
			final int[] indices=new int[values.length];	//create a new array in which to hold the indices to select
			for(int i=values.length-1; i>=0; --i)	//for each value
			{
				indices[i]=selectModel.indexOf(values[i]);	//get the index of this value, ignoring whether it is valid as its validity will be checked in setSelectedIndices()
			}
			setSelectedIndices(selectModel, indices);	//select the indices
		}
	}

	/**Adds a selection at the given index.
	An invalid index will be ignored.
	@param selectModel The model containing the values to select.
	@param index The index to add as a selection.
	@see #setSelectedIndices(ListSelectModel, int[])
	@see #canSelectIndex(ListSelectModel, int)
	*/
	public void addSelectedIndex(final ListSelectModel<V> selectModel, final int index)
	{
		synchronized(selectedIndices)	//don't allow the selection set to be changed while we modify it 
		{
			if(canSelectIndex(selectModel, index))	//if we can select this index
			{
				selectedIndices.add(new Integer(index));	//add this selection to the set
			}
		}
	}

	/**Removes a selection at the given index.
	An invalid index will be ignored.
	@param selectModel The model containing the values to select.
	@param index The index to remove as a selection.
	@see #setSelectedIndices(ListSelectModel, int[])
	*/
	public void removeSelectedIndex(final ListSelectModel<V> selectModel, final int index)
	{
		synchronized(selectedIndices)	//don't allow the selection set to be changed while we modify it 
		{
			//TODO add removal checks, and call the removal check method when setting new selections across the board
			selectedIndices.remove(new Integer(index));	//remove this selection from the set
		}
	}

	/**Determines whether the provided index can be added to the selected indices.
	This method ensures the indicated index is within the range of the provided model.
	@param selectModel The model containing the values to select.
	@param index The index to be selected.
	@return <code>true</code> if the provided index can be added to the selected indices.
	*/
	protected boolean canSelectIndex(final ListSelectModel<V> selectModel, final int index)
	{
		return index>=0 && index<selectModel.size();	//make sure the index is within the allowed range
	}

	/**Called when a list is modified.
	@param listEvent The event indicating the source of the event and the list modifications.
	*/
	public void listModified(final ListEvent<ListSelectModel<V>, V> listEvent)	//TODO fire selection change events if we need to
	{
		final ListSelectModel<V> selectModel=listEvent.getSource();	//get the source of the event
		final int index=listEvent.getIndex();	//get the modified index, if any
		final V addedElement=listEvent.getAddedElement();	//get the added element, if any
		final V removedElement=listEvent.getRemovedElement();	//get the removed element, if any
		if(index>=0 && addedElement==null && removedElement!=null)	//if a single element was removed and not replaced
		{
			removeSelectedIndex(selectModel, index);	//make sure the removed index is not selected, as there's a different (or no) value there, now
		}
		else if(index<0 || (addedElement==null && removedElement==null))	//if we don't have enough information about what exactly happened
		{
			setSelectedIndices(selectModel);	//clear all selections, as we don't know which values were added or removed
		}
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

	/**Fires an event to all registered selection listeners indicating the selection changed.
	@param selectModel The model that is the source of the event.
	@param addedIndex The index that was added to the selection, or <code>null</code> if no index was added or it is unknown whether or which indices were added.
	@param removedIndex The index that was removed from the list, or <code>null</code> if no index was removed or it is unknown whether or which indices were removed.
	@see ListSelectionListener
	@see ListSelectionEvent
	*/
	protected void fireSelectionChanged(final ListSelectModel<V> selectModel, final Integer addedIndex, final Integer removedIndex)
	{
//TODO del; the event needs to be reported to the session unconditionally		if(getEventListenerManager().hasListeners(ListSelectionListener.class))	//if there are appropriate listeners registered
		{
			final ListSelectionEvent<V> selectionEvent=new ListSelectionEvent<V>(selectModel.getSession(), selectModel, addedIndex, removedIndex);	//create a new event
			selectModel.getSession().queueModelEvent(new PostponedListSelectionEvent<V>(getEventListenerManager(), selectionEvent));	//tell the Guise session to queue the event
		}
	}
}
