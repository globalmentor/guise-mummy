package com.garretwilson.guise.component;

import com.garretwilson.guise.model.ValueModel;

/**A control to accept input from the user.
@param <V> The type of value to represent.
@author Garret Wilson
*/
public interface ValueControl<V, C extends ValueControl<V, C>> extends Control<ValueModel<V>, C>
{
}
