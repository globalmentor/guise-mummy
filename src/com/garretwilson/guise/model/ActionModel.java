package com.garretwilson.guise.model;

import com.garretwilson.guise.event.*;
import static com.garretwilson.lang.ClassUtilities.*;

/**A model for a potential action.
@author Garret Wilson
*/
public interface ActionModel extends ControlModel
{

	/**The confirmation message bound property.*/
	public final static String CONFIRMATION_MESSAGE_PROPERTY=getPropertyName(ActionModel.class, "confirmationMessage");

	/**@return The confirmation message for the action, or <code>null</code> if there is no confirmation message.*/
	public MessageModel getConfirmationMessage();

	/**Sets the confirmation message.
	This is a bound property
	@param newConfirmationMessage The new confirmation message for the action, or <code>null</code> if there is no confirmation message.
	@see #CONFIRMATION_MESSAGE_PROPERTY
	*/
	public void setConfirmationMessage(final MessageModel newConfirmationMessage);

	/**Adds an action listener.
	@param actionListener The action listener to add.
	*/
	public void addActionListener(final ActionListener<ActionModel> actionListener);

	/**Removes an action listener.
	@param actionListener The action listener to remove.
	*/
	public void removeActionListener(final ActionListener<ActionModel> actionListener);

	/**Fires an action to all registered action listeners.
	@see ActionListener
	@see ActionEvent
	*/
	public void fireAction();

}
