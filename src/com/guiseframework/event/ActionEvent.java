package com.guiseframework.event;

import com.guiseframework.GuiseSession;

/**An event indicating an action should take place.
@author Garret Wilson
*/
public class ActionEvent extends AbstractGuiseEvent
{

	/**Session and source constructor.
	@param session The Guise session in which this event was generated.
	@param source The object on which the event initially occurred.
	@exception NullPointerException if the given session and/or source is <code>null</code>.
	*/
	public ActionEvent(final GuiseSession session, final Object source)
	{
		super(session, source);	//construct the parent class
	}

}
