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

import java.util.Set;

import com.guiseframework.input.GestureInput;
import com.guiseframework.input.Key;

/**
 * An event providing information on input from a user gesture.
 * @author Garret Wilson
 */
public interface GestureInputEvent extends InputEvent {

	/** @return The keys that were pressed when this event was generated. */
	public Set<Key> getKeys();

	/**
	 * Determines whether an Alt key was pressed when this event was generated.
	 * @return <code>true</code> if one of the Alt keys were pressed when this event was generated.
	 * @see #getKeys()
	 */
	public boolean hasAltKey();

	/**
	 * Determines whether a Control key was pressed when this event was generated.
	 * @return <code>true</code> if one of the Control keys were pressed when this event was generated.
	 * @see #getKeys()
	 */
	public boolean hasControlKey();

	/**
	 * Determines whether a Shift key was pressed when this event was generated.
	 * @return <code>true</code> if one of the Shift keys were pressed when this event was generated.
	 * @see #getKeys()
	 */
	public boolean hasShiftKey();

	/** @return The input associated with this event, or <code>null</code> if there is no input associated with this event. */
	public GestureInput getInput();

}
