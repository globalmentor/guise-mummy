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

package com.guiseframework;

import java.io.InputStream;
import java.net.URI;

import com.globalmentor.net.ResourceIOException;
import com.globalmentor.net.URIPath;
import com.globalmentor.urf.URFResource;
import com.globalmentor.urf.content.*;

/**A navigation point that retrieves a resource description and/or contents.
@author Garret Wilson
*/
public interface ResourceReadDestination extends Destination
{

	/**Returns a description of the resource.
	The resource should include valid values for the following properties:
	<ul>
		<li>{@value Content#TYPE_PROPERTY_URI}</li>
		<li>{@value Content#LENGTH_PROPERTY_URI}</li>
		<li>{@value Content#MODIFIED_PROPERTY_URI}</li>
	</ul>
	@param session The current Guise Session. 
	@param navigationPath The navigation path relative to the application context path.
	@param bookmark The bookmark for which navigation should occur at this navigation path, or <code>null</code> if there is no bookmark involved in navigation.
	@param referrerURI The URI of the referring component or other entity with no query or fragment, or <code>null</code> if no referring URI is known.
	@return A description of the resource.
	@exception NullPointerException if the given navigation path is <code>null</code>.
	@exception ResourceIOException if there is an error retrieving the resource description.
	*/
	public URFResource getResourceDescription(final GuiseSession session, final URIPath navigationPath, final Bookmark bookmark, final URI referrerURI) throws ResourceIOException;

	/**Retrieves an input stream to the resource.
	@param session The current Guise Session. 
	@param navigationPath The navigation path relative to the application context path.
	@param bookmark The bookmark for which navigation should occur at this navigation path, or <code>null</code> if there is no bookmark involved in navigation.
	@param referrerURI The URI of the referring component or other entity with no query or fragment, or <code>null</code> if no referring URI is known.
	@return An input stream to the given resource.
	@exception NullPointerException if the given navigation path is <code>null</code>.
	@exception ResourceIOException Thrown if there is an error accessing the resource, such as a missing file.
	*/
	public InputStream getInputStream(final GuiseSession session, final URIPath navigationPath, final Bookmark bookmark, final URI referrerURI) throws ResourceIOException;

}
