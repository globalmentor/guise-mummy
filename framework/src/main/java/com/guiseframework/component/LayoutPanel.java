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

import com.guiseframework.component.layout.*;

/**
 * A general panel with a default page flow layout. This class, which has no particular semantics, is used for laying out child components without providing
 * extra arrangement such as inter-child-component spacing.
 * @author Garret Wilson
 */
public class LayoutPanel extends AbstractPanel {

	/** Default constructor with a default page flow layout. */
	public LayoutPanel() {
		this(new FlowLayout(Flow.PAGE)); //default to flowing vertically
	}

	/**
	 * Layout constructor.
	 * @param layout The layout definition for the container.
	 * @throws NullPointerException if the given layout is <code>null</code>.
	 */
	public LayoutPanel(final Layout<?> layout) {
		super(layout); //construct the parent class
	}
}
