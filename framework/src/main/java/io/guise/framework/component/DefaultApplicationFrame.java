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

import static io.guise.framework.Resources.*;

import io.guise.framework.Resources;

/**
 * Default implementation of an application frame with no default component.
 * @author Garret Wilson
 */
public class DefaultApplicationFrame extends AbstractApplicationFrame {

	/** Default constructor. */
	public DefaultApplicationFrame() {
		this(null); //construct the class with no child component
	}

	/**
	 * Component constructor.
	 * @param component The single child component, or <code>null</code> if this frame should have no child component.
	 */
	public DefaultApplicationFrame(final Component component) {
		super(component); //construct the parent class
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation returns a string reference to the application name.
	 * </p>
	 */
	@Override
	protected String getBasePlainLabel() {
		return APPLICATION_NAME;
	}

}
