package com.guiseframework.component;

/**An action control with link semantics.
@author Garret Wilson
*/
public interface LinkControl<C extends LinkControl<C>> extends LabelComponent<C>, ActionControl<C>, LabelDisplayableComponent<C>
{
}
