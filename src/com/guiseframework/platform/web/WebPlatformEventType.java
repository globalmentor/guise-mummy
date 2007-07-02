package com.guiseframework.platform.web;

import com.garretwilson.lang.EnumUtilities;

/**The type of event received from the web platform.
The name of the XML element in which the event is serialized will be the serialized from of the event type name.
@see EnumUtilities#getSerializedEnum(Class, String)
@author Garret Wilson
*/
public enum WebPlatformEventType
{
	/**An action on a component.*/
	ACTION,
	/**A property change on a component.*/
	CHANGE,
	/**The end of a drag-and-drop operation.*/
	DROP,
	/**A focus change.*/
	FOCUS,
	/**Information resulting from form changes, analogous to that in an HTTP POST.*/
	INIT,
	/**A key pressed anywhere.*/
	KEYPRESS,
	/**A key released anywhere.*/
	KEYRELEASE,
	/**Sends debug information to the server.*/
	LOG,
	/**A mouse click event related to a component.*/
	MOUSECLICK,
	/**A mouse enter event related to a component.*/
	MOUSEENTER,
	/**A mouse exit event related to a component.*/
	MOUSEEXIT,
	/**Polls the server to check for updates.*/
	POLL;
}
