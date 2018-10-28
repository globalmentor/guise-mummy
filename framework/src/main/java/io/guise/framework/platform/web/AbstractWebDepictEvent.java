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

package io.guise.framework.platform.web;

import io.guise.framework.platform.*;

/**
 * The base class for events to or from a depicted object on the web platform. The source of the event is the depicted object.
 * @author Garret Wilson
 */
public abstract class AbstractWebDepictEvent extends AbstractDepictEvent implements WebDepictEvent {

	/**
	 * Depicted object constructor.
	 * @param depictedObject The depicted object on which the event initially occurred.
	 * @throws NullPointerException if the given depicted object is <code>null</code>.
	 */
	public AbstractWebDepictEvent(final DepictedObject depictedObject) {
		super(depictedObject); //construct the parent class
	}

}
