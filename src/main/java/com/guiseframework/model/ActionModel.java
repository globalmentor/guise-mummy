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

package com.guiseframework.model;

import com.guiseframework.event.*;

/**
 * A model for a potential action.
 * @author Garret Wilson
 */
public interface ActionModel extends Model, ActionListenable {

	/** @return all registered action listeners. */
	public Iterable<ActionListener> getActionListeners(); //TODO del from interface eventually

	/**
	 * Performs the action with default force and default option. An {@link ActionEvent} is fired to all registered {@link ActionListener}s. This method delegates
	 * to {@link #performAction(int, int)}.
	 */
	public void performAction();

	/**
	 * Performs the action with the given force and option. An {@link ActionEvent} is fired to all registered {@link ActionListener}s.
	 * @param force The zero-based force, such as 0 for no force or 1 for an action initiated by from a mouse single click.
	 * @param option The zero-based option, such as 0 for an event initiated by a mouse left button click or 1 for an event initiated by a mouse right button
	 *          click.
	 */
	public void performAction(final int force, final int option);

}
