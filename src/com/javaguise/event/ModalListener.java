package com.javaguise.event;

/**An object that listens for a change in mode.
@param <S> The type of the event source.
@author Garret Wilson
*/
public interface ModalListener<S> extends GuiseEventListener<S>
{

	/**Called when the mode begins.
	@param modalEvent The event indicating the object beginning its mode.
	*/
	public void modalBegan(final ModalEvent<S> modalEvent);

	/**Called when the mode ends.
	@param modalEvent The event indicating the object ending its mode.
	*/
	public void modalEnded(final ModalEvent<S> modalEvent);

}
