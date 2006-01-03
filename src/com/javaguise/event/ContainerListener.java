package com.javaguise.event;

/**An object that listens for container modification events.
@author Garret Wilson
*/
public interface ContainerListener extends GuiseEventListener
{

	/**Called when a container is modified.
	@param containerEvent The event indicating the source of the event and the container modifications.
	*/
	public void containerModified(final ContainerEvent containerEvent);

}
