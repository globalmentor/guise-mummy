package com.guiseframework.component;

import java.util.Iterator;

import com.guiseframework.event.ActionEvent;
import com.guiseframework.event.ActionListener;

/**A general control with an action model.
@author Garret Wilson
*/
public interface ActionControl<C extends ActionControl<C>> extends Control<C>
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
	public Iterator<ActionListener> getActionListeners();

	/**Fires an action to all registered action listeners.
	@see ActionListener
	@see ActionEvent
	*/
	public void fireAction();

}
