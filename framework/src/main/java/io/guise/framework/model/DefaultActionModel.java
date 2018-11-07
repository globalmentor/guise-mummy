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

package io.guise.framework.model;

import io.guise.framework.event.*;

/**
 * A default implementation of an action model. If a subclass wants to perform some action, it should override {@link #action(int, int)}.
 * @author Garret Wilson
 */
public class DefaultActionModel extends AbstractModel implements ActionModel {

	@Override
	public void addActionListener(final ActionListener actionListener) {
		getEventListenerManager().add(ActionListener.class, actionListener); //add the listener
	}

	@Override
	public void removeActionListener(final ActionListener actionListener) {
		getEventListenerManager().remove(ActionListener.class, actionListener); //remove the listener
	}

	@Override
	public Iterable<ActionListener> getActionListeners() {
		return getEventListenerManager().getListeners(ActionListener.class); //remove the listener
	}

	@Override
	public void performAction() {
		performAction(1, 0); //fire an event saying that the action has been performed with the default force and option
	}

	@Override
	public void performAction(final int force, final int option) {
		action(force, option); //actually perform the action
		fireActionPerformed(force, option); //fire an event saying that the action has been performed with the given force and option
	}

	/**
	 * Performs whatever is necessary. This method is guaranteed to be called before any action event is fired to any listeners. This version does nothing.
	 * @param force The zero-based force, such as 0 for no force or 1 for an action initiated by from a mouse single click.
	 * @param option The zero-based option, such as 0 for an event initiated by a mouse left button click or 1 for an event initiaged by a mouse right button
	 *          click.
	 */
	protected void action(final int force, final int option) {
	}

	/**
	 * Fires an action event to all registered action listeners. This method delegates to {@link #fireActionPerformed(ActionEvent)}.
	 * @param force The zero-based force, such as 0 for no force or 1 for an action initiated by from a mouse single click.
	 * @param option The zero-based option, such as 0 for an event initiated by a mouse left button click or 1 for an event initiaged by a mouse right button
	 *          click.
	 * @see ActionListener
	 * @see ActionEvent
	 */
	protected void fireActionPerformed(final int force, final int option) {
		if(getEventListenerManager().hasListeners(ActionListener.class)) { //if there are action listeners registered
			fireActionPerformed(new ActionEvent(this, force, option)); //create and fire a new action event
		}
	}

	/**
	 * Fires a given action event to all registered action listeners.
	 * @param actionEvent The action event to fire.
	 */
	protected void fireActionPerformed(final ActionEvent actionEvent) {
		for(final ActionListener actionListener : getEventListenerManager().getListeners(ActionListener.class)) { //for each action listener
			actionListener.actionPerformed(actionEvent); //dispatch the action to the listener
		}
	}
}
