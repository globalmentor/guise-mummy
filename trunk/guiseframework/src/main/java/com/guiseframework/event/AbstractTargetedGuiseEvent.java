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

import static com.globalmentor.java.Objects.*;

import com.globalmentor.event.TargetedEvent;

/**
 * An abstract implementation of a Guise event that is targeted. The event target indicates the component that originally initiated the action.
 * @author Garret Wilson
 */
public class AbstractTargetedGuiseEvent extends AbstractGuiseEvent implements TargetedEvent {

	/** The target of the event, or <code>null</code> if the event target is not known. */
	private final Object target;

	/**
	 * Returns the object to which the event applies. This may be a different than <dfn>source</dfn>, which is the object that generated this event instance.
	 * @return The target of the event.
	 */
	public Object getTarget() {
		return target;
	}

	/**
	 * Source constructor. The target will be set to be the same as the given source.
	 * @param source The object on which the event initially occurred.
	 * @throws NullPointerException if the given source is <code>null</code>.
	 */
	public AbstractTargetedGuiseEvent(final Object source) {
		this(source, source); //construct the class with the same target as the source
	}

	/**
	 * Source and target constructor.
	 * @param source The object on which the event initially occurred.
	 * @param target The target of the event.
	 * @throws NullPointerException if the given source and/or target is <code>null</code>.
	 */
	public AbstractTargetedGuiseEvent(final Object source, final Object target) {
		super(source); //construct the parent class
		this.target = checkInstance(target, "Event target object cannot be null."); //save the target
	}

}
