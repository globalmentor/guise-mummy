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

package io.guise.framework;

import java.net.URI;

import static java.util.Objects.*;

import com.globalmentor.net.URIPath;

import io.guise.framework.model.DefaultInfoModel;

/**
 * Encapsulates navigation information for particular location, such as the segment of a navigation URI.
 * @author Garret Wilson
 */
public class Breadcrumb extends DefaultInfoModel {

	/** The navigation path this breadcrumb represents. */
	private final URIPath navigationPath;

	/** @return The navigation path this breadcrumb represents. */
	public URIPath getNavigationPath() {
		return navigationPath;
	}

	/**
	 * Navigation path constructor.
	 * @param navigationPath The navigation path this breadcrumb represents.
	 * @throws NullPointerException if the given navigation path is <code>null</code>.
	 */
	public Breadcrumb(final URIPath navigationPath) {
		this(navigationPath, null); //construct the class with no label
	}

	/**
	 * Navigation path and Label constructor.
	 * @param navigationPath The navigation path this breadcrumb represents.
	 * @param labelText The text of the label, or <code>null</code> if there should be no label.
	 */
	public Breadcrumb(final URIPath navigationPath, final String labelText) {
		this(navigationPath, labelText, null); //construct the label model with no glyph
	}

	/**
	 * Navigation path, label, and glyph URI constructor.
	 * @param navigationPath The navigation path this breadcrumb represents.
	 * @param labelText The text of the label, or <code>null</code> if there should be no label.
	 * @param glyphURI The glyph URI, which may be a resource URI, or <code>null</code> if there is no glyph URI.
	 */
	public Breadcrumb(final URIPath navigationPath, final String labelText, final URI glyphURI) {
		super(labelText, glyphURI); //construct the parent class
		this.navigationPath = requireNonNull(navigationPath, "navigation path cannot be null.");
	}

}
