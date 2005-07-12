package com.javaguise.model;

import com.javaguise.event.ListListener;
import com.javaguise.event.ListSelectionListener;

/**A selection strategy for a select model.
@param <V> The type of values contained in the select model.
@author Garret Wilson
@see ListSelectModel
*/
public interface ListSelectionStrategy<V> extends ListListener<ListSelectModel<V>, V>
{

	/**Determines the selected indices.
	@param selectModel The model containing the selected values.
	@return The indices currently selected.
	@see #getSelectedValues(ListSelectModel)
	*/
	public int[] getSelectedIndices(final ListSelectModel<V> selectModel);

	/**Determines the selected values.
	@param selectModel The model containing the selected values.
	@return The values currently selected.
	@see #getSelectedIndices(ListSelectModel)
	*/
	public V[] getSelectedValues(final ListSelectModel<V> selectModel);

	/**Determines the selected index.
	If more than one index is selected, the lead selected index will be returned.
	@param selectModel The model containing the selected values.
	@return The index currently selected, or -1 if no index is selected.
	@see #getSelectedValue(ListSelectModel)
	*/
	public int getSelectedIndex(final ListSelectModel<V> selectModel);

	/**Sets the selected indices.
	Invalid and duplicate indices will be ignored.
	@param selectModel The model containing the values to select.
	@param indices The indices to select.
	@see #setSelectedValues(ListSelectModel, V[])
	@see #addSelectedIndex(ListSelectModel, int)
	*/
	public void setSelectedIndices(final ListSelectModel<V> selectModel, final int... indices);

	/**Determines the selected value.
	If more than one value is selected, the lead selected value will be returned.
	@param selectModel The model containing the selected values.
	@return The value currently selected, or <code>null</code> if no value is currently selected.
	@see #getSelectedIndex(ListSelectModel)
	*/
	public V getSelectedValue(final ListSelectModel<V> selectModel);

	/**Sets the selected values.
	If a value occurs more than one time in the model, the first occurrence of the value will be selected.
	Values that do not occur in the select model will be ignored.
	@param selectModel The model containing the values to select.
	@param values The values to select.
	@see #setSelectedIndices(ListSelectModel, int[])
	*/
	public void setSelectedValues(final ListSelectModel<V> selectModel, final V... values);

	/**Adds a selection at the given index.
	An invalid index will be ignored.
	@param selectModel The model containing the values to select.
	@param index The index to add as a selection.
	@see #setSelectedIndices(ListSelectModel, int[])
	*/
	public void addSelectedIndex(final ListSelectModel<V> selectModel, final int index);

	/**Removes a selection at the given index.
	An invalid index will be ignored.
	@param selectModel The model containing the values to select.
	@param index The index to remove as a selection.
	@see #setSelectedIndices(ListSelectModel, int[])
	*/
	public void removeSelectedIndex(final ListSelectModel<V> selectModel, final int index);

	/**Adds a list selection listener.
	@param selectionListener The selection listener to add.
	*/
	public void addListSelectionListener(final ListSelectionListener<V> selectionListener);

	/**Removes a list selection listener.
	@param selectionListener The selection listener to remove.
	*/
	public void removeListSelectionListener(final ListSelectionListener<V> selectionListener);
}
