package com.guiseframework.model;

import com.guiseframework.event.*;

/**A default implementation of a button model.
@author Garret Wilson
*/
public class DefaultActionModel extends AbstractModel implements ActionModel
{

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

	/**Performs the action.
	An {@link ActionEvent} is fired to all registered {@link ActionListener}s.
	*/
	public void performAction()
	{
		fireActionPerformed();	//fire an event saying that the action has been performed
	}

	/**Fires an action event to all registered action listeners.
	This method delegates to {@link #fireActionPerformed(ActionEvent)}.
	@see ActionListener
	@see ActionEvent
	*/
	protected void fireActionPerformed()
	{
		final EventListenerManager eventListenerManager=getEventListenerManager();	//get event listener support
		if(eventListenerManager.hasListeners(ActionListener.class))	//if there are action listeners registered
		{
			fireActionPerformed(new ActionEvent(this));	//create and fire a new action event
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
