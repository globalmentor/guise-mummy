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

package com.guiseframework.component.layout;

import static com.globalmentor.java.Objects.*;

/**Constraints on individual component layout.
These constraints specify an identifier to which a component should be bound.
@author Garret Wilson
*/
public class ReferenceConstraints extends AbstractConstraints
{
	/**The reference ID to which the component should be bound.*/
	private final String id;	//TODO maybe make this mutable

		/**@return The reference ID to which the component should be bound.*/
		public String getID() {return id;}

	/**Creates constraints with a reference ID to which a component should be bound.
	@param id The ID to which the component should be bound.
	@throws NullPointerException if the given ID is <code>null</code>.
	*/
	public ReferenceConstraints(final String id)
	{
		this.id=checkInstance(id, "ID cannot be null");	//save the ID
	}

}
