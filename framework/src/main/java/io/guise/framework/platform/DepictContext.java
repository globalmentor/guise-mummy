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

package io.guise.framework.platform;

import java.net.URI;

import com.globalmentor.beans.PropertyBindable;
import com.globalmentor.net.URIPath;

import io.guise.framework.*;

/**
 * Encapsulation of information related to the current depiction.
 * @author Garret Wilson
 */
public interface DepictContext extends PropertyBindable {

	/** @return The Guise user session of which this context is a part. */
	public GuiseSession getSession();

	/** @return The platform on which Guise objects are depicted. */
	public Platform getPlatform();

	/** @return The destination with which this context is associated. */
	public Destination getDestination();

	/** @return The current full absolute URI for this depiction, including any query. */
	public URI getDepictionURI();

	/**
	 * Determines the URI to use for depiction based upon a navigation path. The path will first be dereferenced for the current session and then resolved to the
	 * application. The resulting URI may not be absolute, but can be made absolute by resolving it against the depiction root URI. This method is equivalent to
	 * calling {@link GuiseSession#getDepictionURI(URIPath, String...)}.
	 * @param navigationPath The navigation path, which may be absolute or relative to the application.
	 * @param suffixes The suffixes, if any, to append to a resource key in a URI reference.
	 * @return A URI suitable for depiction, deferenced and resolved to the application.
	 * @see GuiseSession#getDepictionURI(URIPath, String...)
	 */
	public URI getDepictionURI(final URIPath navigationPath, final String... suffixes);

	/**
	 * Determines the URI to use for depiction based upon a navigation URI. The URI will first be dereferenced for the current session and then resolved to the
	 * application. The resulting URI may not be absolute, but can be made absolute by resolving it against the depiction root URI. This method is equivalent to
	 * calling {@link GuiseSession#getDepictionURI(URI, String...)}.
	 * @param navigationURI The navigation URI, which may be absolute or have an absolute path or a path relative to the application.
	 * @param suffixes The suffixes, if any, to append to a resource key in a URI reference.
	 * @return A URI suitable for depiction, deferenced and resolved to the application.
	 * @see GuiseSession#getDepictionURI(URI, String...)
	 */
	public URI getDepictionURI(final URI navigationURI, final String... suffixes);

	/**
	 * Retrieves styles for this context. Styles appear in the following order:
	 * <ol>
	 * <li>theme styles (from most distant parent to current theme)</li>
	 * <li>application style</li>
	 * <li>destination style</li>
	 * </ol>
	 * @return The URIs of the styles for this context, in order.
	 */
	public Iterable<URI> getStyles();

}
