package com.guiseframework.component.layout;

import com.guiseframework.model.*;

/**A layout that manages child components as an ordered stack of cards.
Only one child component is visible at a time.
The card layout maintains its own value model that maintains the current selected card.
If a card implements {@link Activeable} the card is set as active when selected and set as inactive when the card is unselected.
@author Garret Wilson
*/
public class CardLayout extends AbstractValueLayout<CardConstraints>
{

	/**@return The class representing the type of constraints appropriate for this layout.*/
	public Class<? extends CardConstraints> getConstraintsClass() {return CardConstraints.class;}

	/**Creates default constraints for the container.
	@return New default constraints for the container.
	*/
	public CardConstraints createDefaultConstraints()
	{
		return new CardConstraints();	//create constraints with a default label model
	}

}
