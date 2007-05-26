package com.guiseframework.event;

/**A key on a keyboard.
The Unicode code point to which a particular key corresponds vary according to keyboard layout.
Some keys, such as control keys, do not respond to any Unicode code point at all.
@author Garret Wilson
@see java.awt.event.KeyEvent
*/
public enum Key
{
	ALT_LEFT,
	ALT_RIGHT,
	BACKSPACE,
	CANCEL,
	CAPS_LOCK,
	CLEAR,
  CONTEXT_MENU,
	CONTROL_LEFT,
	CONTROL_RIGHT,
	DOWN,
	END,
	ENTER,
	ESCAPE,
  F1,
  F2,
  F3,
  F4,
  F5,
  F6,
  F7,
  F8,
  F9,
  F10,
  F11,
  F12,
//TODO add F13-F24 for IBM 3270 keyboard  
	HOME,
  INSERT,
	LEFT,
  MULTIPLY,
  NUMPAD_0,
  NUMPAD_1,
  NUMPAD_2,
  NUMPAD_3,
  NUMPAD_4,
  NUMPAD_5,
  NUMPAD_6,
  NUMPAD_7,
  NUMPAD_8,
  NUMPAD_9,
  NUMPAD_ADD,
  NUMPAD_DECIMAL,
  NUMPAD_DIVIDE,
  NUMPAD_ENTER,
  NUMPAD_NUM_LOCK,
  NUMPAD_SCROLL_LOCK,
  NUMPAD_SUBTRACT,
	PAGE_DOWN,
	PAGE_UP,
	PAUSE,
  PRINT_SCREEN,
	RIGHT,
	SHIFT,
	SPACE,
	TAB,
	UP,
  WINDOWS;
}
