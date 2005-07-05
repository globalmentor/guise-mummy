package com.garretwilson.guise.model;

import com.garretwilson.guise.event.ListListener;

/**A selection strategy for a select model.
@param <V> The type of values contained in the select model.
@author Garret Wilson
@see SelectModel
*/
public interface SelectionStrategy<V> extends ListListener<SelectModel<V>, V>
{

	/**Determines the selected indices.
	@param selectModel The model containing the selected values.
	@return The indices currently selected.
	@see #getSelectedValues(SelectModel)
	*/
	public int[] getSelectedIndices(final SelectModel<V> selectModel);

	/**Determines the selected values.
	@param selectModel The model containing the selected values.
	@return The values currently selected.
	@see #getSelectedIndices(SelectModel)
	*/
	public V[] getSelectedValues(final SelectModel<V> selectModel);

	/**Sets the selected indices.
	Invalid and duplicate indices will be ignored.
	@param selectModel The model containing the values to select.
	@param indices The indices to select.
	@see #setSelectedValues(SelectModel, V[])
	@see #addSelectedIndex(SelectModel, int)
	*/
	public void setSelectedIndices(final SelectModel<V> selectModel, final int... indices);

	/**Sets the selected values.
	If a value occurs more than one time in the model, the first occurrence of the value will be selected.
	Values that do not occur in the select model will be ignored.
	@param selectModel The model containing the values to select.
	@param values The values to select.
	@see #setSelectedIndices(SelectModel, int[])
	*/
	public void setSelectedValues(final SelectModel<V> selectModel, final V... values);

	/**Adds a selection at the given index.
	An invalid index will be ignored.
	@param selectModel The model containing the values to select.
	@param index The index to add as a selection.
	@see #setSelectedIndices(SelectModel, int[])
	*/
	public void addSelectedIndex(final SelectModel<V> selectModel, final int index);

	/**Removes a selection at the given index.
	An invalid index will be ignored.
	@param selectModel The model containing the values to select.
	@param index The index to remove as a selection.
	@see #setSelectedIndices(SelectModel, int[])
	*/
	public void removeSelectedIndex(final SelectModel<V> selectModel, final int index);

}
