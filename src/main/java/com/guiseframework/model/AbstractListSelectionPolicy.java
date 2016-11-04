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

/**
 * An abstract implementation of a list selection strategy for a list select model. This class is thread-safe, and assumes that the corresponding select model
 * is thread-safe, synchronized on itself.
 * @param <V> The type of values contained in the select model.
 * @author Garret Wilson
 * @see ListSelectModel
 */
public abstract class AbstractListSelectionPolicy<V> implements ListSelectionPolicy<V> {

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation allows the setting of all requested indices.
	 * </p>
	 */
	@Override
	public int[] getSetSelectedIndices(final ListSelectModel<V> selectModel, final int[] indices) {
		return indices; //allow all the requested indices to be set as the selection
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation allows the addition of all requested indices.
	 * </p>
	 */
	@Override
	public int[] getAddSelectedIndices(final ListSelectModel<V> selectModel, final int[] indices) {
		return indices; //allow all the requested indices to be added to the selection
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation allows the removal of all requested indices.
	 * </p>
	 */
	@Override
	public int[] getRemoveSelectedIndices(final ListSelectModel<V> selectModel, final int[] indices) {
		return indices; //allow all the requested indices to be removed from the selection
	}
}
