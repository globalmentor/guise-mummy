package com.guiseframework.event;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.guiseframework.GuiseSession;
import com.guiseframework.model.Notification;

/**An event indicating there should be a notification message of some event or state.
@author Garret Wilson
*/
public class NotificationEvent extends AbstractGuiseEvent
{

	/**The notification information.*/
	private final Notification notification;

		/**@return The notification information.*/
		private final Notification getNotification() {return notification;}

	/**Session, source, and notification  constructor.
	@param session The Guise session in which this event was generated.
	@param source The object on which the event initially occurred.
	@param notification The notification information.
	@exception NullPointerException if the given session, source, and/or notification is <code>null</code>.
	*/
	public NotificationEvent(final GuiseSession session, final Object source, final Notification notification)
	{
		super(session, source);	//construct the parent class
		this.notification=checkNull(notification, "Notification must be provided.");
	}

}
