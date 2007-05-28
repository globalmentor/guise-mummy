package com.guiseframework.event;

/**An abstract mouse listener providing default method implementations.
@author Garret Wilson
*/
public abstract class MouseAdapter implements MouseListener
{

	/**Called when the mouse enters the target.
	@param mouseEvent The event providing mouse information
	*/
	public void mouseEntered(final MouseEnterEvent mouseEvent) {}

	/**Called when the mouse exits the target.
	@param mouseEvent The event providing mouse information
	*/
	public void mouseExited(final MouseExitEvent mouseEvent) {}

}
