package com.guiseframework.event;

/**An object that listens for progress events.
@author Garret Wilson
*/
public interface ProgressListener extends GuiseEventListener
{

	/**Called when a task makes progress.
	@param progressEvent The event indicating the source of the action.
	*/
	public void progressed(final ProgressEvent progressEvent);

}
