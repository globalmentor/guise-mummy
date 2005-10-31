package com.javaguise.event;

import com.javaguise.session.GuiseSession;

/**An event providing mouse information.
@param <S> The type of the event source.
@author Garret Wilson
*/
public class MouseEvent<S> extends GuiseEvent<S>
{

	/**Session and source constructor.
	@param session The Guise session in which this event was generated.
	@param source The object on which the event initially occurred.
	@exception NullPointerException if the given session and/or source is <code>null</code>.
	*/
	public MouseEvent(final GuiseSession session, final S source)
	{
		super(session, source);	//construct the parent class
	}

}
