package com.garretwilson.guise.model;

import java.util.Set;

import com.garretwilson.guise.event.*;

/**A default implementation of a button model.
@author Garret Wilson
*/
public class DefaultActionModel extends DefaultMessageModel implements ActionModel
{

	/**Whether the button is pressed.*/
	private boolean pressed=false;

		/**@return Whether the button is pressed.*/
		public boolean isPressed() {return pressed;}

		/**Sets whether the button is pressed.
		@param newPressed Whether the button should be in the pressed state.
		*/
		public void setPressed(final boolean newPressed) {pressed=newPressed;}

	/**Adds an action listener.
	@param actionListener The action listener to add.
	*/
	public void addActionListener(final ActionListener<ActionModel> actionListener)
	{
		getEventListenerManager().add(ActionListener.class, actionListener);	//add the listener
	}

	/**Removes an action listener.
	@param actionListener The action listener to remove.
	*/
	public void removeActionListener(final ActionListener<ActionModel> actionListener)
	{
		getEventListenerManager().remove(ActionListener.class, actionListener);	//remove the listener
	}

	/**Fires an action to all registered action listeners.
	@see com.garretwilson.guise.event.ActionListener
	@see com.garretwilson.guise.event.ActionEvent
	*/
	@SuppressWarnings("unchecked")
	public void fireAction()
	{
		final Set<ActionListener<ActionModel>> listeners=(Set<ActionListener<ActionModel>>)getEventListenerManager().getListeners(ActionListener.class);
		if(!listeners.isEmpty())	//if there are listeners
		{
			final ActionEvent<ActionModel> actionEvent=new ActionEvent<ActionModel>(this);	//create a new event
			for(final ActionListener<ActionModel> actionListener:listeners)	//for each action listener
			{
				actionListener.onAction(actionEvent);	//dispatch the action
			}
		}		
	}
}
