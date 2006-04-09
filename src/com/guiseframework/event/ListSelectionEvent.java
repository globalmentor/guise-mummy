package com.guiseframework.event;

import com.guiseframework.model.ListSelectModel;

/**An event indicating the list selection has been modified.
An added or removed element represents an added or removed index of the selection.
If neither an added nor a removed element are provided, the event represents a general set modification.
@param <V> The type of values selected.
@author Garret Wilson
*/
public class ListSelectionEvent<V> extends SetEvent<Integer>
{

	/**@return The source of the event.*/
	@SuppressWarnings("unchecked")
	public ListSelectModel<V> getSource()
	{
		return (ListSelectModel<V>)super.getSource();	//cast the event to the appropriate type
	}

	/**Source constructor for general selection modification.
	@param source The object on which the event initially occurred.
	@exception NullPointerException if the given source is <code>null</code>.
	*/
	public ListSelectionEvent(final ListSelectModel<V> source)
	{
		this(source, null, null);	//construct the class with no known modification values
	}

	/**Source constructor for an added and/or removed element.
	@param source The object on which the event initially occurred.
	@param addedElement The index that was added to the selection, or <code>null</code> if no index was added or it is unknown whether or which indices were added.
	@param removedElement The index that was removed from the selection, or <code>null</code> if no index was removed or it is unknown whether or which indices were removed.
	@exception NullPointerException if the given source is <code>null</code>.
	*/
	public ListSelectionEvent(final ListSelectModel<V> source, final Integer addedElement, final Integer removedElement)
	{
		super(source, addedElement, removedElement);	//construct the parent class
	}
}
