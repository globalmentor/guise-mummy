package com.guiseframework.event;

import java.util.EventObject;

import com.guiseframework.Guise;
import com.guiseframework.GuiseSession;

import static com.garretwilson.lang.ObjectUtilities.*;

/**The base class for custom Guise events.
@author Garret Wilson
*/
public abstract class AbstractGuiseEvent extends EventObject
{

	/**The Guise session in which this event was generated.*/
	private final GuiseSession session;

		/**@return The Guise session in which this event was generated.*/
		public GuiseSession getSession() {return session;}

	/**Source constructor.
	@param source The object on which the event initially occurred.
	@exception NullPointerException if the given source is <code>null</code>.
	*/
	public AbstractGuiseEvent(final Object source)
	{
		super(checkInstance(source, "Event source object cannot be null."));	//construct the parent class
		this.session=Guise.getInstance().getGuiseSession();	//store a reference to the current Guise session
	}

}
