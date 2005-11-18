package com.javaguise.model;

import java.util.List;

import com.javaguise.event.ListListener;
import com.javaguise.event.ListSelectionListener;
import com.javaguise.validator.ValidationException;

/**A model for selecting one or more values from a list.
The model must be thread-safe, synchronized on itself. Any iteration over values should include synchronization on the instance of this interface.
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
	public int[] getSelectedIndices();
	
	/**Sets the selected indices.
	Invalid and duplicate indices will be ignored.
	@param indices The indices to select.
	@exception ValidationException if the provided value is not valid.
	@see ListSelectionPolicy#getSetSelectedIndices(ListSelectModel, int[])
	@see #setSelectedValues(V[])
	@see #addSelectedIndices(int...)
	*/
	public void setSelectedIndices(int... indices) throws ValidationException;
	
	/**Adds a selection at the given indices.
	Any invalid indices will be ignored.
	@param indices The indices to add to the selection.
	@exception ValidationException if the provided value is not valid.
	@see ListSelectionPolicy#getAddSelectedIndices(ListSelectModel, int[])
	@see #setSelectedIndices(int[])
	*/
	public void addSelectedIndices(int... indices) throws ValidationException;
	
	/**Removes a selection at the given indices.
	Any invalid indices will be ignored.
	@param indices The indices to remove from the selection.
	@exception ValidationException if the provided value is not valid.
	@see ListSelectionPolicy#getRemoveSelectedIndices(ListSelectModel, int[])
	@see #setSelectedIndices(int[])
	*/
	public void removeSelectedIndices(int... indices) throws ValidationException;
	
	/**Determines the selected value.
	If more than one value is selected, the lead selected value will be returned.
	@return The value currently selected, or <code>null</code> if no value is currently selected.
	@see #getSelectedIndex()
	*/
	public V getSelectedValue();
	
	/**Determines the selected values.
	This method delegates to the selection strategy.
	@return The values currently selected.
	@see #getSelectedIndices()
	*/
	public V[] getSelectedValues();
	
	/**Sets the selected values.
	If a value occurs more than one time in the model, the first occurrence of the value will be selected.
	Values that do not occur in the select model will be ignored.
	This method delegates to the selection strategy.
	@param values The values to select.
	@exception ValidationException if the provided value is not valid.
	@see #setSelectedIndices(int[])
	*/
	public void setSelectedValues(final V... values) throws ValidationException;
	
	/**Adds a list listener.
	@param listListener The list listener to add.
	*/
	public void addListListener(final ListListener<ListSelectModel<V>, V> listListener);

	/**Removes a list listener.
	@param listListener The list listener to remove.
	*/
	public void removeListListener(final ListListener<ListSelectModel<V>, V> listListener);

	/**Adds a list selection listener.
	@param selectionListener The selection listener to add.
	*/
	public void addListSelectionListener(final ListSelectionListener<V> selectionListener);

	/**Removes a list selection listener.
	@param selectionListener The selection listener to remove.
	*/
	public void removeListSelectionListener(final ListSelectionListener<V> selectionListener);

}
