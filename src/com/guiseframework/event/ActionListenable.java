package com.guiseframework.event;

/**An object that allows the registration of action listeners.
@author Garret Wilson
*/
public interface ActionListenable
{

	/**Adds an action listener.
	@param actionListener The action listener to add.
	*/
	public void addActionListener(final ActionListener actionListener);

	/**Removes an action listener.
	@param actionListener The action listener to remove.
	*/
	public void removeActionListener(final ActionListener actionListener);

}
