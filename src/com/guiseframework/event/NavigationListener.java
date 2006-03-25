package com.guiseframework.event;

/**An object that listens for navigation events.
Components that implement this interface are called automatically when navigation occurs if they are part of the target navigation component hierarchy.
@author Garret Wilson
*/
public interface NavigationListener extends GuiseEventListener
{

	/**Called when navigation occurs.
	@param navigationEvent The event indicating navigation details.
	*/
	public void navigated(final NavigationEvent navigationEvent);

}
