package com.guiseframework.event;

/**An object that allows listeners for list modification events.
@param <E> The type of elements contained in the list.
@author Garret Wilson
*/
public interface ListListenable<E>
{
	/**Adds a list listener.
	@param listListener The list listener to add.
	*/
	public void addListListener(final ListListener<E> listListener);

	/**Removes a list listener.
	@param listListener The list listener to remove.
	*/
	public void removeListListener(final ListListener<E> listListener);

}
