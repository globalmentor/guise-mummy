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

import java.util.regex.Pattern;

import com.globalmentor.net.URIPath;

/**A destination that temporarily redirects to another referenced destination.
@author Garret Wilson
*/
public class TemporaryRedirectReferenceDestination extends AbstractReferenceDestination implements TemporaryRedirectDestination
{

	/**Path and referenced destination constructor.
	@param path The application context-relative path within the Guise container context, which does not begin with '/'.
	@param destination The referenced destination.
	@exception NullPointerException if the path and/or destination is <code>null</code>.
	@exception IllegalArgumentException if the provided path is absolute.
	*/
	public TemporaryRedirectReferenceDestination(final URIPath path, final Destination destination)
	{
		super(path, destination);	//construct the parent class
	}

	/**Path pattern and referenced destination constructor.
	@param pathPattern The pattern to match an application context-relative path within the Guise container context, which does not begin with '/'.
	@param destination The referenced destination.
	@exception NullPointerException if the path pattern and/or destination is <code>null</code>.
	*/
	public TemporaryRedirectReferenceDestination(final Pattern pathPattern, final Destination destination)
	{
		super(pathPattern, destination);	//construct the parent class
	}
}
