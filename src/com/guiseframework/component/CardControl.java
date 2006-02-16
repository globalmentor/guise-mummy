package com.guiseframework.component;

/**A card container that contains a list select model providing access to the cards.
@author Garret Wilson
*/
public interface CardControl<C extends CardControl<C>> extends CardContainer<C>, ListSelectControl<Component<?>, C>
{
}