/*
 * Copyright © 2011 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package com.guiseframework.model;

import static java.util.Objects.*;

import com.globalmentor.net.URIPath;
import com.guiseframework.component.*;

/**
 * A navigation model that retrieves its navigation path dynamically from the indicated component.
 * <p>
 * This implementation does not allow the navigation path to be set.
 * </p>
 * @author Garret Wilson
 * @see Components#getNavigationPath(Component)
 */
public class ComponentNavigationModel extends AbstractModel implements NavigationModel {

	/** The component used for retrieving the navigation path. */
	private final Component component;

	/**
	 * Component constructor.
	 * @param component The component used for retrieving the navigation path.
	 * @throws NullPointerException if the given component is <code>null</code>.
	 */
	public ComponentNavigationModel(final Component component) {
		this.component = requireNonNull(component);
	}

	@Override
	public URIPath getNavigationPath() {
		return Components.getNavigationPath(component); //retrieve the navigation path from the component
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation does not allow the navigation path to be set.
	 * </p>
	 * @throws UnsupportedOperationException because the content cannot be changed.
	 */
	@Override
	public void setNavigationPath(final URIPath newNavigationPath) {
		throw new UnsupportedOperationException("Component navigation models do not allow the navigation path to be set.");
	}

}
