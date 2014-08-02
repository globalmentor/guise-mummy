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

package com.guiseframework.component.effect;

import static com.globalmentor.java.Classes.*;

/**Encapsulates information for a component effect.
@author Garret Wilson
*/
public interface Effect
{
	/**The delay bound property.*/
	public final static String DELAY_PROPERTY=getPropertyName(Effect.class, "delay");

	/**@return The delay, in milliseconds, before the effect takes place.*/
	public int getDelay();

	/**Sets the delay before the effect takes place.
	This is a bound property of type <code>Integer</code>.
	@param newDelay The delay, in milliseconds, before the effect takes place.
	@throws IllegalArgumentException if the given delay is negative.
	@see #DELAY_PROPERTY
	*/
	public void setDelay(final int newDelay);

}
