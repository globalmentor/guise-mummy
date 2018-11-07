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

/**
 * The default implementation of a Guise application.
 * @author Garret Wilson
 */
public class DefaultGuiseApplication extends AbstractGuiseApplication {

	/** Default constructor with no identifier. */
	public DefaultGuiseApplication() {
		this(null);
	}

	/**
	 * URI constructor. The URI identifier may or may not be the URI at which the application can be accessed.
	 * @param uri The URI for the application, or <code>null</code> if there is no identifier.
	 */
	public DefaultGuiseApplication(final URI uri) {
		super(uri); //construct the parent class
	}

}
