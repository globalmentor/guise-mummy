package com.guiseframework.component;

import com.guiseframework.model.ValueModel;

/**A frame for communication of a value.
A dialog frame by default is modal and movable but not resizable.
@param <V> The value to be communicated.
@author Garret Wilson
*/
public interface DialogFrame<V, C extends DialogFrame<V, C>> extends Frame<C>
{

	/**@return The data model used by this component.*/
	public ValueModel<V> getModel();

}
