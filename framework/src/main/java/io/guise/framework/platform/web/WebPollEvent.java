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

package io.guise.framework.platform.web;

/**
 * A web event for polling the server.
 * @author Garret Wilson
 */
public class WebPollEvent extends AbstractWebPlatformEvent {

	private static final long serialVersionUID = -1727521607100020977L;

	/**
	 * Context constructor.
	 * @param source The context in which this control event was produced.
	 * @throws NullPointerException if the given context is <code>null</code>.
	 */
	public WebPollEvent(final WebPlatform source) {
		super(source); //construct the parent class
	}
}
