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

import com.guiseframework.geometry.*;
import com.guiseframework.input.Key;

/**
 * An abstract event providing mouse input information.
 * @author Garret Wilson
 */
public abstract class AbstractMouseEvent extends AbstractGestureInputEvent implements MouseEvent {

	/** The target of the event, or <code>null</code> if the event target is not known. */
	private final Object target;

	/**
	 * Returns the object to which the event applies. This may be a different than <dfn>source</dfn>, which is the object that generated this event instance.
	 * @return The target of the event.
	 */
	public Object getTarget() {
		return target;
	}

	/** The absolute bounds of the event target. */
	private final Rectangle targetBounds;

	/** @return The absolute bounds of the event target. */
	public Rectangle getTargetBounds() {
		return targetBounds;
	}

	/** The absolute bounds of the viewport. */
	private final Rectangle viewportBounds;

	/** @return The absolute bounds of the viewport. */
	public Rectangle getViewportBounds() {
		return viewportBounds;
	}

	/** The position of the mouse relative to the viewport. */
	private final Point mousePosition;

	/** @return The position of the mouse relative to the viewport. */
	public Point getMousePosition() {
		return mousePosition;
	}

	/**
	 * Source and target constructor.
	 * @param source The object on which the event initially occurred.
	 * @param target The target of the event.
	 * @param targetBounds The absolute bounds of the event target.
	 * @param viewportBounds The absolute bounds of the viewport.
	 * @param mousePosition The position of the mouse relative to the viewport.
	 * @param keys The keys that were pressed when this event was generated.
	 * @throws NullPointerException if the given source, target, target bounds, viewport bounds, mouse position, and/or keys is <code>null</code>.
	 */
	public AbstractMouseEvent(final Object source, final Object target, final Rectangle targetBounds, final Rectangle viewportBounds, final Point mousePosition,
			final Key... keys) {
		super(source, keys); //construct the parent class
		this.target = checkInstance(target, "Event target object cannot be null."); //save the target
		this.targetBounds = checkInstance(targetBounds, "Target bounds cannot be null");
		this.viewportBounds = checkInstance(viewportBounds, "Viewport bounds cannot be null");
		this.mousePosition = checkInstance(mousePosition, "Mouse position cannot be null");
	}

}
