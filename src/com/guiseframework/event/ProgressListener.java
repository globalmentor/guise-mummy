package com.guiseframework.event;

/**An object that listens for progres events.
@author Garret Wilson
*/
public interface ProgressListener extends GuiseEventListener
{

	/**Called when an action is initiated.
	@param actionEvent The event indicating the source of the action.
	*/
	public void progressed(final ProgressEvent progressEvent);

}
