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
import java.util.*;
import static java.util.Collections.*;
import static java.util.Objects.*;
import java.util.regex.Pattern;

import org.urframework.URFResource;

import com.globalmentor.beans.BoundPropertyObject;
import com.globalmentor.java.Objects;
import com.globalmentor.net.*;

import static com.globalmentor.net.URIs.*;

/**
 * Abstract implementation of a navigation point, its properties, and its restrictions. Destinations of identical types with identical paths and path patterns
 * are considered equal.
 * @author Garret Wilson
 */
public abstract class AbstractDestination extends BoundPropertyObject implements Destination {

	/** The map of sub-categories; it is not thread-safe, but any changes will simply create a new list. */
	private List<Category> categories = unmodifiableList(new ArrayList<Category>()); //TODO add a property and fire a change

	@Override
	public Iterable<Category> getCategories() {
		return categories;
	}

	@Override
	public void setCategories(final List<Category> categories) {
		this.categories = unmodifiableList(new ArrayList<Category>(categories)); //create a copy of the list and save the list
	}

	/**
	 * The application context-relative path within the Guise container context, which does not begin with '/', or <code>null</code> if there is no path specified
	 * for this destination.
	 */
	private final URIPath path;

	@Override
	public URIPath getPath() {
		return path;
	}

	/**
	 * The pattern to match an application context-relative path within the Guise container context, which does not begin with '/', or <code>null</code> if there
	 * is no path pattern specified for this destination.
	 */
	private final Pattern pathPattern;

	@Override
	public Pattern getPathPattern() {
		return pathPattern;
	}

	/**
	 * Path constructor.
	 * @param path The application context-relative path within the Guise container context, which does not begin with '/'.
	 * @throws NullPointerException if the path is <code>null</code>.
	 * @throws IllegalArgumentException if the provided path is absolute.
	 */
	public AbstractDestination(final URIPath path) {
		this.path = requireNonNull(path, "Navigation path cannot be null.").checkRelative(); //store the path, making sure it is relative
		this.pathPattern = null; //indicate that there is no path pattern
	}

	/**
	 * Path pattern constructor.
	 * @param pathPattern The pattern to match an application context-relative path within the Guise container context, which does not begin with '/'.
	 * @throws NullPointerException if the path pattern is <code>null</code>.
	 */
	public AbstractDestination(final Pattern pathPattern) {
		this.pathPattern = requireNonNull(pathPattern, "Navigation path pattern cannot be null.");
		this.path = null; //indicate that there is no path
	}

	@Override
	public URIPath getPath(final GuiseSession session, final URIPath path, final Bookmark bookmark, final URI referrerURI) throws ResourceIOException {
		if(!exists(session, path, bookmark, referrerURI)) { //if this destination doesn't exist	
			if(!path.isCollection()) { //if a non-collection path was requested
				final URIPath collectionPath = new URIPath(path.toString() + PATH_SEPARATOR); //create a collection version of the path
				if(exists(session, collectionPath, bookmark, referrerURI)) { //if the collection form of the path exists
					return collectionPath; //return the collection path
				}
			}
		}
		return path; //return the unmodified path by default
	}

	@Override
	public boolean exists(final GuiseSession session, final URIPath navigationPath, final Bookmark bookmark, final URI referrerURI) throws ResourceIOException {
		return true; //make it easy for simple resource destinations by assuming the resource exists
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation authorizes all resources.
	 * </p>
	 */
	@Override
	public boolean isAuthorized(final GuiseSession session, final URIPath navigationPath, final Bookmark bookmark, final URI referrerURI)
			throws ResourceIOException {
		return true; //by default authorize all resources
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation returns <code>null</code>.
	 * </p>
	 */
	@Override
	public URFResource getDescription(final GuiseSession session, final URIPath navigationPath, final Bookmark bookmark, final URI referrerURI)
			throws ResourceIOException {
		return null;
	}

	@Override
	public int hashCode() {
		return Objects.getHashCode(getPath(), getPathPattern()); //construct a hash code from the path and path pattern
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation considers destinations of identical types with identical paths and path patterns to be equivalent.
	 * </p>
	 */
	@Override
	public boolean equals(final Object object) {
		if(getClass().isInstance(object)) { //if the given object is an instance of this object's class
			final Destination destination = (Destination)object; //cast the object to a destination (which it must be, if it's the same type as this instance
			return Objects.equals(getPath(), destination.getPath()) && Objects.equals(getPathPattern(), destination.getPathPattern()); //see if the paths and path patterns match 
		}
		return false; //indicate that the objects don't match
	}
}
