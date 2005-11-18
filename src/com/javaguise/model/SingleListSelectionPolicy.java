package com.javaguise.model;

import java.util.Set;

/**An implementation of a selection strategy for a select model allowing only a single selection at a time.
This class is marked final because it demarcates certain selection semantics that, if they can be assumed, may be offloaded to a component's view in certain circumstances. 
This class is thread-safe, and assumes that the corresponding select model is thread-safe, synchronized on itself.
@param <V> The type of values contained in the select model.
@author Garret Wilson
@see ListSelectModel
*/
public final class SingleListSelectionPolicy<V> extends AbstractListSelectionPolicy<V>
{
	/**Determines which requested indices may be set as the selection.
	This implementation only allows the first index to be set.
	@param selectModel The model containing the values to be selected.
	@param indices The requested indices to set as the selection.
	@return The indices that can be set as the selection.
	*/
	public int[] getSetSelectedIndices(final ListSelectModel<V> selectModel, final int[] indices)
	{
		return indices.length<=1 ? indices : new int[]{indices[0]};	//if there is more than one index requested, only use the first
	}

	/**Determines which requested indices may be added to the selection.
	This implementation allows the addition of all requested indices.
	@param selectModel The model containing the values to be selected.
	@param indices The requested indices to add to the selection.
	@return The indices that can be added to the selection.
	*/
	public int[] getAddSelectedIndices(final ListSelectModel<V> selectModel, final int[] indices)
	{
		if(selectModel.isEmpty())	//if the model is empty
		{
			return indices.length<=1 ? indices : new int[]{indices[0]};	//allow at most one index to be set			
		}
		else	//if the model isn't empty
		{
			return new int[]{};	//don't allow anything to be added
		}
	}

	/**Determines whether the provided index can be added to the selected indices.
	This method does default validations and then ensures that only one item is selected at any given time.
	@param selectModel The model containing the values to select.
	@param index The index to be selected.
	@return <code>true</code> if the provided index is valid and no other indices are selected.
	*/
/*TODO del when works
	protected boolean canSelectIndex(final ListSelectModel<V> selectModel, final int index)
	{
		final Set<Integer> selectedIndices=getSelectedIndices();	//get the set of selected indices
		synchronized(selectedIndices)	//don't allow anyone to alter the selected indices while we check its size and contents
		{
			return super.canSelectIndex(selectModel, index)	//do default checks
					&& (selectedIndices.isEmpty() || (selectedIndices.size()==1 && selectedIndices.iterator().next().intValue()==index));	//only allow this selection to be added if there are no selections besides this one
		}
	}
*/
}
