package com.javaguise.component;

import com.javaguise.component.layout.CardLayout;

/**Container that uses a card layout.
@author Garret Wilson
@see CardLayout
*/
public interface CardContainer<C extends CardContainer<C>> extends Container<C>
{

	/**@return The layout definition for the container.*/
	public CardLayout getLayout();

}