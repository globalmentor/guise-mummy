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

import com.globalmentor.java.Objects;
import com.globalmentor.net.URIPath;

/**
 * A default implementation of a model for identifying a navigation path.
 * @author Garret Wilson
 */
public class DefaultNavigationModel extends AbstractModel implements NavigationModel {

	private URIPath navigationPath;

	/** Default constructor. */
	public DefaultNavigationModel() {
		this(null);
	}

	/**
	 * Navigation path constructor.
	 * @param navigationPath The navigation path, or <code>null</code> if the navigation path is not available.
	 */
	public DefaultNavigationModel(final URIPath navigationPath) {
		this.navigationPath = navigationPath;
	}

	@Override
	public URIPath getNavigationPath() {
		return navigationPath;
	}

	@Override
	public void setNavigationPath(final URIPath newNavigationPath) {
		if(!Objects.equals(navigationPath, newNavigationPath)) { //if the value is really changing
			final URIPath oldNavigationPath = navigationPath; //get the old value
			navigationPath = newNavigationPath; //actually change the value
			firePropertyChange(NAVIGATION_PATH_PROPERTY, oldNavigationPath, newNavigationPath); //indicate that the value changed
		}
	}

	@Override
	public String toString() {
		final URIPath navigationPath = getNavigationPath(); //get the navigation path, if any
		return navigationPath != null ? getClass().getName() + ": " + navigationPath : super.toString(); //return the class and navigation path, or the default string if there is no navigation path
	}
}
