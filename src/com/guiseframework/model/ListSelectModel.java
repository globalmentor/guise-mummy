package com.guiseframework.model;

import java.util.List;

import com.guiseframework.event.ListListener;
import com.guiseframework.event.ListSelectionListener;
import com.guiseframework.validator.ValidationException;

/**A model for selecting one or more values from a list.
The model must be thread-safe, synchronized on itself. Any iteration over values should include synchronization on the instance of this interface.
When the selection is changed, a {@link com.guiseframework.event.ListSelectionEvent} is fired.
When the state of a value (besides its selection, such as its enabled status) changes, a {@link com.garretwilson.beans.PropertyValueChangeEvent} event is fired referencing the value the state of which has changed.
@param <V> The type of values contained in the model.
@author Garret Wilson
*/
public interface ListSelectModel<V> extends SelectModel<V>, List<V>
{

	/**Replaces the first occurrence of the given value with its replacement.
	This method ensures that another thread does not change the model while the search and replace operation occurs.
	@param oldValue The value for which to search.
	@param newValue The replacement value.
	@return Whether the operation resulted in a modification of the model.
	*/
	public boolean replace(final V oldValue, final V newValue);

	/**@return The selection policy for this model.*/
	public ListSelectionPolicy<V> getSelectionPolicy();

	/**Determines the selected index.
	If more than one index is selected, the lead selected index will be returned.
	@return The index currently selected, or -1 if no index is selected.
	@see #getSelectedValue()
	*/
	public int getSelectedIndex();
	
	/**Determines the selected indices.
	@return The indices currently selected.
	@see #getSelectedValues()
	*/
	public int[] getSelectedIndexes();
	
	/**Sets the selected indices.
	Invalid and duplicate indices will be ignored.
	@param indexes The indices to select.
	@exception ValidationException if the provided value is not valid.
	@see ListSelectionPolicy#getSetSelectedIndices(ListSelectModel, int[])
	@see #setSelectedValues(V[])
	@see #addSelectedIndexes(int...)
	*/
	public void setSelectedIndexes(int... indexes) throws ValidationException;
	
	/**Adds a selection at the given indices.
	Any invalid indices will be ignored.
	@param indexes The indices to add to the selection.
	@exception ValidationException if the provided value is not valid.
	@see ListSelectionPolicy#getAddSelectedIndices(ListSelectModel, int[])
	@see #setSelectedIndexes(int[])
	*/
	public void addSelectedIndexes(int... indexes) throws ValidationException;
	
	/**Removes a selection at the given indices.
	Any invalid indices will be ignored.
	@param indexes The indices to remove from the selection.
	@exception ValidationException if the provided value is not valid.
	@see ListSelectionPolicy#getRemoveSelectedIndices(ListSelectModel, int[])
	@see #setSelectedIndexes(int[])
	*/
	public void removeSelectedIndexes(int... indexes) throws ValidationException;
	
	/**Determines the selected value.
	If more than one value is selected, the lead selected value will be returned.
	@return The value currently selected, or <code>null</code> if no value is currently selected.
	@see #getSelectedIndex()
	*/
	public V getSelectedValue();
	
	/**Determines the selected values.
	@return The values currently selected.
	@see #getSelectedIndexes()
	*/
	public V[] getSelectedValues();
	
	/**Sets the selected values.
	If a value occurs more than one time in the model, the first occurrence of the value will be selected.
	Values that do not occur in the select model will be ignored.
	@param values The values to select.
	@exception ValidationException if the provided value is not valid.
	@see #setSelectedIndexes(int[])
	*/
	public void setSelectedValues(final V... values) throws ValidationException;

	/**Determines the enabled status of the first occurrence of a given value.
	@param value The value for which the enabled status is to be determined.
	@return <code>true</code> if the value is enabled, else <code>false</code>.
	@exception IndexOutOfBoundsException if the given value does not occur in the model.
	*/
	public boolean isValueEnabled(final V value);

	/**Sets the enabled status of the first occurrence of a given value.
	This is a bound value state property.
	@param value The value to enable or disable.
	@param newEnabled Whether the value should be enabled.
	@see ValuePropertyChangeEvent
	@see ControlModel#ENABLED_PROPERTY
	*/
	public void setValueEnabled(final V value, final boolean newEnabled); 

	/**Determines the enabled status of a given index.
	@param index The index of the value for which the enabled status is to be determined.
	@return <code>true</code> if the value at the given index is enabled, else <code>false</code>.
	*/
	public boolean isIndexEnabled(final int index);
	
	/**Sets the enabled status of a given index.
	This is a bound value state property.
	@param index The index of the value to enable or disable.
	@param newEnabled Whether the value at the given index should be enabled.
	@see ValuePropertyChangeEvent
	@see ControlModel#ENABLED_PROPERTY
	@exception IndexOutOfBoundsException if the given index is not within the range of the list.
	*/
	public void setIndexEnabled(final int index, final boolean newEnabled); 

	/**Adds a list listener.
	@param listListener The list listener to add.
	*/
	public void addListListener(final ListListener<V> listListener);

	/**Removes a list listener.
	@param listListener The list listener to remove.
	*/
	public void removeListListener(final ListListener<V> listListener);

	/**Adds a list selection listener.
	@param selectionListener The selection listener to add.
	*/
	public void addListSelectionListener(final ListSelectionListener<V> selectionListener);

	/**Removes a list selection listener.
	@param selectionListener The selection listener to remove.
	*/
	public void removeListSelectionListener(final ListSelectionListener<V> selectionListener);

}
