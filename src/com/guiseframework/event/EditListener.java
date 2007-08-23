package com.guiseframework.event;

/**An object that listens for edit events.
@author Garret Wilson
*/
public interface EditListener extends GuiseEventListener
{

	/**Called when a coarse-grained edit has occurred.
	@param editEvent The event indicating the source of the action.
	*/
	public void edited(final EditEvent editEvent);

}
