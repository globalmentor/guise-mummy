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
 * An object that allows the registration of action listeners.
 * @author Garret Wilson
 */
public interface ActionListenable {

	/**
	 * Adds an action listener.
	 * @param actionListener The action listener to add.
	 */
	public void addActionListener(final ActionListener actionListener);

	/**
	 * Removes an action listener.
	 * @param actionListener The action listener to remove.
	 */
	public void removeActionListener(final ActionListener actionListener);

}
