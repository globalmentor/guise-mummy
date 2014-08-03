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

package com.guiseframework.event;

/**
 * An event indicating a set has been modified. If a single element was replaced both an added and removed element will be provided. If neither an added nor a
 * removed element are provided, the event represents a general set modification.
 * @param <E> The type of elements contained in the set.
 * @author Garret Wilson
 */
public class SetEvent<E> extends CollectionEvent<E> {

	/**
	 * Source constructor for general set modification.
	 * @param session The Guise session in which this event was generated.
	 * @param source The object on which the event initially occurred.
	 * @throws NullPointerException if the given source is <code>null</code>.
	 */
	public SetEvent(final Object source) {
		this(source, null, null); //construct the class with no known modification values
	}

	/**
	 * Source constructor for an added and/or removed element.
	 * @param source The object on which the event initially occurred.
	 * @param addedElement The element that was added to the set, or <code>null</code> if no element was added or it is unknown whether or which elements were
	 *          added.
	 * @param removedElement The element that was removed from the set, or <code>null</code> if no element was removed or it is unknown whether or which elements
	 *          were removed.
	 * @throws NullPointerException if the given source is <code>null</code>.
	 */
	public SetEvent(final Object source, final E addedElement, final E removedElement) {
		super(source, addedElement, removedElement); //construct the parent class
	}
}
