package com.javaguise.event;

import java.util.EventObject;

import com.javaguise.session.GuiseSession;
import static com.garretwilson.lang.ObjectUtilities.*;

/**The base class for all Guise events.
@author Garret Wilson
*/
public class GuiseEvent<S> extends EventObject
{

	/**The Guise session in which this event was generated.*/
	private final GuiseSession session;

		/**@return The Guise session in which this event was generated.*/
		public GuiseSession getSession() {return session;}

	/**@return The source of the event.*/
	@SuppressWarnings("unchecked")
	public S getSource()
	{
		return (S)super.getSource();	//cast the event to the appropriate type
	}

	/**Session and source constructor.
	@param session The Guise session in which this event was generated.
	@param source The object on which the event initially occurred.
	@exception NullPointerException if the given session and/or source is <code>null</code>.
	*/
	public GuiseEvent(final GuiseSession session, final S source)
	{
		super(checkNull(source, "Event source object cannot be null"));	//construct the parent class
		this.session=checkNull(session, "Session cannot be null");	//save the session
	}

}
