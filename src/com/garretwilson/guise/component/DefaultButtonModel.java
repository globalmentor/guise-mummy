package com.garretwilson.guise.component;

import com.garretwilson.guise.event.ActionEvent;
import com.garretwilson.guise.event.ActionListener;

/**A default implementation of a button model.
@author Garret Wilson
*/
public class DefaultButtonModel extends AbstractModel implements ButtonModel
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
	public void addActionListener(final ActionListener<ButtonModel> actionListener)
	{
		getEventListenerManager().add(ActionListener.class, actionListener);	//add the listener
	}

	/**Removes an action listener.
	@param actionListener The action listener to remove.
	*/
	public void removeActionListener(final ActionListener<ButtonModel> actionListener)
	{
		getEventListenerManager().remove(ActionListener.class, actionListener);	//remove the listener
	}

	/**Fires an action to all action listeners.*/
	@SuppressWarnings("unchecked")
	protected void fireAction()
	{
		final ActionListener<ButtonModel>[] listeners=getEventListenerManager().getListeners(ActionListener.class);
		if(listeners.length>0)	//if there are listeners
		{
			final ActionEvent<ButtonModel> actionEvent=new ActionEvent<ButtonModel>(this);	//create a new event
			for(final ActionListener<ButtonModel> actionListener:listeners)	//for each action listener
			{
				actionListener.onAction(actionEvent);	//dispatch the action
			}
		}		
	}
}
