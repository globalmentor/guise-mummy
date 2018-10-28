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

import io.guise.framework.input.Input;

/**
 * An event providing information from input such as a keystroke or a command.
 * @author Garret Wilson
 */
public interface InputEvent extends GuiseEvent {

	/** @return Whether the input associated with this event has been consumed. */
	public boolean isConsumed();

	/**
	 * Consumes the input associated with this event. The event is marked as consumed so that other listeners will be on notice not to consume the input.
	 */
	public void consume();

	/** @return The input associated with this event, or <code>null</code> if there is no input associated with this event. */
	public Input getInput();

}
