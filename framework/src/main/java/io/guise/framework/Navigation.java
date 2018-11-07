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

package io.guise.framework;

import java.net.URI;

import static java.util.Objects.*;

/**
 * The encapsulation of a point of navigation.
 * @author Garret Wilson
 */
public class Navigation {

	/** The old point of navigation, either absolute or application-relative. */
	private final URI oldNavigationURI;

	/** @return The old point of navigation, either absolute or application-relative. */
	public URI getOldNavigationURI() {
		return oldNavigationURI;
	}

	/** The new point of navigation, either absolute or application-relative. */
	private final URI newNavigationURI;

	/** @return The new point of navigation, either absolute or application-relative. */
	public URI getNewNavigationURI() {
		return newNavigationURI;
	}

	/** The ID of the viewport in which navigation should occur, or <code>null</code> if navigation should occur in the current viewport. */
	private final String viewportID;

	/** @return The ID of the viewport in which navigation should occur, or <code>null</code> if navigation should occur in the current viewport.. */
	public String getViewportID() {
		return viewportID;
	}

	/**
	 * Creates an object encapsulating a point of navigation.
	 * @param oldNavigationURI The old point of navigation, either absolute or application-relative.
	 * @param newNavigationURI The new point of navigation, either absolute or application-relative.
	 * @throws NullPointerException if one of the navigation URIs is <code>null</code>, or does not contain a path.
	 */
	public Navigation(final URI oldNavigationURI, final URI newNavigationURI) {
		this(oldNavigationURI, newNavigationURI, null); //construct a navigation object with no viewport ID
	}

	/**
	 * Creates an object encapsulating a point of navigation.
	 * @param oldNavigationURI The old point of navigation, either absolute or application-relative.
	 * @param newNavigationURI The new point of navigation, either absolute or application-relative.
	 * @param viewportID The ID of the viewport in which navigation should occur, or <code>null</code> if navigation should occur in the current viewport.
	 * @throws NullPointerException if one of the navigation URIs is <code>null</code>, or does not contain a path.
	 */
	public Navigation(final URI oldNavigationURI, final URI newNavigationURI, final String viewportID) {
		this.oldNavigationURI = requireNonNull(oldNavigationURI, "Old navigation URI cannot be null.");
		this.newNavigationURI = requireNonNull(newNavigationURI, "New navigation URI cannot be null.");
		this.viewportID = viewportID;
	}
}
