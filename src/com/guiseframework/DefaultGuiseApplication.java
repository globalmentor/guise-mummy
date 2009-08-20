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

import java.net.URI;
import java.util.*;

import com.globalmentor.model.UUIDs;

/**The default implementation of a Guise application.
@author Garret Wilson
*/
public class DefaultGuiseApplication extends AbstractGuiseApplication
{

	/**Default constructor.
	This implementation generates a new UUID URI for the application identifier.
	*/
	public DefaultGuiseApplication()
	{
		this(UUIDs.toURI(UUID.randomUUID()));	//construct the class with the JVM default locale
	}

	/**URI constructor.
	@param uri The URI for the application, which may or may not be the URI at which the application can be accessed.
	@throws NullPointerException if the given URI is <code>null</code>.
	*/
	public DefaultGuiseApplication(final URI uri)
	{
		super(uri);	//construct the parent class
	}

}
