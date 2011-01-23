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

import com.guiseframework.geometry.*;
import com.guiseframework.input.*;

/**An event providing mouse information of a mouse exiting a target.
@author Garret Wilson
*/
public class MouseExitEvent extends AbstractMouseEvent
{

	/**Source constructor.
	The target will be set to be the same as the given source.
	@param source The object on which the event initially occurred.
	@param targetBounds The absolute bounds of the event target.
	@param viewportBounds The absolute bounds of the viewport.
	@param mousePosition The position of the mouse relative to the viewport.
	@param keys The keys that were pressed when this event was generated.
	@exception NullPointerException if the given source, target bounds, viewport bounds, mouse position, and/or keys is <code>null</code>.
	*/
	public MouseExitEvent(final Object source, final Rectangle targetBounds, final Rectangle viewportBounds, final Point mousePosition, final Key... keys)
	{
		this(source, source, targetBounds, viewportBounds, mousePosition, keys);	//construcdt the class with the target set to the source
	}

	/**Source and target constructor.
	@param source The object on which the event initially occurred.
	@param target The target of the event.
	@param targetBounds The absolute bounds of the event target.
	@param viewportBounds The absolute bounds of the viewport.
	@param mousePosition The position of the mouse relative to the viewport.
	@param keys The keys that were pressed when this event was generated.
	@exception NullPointerException if the given source, target, target bounds, viewport bounds, mouse position, and/or keys is <code>null</code>.
	*/
	public MouseExitEvent(final Object source, final Object target, final Rectangle sourceBounds, final Rectangle viewportBounds, final Point mousePosition, final Key... keys)
	{
		super(source, target, sourceBounds, viewportBounds, mousePosition, keys);	//construct the parent class
	}

	/**Copy constructor that specifies a different source.
	@param source The object on which the event initially occurred.
	@param mouseExitEvent The event the properties of which will be copied.
	@exception NullPointerException if the given source and/or event is <code>null</code>.
	*/
	public MouseExitEvent(final Object source, final MouseExitEvent mouseExitEvent)
	{
		this(source, mouseExitEvent.getTarget(), mouseExitEvent.getTargetBounds(), mouseExitEvent.getViewportBounds(), mouseExitEvent.getMousePosition(), mouseExitEvent.getKeys().toArray(new Key[mouseExitEvent.getKeys().size()]));	//construct the class with the same target		
	}

	/**@return The input associated with this event, or <code>null</code> if there is no input associated with this event.*/
	public MouseInput getInput()
	{
		return null;	//TODO implement mouse exit input
	}

}
