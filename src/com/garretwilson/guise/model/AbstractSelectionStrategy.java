package com.garretwilson.guise.model;

import java.util.*;

import com.garretwilson.event.EventListenerManager;
import com.garretwilson.guise.event.ListEvent;
import com.garretwilson.guise.event.ListListener;
import com.garretwilson.guise.event.PostponedListEvent;
import com.garretwilson.guise.event.PostponedSelectionEvent;
import com.garretwilson.guise.event.SelectionEvent;
import com.garretwilson.guise.event.SelectionListener;

import static java.util.Collections.*;
import static com.garretwilson.util.ArrayUtilities.*;

/**An abstract implementation of a selection strategy for a select model.
This class is thread-safe, and assumes that the corresponding select model is thread-safe, synchronized on itself.
@param <V> The type of values contained in the select model.
@author Garret Wilson
@see SelectModel
*/
public abstract class AbstractSelectionStrategy<V> implements SelectionStrategy<V>
{

	/**The object managing event listeners.*/
	private final EventListenerManager eventListenerManager=new EventListenerManager();

		/**@return The object managing event listeners.*/
		protected EventListenerManager getEventListenerManager() {return eventListenerManager;}

	/**The thread-safe sorted set of selected indices on which synchronization can be performed.*/
	private final Set<Integer> selectedIndices=synchronizedSortedSet(new TreeSet<Integer>());

		/**@return The thread-safe sorted set of selected indices on which synchronization can be performed.*/
		protected Set<Integer> getSelectedIndices() {return selectedIndices;}

	/**Determines the selected indices.
	@param selectModel The model containing the selected values.
	@return The indices currently selected.
	@see #getSelectedValues(SelectModel)
	*/
	public int[] getSelectedIndices(final SelectModel<V> selectModel)
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

	/**Determines the selected values.
	@param selectModel The model containing the selected values.
	@return The values currently selected.
	@see #getSelectedIndices(SelectModel)
	*/
	public V[] getSelectedValues(final SelectModel<V> selectModel)
	{
		synchronized(selectModel)	//don't allow the model to be changed while we determine the selections 
		{
			final int[] selectedIndices=getSelectedIndices(selectModel);	//get the selected indices
			final V[] selectedValues=createArray(selectModel.getValueClass(), selectedIndices.length);	//create an array of selected objects
			for(int i=selectedIndices.length; i>=0; --i)	//for each selected index
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
	@see #setSelectedValues(SelectModel, V[])
	@see #canSelectIndex(SelectModel, int)
	@see #addSelectedIndex(SelectModel, int)
	*/
	public void setSelectedIndices(final SelectModel<V> selectModel, final int... indices)
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
	@param selectModel The model containing the values to select.
	@param values The values to select.
	@see #setSelectedIndices(SelectModel, int[])
	@see #canSelectIndex(SelectModel, int)
	*/
	public void setSelectedValues(final SelectModel<V> selectModel, final V... values)
	{
		synchronized(selectModel)	//don't allow the model to be changed while we determine the indices 
		{
			final int[] indices=new int[values.length];	//create a new array in which to hold the indices to select
			for(int i=values.length; i>=0; --i)	//for each value
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
	@see #setSelectedIndices(SelectModel, int[])
	@see #canSelectIndex(SelectModel, int)
	*/
	public void addSelectedIndex(final SelectModel<V> selectModel, final int index)
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
	@see #setSelectedIndices(SelectModel, int[])
	*/
	public void removeSelectedIndex(final SelectModel<V> selectModel, final int index)
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
	protected boolean canSelectIndex(final SelectModel<V> selectModel, final int index)
	{
		return index>=0 && index<selectModel.size();	//make sure the index is within the allowed range
	}

	/**Called when a list is modified.
	@param listEvent The event indicating the source of the event and the list modifications.
	*/
	public void listModified(final ListEvent<SelectModel<V>, V> listEvent)
	{
		final SelectModel<V> selectModel=listEvent.getSource();	//get the source of the event
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

	/**Adds a selection listener.
	@param selectionListener The selection listener to add.
	*/
	public void addSelectionListener(final SelectionListener<V> selectionListener)
	{
		getEventListenerManager().add(SelectionListener.class, selectionListener);	//add the listener
	}

	/**Removes a selection listener.
	@param selectionListener The selection listener to remove.
	*/
	public void removeSelectionListener(final SelectionListener<V> selectionListener)
	{
		getEventListenerManager().remove(SelectionListener.class, selectionListener);	//remove the listener
	}

	/**Fires an event to all registered selection listeners indicating the selection changed.
	@param addedIndex The index that was added to the selection, or <code>null</code> if no index was added or it is unknown whether or which indices were added.
	@param removedIndex The index that was removed from the list, or <code>null</code> if no index was removed or it is unknown whether or which indices were removed.
	@see SelectionListener
	@see SelectionEvent
	*/
	protected void fireSelectionChanged(final SelectModel<V> selectModel, final Integer addedIndex, final Integer removedIndex)
	{
		if(getEventListenerManager().hasListeners(SelectionListener.class))	//if there are appropriate listeners registered
		{
			final SelectionEvent<V> selectionEvent=new SelectionEvent<V>(selectModel.getSession(), selectModel, addedIndex, removedIndex);	//create a new event
			selectModel.getSession().queueModelEvent(new PostponedSelectionEvent<V>(getEventListenerManager(), selectionEvent));	//tell the Guise session to queue the event
		}
	}

}
