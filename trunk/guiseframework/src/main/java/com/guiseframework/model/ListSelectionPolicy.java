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

/**A selection strategy for a select model.
@param <V> The type of values contained in the select model.
@author Garret Wilson
@see ListSelectModel
*/
public interface ListSelectionPolicy<V>
{

	/**Determines which requested indices may be set as the selection.
	This implementation allows the setting of all requested indices.
	@param selectModel The model containing the values to be selected.
	@param indices The requested indices to set as the selection.
	@return The indices that can be set as the selection.
	*/
	public int[] getSetSelectedIndices(final ListSelectModel<V> selectModel, final int[] indices);

	/**Determines which requested indices may be added to the selection.
	This implementation allows the addition of all requested indices.
	@param selectModel The model containing the values to be selected.
	@param indices The requested indices to add to the selection.
	@return The indices that can be added to the selection.
	*/
	public int[] getAddSelectedIndices(final ListSelectModel<V> selectModel, final int[] indices);

	/**Determines which requested indices may be removed from the selection.
	This implementation allows the removal of all requested indices.
	@param selectModel The model containing the values to be removed.
	@param indices The requested indices to remove to the selection.
	@return The indices that can be removed from the selection.
	*/
	public int[] getRemoveSelectedIndices(final ListSelectModel<V> selectModel, final int[] indices);
}
