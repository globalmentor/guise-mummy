/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guiseframework.model;

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

}
