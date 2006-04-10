package com.guiseframework.model;

import com.guiseframework.event.*;

/**A model for a potential action.
@author Garret Wilson
*/
public interface ActionModel extends Model
{

	/**Adds an action listener.
	@param actionListener The action listener to add.
	*/
	public void addActionListener(final ActionListener actionListener);

	/**Removes an action listener.
	@param actionListener The action listener to remove.
	*/
	public void removeActionListener(final ActionListener actionListener);

	/**@return all registered action listeners.*/
	public Iterable<ActionListener> getActionListeners();

	/**Performs the action.
	An {@link ActionEvent} is fired to all registered {@link ActionListener}s.
	*/
	public void performAction();

}
