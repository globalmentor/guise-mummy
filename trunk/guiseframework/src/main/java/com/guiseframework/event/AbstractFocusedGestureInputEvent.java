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

/**
 * An abstract gesture input event such as a keypress that is directed towards the component with input focus.
 * @author Garret Wilson
 */
public abstract class AbstractFocusedGestureInputEvent extends AbstractGestureInputEvent implements FocusedInputEvent {

	/**
	 * Source constructor.
	 * @param source The object on which the event initially occurred.
	 * @param keys The keys that were pressed when this event was generated.
	 * @throws NullPointerException if the given source and/or keys is <code>null</code>.
	 */
	public AbstractFocusedGestureInputEvent(final Object source, final Key... keys) {
		super(source, keys); //construct the parent class
	}

}
