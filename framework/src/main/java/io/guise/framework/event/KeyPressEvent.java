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

import io.guise.framework.input.Key;
import io.guise.framework.input.KeystrokeInput;

/**
 * An event providing information on a keyboard key press.
 * @author Garret Wilson
 */
public class KeyPressEvent extends AbstractKeyboardEvent {

	/**
	 * Source constructor.
	 * @param source The object on which the event initially occurred.
	 * @param key The key that was pressed.
	 * @param keys The keys that were pressed when this event was generated.
	 * @throws NullPointerException if the given source, key, and/or keys is <code>null</code>.
	 */
	public KeyPressEvent(final Object source, final Key key, final Key... keys) {
		super(source, key, keys); //construct the parent class
	}

	/**
	 * Keystroke input constructor.
	 * @param source The object on which the event initially occurred.
	 * @param keystrokeInput The keystroke input the properties of which will be copied.
	 * @throws NullPointerException if the given source and/or input is <code>null</code>.
	 */
	public KeyPressEvent(final Object source, final KeystrokeInput keystrokeInput) {
		this(source, keystrokeInput.getKey(), keystrokeInput.getKeys().toArray(new Key[keystrokeInput.getKeys().size()])); //construct the class with the specified source		
	}

	/**
	 * Copy constructor that specifies a different source.
	 * @param source The object on which the event initially occurred.
	 * @param keyPressEvent The event the properties of which will be copied.
	 * @throws NullPointerException if the given source, key, and/or event is <code>null</code>.
	 */
	public KeyPressEvent(final Object source, final KeyPressEvent keyPressEvent) {
		this(source, keyPressEvent.getKey(), keyPressEvent.getKeys().toArray(new Key[keyPressEvent.getKeys().size()])); //construct the class with the specified source		
	}

	@Override
	public KeystrokeInput getInput() {
		return new KeystrokeInput(getKey(), getKeys().toArray(new Key[getKeys().size()])); //return new key input based upon this event
	}
}
