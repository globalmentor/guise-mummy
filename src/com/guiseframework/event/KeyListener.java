package com.guiseframework.event;

/**An object that listens for key events.
@author Garret Wilson
*/
public interface KeyListener extends GuiseEventListener
{

	/**Called when a key is pressed.
	@param keyEvent The event providing key information
	*/
	public void keyPressed(final KeyPressEvent keyEvent);

	/**Called when a key is released.
	@param keyEvent The event providing key information
	*/
	public void keyReleased(final KeyReleaseEvent keyEvent);

}
