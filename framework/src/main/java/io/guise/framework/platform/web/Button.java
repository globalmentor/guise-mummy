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

package io.guise.framework.platform.web;

import static java.util.Objects.*;

import io.guise.framework.input.MouseButton;

/**
 * A button on a mouse as reported by a browser's Event.button property.
 * @see <a href="http://www.w3.org/TR/DOM-Level-2-Events/events.html#Events-MouseEvent">DOM Level 2 Mouse Events</a>
 * @see <a href="http://www.quirksmode.org/dom/w3c_events.html">Quirksmode W3C DOM Events</a>
 * @author Garret Wilson
 */
public enum Button {

	/** Left button. */
	LEFT(0, MouseButton.LEFT),
	/** Middle button. */
	MIDDLE(1, MouseButton.MIDDLE),
	/** Right button. */
	RIGHT(2, MouseButton.RIGHT);

	/** The code reported by the browser. */
	private final int code;

	/** @return The code reported by the browser. */
	public int getCode() {
		return code;
	}

	/** The mouse button this button represents. */
	private final MouseButton mouseButton;

	/** @return The mouse button this button represents. */
	public MouseButton getMouseButton() {
		return mouseButton;
	}

	/**
	 * Code and mouse button constructor.
	 * @param code The code reported by the browser.
	 * @param mouseButton The mouse button this button represents.
	 * @throws NullPointerException if the given mouse button is <code>null</code>.
	 */
	private Button(final int code, final MouseButton mouseButton) {
		this.code = code;
		this.mouseButton = requireNonNull(mouseButton, "Mouse button cannot be null.");
	}

	/**
	 * Retrieves a button corresponding to the given code.
	 * @param code The code for which a button should be returned.
	 * @return The button representing the given code.
	 * @throws IllegalArgumentException if the given code does not match one of the known buttons.
	 */
	public static Button valueOf(final int code) {
		switch(code) { //see which code is passed (there are only three codes; using a map would be overkill and less efficient than a switch statement)
			case 0:
				return Button.LEFT;
			case 1:
				return Button.MIDDLE;
			case 2:
				return Button.RIGHT;
			default:
				throw new IllegalArgumentException("The code " + code + " does not represent a known button.");
		}
	}

}
