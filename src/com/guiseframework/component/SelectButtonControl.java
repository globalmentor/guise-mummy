package com.guiseframework.component;

/**A button that keeps track of its selected state.
@author Garret Wilson
*/
public interface SelectButtonControl<C extends SelectButtonControl<C>> extends SelectActionControl<C>, ButtonControl<C>
{
}
