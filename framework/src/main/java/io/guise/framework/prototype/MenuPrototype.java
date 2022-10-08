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

package io.guise.framework.prototype;

import java.net.URI;

/**
 * Contains prototype information for a menu.
 * @author Garret Wilson
 */
public class MenuPrototype extends AbstractActionPrototype {

	/** Default constructor. */
	public MenuPrototype() {
		this(null); //construct the class with no label
	}

	/**
	 * Label constructor.
	 * @param label The text of the label, or <code>null</code> if there should be no label.
	 */
	public MenuPrototype(final String label) {
		this(label, null); //construct the label model with no icon
	}

	/**
	 * Label and icon constructor.
	 * @param label The text of the label, or <code>null</code> if there should be no label.
	 * @param icon The icon URI, which may be a resource URI, or <code>null</code> if there is no icon URI.
	 */
	public MenuPrototype(final String label, final URI icon) {
		super(label, icon); //construct the parent class
	}

	@Override
	protected void action(final int force, final int option) {
	}
}
