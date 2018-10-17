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

package com.guiseframework.component.layout;

import com.guiseframework.model.*;

/**
 * A layout that manages child components as an ordered stack of cards. Only one child component is visible at a time. The card layout maintains its own value
 * model that maintains the current selected card. If a card implements {@link Activeable} the card is set as active when selected and set as inactive when the
 * card is unselected.
 * @author Garret Wilson
 */
public class CardLayout extends AbstractValueLayout<CardConstraints> {

	@Override
	public Class<? extends CardConstraints> getConstraintsClass() {
		return CardConstraints.class;
	}

	@Override
	public CardConstraints createDefaultConstraints() {
		return new CardConstraints(); //create constraints with a default label model
	}

}
