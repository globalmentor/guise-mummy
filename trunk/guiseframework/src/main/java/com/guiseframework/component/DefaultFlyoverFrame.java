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

/**
 * Default implementation of a flyover frame with default layout panel. A flyover frame by default is nonmodal, immovable, and not resizable. For example, with
 * a tether bearing of 250 and a tether resource key of "myTether", a resource key will be requested using "myTether.WSW", "myTether.SWbW", "myTether.SW", etc.
 * until all compass points are exhausted, after which a resource key of "myTether" will be requested.
 * @author Garret Wilson
 */
public class DefaultFlyoverFrame extends AbstractFlyoverFrame {

	/** Default constructor. */
	public DefaultFlyoverFrame() {
		this(new LayoutPanel()); //default to a layout panel
	}

	/**
	 * Component constructor.
	 * @param component The single child component, or <code>null</code> if this frame should have no child component.
	 */
	public DefaultFlyoverFrame(final Component component) {
		super(component); //construct the parent class
	}

}
