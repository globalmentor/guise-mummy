package com.guiseframework.component;

/**A frame for communication of a value.
A dialog frame by default is modal and movable but not resizable.
@param <V> The value to be communicated.
@author Garret Wilson
*/
public interface DialogFrame<V, C extends DialogFrame<V, C>> extends Frame<C>, ValueControl<V, C>
{
}
