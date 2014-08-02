/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guiseframework.component;

import com.guiseframework.component.layout.CardConstraints;

/**A card container that contains a list select model providing access to the cards.
@author Garret Wilson
*/
public interface CardControl extends CardContainer, ContainerControl, ListSelectControl<Component>
{
	/**Convenience method to determine whether a card is displayed based upon its associated constraints.
	@return Whether the card is displayed or has no representation, taking up no space.
	@throws IllegalStateException if the given component has no associated constraints.
	@see CardConstraints#isDisplayed()
	*/
	public boolean isDisplayed(final Component component);

	/**Sets a card displayed or not displayed.
	This convenience method changes the displayed status of the component's associated constraints.
	@param component The component for which the card should be displayed or not displayed.
	@param newDisplayed <code>true</code> if the card should be displayed.
	@throws IllegalStateException if the given component has no associated constraints.
	@see CardConstraints#setDisplayed(boolean)
	*/
	public void setDisplayed(final Component component, final boolean newDisplayed);

	/**Convenience method to determine whether a card is enabled based upon its associated constraints.
	@return Whether the card is enabled and can receive user input.
	@throws IllegalStateException if the given component has no associated constraints.
	@see CardConstraints#isEnabled()
	*/
	public boolean isEnabled(final Component component);

	/**Enables or disables a card.
	This convenience method changes the enabled status of the component's associated constraints.
	@param component The component for which the card should be enabled or disabled.
	@param newEnabled <code>true</code> if the card can be selected.
	@throws IllegalStateException if the given component has no associated constraints.
	@see CardConstraints#setEnabled(boolean)
	*/
	public void setEnabled(final Component component, final boolean newEnabled);

}