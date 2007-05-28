package com.guiseframework.event;

/**An abstract key listener providing default method implementations.
@author Garret Wilson
*/
public abstract class KeyAdapter implements KeyListener
{

	/**Called when a key is pressed.
	@param keyEvent The event providing key information
	*/
	public void keyPressed(final KeyPressEvent keyEvent) {}

	/**Called when a key is released.
	@param keyEvent The event providing key information
	*/
	public void keyReleased(final KeyReleaseEvent keyEvent) {}

}
