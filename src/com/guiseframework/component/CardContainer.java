package com.guiseframework.component;

import com.guiseframework.component.layout.CardLayout;

/**Container that uses a card layout.
@author Garret Wilson
@see CardLayout
*/
public interface CardContainer extends Container
{

	/**@return The layout definition for the container.*/
	public CardLayout getLayout();

}