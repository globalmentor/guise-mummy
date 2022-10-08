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

package io.guise.framework.input;

/**
 * A key on a keyboard. The Unicode code point to which a particular key corresponds vary according to keyboard layout. Some keys, such as control keys, do not
 * respond to any Unicode code point at all.
 * @author Garret Wilson
 * @see java.awt.event.KeyEvent
 */
public enum Key {
	/** Key definition */
	ALT_LEFT,
	/** Key definition */
	ALT_RIGHT,
	/** Key definition */
	BACKSPACE,
	/** Key definition */
	CANCEL,
	/** Key definition */
	CAPS_LOCK,
	/** Key definition */
	CLEAR,
	/** Key definition */
	CONTEXT_MENU,
	/** Key definition */
	CONTROL_LEFT,
	/** Key definition */
	CONTROL_RIGHT,
	/** Key definition */
	DELETE,
	/** Key definition */
	DOWN,
	/** Key definition */
	END,
	/** Key definition */
	ENTER,
	/** Key definition */
	ESCAPE,
	/** Key definition */
	F1,
	/** Key definition */
	F2,
	/** Key definition */
	F3,
	/** Key definition */
	F4,
	/** Key definition */
	F5,
	/** Key definition */
	F6,
	/** Key definition */
	F7,
	/** Key definition */
	F8,
	/** Key definition */
	F9,
	/** Key definition */
	F10,
	/** Key definition */
	F11,
	/** Key definition */
	F12,
	//TODO add F13-F24 for IBM 3270 keyboard  
	/** Key definition */
	HOME,
	/** Key definition */
	INSERT,
	/** Key definition */
	LEFT,
	/** Key definition */
	MULTIPLY,
	/** Key definition */
	NUMPAD_0,
	/** Key definition */
	NUMPAD_1,
	/** Key definition */
	NUMPAD_2,
	/** Key definition */
	NUMPAD_3,
	/** Key definition */
	NUMPAD_4,
	/** Key definition */
	NUMPAD_5,
	/** Key definition */
	NUMPAD_6,
	/** Key definition */
	NUMPAD_7,
	/** Key definition */
	NUMPAD_8,
	/** Key definition */
	NUMPAD_9,
	/** Key definition */
	NUMPAD_ADD,
	/** Key definition */
	NUMPAD_DECIMAL,
	/** Key definition */
	NUMPAD_DIVIDE,
	/** Key definition */
	NUMPAD_ENTER,
	/** Key definition */
	NUMPAD_NUM_LOCK,
	/** Key definition */
	NUMPAD_SCROLL_LOCK,
	/** Key definition */
	NUMPAD_SUBTRACT,
	/** Key definition */
	PAGE_DOWN,
	/** Key definition */
	PAGE_UP,
	/** Key definition */
	PAUSE,
	/** Key definition */
	PRINT_SCREEN,
	/** Key definition */
	RIGHT,
	/** Key definition */
	SHIFT_LEFT,
	/** Key definition */
	SHIFT_RIGHT,
	/** Key definition */
	SPACE,
	/** Key definition */
	TAB,
	/** Key definition */
	UP,
	/** Key definition */
	WINDOWS;
}
