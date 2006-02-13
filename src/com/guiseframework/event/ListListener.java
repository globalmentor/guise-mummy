package com.guiseframework.event;

/**An object that listens for list modification events.
@param <E> The type of elements contained in the list.
@author Garret Wilson
*/
public interface ListListener<E> extends GuiseEventListener
{

	/**Called when a list is modified.
	@param listEvent The event indicating the source of the event and the list modifications.
	*/
	public void listModified(final ListEvent<E> listEvent);

}
