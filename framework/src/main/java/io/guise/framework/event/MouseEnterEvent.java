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

package io.guise.framework.event;

import io.guise.framework.geometry.*;
import io.guise.framework.input.*;

/**
 * An event providing mouse information of a mouse entering a target.
 * @author Garret Wilson
 */
public class MouseEnterEvent extends AbstractMouseEvent {

	/**
	 * Source constructor. The target will be set to be the same as the given source.
	 * @param source The object on which the event initially occurred.
	 * @param targetBounds The absolute bounds of the event target.
	 * @param viewportBounds The absolute bounds of the viewport.
	 * @param mousePosition The position of the mouse relative to the viewport.
	 * @param keys The keys that were pressed when this event was generated.
	 * @throws NullPointerException if the given source, target bounds, viewport bounds, mouse position, and/or keys is <code>null</code>.
	 */
	public MouseEnterEvent(final Object source, final Rectangle targetBounds, final Rectangle viewportBounds, final Point mousePosition, final Key... keys) {
		this(source, source, targetBounds, viewportBounds, mousePosition, keys); //construcdt the class with the target set to the source
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
	public MouseEnterEvent(final Object source, final Object target, final Rectangle targetBounds, final Rectangle viewportBounds, final Point mousePosition,
			final Key... keys) {
		super(source, target, targetBounds, viewportBounds, mousePosition, keys); //construct the parent class
	}

	/**
	 * Copy constructor that specifies a different source.
	 * @param source The object on which the event initially occurred.
	 * @param mouseEnterEvent The event the properties of which will be copied.
	 * @throws NullPointerException if the given source and/or event is <code>null</code>.
	 */
	public MouseEnterEvent(final Object source, final MouseEnterEvent mouseEnterEvent) {
		this(source, mouseEnterEvent.getTarget(), mouseEnterEvent.getTargetBounds(), mouseEnterEvent.getViewportBounds(), mouseEnterEvent.getMousePosition(),
				mouseEnterEvent.getKeys().toArray(new Key[mouseEnterEvent.getKeys().size()])); //construct the class with the same target		
	}

	@Override
	public MouseInput getInput() {
		return null; //TODO implement mouse enter input
	}

}
