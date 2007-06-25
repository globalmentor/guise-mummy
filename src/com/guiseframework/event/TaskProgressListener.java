package com.guiseframework.event;

/**An object that listens for task progress events.
@author Garret Wilson
*/
public interface TaskProgressListener extends GuiseEventListener
{

	/**Called when a task makes progress.
	@param progressEvent The event indicating the source of the action.
	*/
	public void taskProgressed(final TaskProgressEvent progressEvent);

}
