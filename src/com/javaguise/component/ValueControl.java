package com.javaguise.component;

import com.javaguise.model.ValueModel;

/**A control to accept input by the user of a value.
@param <V> The type of value to represent.
@author Garret Wilson
*/
public interface ValueControl<V, C extends ValueControl<V, C>> extends Control<ValueModel<V>, C>
{
}
