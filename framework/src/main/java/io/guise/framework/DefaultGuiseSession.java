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

import io.guise.framework.platform.Platform;

/**
 * A default implementation of a Guise session.
 * @author Garret Wilson
 */
public class DefaultGuiseSession extends AbstractGuiseSession {

	/**
	 * Application and platform constructor.
	 * @param application The Guise application to which this session belongs.
	 * @param platform The platform on which this session's objects are depicted.
	 * @throws NullPointerException if the given application and/or platform is <code>null</code>.
	 */
	public DefaultGuiseSession(final GuiseApplication application, final Platform platform) {
		super(application, platform); //construct the parent class
	}

}
