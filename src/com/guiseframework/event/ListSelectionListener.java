package com.guiseframework.event;

/**An object that listens for list selection modification events.
@param <V> The type of values selected.
@author Garret Wilson
*/
public interface ListSelectionListener<V> extends GuiseEventListener
{

	/**Called when a selection changes.
	@param selectionEvent The event indicating the source of the event and the selectionmodifications.
	*/
	public void listSelectionChanged(final ListSelectionEvent<V> selectionEvent);

}
