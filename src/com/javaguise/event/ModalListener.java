package com.javaguise.event;

/**An object that listens for a change in mode.
@author Garret Wilson
*/
public interface ModalListener extends GuiseEventListener
{

	/**Called when the mode begins.
	@param modalEvent The event indicating the object beginning its mode.
	*/
	public void modalBegan(final ModalEvent modalEvent);

	/**Called when the mode ends.
	@param modalEvent The event indicating the object ending its mode.
	*/
	public void modalEnded(final ModalEvent modalEvent);

}
