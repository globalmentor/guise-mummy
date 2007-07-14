package com.guiseframework.event;

/**An object that listens for selection of a value.
@param <V> The type of value being selected.
@author Garret Wilson
*/
public interface ValueSelectListener<V> extends GuiseEventListener
{

	/**Called when a value is selected.
	@param valueEvent The event indicating the value selected.
	*/
	public void valueSelected(final ValueEvent<V> valueEvent);

}
