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

import static com.guiseframework.theme.Theme.GLYPH_BUSY;
import static com.guiseframework.theme.Theme.MESSAGE_BUSY;

import com.guiseframework.component.layout.*;

/**
 * The default panel used to indicate Guise busy status.
 * @author Garret Wilson
 */
public class BusyPanel extends LayoutPanel {

	/** Default constructor. */
	public BusyPanel() {
		super(new RegionLayout()); //construct the parent class with a region layout
		final Label label = new Label(); //create a new label
		label.setGlyphURI(GLYPH_BUSY); //show the busy icon
		label.setLabel(MESSAGE_BUSY); //show the busy message
		add(label, new RegionConstraints(Region.CENTER)); //put the label in the center
	}

}
