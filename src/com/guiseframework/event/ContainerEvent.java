package com.guiseframework.event;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.Component;
import com.guiseframework.component.Container;

/**An event indicating a container has been modified.
If a single component was replaced both an added and removed compontent will be provided.
If neither an added nor a removed component are provided, the event represents a general container modification.
@author Garret Wilson
*/
public class ContainerEvent extends ListEvent<Component<?>>
{

	/**@return The source of the event.*/
	@SuppressWarnings("unchecked")
	public Container<?> getSource()
	{
		return (Container<?>)super.getSource();	//cast the event to the appropriate type
	}

	/**Session and source constructor for general container modification.
	@param session The Guise session in which this event was generated.
	@param source The object on which the event initially occurred.
	@exception NullPointerException if the given session and/or source is <code>null</code>.
	*/
	public ContainerEvent(final GuiseSession session, final Container<?> source)
	{
		this(session, source, -1, null, null);	//construct the class with no known modification values
	}

	/**Session and source constructor for an added and/or removed component.
	@param session The Guise session in which this event was generated.
	@param source The object on which the event initially occurred.
	@param index The index at which an component was added and/or removed, or -1 if the index is unknown.
	@param addedComponent The component that was added to the container, or <code>null</code> if no component was added or it is unknown whether or which components were added.
	@param removedComponent The component that was removed from the container, or <code>null</code> if no component was removed or it is unknown whether or which components were removed.
	@exception NullPointerException if the given session and/or source is <code>null</code>.
	*/
	public ContainerEvent(final GuiseSession session, final Container<?> source, final int index, final Component<?> addedComponent, final Component<?> removedComponent)
	{
		super(session, source, index, addedComponent, removedComponent);	//construct the parent class
	}
}