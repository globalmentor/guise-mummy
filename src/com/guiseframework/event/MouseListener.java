package com.guiseframework.event;

/**An object that listens for mouse events.
@author Garret Wilson
*/
public interface MouseListener extends GuiseEventListener
{

	/**Called when the mouse enters the target.
	@param mouseEvent The event providing mouse information
	*/
	public void mouseEntered(final MouseEnterEvent mouseEvent);

	/**Called when the mouse exits the target.
	@param mouseEvent The event providing mouse information
	*/
	public void mouseExited(final MouseExitEvent mouseEvent);

}
