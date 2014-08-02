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

import com.guiseframework.event.GuiseBoundPropertyObject;

/**An abstract implementation of a component effect.
@author Garret Wilson
*/
public abstract class AbstractEffect extends GuiseBoundPropertyObject implements Effect	//TODO listen for changes in effect properties to dirty the view
{

	/**The delay, in milliseconds, before the effect takes place.*/
	private int delay=0;

		/**@return The delay, in milliseconds, before the effect takes place.*/
		public int getDelay() {return delay;}

		/**Sets the delay before the effect takes place.
		This is a bound property of type <code>Integer</code>.
		@param newDelay The delay, in milliseconds, before the effect takes place.
		@throws IllegalArgumentException if the given delay is negative.
		@see Effect#DELAY_PROPERTY
		*/
		public void setDelay(final int newDelay)
		{
			if(newDelay<0)	//if the delay is negative
			{
				throw new IllegalArgumentException("Delay "+newDelay+" cannot be negative.");
			}
			if(delay!=newDelay)	//if the value is really changing
			{
				final int oldDelay=delay;	//get the current value
				delay=newDelay;	//update the value
				firePropertyChange(DELAY_PROPERTY, new Integer(oldDelay), new Integer(newDelay));
			}
		}

	/**Default constructor with no delay.*/
	public AbstractEffect()
	{
		this(0);	//construct the effect with no delay
	}

	/**Delay constructor.
	@param delay The delay in milliseconds.
	@throws IllegalArgumentException if the given delay is negative.
	*/
	public AbstractEffect(final int delay)
	{
		if(delay<0)	//if the delay is negative
		{
			throw new IllegalArgumentException("Delay "+delay+" cannot be negative.");
		}
		this.delay=delay;	//save the delay
	}

}
