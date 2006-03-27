package com.guiseframework.component;

import com.guiseframework.component.layout.CardConstraints;

/**A card container that contains a list select model providing access to the cards.
@author Garret Wilson
*/
public interface CardControl<C extends CardControl<C>> extends CardContainer<C>, ContainerControl<C>, ListSelectControl<Component<?>, C>
{
	/**Convenience method to determine whether a card is displayed based upon its associated constraints.
	@return Whether the card is displayed or has no representation, taking up no space.
	@exception IllegalStateException if the given component has no associated constraints.
	@see CardConstraints#isDisplayed()
	*/
	public boolean isDisplayed(final Component<?> component);

	/**Sets a card displayed or not displayed.
	This convenience method changes the displayed status of the component's associated constraints.
	@param component The component for which the card should be displayed or not displayed.
	@param newDisplayed <code>true</code> if the card should be displayed.
	@exception IllegalStateException if the given component has no associated constraints.
	@see CardConstraints#setDisplayed(boolean)
	*/
	public void setDisplayed(final Component<?> component, final boolean newDisplayed);

	/**Convenience method to determine whether a card is enabled based upon its associated constraints.
	@return Whether the card is enabled and can receive user input.
	@exception IllegalStateException if the given component has no associated constraints.
	@see CardConstraints#isEnabled()
	*/
	public boolean isEnabled(final Component<?> component);

	/**Enables or disables a card.
	This convenience method changes the enabled status of the component's associated constraints.
	@param component The component for which the card should be enabled or disabled.
	@param newEnabled <code>true</code> if the card can be selected.
	@exception IllegalStateException if the given component has no associated constraints.
	@see CardConstraints#setEnabled(boolean)
	*/
	public void setEnabled(final Component<?> component, final boolean newEnabled);

}