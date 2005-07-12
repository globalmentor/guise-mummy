package com.javaguise.event;

import com.javaguise.model.ListSelectModel;

/**An object that listens for list selection modification events.
@param <V> The type of values selected.
@author Garret Wilson
*/
public interface ListSelectionListener<V> extends GuiseEventListener<ListSelectModel<V>>
{

	/**Called when a selection changes.
	@param selectionEvent The event indicating the source of the event and the selectionmodifications.
	*/
	public void listSelectionChanged(final ListSelectionEvent<V> selectionEvent);

}
