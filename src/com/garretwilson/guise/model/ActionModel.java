package com.garretwilson.guise.model;

import com.garretwilson.guise.event.*;

/**A model for a potential action.
@author Garret Wilson
*/
public interface ActionModel extends ControlModel
{

	/**@return Whether the button is pressed.*/
	public boolean isPressed();

	/**Sets whether the button is pressed.
	@param newPressed Whether the button should be in the pressed state.
	*/
	public void setPressed(final boolean newPressed);

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
