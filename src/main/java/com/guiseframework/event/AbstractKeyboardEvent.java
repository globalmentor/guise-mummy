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

import static java.util.Objects.*;

import com.guiseframework.input.Key;

/**
 * An abstract event providing information on a keyboard key event.
 * @author Garret Wilson
 */
public abstract class AbstractKeyboardEvent extends AbstractFocusedGestureInputEvent implements KeyboardEvent {

	/** The key that was pressed. */
	private final Key key;

	@Override
	public Key getKey() {
		return key;
	}

	/**
	 * Source constructor.
	 * @param source The object on which the event initially occurred.
	 * @param key The key that was pressed.
	 * @param keys The keys that were pressed when this event was generated.
	 * @throws NullPointerException if the given source, key, and/or keys is <code>null</code>.
	 */
	public AbstractKeyboardEvent(final Object source, final Key key, final Key... keys) {
		super(source, keys); //construct the parent class
		this.key = requireNonNull(key, "Key cannot be null."); //save the key
	}
}
