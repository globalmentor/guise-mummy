package com.guiseframework.event;

/**An object that listens for action events.
@author Garret Wilson
*/
public interface ActionListener extends GuiseEventListener
{

	/**Called when an action is initiated.
	@param actionEvent The event indicating the source of the action.
	*/
	public void actionPerformed(final ActionEvent actionEvent);

}
