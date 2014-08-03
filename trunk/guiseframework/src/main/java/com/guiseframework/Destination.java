/*
 * Copyright © 2005-2011 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package com.guiseframework;

import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;

import org.urframework.URFResource;

import com.globalmentor.beans.PropertyBindable;
import com.globalmentor.net.*;

/**
 * Description of a navigation point, its properties, and its restrictions.
 * @author Garret Wilson
 */
public interface Destination extends PropertyBindable {

	/**
	 * @return The application context-relative path within the Guise container context, which does not begin with '/', or <code>null</code> if there is no path
	 *         specified for this destination.
	 */
	public URIPath getPath();

	/**
	 * @return The pattern to match an application context-relative path within the Guise container context, which does not begin with '/', or <code>null</code>
	 *         if there is no path pattern specified for this destination.
	 */
	public Pattern getPathPattern();

	/** The read-only iterable of categories. */
	public Iterable<Category> getCategories();

	/**
	 * Sets the categories.
	 * @param categories The list of new categories.
	 */
	public void setCategories(final List<Category> categories);

	/**
	 * Determines the path to use for the requested path. If there is a preferred path, it is returned; otherwise, the path is returned unmodified. If there is no
	 * principal or the principal is not the owner of the identified resource; the determined path is a collection path; and there exists a discoverable home page
	 * in the collection, this version returns the path to the home page.
	 * @param session The current Guise session.
	 * @param navigationPath The navigation path relative to the application context path.
	 * @param bookmark The bookmark for this path, or <code>null</code> if there is no bookmark.
	 * @param referrerURI The URI of the referring destination or other entity with no query or fragment, or <code>null</code> if no referring URI is known.
	 * @return The preferred path.
	 * @throws NullPointerException if the given session and/or path is <code>null</code>.
	 * @throws ResourceIOException if there is an error accessing the resource.
	 */
	public URIPath getPath(final GuiseSession session, URIPath navigationPath, final Bookmark bookmark, final URI referrerURI) throws ResourceIOException;

	/**
	 * Determines if the given path does indeed exist for this destination.
	 * @param session The current Guise session.
	 * @param navigationPath The navigation path relative to the application context path.
	 * @param bookmark The bookmark for which navigation should occur at this navigation path, or <code>null</code> if there is no bookmark involved in
	 *          navigation.
	 * @param referrerURI The URI of the referring navigation panel or other entity with no query or fragment, or <code>null</code> if no referring URI is known.
	 * @return Whether the requested path exists.
	 * @throws NullPointerException if the given navigation path is <code>null</code>.
	 * @throws ResourceIOException if there is an error accessing the resource.
	 */
	public boolean exists(final GuiseSession session, final URIPath navigationPath, final Bookmark bookmark, final URI referrerURI) throws ResourceIOException;

	/**
	 * Determines if access to the given path is authorized for access by the current user, if any.
	 * <p>
	 * The result of this method for resources that do not exist is undefined; it is assumed that this method will not be called unless
	 * {@link #exists(GuiseSession, URIPath, Bookmark, URI)} returns <code>true</code> for that resource, although this method must not produce an error if the
	 * resource does not exist.
	 * </p>
	 * <p>
	 * This method allows the underlying platform to handle unauthorized resources. If this implementation wishes to handle unauthorized resources, this method
	 * should return <code>true</code> and provide a resource that indicates the true resource is unauthorized or at the appropriate time redirect to an
	 * unauthorized indication page.
	 * </p>
	 * @param session The current Guise session.
	 * @param navigationPath The navigation path relative to the application context path.
	 * @param bookmark The bookmark for which navigation should occur at this navigation path, or <code>null</code> if there is no bookmark involved in
	 *          navigation.
	 * @param referrerURI The URI of the referring navigation panel or other entity with no query or fragment, or <code>null</code> if no referring URI is known.
	 * @return Whether the requested path exists.
	 * @throws NullPointerException if the given navigation path is <code>null</code>.
	 * @throws ResourceIOException if there is an error accessing the resource.
	 */
	public boolean isAuthorized(final GuiseSession session, final URIPath navigationPath, final Bookmark bookmark, final URI referrerURI)
			throws ResourceIOException;

	/**
	 * Returns a description of the resource for this destination at the given navigation path and bookmark.
	 * @param session The current Guise session.
	 * @param navigationPath The navigation path relative to the application context path.
	 * @param bookmark The bookmark for which navigation should occur at this navigation path, or <code>null</code> if there is no bookmark involved in
	 *          navigation.
	 * @param referrerURI The URI of the referring navigation panel or other entity with no query or fragment, or <code>null</code> if no referring URI is known.
	 * @return A description of the indicated navigation path for this destination, or <code>null</code> if nothing exists at the given navigation path.
	 * @throws IllegalArgumentException If the given navigation path is not a valid path serviced by this destination.
	 * @throws ResourceIOException if there is an error accessing the resource.
	 */
	public URFResource getDescription(final GuiseSession session, final URIPath navigationPath, final Bookmark bookmark, final URI referrerURI)
			throws ResourceIOException;

}
