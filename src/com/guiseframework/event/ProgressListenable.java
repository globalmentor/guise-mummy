package com.guiseframework.event;

/**An object that allows the registration of progress listeners.
@author Garret Wilson
*/
public interface ProgressListenable
{

	/**Adds a progress listener.
	@param progressListener The progress listener to add.
	*/
	public void addProgressListener(final ProgressListener progressListener);

	/**Removes an progress listener.
	@param progressListener The progress listener to remove.
	*/
	public void removeProgressListener(final ProgressListener progressListener);

}
