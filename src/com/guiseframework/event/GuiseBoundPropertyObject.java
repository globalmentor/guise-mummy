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

import com.globalmentor.beans.BoundPropertyObject;
import com.guiseframework.Guise;
import com.guiseframework.GuiseSession;

/**A bound property object that reports all property change events to the current session.
This class may postpone certain events if Guise is processing component controller events.
@author Garret Wilson
*/
public class GuiseBoundPropertyObject extends BoundPropertyObject
{
	/**The Guise session that owns this object.*/
	private final GuiseSession session;

		/**@return The Guise session that owns this object.*/
		public GuiseSession getSession() {return session;}

	/**Default constructor.*/
	public GuiseBoundPropertyObject()
	{
		this.session=Guise.getInstance().getGuiseSession();	//store a reference to the current Guise session
	}

	/**Reports that a bound property has changed.
	This implementation delegates to the Guise session to fire or postpone the property change event.
	@param propertyChangeEvent The event to fire.
	@see GuiseSession#queueEvent(com.garretwilson.event.PostponedEvent)
	*/
/*TODO del when postponed property change events are removed
	protected void firePropertyChange(final PropertyChangeEvent propertyChangeEvent)
	{
		getSession().queueEvent(createPostponedPropertyChangeEvent(propertyChangeEvent));	//create and queue a postponed property change event
	}
*/

}
