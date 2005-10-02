package com.javaguise.component;

import com.javaguise.model.ValueModel;

/**A frame for communication of a value.
@param <V> The value to be communicated.
@author Garret Wilson
*/
public interface DialogFrame<V, C extends DialogFrame<V, C>> extends CompositeComponent<C>
{

	/**@return The data model used by this component.*/
	public ValueModel<V> getModel();

}
