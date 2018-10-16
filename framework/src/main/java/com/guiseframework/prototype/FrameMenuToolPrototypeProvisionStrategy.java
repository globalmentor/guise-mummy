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

package com.guiseframework.prototype;

import com.guiseframework.component.*;

/**
 * The default strategy for keeping track of prototype providers and working with provisioned prototypes, merging them into the current menu and/or a toolbar of
 * a frame. When the prototype providers change provided prototypes, those provided prototypes are processed. This version monitors the parent composite
 * component children and automatically uses top-level prototype providers added to or removed from the hierarchy. Prototype provisions are not processed
 * initially; this strategy should be initialized after construction by calling {@link #processPrototypeProvisions()}. This class is thread safe based upon its
 * exposed read and write locks.
 * @author Garret Wilson
 */
public class FrameMenuToolPrototypeProvisionStrategy extends AbstractMenuToolCompositeComponentPrototypeProvisionStrategy {

	@Override
	protected final Frame getParentComponent() {
		return (Frame)super.getParentComponent();
	}

	@Override
	protected Menu getMenu() {
		return getParentComponent().getMenu();
	}

	@Override
	protected Toolbar getToolbar() {
		return getParentComponent().getToolbar();
	}

	/**
	 * Frame prototype providers constructor.
	 * @param frame The composite component the top-level prototype provider children of which will be monitored.
	 * @param defaultPrototypeProviders The default prototype providers that will provide prototypes for processing, outside the children of the composite
	 *          component parent.
	 * @throws NullPointerException if the given parent component, prototype providers, and/or one or more prototype provider is <code>null</code>.
	 */
	public FrameMenuToolPrototypeProvisionStrategy(final Frame frame, final PrototypeProvider... defaultPrototypeProviders) {
		super(frame, defaultPrototypeProviders); //construct the parent class
	}
}
