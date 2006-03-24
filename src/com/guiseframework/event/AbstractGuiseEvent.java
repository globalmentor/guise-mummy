package com.guiseframework.event;

import java.util.EventObject;

import com.guiseframework.GuiseSession;

import static com.garretwilson.lang.ObjectUtilities.*;

/**The base class for custom Guise events.
@author Garret Wilson
*/
public abstract class AbstractGuiseEvent extends EventObject implements GuiseEvent
{

	/**The Guise session in which this event was generated.*/
	private final GuiseSession session;

		/**@return The Guise session in which this event was generated.*/
		public GuiseSession getSession() {return session;}

	/**Session and source constructor.
	@param session The Guise session in which this event was generated.
	@param source The object on which the event initially occurred.
	@exception NullPointerException if the given session and/or source is <code>null</code>.
	*/
	public AbstractGuiseEvent(final GuiseSession session, final Object source)
	{
		super(checkNull(source, "Event source object cannot be null"));	//construct the parent class
		this.session=checkNull(session, "Session cannot be null");	//save the session
	}

}
