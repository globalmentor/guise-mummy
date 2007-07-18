package com.guiseframework.event;

/**An object that listens for progress events.
@param <P> The type of progress being made.
@author Garret Wilson
*/
public interface ProgressListener<P> extends GuiseEventListener
{

	/**Called when a task makes progress.
	@param progressEvent The event indicating the source of the action.
	*/
	public void progressed(final ProgressEvent<P> progressEvent);

}
