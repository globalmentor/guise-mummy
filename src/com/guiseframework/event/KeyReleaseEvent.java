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

import com.guiseframework.input.Key;
import com.guiseframework.input.KeystrokeInput;

/**An event providing information on a keyboard key release.
@author Garret Wilson
*/
public class KeyReleaseEvent extends AbstractKeyboardEvent
{

	/**Source constructor.
	@param source The object on which the event initially occurred.
	@parma key The key that was pressed.
	@param keys The keys that were pressed when this event was generated.
	@exception NullPointerException if the given source, key, and/or keys is <code>null</code>.
	*/
	public KeyReleaseEvent(final Object source, final Key key, final Key... keys)
	{
		super(source, key, keys);	//construct the parent class
	}

	/**Copy constructor that specifies a different source.
	@param source The object on which the event initially occurred.
	@param keyPressEvent The event the properties of which will be copied.
	@exception NullPointerException if the given source, key, and/or event is <code>null</code>.
	*/
	public KeyReleaseEvent(final Object source, final KeyReleaseEvent keyReleaseEvent)
	{
		this(source, keyReleaseEvent.getKey(), keyReleaseEvent.getKeys().toArray(new Key[keyReleaseEvent.getKeys().size()]));	//construct the class with the specified source		
	}

	/**Returns the key input associated with this event.
	@return The input associated with this event, or <code>null</code> if there is no input associated with this event.
	This version returns <code>null</code>, as a key release, unlike a key press, produces no input.
	@see KeyPressEvent#getInput()
	*/
	public KeystrokeInput getInput()
	{
		return null;	//key presses don't produce input
	}

}
