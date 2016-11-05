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

import static java.util.Objects.*;

import com.guiseframework.component.Component;

/**
 * An event relating to a component. The event target indicates the object that originally fired the event.
 * @author Garret Wilson
 * @see CompositeComponentListener
 */
public class ComponentEvent extends AbstractTargetedGuiseEvent {

	/** The component affected by the event. */
	private final Component component;

	/** @return The component affected by the event. */
	public Component getComponent() {
		return component;
	}

	/**
	 * Source and component constructor. The target will be set to be the same as the given source.
	 * @param source The object on which the event initially occurred.
	 * @param component The component affected by the event.
	 * @throws NullPointerException if the given source and/or component is <code>null</code>.
	 */
	public ComponentEvent(final Object source, final Component component) {
		this(source, source, component); //construct the class with the same target as the source
	}

	/**
	 * Source, target, and component constructor.
	 * @param source The object on which the event initially occurred.
	 * @param target The target of the event.
	 * @param component The component affected by the event.
	 * @throws NullPointerException if the given source, target, and/or component is <code>null</code>.
	 */
	public ComponentEvent(final Object source, final Object target, final Component component) {
		super(source, target); //construct the parent class
		this.component = requireNonNull(component, "Component cannot be null.");
	}

	/**
	 * Copy constructor that specifies a different source.
	 * @param source The object on which the event initially occurred.
	 * @param componentEvent The event the properties of which will be copied.
	 * @throws NullPointerException if the given source and/or event is <code>null</code>.
	 */
	public ComponentEvent(final Object source, final ComponentEvent componentEvent) {
		this(source, componentEvent.getTarget(), componentEvent.getComponent()); //construct the class with the same target		
	}
}
