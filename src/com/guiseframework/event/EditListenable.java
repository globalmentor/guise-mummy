package com.guiseframework.event;

/**An object that allows the registration of edit listeners.
@author Garret Wilson
*/
public interface EditListenable
{

	/**Adds an edit listener.
	@param editListener The edit listener to add.
	*/
	public void addEditListener(final EditListener editListener);

	/**Removes an edit listener.
	@param editListener The edit listener to remove.
	*/
	public void removeEditListener(final EditListener editListener);

}
