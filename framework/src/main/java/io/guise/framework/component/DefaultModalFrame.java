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

package io.guise.framework.component;

/**
 * Default implementation of a modal frame with a default layout panel.
 * @param <R> The type of modal result this modal frame produces.
 * @author Garret Wilson
 */
public class DefaultModalFrame<R> extends AbstractModalFrame<R> {

	/** Default constructor with a default layout panel. */
	public DefaultModalFrame() {
		this(new LayoutPanel()); //default to a layout panel
	}

	/**
	 * Component constructor.
	 * @param component The single child component, or <code>null</code> if this frame should have no child component.
	 */
	public DefaultModalFrame(final Component component) {
		super(component); //construct the parent class
	}

}
