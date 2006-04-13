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

	/**Performs the action with default force and default option.
	An {@link ActionEvent} is fired to all registered {@link ActionListener}s.
	This method delegates to {@link #performAction(int, int)}.
	*/
	public void performAction();

	/**Performs the action with the given force and option.
	An {@link ActionEvent} is fired to all registered {@link ActionListener}s.
	@param force The zero-based force, such as 0 for no force or 1 for an action initiated by from a mouse single click.
	@param option The zero-based option, such as 0 for an event initiated by a mouse left button click or 1 for an event initiaged by a mouse right button click.
	*/
	public void performAction(final int force, final int option);

}
