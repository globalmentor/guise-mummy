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

package io.guise.framework.component;

import io.guise.framework.component.layout.CardLayout;

/**
 * A tabbed panel with a card layout. The panel's value model reflects the currently selected component, if any.
 * @author Garret Wilson
 * @see CardLayout
 */
public class TabbedPanel extends AbstractCardPanel {

	/** Default constructor. */
	public TabbedPanel() {
		this(new CardLayout()); //construct the panel using a default layout
	}

	/**
	 * Layout constructor.
	 * @param layout The layout definition for the container.
	 * @throws NullPointerException if the given layout is <code>null</code>.
	 */
	protected TabbedPanel(final CardLayout layout) {
		super(layout); //construct the parent class, using the card layout's value model
	}

}
