package com.javaguise.event;

import com.javaguise.GuiseSession;
import com.javaguise.component.ModalNavigationPanel;

/**An event indicating that a component changed modes.
author Garret Wilson
*/
public class ModalEvent extends AbstractGuiseEvent
{

	/**@return The source of the event.*/
	public ModalNavigationPanel<?, ?> getSource()
	{
		return (ModalNavigationPanel<?, ?>)super.getSource();	//cast the event to the appropriate type
	}

	/**Session and source constructor.
	@param session The Guise session in which this event was generated.
	@param source The object on which the event initially occurred.
	@exception NullPointerException if the given session and/or source is <code>null</code>.
	*/
	public ModalEvent(final GuiseSession session, final ModalNavigationPanel<?, ?> source)
	{
		super(session, source);	//construct the parent class
	}

}
