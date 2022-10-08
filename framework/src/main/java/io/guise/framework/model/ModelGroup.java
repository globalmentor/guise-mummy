/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.model;

/**
 * A group of similar models for providing such functions as communication or mutual exclusion.
 * @param <M> The type of model contained in the group.
 * @author Garret Wilson.
 */
public interface ModelGroup<M extends Model> {

	/**
	 * Determines whether this group contains the given model.
	 * @param model The model being checked for group inclusion.
	 * @return <code>true</code> if the model is contained in this group, else <code>false</code>.
	 * @throws NullPointerException if the given model is <code>null</code>.
	 */
	public boolean contains(final Model model);

	/**
	 * Adds a model to the group. If the model is already included in the group, no action occurs.
	 * @param model The model to add to the group.
	 * @throws NullPointerException if the given model is <code>null</code>.
	 */
	public void add(final M model);

	/**
	 * Removes a model from the group. If the model is not included in this group, no action occurs.
	 * @param model The model to remove from the group.
	 * @throws NullPointerException if the given model is <code>null</code>.
	 */
	public void remove(final M model);

}
