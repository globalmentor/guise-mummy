package com.javaguise.event;

import com.javaguise.session.GuiseSession;

/**An event indicating an action should take place.
@param <S> The type of the event source.
@author Garret Wilson
*/
public class ActionEvent<S> extends GuiseEvent<S>
{

	/**Session and source constructor.
	@param session The Guise session in which this event was generated.
	@param source The object on which the event initially occurred.
	@exception NullPointerException if the given session and/or source is <code>null</code>.
	*/
	public ActionEvent(final GuiseSession session, final S source)
	{
		super(session, source);	//construct the parent class
	}

}
