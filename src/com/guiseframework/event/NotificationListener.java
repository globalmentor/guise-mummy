package com.guiseframework.event;

/**An object that listens for notification events.
The notification system is used to pass messages that should be reported.
@author Garret Wilson
*/
public interface NotificationListener extends GuiseEventListener
{

	/**Called when a notification event occurs.
	@param notificationEvent The event containing notification information.
	*/
	public void notified(final NotificationEvent notificationEvent);

}
