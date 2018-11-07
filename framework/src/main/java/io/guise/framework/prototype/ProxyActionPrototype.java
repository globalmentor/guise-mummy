/*
 * Copyright © 2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.guise.framework.prototype;

import io.guise.framework.event.*;

/**
 * An action prototype that is a proxy for another action prototype.
 * @author Garret Wilson
 */
public class ProxyActionPrototype extends AbstractEnableableProxyPrototype<ActionPrototype> implements ActionPrototype {

	/** A lazily-created action listener to repeat copies of events received, using this object as the source. */
	private ActionListener repeatActionListener = null;

	/** @return An action listener to repeat copies of events received, using this object as the source. */
	protected synchronized ActionListener getRepeatActionListener() { //TODO synchronize on something else
		if(repeatActionListener == null) { //if we have not yet created the repeater action listener
			repeatActionListener = new ActionListener() { //create a listener to listen for an action

				@Override
				public void actionPerformed(final ActionEvent actionEvent) { //if the action is performed
					final ActionEvent repeatActionEvent = new ActionEvent(ProxyActionPrototype.this, actionEvent); //copy the action event with this class as its source, but keeping the same target if present
					fireActionPerformed(actionEvent); //fire the repeated action event
				}

			};
		}
		return repeatActionListener; //return the repeater action listener
	}

	@Override
	protected void uninstallListeners(final ActionPrototype oldProxiedPrototype) {
		super.uninstallListeners(oldProxiedPrototype);
		oldProxiedPrototype.removeActionListener(getRepeatActionListener()); //stop repeating all actions of the proxied prototype
	}

	@Override
	protected void installListeners(final ActionPrototype newProxiedPrototype) {
		super.installListeners(newProxiedPrototype);
		newProxiedPrototype.addActionListener(getRepeatActionListener()); //listen and repeat all actions of the proxied prototype
	}

	/**
	 * Proxied prototype constructor.
	 * @param proxiedPrototype The prototype proxied by this prototype.
	 * @throws NullPointerException if the given proxied prototype is <code>null</code> is <code>null</code>.
	 */
	public ProxyActionPrototype(final ActionPrototype proxiedPrototype) {
		super(proxiedPrototype);
	}

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

	/**
	 * Performs the action with default force and default option. An {@link ActionEvent} is fired to all registered {@link ActionListener}s. This method delegates
	 * to {@link #performAction(int, int)}.
	 */
	public void performAction() {
		performAction(1, 0); //fire an event saying that the action has been performed with the default force and option
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation calls {@link ActionPrototype#performAction(int, int)} on the proxied prototype to perform the actual action. An {@link ActionEvent} is
	 * not fired to registered {@link ActionListener}s; the proxied action prototype should fire such an event, which we will them repeat.
	 * </p>
	 */
	@Override
	public void performAction(final int force, final int option) {
		getProxiedPrototype().performAction(force, option); //send the action on to the proxied prototype
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
