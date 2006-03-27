package com.guiseframework.component;

/**A container that is also a control.
@author Garret Wilson
*/
public interface ContainerControl<C extends ContainerControl<C>> extends Container<C>, Control<C>
{
}
