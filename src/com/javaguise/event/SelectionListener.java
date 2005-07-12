package com.javaguise.event;

import com.javaguise.model.SelectModel;

/**An object that listens for selection modification events.
@param <V> The type of values selected.
@author Garret Wilson
*/
public interface SelectionListener<V> extends GuiseEventListener<SelectModel<V>>
{

	/**Called when a selection changes.
	@param selectionEvent The event indicating the source of the event and the selectionmodifications.
	*/
	public void selectionChanged(final SelectionEvent<V> selectionEvent);

}
