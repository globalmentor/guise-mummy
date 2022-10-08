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

package io.guise.framework.event;

import io.guise.framework.model.ListSelectModel;

/**
 * An event indicating the list selection has been modified. An added or removed element represents an added or removed index of the selection. If neither an
 * added nor a removed element are provided, the event represents a general set modification.
 * @param <V> The type of values selected.
 * @author Garret Wilson
 */
public class ListSelectionEvent<V> extends SetEvent<Integer> {

	@SuppressWarnings("unchecked")
	@Override
	public ListSelectModel<V> getSource() {
		return (ListSelectModel<V>)super.getSource(); //cast the event to the appropriate type
	}

	/**
	 * Source constructor for general selection modification.
	 * @param source The object on which the event initially occurred.
	 * @throws NullPointerException if the given source is <code>null</code>.
	 */
	public ListSelectionEvent(final ListSelectModel<V> source) {
		this(source, null, null); //construct the class with no known modification values
	}

	/**
	 * Source constructor for an added and/or removed element.
	 * @param source The object on which the event initially occurred.
	 * @param addedElement The index that was added to the selection, or <code>null</code> if no index was added or it is unknown whether or which indices were
	 *          added.
	 * @param removedElement The index that was removed from the selection, or <code>null</code> if no index was removed or it is unknown whether or which indices
	 *          were removed.
	 * @throws NullPointerException if the given source is <code>null</code>.
	 */
	public ListSelectionEvent(final ListSelectModel<V> source, final Integer addedElement, final Integer removedElement) {
		super(source, addedElement, removedElement); //construct the parent class
	}
}
