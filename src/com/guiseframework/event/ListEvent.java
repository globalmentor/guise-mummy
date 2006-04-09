package com.guiseframework.event;

/**An event indicating a list has been modified.
If a single element was replaced both an added and removed element will be provided.
If neither an added nor a removed element are provided, the event represents a general list modification.
@param <E> The type of elements contained in the list.
@author Garret Wilson
*/
public class ListEvent<E> extends CollectionEvent<E>
{

	/**The index at which an element was added and/or removed, or -1 if the index is unknown.*/
	private final int index;

		/**@return The index at which an element was added and/or removed, or -1 if the index is unknown.*/
		public int getIndex() {return index;}

	/**Source constructor for general list modification.
	@param source The object on which the event initially occurred.
	@exception NullPointerException if the given source is <code>null</code>.
	*/
	public ListEvent(final Object source)
	{
		this(source, -1, null, null);	//construct the class with no known modification values
	}

	/**Source constructor for an added and/or removed element.
	@param source The object on which the event initially occurred.
	@param index The index at which an element was added and/or removed, or -1 if the index is unknown.
	@param addedElement The element that was added to the list, or <code>null</code> if no element was added or it is unknown whether or which elements were added.
	@param removedElement The element that was removed from the list, or <code>null</code> if no element was removed or it is unknown whether or which elements were removed.
	@exception NullPointerException if the given source is <code>null</code>.
	*/
	public ListEvent(final Object source, final int index, final E addedElement, final E removedElement)
	{
		super(source, addedElement, removedElement);	//construct the parent class
		this.index=index;	//save the index
	}
}
