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

package io.guise.framework.event;

import com.globalmentor.event.TargetedEvent;

import io.guise.framework.geometry.*;

/**
 * An event providing mouse information.
 * @author Garret Wilson
 */
public interface MouseEvent extends GestureInputEvent, TargetedEvent {

	/** @return The absolute bounds of the event target. */
	public Rectangle getTargetBounds();

	/** @return The absolute bounds of the viewport. */
	public Rectangle getViewportBounds();

	/** @return The position of the mouse relative to the viewport. */
	public Point getMousePosition();

}
