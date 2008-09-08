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

package com.guiseframework.event;

import com.globalmentor.event.AbstractEvent;

import com.guiseframework.Guise;
import com.guiseframework.GuiseSession;

/**The base class for custom Guise events.
@author Garret Wilson
*/
public abstract class AbstractGuiseEvent extends AbstractEvent implements GuiseEvent
{

	/**The Guise session in which this event was generated.*/
	private final GuiseSession session;

		/**@return The Guise session in which this event was generated.*/
		public GuiseSession getSession() {return session;}

	/**Source constructor.
	@param source The object on which the event initially occurred.
	@exception NullPointerException if the given source is <code>null</code>.
	*/
	public AbstractGuiseEvent(final Object source)
	{
		super(source);	//construct the parent class
		this.session=Guise.getInstance().getGuiseSession();	//store a reference to the current Guise session
	}

}
