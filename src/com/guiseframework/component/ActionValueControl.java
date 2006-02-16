package com.guiseframework.component;

/**An action control that also contains a value in its model.
@author Garret Wilson
@param <V> The type of value the action represents.
*/
public interface ActionValueControl<V, C extends ActionValueControl<V, C>> extends ActionControl<C>, ValueControl<V, C>
{
}
