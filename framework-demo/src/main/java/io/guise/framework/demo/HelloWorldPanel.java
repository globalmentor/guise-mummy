/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.demo;

import io.guise.framework.component.*;
import io.guise.framework.component.layout.RegionLayout;

/**
 * Hello World Guise demonstration panel. Copyright © 2005-2007 GlobalMentor, Inc. Demonstrates layout panels, region layouts, and headings.
 * @author Garret Wilson
 */
public class HelloWorldPanel extends LayoutPanel {

	/** Default constructor. */
	public HelloWorldPanel() {
		super(new RegionLayout()); //construct the parent class, using a region layout
		setLabel("Guise\u2122 Demonstration: Hello World"); //set the panel title

		final Heading helloWorldHeading = new Heading(0); //create a top-level heading
		helloWorldHeading.setLabel("Hello World!"); //set the text of the heading, using its model
		add(helloWorldHeading); //add the heading to the panel in the default center
	}

}
