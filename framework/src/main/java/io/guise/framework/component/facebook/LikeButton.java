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

package io.guise.framework.component.facebook;

import static java.util.Objects.*;

import com.globalmentor.net.URIPath;

import io.guise.framework.component.AbstractComponent;
import io.guise.framework.model.ComponentNavigationModel;
import io.guise.framework.model.DefaultNavigationModel;
import io.guise.framework.model.NavigationModel;

/**
 * A component representing a Facebook Like button.
 * @author Garret Wilson
 */
public class LikeButton extends AbstractComponent implements NavigationModel {

	private final NavigationModel navigationModel;

	/** @return The internal navigation model. */
	protected NavigationModel getNavigationModel() {
		return navigationModel;
	}

	@Override
	public URIPath getNavigationPath() {
		return getNavigationModel().getNavigationPath();
	}

	@Override
	public void setNavigationPath(final URIPath newNavigationPath) {
		getNavigationModel().setNavigationPath(newNavigationPath);
	}

	/**
	 * Default constructor. The navigation path will dynamically be determined based upon the hierarchy in which this component is installed.
	 */
	public LikeButton() {
		this.navigationModel = new ComponentNavigationModel(this);
	}

	/**
	 * Navigation path constructor.
	 * @param navigationPath The navigation path, or <code>null</code> if the navigation path is not available.
	 */
	public LikeButton(final URIPath navigationPath) {
		this(new DefaultNavigationModel(navigationPath));
	}

	/**
	 * Navigation model constructor.
	 * @param navigationModel The model for retrieving the navigation path.
	 * @throws NullPointerException if the given navigation model is <code>null</code>.
	 */
	public LikeButton(final NavigationModel navigationModel) {
		this.navigationModel = requireNonNull(navigationModel);
	}
}
