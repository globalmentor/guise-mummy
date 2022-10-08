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

package io.guise.framework.platform.web;

import java.util.*;

import io.guise.framework.input.Key;

import static java.util.Collections.*;
import static java.util.Objects.*;

/**
 * A key on a keyboard as reported by a browser's Event.keyCode property.
 * @author Garret Wilson
 */
public enum KeyCode {
	/** Key code definition. */
	ALT(18, Key.ALT_LEFT),
	/** Key code definition. */
	BACKSPACE(8, Key.BACKSPACE),
	/** Key code definition. */
	CONTROL(17, Key.CONTROL_LEFT),
	/** Key code definition. */
	DELETE(46, Key.DELETE),
	/** Key code definition. */
	DOWN(40, Key.DOWN),
	/** Key code definition. */
	END(35, Key.END),
	/** Key code definition. */
	ENTER(13, Key.ENTER),
	/** Key code definition. */
	ESCAPE(27, Key.ESCAPE),
	/** Key code definition. */
	F1(112, Key.F1),
	/** Key code definition. */
	F2(113, Key.F2),
	/** Key code definition. */
	F3(114, Key.F3),
	/** Key code definition. */
	F4(115, Key.F4),
	/** Key code definition. */
	F5(116, Key.F5),
	/** Key code definition. */
	F6(117, Key.F6),
	/** Key code definition. */
	F7(118, Key.F7),
	/** Key code definition. */
	F8(119, Key.F8),
	/** Key code definition. */
	F9(120, Key.F9),
	/** Key code definition. */
	F10(121, Key.F10),
	/** Key code definition. */
	F11(122, Key.F11),
	/** Key code definition. */
	F12(123, Key.F12),
	/** Key code definition. */
	HOME(36, Key.HOME),
	/** Key code definition. */
	LEFT(37, Key.LEFT),
	/** Key code definition. */
	PAGE_UP(33, Key.PAGE_UP),
	/** Key code definition. */
	PAGE_DOWN(34, Key.PAGE_DOWN),
	/** Key code definition. */
	RIGHT(39, Key.RIGHT),
	/** Key code definition. */
	SHIFT(16, Key.SHIFT_LEFT),
	/** Key code definition. */
	TAB(9, Key.TAB),
	/** Key code definition. */
	UP(38, Key.UP);

	/** The code reported by the browser. */
	private final int code;

	/** @return The code reported by the browser. */
	public int getCode() {
		return code;
	}

	/** The key this key code represents. */
	private final Key key;

	/** @return The key this key code represents. */
	public Key getKey() {
		return key;
	}

	/**
	 * Code and key constructor.
	 * @param code The code reported by the browser.
	 * @param key The key this key code represents.
	 * @throws NullPointerException if the given key is <code>null</code>.
	 */
	private KeyCode(final int code, final Key key) {
		this.code = code;
		this.key = requireNonNull(key, "Key cannot be null.");
	}

	/** The read-only map of key codes keyed to codes. */
	private static Map<Integer, KeyCode> keyCodeMap = null;

	static {
		final Map<Integer, KeyCode> tempMap = new HashMap<Integer, KeyCode>(); //create a new hash map
		for(final KeyCode keyCode : KeyCode.values()) { //for each key code
			tempMap.put(Integer.valueOf(keyCode.getCode()), keyCode); //store the key code in the map keyed to its code
		}
		keyCodeMap = unmodifiableMap(tempMap); //save a read-only copy of the map
	}

	/**
	 * Retrieves a key code corresponding to the given code.
	 * @param code The code for which a key code should be returned.
	 * @return The key code representing the given code.
	 * @throws IllegalArgumentException if the given code does not match one of the known key codes.
	 */
	public static KeyCode valueOf(final int code) {
		final Integer codeInteger = Integer.valueOf(code); //create an integer object from the code to serve as the map key
		final KeyCode keyCode = keyCodeMap.get(codeInteger); //look up the corresponding key code from the map
		if(keyCode == null) { //if there is no such key code in the map
			throw new IllegalArgumentException("The code " + code + " does not represent a known key code.");
		}
		return keyCode; //return the key code we found
	}

}
