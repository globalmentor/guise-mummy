package com.garretwilson.guise.event;

/**An object that listens for action events.
@param <S> The type of the event source.
@author Garret Wilson
*/
public interface ActionListener<S> extends GuiseEventListener<S>
{

	/**Called when an action is initiated.
	@param actionEvent The event indicating the source of the action.
	*/
	public void actionPerformed(final ActionEvent<S> actionEvent);

}
