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

/**
 * An event indicating that coarse-grained editing occurred. The event target indicates the component that originally initiated the action.
 * @author Garret Wilson
 * @see EditListener
 */
public class EditEvent extends AbstractTargetedGuiseEvent {

	/**
	 * Source constructor. The target will be set to be the same as the given source.
	 * @param source The object on which the event initially occurred.
	 * @throws NullPointerException if the given source is <code>null</code>.
	 */
	public EditEvent(final Object source) {
		this(source, source); //construct the class with the same target as the source
	}

	/**
	 * Source and target constructor.
	 * @param source The object on which the event initially occurred.
	 * @param target The target of the event.
	 * @throws NullPointerException if the given source and/or target is <code>null</code>.
	 */
	public EditEvent(final Object source, final Object target) {
		super(source, target); //construct the parent class
	}

	/**
	 * Copy constructor that specifies a different source.
	 * @param source The object on which the event initially occurred.
	 * @param editEvent The event the properties of which will be copied.
	 * @throws NullPointerException if the given source and/or event is <code>null</code>.
	 */
	public EditEvent(final Object source, final EditEvent editEvent) {
		this(source, editEvent.getTarget()); //construct the class with the same target		
	}

}
