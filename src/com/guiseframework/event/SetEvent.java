package com.guiseframework.event;

/**An event indicating a set has been modified.
If a single element was replaced both an added and removed element will be provided.
If neither an added nor a removed element are provided, the event represents a general set modification.
@param <E> The type of elements contained in the set.
@author Garret Wilson
*/
public class SetEvent<E> extends CollectionEvent<E>
{

	/**Source constructor for general set modification.
	@param session The Guise session in which this event was generated.
	@param source The object on which the event initially occurred.
	@exception NullPointerException if the given source is <code>null</code>.
	*/
	public SetEvent(final Object source)
	{
		this(source, null, null);	//construct the class with no known modification values
	}

	/**Source constructor for an added and/or removed element.
	@param source The object on which the event initially occurred.
	@param addedElement The element that was added to the set, or <code>null</code> if no element was added or it is unknown whether or which elements were added.
	@param removedElement The element that was removed from the set, or <code>null</code> if no element was removed or it is unknown whether or which elements were removed.
	@exception NullPointerException if the given source is <code>null</code>.
	*/
	public SetEvent(final Object source, final E addedElement, final E removedElement)
	{
		super(source, addedElement, removedElement);	//construct the parent class
	}
}
