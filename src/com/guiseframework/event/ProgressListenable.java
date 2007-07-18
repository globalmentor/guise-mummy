package com.guiseframework.event;

/**An object that allows the registration of progress listeners.
@param <P> The type of progress being made.
@author Garret Wilson
*/
public interface ProgressListenable<P>
{

	/**Adds a progress listener.
	@param progressListener The progress listener to add.
	*/
	public void addProgressListener(final ProgressListener<P> progressListener);

	/**Removes an progress listener.
	@param progressListener The progress listener to remove.
	*/
	public void removeProgressListener(final ProgressListener<P> progressListener);

}
