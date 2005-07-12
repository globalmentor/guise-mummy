package com.javaguise.model;

import java.util.Set;

/**An implementation of a selection strategy for a select model allowing only a single selection at a time.
This class is marked final because it demarcates certain selection semantics that, if they can be assumed, may be offloaded to a component's view in certain circumstances. 
This class is thread-safe, and assumes that the corresponding select model is thread-safe, synchronized on itself.
@param <V> The type of values contained in the select model.
@author Garret Wilson
@see ListSelectModel
*/
public final class SingleListSelectionStrategy<V> extends AbstractListSelectionStrategy<V>
{
	/**Determines whether the provided index can be added to the selected indices.
	This method does default validations and then ensures that only one item is selected at any given time.
	@param selectModel The model containing the values to select.
	@param index The index to be selected.
	@return <code>true</code> if the provided index is valid and no other indices are selected.
	*/
	protected boolean canSelectIndex(final ListSelectModel<V> selectModel, final int index)
	{
		final Set<Integer> selectedIndices=getSelectedIndices();	//get the set of selected indices
		synchronized(selectedIndices)	//don't allow anyone to alter the selected indices while we check its size and contents
		{
			return super.canSelectIndex(selectModel, index)	//do default checks
					&& (selectedIndices.isEmpty() || (selectedIndices.size()==1 && selectedIndices.iterator().next().intValue()==index));	//only allow this selection to be added if there are no selections besides this one
		}
	}
}
