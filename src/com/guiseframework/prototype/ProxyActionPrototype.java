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

package com.guiseframework.prototype;

import com.guiseframework.event.*;

/**An action prototype that is a proxy for another action prototype.
@author Garret Wilson
*/
public class ProxyActionPrototype extends AbstractEnableableProxyPrototype<ActionPrototype> implements ActionPrototype 
{

	/**A lazily-created action listener to repeat copies of events received, using this object as the source.*/ 
	private ActionListener repeatActionListener=null;

		/**@return An action listener to repeat copies of events received, using this object as the source.*/ 
		protected synchronized ActionListener getRepeatActionListener()
		{	//TODO synchronize on something else
			if(repeatActionListener==null)	//if we have not yet created the repeater action listener
			{
				repeatActionListener=new ActionListener()	//create a listener to listen for a changing property value
						{
							public void actionPerformed(final ActionEvent actionEvent)	//if the action is performed
									{
										final ActionEvent repeatActionEvent=new ActionEvent(ProxyActionPrototype.this, actionEvent);	//copy the action event with this class as its source, but keeping the same target if present
										fireActionPerformed(actionEvent);	//fire the repeated action event
									}
						};
			}
			return repeatActionListener;	//return the repeater action listener
		}

	/**Uninstalls listeners from a proxied prototype.
	@param oldProxiedPrototype The old proxied prototype.
	*/
	protected void uninstallListeners(final ActionPrototype oldProxiedPrototype)
	{
		super.uninstallListeners(oldProxiedPrototype);
		oldProxiedPrototype.removeActionListener(getRepeatActionListener());	//stop repeating all actions of the proxied prototype
	}

	/**Installs listeners to a proxied prototype.
	@param newProxiedPrototype The new proxied prototype.
	*/
	protected void installListeners(final ActionPrototype newProxiedPrototype)
	{
		super.installListeners(newProxiedPrototype);
		newProxiedPrototype.addActionListener(getRepeatActionListener());	//listen and repeat all actions of the proxied prototype
	}

	/**Proxied prototype constructor.
	@param proxiedPrototype The prototype proxied by this prototype.
	@exception NullPointerException if the given proxied prototype is <code>null</code> is <code>null</code>.
	*/
	public ProxyActionPrototype(final ActionPrototype proxiedPrototype)
	{
		super(proxiedPrototype);
	}

	/**Adds an action listener.
	@param actionListener The action listener to add.
	*/
	public void addActionListener(final ActionListener actionListener)
	{
		getEventListenerManager().add(ActionListener.class, actionListener);	//add the listener
	}

	/**Removes an action listener.
	@param actionListener The action listener to remove.
	*/
	public void removeActionListener(final ActionListener actionListener)
	{
		getEventListenerManager().remove(ActionListener.class, actionListener);	//remove the listener
	}

	/**@return all registered action listeners.*/
	public Iterable<ActionListener> getActionListeners()
	{
		return getEventListenerManager().getListeners(ActionListener.class);	//remove the listener
	}

	/**Performs the action with default force and default option.
	An {@link ActionEvent} is fired to all registered {@link ActionListener}s.
	This method delegates to {@link #performAction(int, int)}.
	*/
	public void performAction()
	{
		performAction(1, 0);	//fire an event saying that the action has been performed with the default force and option
	}

	/**Performs the action with the given force and option.
	This implementation calls {@link #action(int, int)} to perform the actual action.
	An {@link ActionEvent} is fired to all registered {@link ActionListener}s.
	@param force The zero-based force, such as 0 for no force or 1 for an action initiated by from a mouse single click.
	@param option The zero-based option, such as 0 for an event initiated by a mouse left button click or 1 for an event initiaged by a mouse right button click.
	*/
	public void performAction(final int force, final int option)
	{
		fireActionPerformed(force, option);	//fire an event saying that the action has been performed with the given force and option
	}

	/**Fires an action event to all registered action listeners.
	This method delegates to {@link #fireActionPerformed(ActionEvent)}.
	@param force The zero-based force, such as 0 for no force or 1 for an action initiated by from a mouse single click.
	@param option The zero-based option, such as 0 for an event initiated by a mouse left button click or 1 for an event initiaged by a mouse right button click.
	@see ActionListener
	@see ActionEvent
	*/
	protected void fireActionPerformed(final int force, final int option)
	{
		final EventListenerManager eventListenerManager=getEventListenerManager();	//get event listener support
		if(eventListenerManager.hasListeners(ActionListener.class))	//if there are action listeners registered
		{
			fireActionPerformed(new ActionEvent(this, force, option));	//create and fire a new action event
		}
	}

	/**Fires a given action event to all registered action listeners.
	@param actionEvent The action event to fire.
	*/
	protected void fireActionPerformed(final ActionEvent actionEvent)
	{
		for(final ActionListener actionListener:getEventListenerManager().getListeners(ActionListener.class))	//for each action listener
		{
			actionListener.actionPerformed(actionEvent);	//dispatch the action to the listener
		}
	}

}
