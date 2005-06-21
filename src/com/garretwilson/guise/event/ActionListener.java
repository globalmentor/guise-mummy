package com.garretwilson.guise.event;

/**An object that listens for action events.
@author Garret Wilson
*/
public interface ActionListener<S> extends GuiseEventListener<S>
{

	/**Called when an action is initiated.
	@param actionEvent The event indicating the source of the action.
	*/
	public void onAction(final ActionEvent<S> actionEvent);

}
