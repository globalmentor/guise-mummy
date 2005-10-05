package com.javaguise.model;

import java.util.Iterator;

import com.javaguise.event.*;
import com.javaguise.session.GuiseSession;

/**A default implementation of a button model.
@author Garret Wilson
*/
public class DefaultActionModel extends AbstractControlModel implements ActionModel
{

	/**The confirmation message for the action, or <code>null</code> if there is no confirmation message.*/
	private MessageModel confirmationMessage;

		/**@return The confirmation message for the action, or <code>null</code> if there is no confirmation message.*/
		public MessageModel getConfirmationMessage() {return confirmationMessage;}

		/**Sets the confirmation message.
		This is a bound property
		@param newConfirmationMessage The new confirmation message for the action, or <code>null</code> if there is no confirmation message.
		@see ActionModel#CONFIRMATION_MESSAGE_PROPERTY
		*/
		public void setConfirmationMessage(final MessageModel newConfirmationMessage)
		{
			if(confirmationMessage!=newConfirmationMessage)	//if the value is really changing
			{
				final MessageModel oldConfirmationMessage=confirmationMessage;	//get the old value
				confirmationMessage=newConfirmationMessage;	//actually change the value
				firePropertyChange(CONFIRMATION_MESSAGE_PROPERTY, oldConfirmationMessage, newConfirmationMessage);	//indicate that the value changed
			}
		}

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

	/**@return all registered action listeners.*/
	@SuppressWarnings("unchecked")
	public Iterator<ActionListener<ActionModel>> getActionListeners()
	{
		return (Iterator<ActionListener<ActionModel>>)(Object)getEventListenerManager().getListeners(ActionListener.class);	//remove the listener TODO find out why we have to use the double cast for JDK 1.5 to compile
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
			getSession().queueEvent(new PostponedActionEvent<ActionModel>(getEventListenerManager(), actionEvent));	//tell the Guise session to queue the event
		}
	}

	/**Session constructor.
	@param session The Guise session that owns this model.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public DefaultActionModel(final GuiseSession session)
	{
		super(session);	//construct the parent class
	}

}
