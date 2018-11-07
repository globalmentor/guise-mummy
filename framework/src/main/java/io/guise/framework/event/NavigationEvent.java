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

package io.guise.framework.event;

import java.net.URI;

import static java.util.Objects.*;

import com.globalmentor.net.URIPath;

import io.guise.framework.Bookmark;

/**
 * An event indicating that navigation has occurred. The source of this event is the Guise session in which navigation occurred.
 * @author Garret Wilson
 */
public class NavigationEvent extends AbstractGuiseEvent {

	/** The navigation path relative to the application context path. */
	private final URIPath navigationPath;

	/** @return The navigation path relative to the application context path. */
	public URIPath getNavigationPath() {
		return navigationPath;
	}

	/** The bookmark for which navigation should occur at the navigation path, or <code>null</code> if there is no bookmark involved in navigation. */
	private final Bookmark bookmark;

	/** @return The bookmark for which navigation should occur at the navigation path, or <code>null</code> if there is no bookmark involved in navigation. */
	public Bookmark getBookmark() {
		return bookmark;
	}

	/** The URI of the referring navigation panel or other entity with no query or fragment, or <code>null</code> if no referring URI is known. */
	private final URI referrerURI;

	/** @return The URI of the referring navigation panel or other entity with no query or fragment, or <code>null</code> if no referring URI is known. */
	public URI getReferrerURI() {
		return referrerURI;
	}

	/**
	 * Source constructor.
	 * @param source The object on which the event initially occurred.
	 * @param navigationPath The navigation path relative to the application context path.
	 * @param bookmark The bookmark for which navigation should occur at this navigation path, or <code>null</code> if there is no bookmark involved in
	 *          navigation.
	 * @param referrerURI The URI of the referring navigation panel or other entity with no query or fragment, or <code>null</code> if no referring URI is known.
	 * @throws NullPointerException if the given source, and/or navigation path is <code>null</code>.
	 */
	public NavigationEvent(final Object source, final URIPath navigationPath, final Bookmark bookmark, final URI referrerURI) {
		super(source); //construct the parent class
		this.navigationPath = requireNonNull(navigationPath, "Navigation path cannot be null.");
		this.bookmark = bookmark;
		this.referrerURI = referrerURI;
	}

}
