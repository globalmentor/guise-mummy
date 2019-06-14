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

import java.io.OutputStream;
import java.net.URI;

import com.globalmentor.net.ResourceIOException;
import com.globalmentor.net.URIPath;

import io.urf.model.UrfResourceDescription;
import io.urf.vocab.content.Content;

/**
 * A navigation point that sets a resource description and/or contents.
 * @author Garret Wilson
 */
public interface ResourceWriteDestination extends Destination {

	/**
	 * Retrieves an output stream to the resource. The resource may include any of the following properties:
	 * <ul>
	 * <li>TODO add <code>urf-name</code> property to spec</li>
	 * <li>{@link Content#TYPE_PROPERTY_TAG}</li>
	 * <li>{@link Content#LENGTH_PROPERTY_TAG}</li>
	 * <li>{@link Content#MODIFIED_PROPERTY_TAG}</li>
	 * </ul>
	 * @param resourceDescription The description of the resource.
	 * @param session The current Guise Session.
	 * @param navigationPath The navigation path relative to the application context path.
	 * @param bookmark The bookmark for this navigation path, or <code>null</code> if there is no bookmark involved in navigation.
	 * @param referrerURI The URI of the referring component or other entity with no query or fragment, or <code>null</code> if no referring URI is known.
	 * @return An output stream to the given resource.
	 * @throws NullPointerException if the given navigation path is <code>null</code>.
	 * @throws ResourceIOException Thrown if there is an error writing to the resource.
	 */
	public OutputStream getOutputStream(final UrfResourceDescription resourceDescription, final GuiseSession session, final URIPath navigationPath,
			final Bookmark bookmark, final URI referrerURI) throws ResourceIOException;

}
