package com.guiseframework.event;

/**An abstract mouse listener providing default method implementations.
@author Garret Wilson
*/
public abstract class MouseAdapter implements MouseListener
{
	/**Called when the mouse enters the source.
	@param mouseEvent The event providing mouse information
	*/
	public void mouseEntered(final MouseEvent mouseEvent) {}

	/**Called when the mouse exits the source.
	@param mouseEvent The event providing mouse information
	*/
	public void mouseExited(final MouseEvent mouseEvent) {}

}
