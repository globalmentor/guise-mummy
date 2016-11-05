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

import static java.util.Objects.*;

import com.guiseframework.component.layout.CardLayout;
import com.guiseframework.component.layout.Flow;
import com.guiseframework.model.*;

/**
 * Convenience tab control that automatically controls the selected card of a card control.
 * @author Garret Wilson
 * @see CardContainer
 */
public class CardTabControl extends TabControl<Component> {

	/**
	 * Card control and axis constructor.
	 * @param cardControl The card control to be controlled.
	 * @param axis The axis along which the tabs are oriented.
	 * @throws NullPointerException if the given card control and/or axis is <code>null</code>.
	 */
	public CardTabControl(final CardControl cardControl, final Flow axis) {
		this(cardControl, axis, -1); //construct the class with no maximum tab count
	}

	/**
	 * Card control, axis, and maximum tab count constructor.
	 * @param cardControl The card control to be controlled.
	 * @param axis The axis along which the tabs are oriented.
	 * @param maxTabCount The requested number of visible tabs, or -1 if no maximum tab count is specified.
	 * @throws NullPointerException if the given card control and/or axis is <code>null</code>.
	 */
	public CardTabControl(final CardControl cardControl, final Flow axis, final int maxTabCount) {
		this(cardControl, new CardRepresentationStrategy(cardControl.getLayout()), axis, maxTabCount); //construct the class with a default representation strategy
	}

	/**
	 * Card control, value representation strategy, and axis constructor.
	 * @param cardControl The card control to be controlled.
	 * @param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	 * @param axis The axis along which the tabs are oriented.
	 * @throws NullPointerException if the given card control, value representation strategy, and/or axis is <code>null</code>.
	 */
	public CardTabControl(final CardControl cardControl, final ValueRepresentationStrategy<Component> valueRepresentationStrategy, final Flow axis) {
		this(cardControl, valueRepresentationStrategy, axis, -1); //construct the class with no maximum tab count
	}

	/**
	 * Card control, value representation strategy, axis, and maximum tab count constructor.
	 * @param cardControl The card control to be controlled.
	 * @param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	 * @param axis The axis along which the tabs are oriented.
	 * @param maxTabCount The requested number of visible tabs, or -1 if no maximum tab count is specified.
	 * @throws NullPointerException if the given card control, value representation strategy, and/or axis is <code>null</code>.
	 */
	public CardTabControl(final CardControl cardControl, final ValueRepresentationStrategy<Component> valueRepresentationStrategy, final Flow axis,
			final int maxTabCount) {
		super(cardControl, valueRepresentationStrategy, axis, maxTabCount); //construct the parent class using the card container's model
	}

	/**
	 * A value representation strategy for representing cards. A label component will be generated based on the card layout information. The value's ID will be
	 * the card component's ID.
	 * @see Label
	 * @author Garret Wilson
	 */
	public static class CardRepresentationStrategy implements ValueRepresentationStrategy<Component> {

		/** The card layout containing component layout information. */
		private final CardLayout cardLayout;

		/** @return The card layout containing component layout information. */
		public final CardLayout getCardLayout() {
			return cardLayout;
		}

		/**
		 * Card layout constructor.
		 * @param cardLayout The card layout containing component layout information.
		 * @throws NullPointerException if the given card layout is <code>null</code>.
		 */
		public CardRepresentationStrategy(final CardLayout cardLayout) {
			this.cardLayout = requireNonNull(cardLayout, "Card layout cannot be null"); //save the card layout
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * This implementation returns a label with information from the card layout.
		 * </p>
		 */
		@Override
		public Label createComponent(final ListSelectModel<Component> model, final Component value, final int index, final boolean selected, final boolean focused) {
			return value != null //if there is a value
			? new Label(getCardLayout().getConstraints(value)) //generate a label using the the card layout constraints as the label model
					: new Label(); //otherwise return an empty label
		}
	}

}
