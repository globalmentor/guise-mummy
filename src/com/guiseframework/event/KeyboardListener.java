package com.guiseframework.event;

/**An object that listens for keyboard events.
@author Garret Wilson
*/
public interface KeyboardListener extends GuiseEventListener
{

	/**Called when a key is pressed.
	@param keyEvent The event providing key information
	*/
	public void keyPressed(final KeyPressEvent keyPressEvent);

	/**Called when a key is released.
	@param keyEvent The event providing key information
	*/
	public void keyReleased(final KeyReleaseEvent keyReleaseEvent);

}
