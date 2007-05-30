package com.guiseframework.event;

/**An abstract mouse listener providing default method implementations.
@author Garret Wilson
*/
public abstract class MouseAdapter implements MouseListener
{

	/**Called when the mouse clicks the target.
	@param mouseEvent The event providing mouse information
	*/
	public void mouseClicked(final MouseClickEvent mouseClickEvent) {}

	/**Called when the mouse enters the target.
	@param mouseEvent The event providing mouse information
	*/
	public void mouseEntered(final MouseEnterEvent mouseEnterEvent) {}

	/**Called when the mouse exits the target.
	@param mouseEvent The event providing mouse information
	*/
	public void mouseExited(final MouseExitEvent mouseEnterEvent) {}

}
