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

/**
 * An effect for simple delay.
 * @author Garret Wilson
 */
public class DelayEffect extends AbstractEffect {

	/** Default constructor with no delay. */
	public DelayEffect() {
		this(0); //construct the effect with no delay
	}

	/**
	 * Delay constructor.
	 * @param session The Guise session that owns this effect.
	 * @param delay The delay in milliseconds.
	 * @throws IllegalArgumentException if the given delay is negative.
	 */
	public DelayEffect(final int delay) {
		super(delay); //construct the parent class
	}

}
