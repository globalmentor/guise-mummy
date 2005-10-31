package com.javaguise.event;

/**An object that listens for mouse events.
@param <S> The type of the event source.
@author Garret Wilson
*/
public interface MouseListener<S> extends GuiseEventListener<S>
{
	/**Called when the mouse enters the source.
	@param mouseEvent The event providing mouse information
	*/
	public void mouseEntered(final MouseEvent<S> mouseEvent);

	/**Called when the mouse exits the source.
	@param mouseEvent The event providing mouse information
	*/
	public void mouseExited(final MouseEvent<S> mouseEvent);

}
