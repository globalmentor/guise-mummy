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
 * An event providing information on a keyboard key release.
 * @author Garret Wilson
 */
public class KeyReleaseEvent extends AbstractKeyboardEvent {

	/**
	 * Source constructor.
	 * @param source The object on which the event initially occurred.
	 * @param key The key that was pressed.
	 * @param keys The keys that were pressed when this event was generated.
	 * @throws NullPointerException if the given source, key, and/or keys is <code>null</code>.
	 */
	public KeyReleaseEvent(final Object source, final Key key, final Key... keys) {
		super(source, key, keys); //construct the parent class
	}

	/**
	 * Copy constructor that specifies a different source.
	 * @param source The object on which the event initially occurred.
	 * @param keyReleaseEvent The event the properties of which will be copied.
	 * @throws NullPointerException if the given source, key, and/or event is <code>null</code>.
	 */
	public KeyReleaseEvent(final Object source, final KeyReleaseEvent keyReleaseEvent) {
		this(source, keyReleaseEvent.getKey(), keyReleaseEvent.getKeys().toArray(new Key[keyReleaseEvent.getKeys().size()])); //construct the class with the specified source		
	}

	@Override
	public KeystrokeInput getInput() {
		return null; //key presses don't produce input
	}

}
