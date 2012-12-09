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

package com.guiseframework.platform.web;

import com.globalmentor.lex.Identifier;

/**The type of event received from the web platform.
The name of the XML element in which the event is serialized will be the serialized from of the event type name.
@author Garret Wilson
*/
public enum WebPlatformEventType implements Identifier
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
