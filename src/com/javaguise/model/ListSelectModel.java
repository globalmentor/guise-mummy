package com.javaguise.model;

import java.util.List;

import com.javaguise.event.ListListener;
import com.javaguise.validator.ValidationException;

/**A model for selecting one or more values from a list.
The model must be thread-safe, synchronized on itself. Any iteration over values should include synchronization on the instance of this interface.
@param <V> The type of values contained in the model.
@author Garret Wilson
*/
public interface ListSelectModel<V> extends SelectModel<V>, List<V>
{

	/**Replaces the first occurrence in the of the given value with its replacement.
	This method ensures that another thread does not change the model while the search and replace operation occurs.
	@param oldValue The value for which to search.
	@param newValue The replacement value.
	@return Whether the operation resulted in a modification of the model.
	*/
	public boolean replace(final V oldValue, final V newValue);

	/**@return The selection strategy for this model.*/
	public ListSelectionStrategy<V> getSelectionStrategy();

	/**Determines the selected index.
	This method delegates to the selection strategy.
	If more than one index is selected, the lead selected index will be returned.
	@return The index currently selected, or -1 if no index is selected.
	@see #getSelectedValue()
	*/
	public int getSelectedIndex();

	/**Determines the selected indices.
	This method delegates to the selection strategy.
	@return The indices currently selected.
	@see #getSelectedValues()
	*/
	public int[] getSelectedIndices();

	/**Sets the selected indices.
	Invalid and duplicate indices will be ignored.
	This method delegates to the selection strategy.
	@param indices The indices to select.
	@exception ValidationException if the provided value is not valid.
	@see #setSelectedValues(V[])
	@see #addSelectedIndex(int)
	*/
	public void setSelectedIndices(final int... indices) throws ValidationException;

	/**Adds a selection at the given index.
	An invalid index will be ignored.
	This method delegates to the selection strategy.
	@param index The index to add as a selection.
	@exception ValidationException if the provided value is not valid.
	@see #setSelectedIndices(int[])
	*/
	public void addSelectedIndex(final int index) throws ValidationException;

	/**Removes a selection at the given index.
	An invalid index will be ignored.
	This method delegates to the selection strategy.
	@param index The index to remove as a selection.
	@exception ValidationException if the provided value is not valid.
	@see #setSelectedIndices(int[])
	*/
	public void removeSelectedIndex(final int index) throws ValidationException;
	
	/**Adds a list listener.
	@param listListener The list listener to add.
	*/
	public void addListListener(final ListListener<ListSelectModel<V>, V> listListener);

	/**Removes a list listener.
	@param listListener The list listener to remove.
	*/
	public void removeListListener(final ListListener<ListSelectModel<V>, V> listListener);

}
