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

import java.io.InputStream;
import java.net.URI;

import com.globalmentor.net.ResourceIOException;
import com.globalmentor.net.URIPath;

import io.urf.model.UrfResourceDescription;
import io.urf.vocab.content.Content;

/**
 * A navigation point that retrieves a resource description and/or contents.
 * @author Garret Wilson
 */
public interface ResourceReadDestination extends Destination {

	/**
	 * Returns a description of the resource. The resource should include valid values for the following properties:
	 * <ul>
	 * <li>{@link Content#TYPE_PROPERTY_TAG}</li>
	 * <li>{@link Content#LENGTH_PROPERTY_TAG}</li>
	 * <li>{@link Content#MODIFIED_AT_PROPERTY_TAG}</li>
	 * </ul>
	 * @param session The current Guise Session.
	 * @param navigationPath The navigation path relative to the application context path.
	 * @param bookmark The bookmark for which navigation should occur at this navigation path, or <code>null</code> if there is no bookmark involved in
	 *          navigation.
	 * @param referrerURI The URI of the referring component or other entity with no query or fragment, or <code>null</code> if no referring URI is known.
	 * @return A description of the resource.
	 * @throws NullPointerException if the given navigation path is <code>null</code>.
	 * @throws ResourceIOException if there is an error retrieving the resource description.
	 */
	public UrfResourceDescription getResourceDescription(final GuiseSession session, final URIPath navigationPath, final Bookmark bookmark, final URI referrerURI)
			throws ResourceIOException;

	/**
	 * Retrieves an input stream to the resource.
	 * @param session The current Guise Session.
	 * @param navigationPath The navigation path relative to the application context path.
	 * @param bookmark The bookmark for which navigation should occur at this navigation path, or <code>null</code> if there is no bookmark involved in
	 *          navigation.
	 * @param referrerURI The URI of the referring component or other entity with no query or fragment, or <code>null</code> if no referring URI is known.
	 * @return An input stream to the given resource.
	 * @throws NullPointerException if the given navigation path is <code>null</code>.
	 * @throws ResourceIOException Thrown if there is an error accessing the resource, such as a missing file.
	 */
	public InputStream getInputStream(final GuiseSession session, final URIPath navigationPath, final Bookmark bookmark, final URI referrerURI)
			throws ResourceIOException;

}
