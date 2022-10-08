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

/**
 * An object that listens for a change in mode.
 * @author Garret Wilson
 */
public interface ModalListener extends GuiseEventListener {

	/**
	 * Called when the mode begins.
	 * @param modalEvent The event indicating the object beginning its mode.
	 */
	public void modalBegan(final ModalEvent modalEvent);

	/**
	 * Called when the mode ends.
	 * @param modalEvent The event indicating the object ending its mode.
	 */
	public void modalEnded(final ModalEvent modalEvent);

}
