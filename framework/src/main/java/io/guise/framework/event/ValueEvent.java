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

package io.guise.framework.event;

/**
 * An event reporting a value.
 * @param <V> The type of value to be reported.
 * @author Garret Wilson
 */
public class ValueEvent<V> extends AbstractGuiseEvent {

	/** The value being reported. */
	private final V value;

	/** @return The value being reported. */
	public V getValue() {
		return value;
	}

	/**
	 * Source and value constructor.
	 * @param source The object on which the event initially occurred.
	 * @param value The value being reported.
	 * @throws NullPointerException if the given source is <code>null</code>.
	 */
	public ValueEvent(final Object source, final V value) {
		super(source); //construct the parent class
		this.value = value; //save the value
	}
}
