/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.platform;

import static java.util.Objects.*;

/**
 * Indicates that a drop action has occurred after a drag on the platform. The drag target serves as the source of the event.
 * @author Garret Wilson
 */
public class PlatformDropEvent extends AbstractDepictEvent {

	/** The source of the drag-drop gesture. */
	private final DepictedObject dragSource;

	/** @return The source of the drag-drop gesture. */
	public DepictedObject getDragSource() {
		return dragSource;
	}

	/** @return The target of the drag-drop gesture. */
	public DepictedObject getDropTarget() {
		return getDepictedObject();
	}

	//TODO add support for mouse position

	/**
	 * Drag source and drop target constructor.
	 * @param dragSource The source of the drag-drop gesture.
	 * @param dropTarget The target of the drag-drop gesture.
	 * @throws NullPointerException if the given drag source and/or drop target is <code>null</code>.
	 */
	public PlatformDropEvent(final DepictedObject dragSource, final DepictedObject dropTarget) {
		super(dropTarget); //construct the parent class with the drop target as the source of the event
		this.dragSource = requireNonNull(dragSource, "Drag source cannot be null.");
	}
}
