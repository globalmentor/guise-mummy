package com.garretwilson.guise.model;

import com.garretwilson.guise.event.*;
import com.garretwilson.guise.session.GuiseSession;

/**A default implementation of a button model.
@author Garret Wilson
*/
public class DefaultActionModel extends DefaultControlModel implements ActionModel
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
	@see ActionListener
	@see ActionEvent
	*/
	public void fireAction()
	{
		if(getEventListenerManager().hasListeners(ActionListener.class))	//if there are action listeners registered
		{
			final ActionEvent<ActionModel> actionEvent=new ActionEvent<ActionModel>(getSession(), this);	//create a new action event
			getSession().queueModelEvent(new PostponedActionEvent<ActionModel>(getEventListenerManager(), actionEvent));	//tell the Guise session to queue the event
		}
	}

	/**Session constructor.
	@param session The Guise session that owns this model.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public DefaultActionModel(final GuiseSession<?> session)
	{
		super(session);	//construct the parent class
	}

}
