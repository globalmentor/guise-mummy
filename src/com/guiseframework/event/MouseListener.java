package com.guiseframework.event;

/**An object that listens for mouse events.
@author Garret Wilson
*/
public interface MouseListener extends GuiseEventListener
{
	/**Called when the mouse enters the source.
	@param mouseEvent The event providing mouse information
	*/
	public void mouseEntered(final MouseEvent mouseEvent);

	/**Called when the mouse exits the source.
	@param mouseEvent The event providing mouse information
	*/
	public void mouseExited(final MouseEvent mouseEvent);

}
