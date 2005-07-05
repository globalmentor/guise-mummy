package com.garretwilson.guise.event;

/**An object that listens for list modification events.
@param <S> The type of the event source.
@param <E> The type of elements contained in the list.
@author Garret Wilson
*/
public interface ListListener<S, E> extends GuiseEventListener<S>
{

	/**Called when a list is modified.
	@param listEvent The event indicating the source of the event and the list modifications.
	*/
	public void listModified(final ListEvent<S, E> listEvent);

}
