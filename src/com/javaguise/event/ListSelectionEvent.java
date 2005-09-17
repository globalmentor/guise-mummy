package com.javaguise.event;

import com.javaguise.model.ListSelectModel;
import com.javaguise.session.GuiseSession;

/**An event indicating the list selection has been modified.
An added or removed element represents an added or removed index of the selection.
If neither an added nor a removed element are provided, the event represents a general set modification.
@param <V> The type of values selected.
@author Garret Wilson
*/
public class ListSelectionEvent<V> extends SetEvent<ListSelectModel<V>, Integer>
{

	/**Session and source constructor for general selection modification.
	@param session The Guise session in which this event was generated.
	@param source The object on which the event initially occurred.
	@exception NullPointerException if the given session and/or source is <code>null</code>.
	*/
	public ListSelectionEvent(final GuiseSession session, final ListSelectModel<V> source)
	{
		this(session, source, null, null);	//construct the class with no known modification values
	}

	/**Session and source constructor for an added and/or removed element.
	@param session The Guise session in which this event was generated.
	@param source The object on which the event initially occurred.
	@param addedElement The index that was added to the selection, or <code>null</code> if no index was added or it is unknown whether or which indices were added.
	@param removedElement The index that was removed from the selection, or <code>null</code> if no index was removed or it is unknown whether or which indices were removed.
	@exception NullPointerException if the given session and/or source is <code>null</code>.
	*/
	public ListSelectionEvent(final GuiseSession session, final ListSelectModel<V> source, final Integer addedElement, final Integer removedElement)
	{
		super(session, source, addedElement, removedElement);	//construct the parent class
	}
}
