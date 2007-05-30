package com.guiseframework.event;

/**An object that listens for mouse events.
@author Garret Wilson
*/
public interface MouseListener extends GuiseEventListener
{

	/**Called when the mouse clicks the target.
	@param mouseEvent The event providing mouse information
	*/
	public void mouseClicked(final MouseClickEvent mouseClickEvent);

	/**Called when the mouse enters the target.
	@param mouseEvent The event providing mouse information
	*/
	public void mouseEntered(final MouseEnterEvent mouseEnterEvent);

	/**Called when the mouse exits the target.
	@param mouseEvent The event providing mouse information
	*/
	public void mouseExited(final MouseExitEvent mouseExitEvent);

}
