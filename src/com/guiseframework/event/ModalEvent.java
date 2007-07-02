package com.guiseframework.event;

import com.guiseframework.component.ModalNavigationPanel;

/**An event indicating that a component changed modes.
author Garret Wilson
*/
public class ModalEvent extends AbstractGuiseEvent
{

	/**@return The source of the event.*/
	public ModalNavigationPanel<?> getSource()
	{
		return (ModalNavigationPanel<?>)super.getSource();	//cast the event to the appropriate type
	}

	/**Source constructor.
	@param source The object on which the event initially occurred.
	@exception NullPointerException if the given source is <code>null</code>.
	*/
	public ModalEvent(final ModalNavigationPanel<?> source)
	{
		super(source);	//construct the parent class
	}

}
