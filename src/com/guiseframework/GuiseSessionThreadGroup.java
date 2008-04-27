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

import static com.globalmentor.java.Objects.*;

/**A thread group allocated to a Guise session.
All threads accessing a Guise session should be part of the session's thread group.
@author Garret Wilson
*/
public class GuiseSessionThreadGroup extends ThreadGroup
{
	/**The Guise session to which this thread group belongs and in which its related threads run.*/
	private final GuiseSession guiseSession;

		/**The Guise session to which this thread group belongs and in which its related threads run.*/
		public GuiseSession getGuiseSession() {return guiseSession;}

	/**Guise session constructor.
	@param guiseSession The Guise session to which this thread group belongs and in which its related threads run.
	@exception NullPointerException if the given Guise session is <code>null</code>. 
	*/
	public GuiseSessionThreadGroup(final GuiseSession guiseSession)
	{
		super("Guise Session Thread Group "+guiseSession.toString());	//construct the parent class TODO improve name
		this.guiseSession=checkInstance(guiseSession, "Guise session cannot be null.");
	}
}
