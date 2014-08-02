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

import static com.globalmentor.java.Objects.*;
import static com.guiseframework.geometry.Point.*;
import static com.guiseframework.geometry.Rectangle.*;
import com.guiseframework.input.*;

/**An event providing mouse information of a mouse clicking a target.
@author Garret Wilson
*/
public class MouseClickEvent extends AbstractMouseEvent
{

	/**The button that was clicked.*/
	private final MouseButton button;

		/**The button that was clicked.*/
		public MouseButton getButton() {return button;}

	/**The number of clicks that were input (e.g. 1 for a single click, 2 for a double click, etc.).*/
	private final int count;

		/**@return The number of clicks that were input (e.g. 1 for a single click, 2 for a double click, etc.).*/
		public int getCount() {return count;}

	/**Source constructor.
	The target will be set to be the same as the given source.
	@param source The object on which the event initially occurred.
	@param targetBounds The absolute bounds of the event target.
	@param viewportBounds The absolute bounds of the viewport.
	@param mousePosition The position of the mouse relative to the viewport.
	@parma button The button that was clicked.
	@param count The number of clicks that were input (e.g. 1 for a single click, 2 for a double click, etc.).
	@param keys The keys that were pressed when this event was generated.
	@throws NullPointerException if the given source, target bounds, viewport bounds, mouse position, button, and/or keys is <code>null</code>.
	@throws IllegalArgumentException if the given count is zero or less.
	*/
	public MouseClickEvent(final Object source, final Rectangle targetBounds, final Rectangle viewportBounds, final Point mousePosition, final MouseButton button, final int count, final Key... keys)
	{
		this(source, source, targetBounds, viewportBounds, mousePosition, button, count, keys);	//construcdt the class with the target set to the source
	}

	/**Source and target constructor.
	@param source The object on which the event initially occurred.
	@param target The target of the event.
	@param targetBounds The absolute bounds of the event target.
	@param viewportBounds The absolute bounds of the viewport.
	@param mousePosition The position of the mouse relative to the viewport.
	@parma button The button that was clicked.
	@param count The number of clicks that were input (e.g. 1 for a single click, 2 for a double click, etc.).
	@param keys The keys that were pressed when this event was generated.
	@throws NullPointerException if the given source, target, target bounds, viewport bounds, mouse position, button, and/or keys is <code>null</code>.
	@throws IllegalArgumentException if the given count is zero or less.
	*/
	public MouseClickEvent(final Object source, final Object target, final Rectangle targetBounds, final Rectangle viewportBounds, final Point mousePosition, final MouseButton button, final int count, final Key... keys)
	{
		super(source, target, targetBounds, viewportBounds, mousePosition, keys);	//construct the parent class
		if(count<=0)	//if the count is not positive
		{
			throw new IllegalArgumentException("Mouse click count must be positive.");
		}
		this.count=count;	//store the count
		this.button=checkInstance(button, "Button cannot be null.");	//save the button
	}

	/**Mouse click input constructor with empty target bounds and viewport bounds and a mouse position at the origin.
	@param source The object on which the event initially occurred.
	@param mouseClickInput The mouse click input the properties of which will be copied.
	@throws NullPointerException if the given source and/or input is <code>null</code>.
	*/
	public MouseClickEvent(final Object source, final MouseClickInput mouseClickInput)
	{
		this(source, EMPTY_RECTANGLE, EMPTY_RECTANGLE, ORIGIN_POINT, mouseClickInput.getButton(), mouseClickInput.getCount(), mouseClickInput.getKeys().toArray(new Key[mouseClickInput.getKeys().size()]));	//construct the class with the specified source		
	}

	/**Copy constructor that specifies a different source.
	@param source The object on which the event initially occurred.
	@param mouseClickEvent The event the properties of which will be copied.
	@throws NullPointerException if the given source and/or event is <code>null</code>.
	@throws IllegalArgumentException if the given count is zero or less.
	*/
	public MouseClickEvent(final Object source, final MouseClickEvent mouseClickEvent)
	{
		this(source, mouseClickEvent.getTarget(), mouseClickEvent.getTargetBounds(), mouseClickEvent.getViewportBounds(), mouseClickEvent.getMousePosition(), mouseClickEvent.getButton(), mouseClickEvent.getCount(), mouseClickEvent.getKeys().toArray(new Key[mouseClickEvent.getKeys().size()]));	//construct the class with the same target		
	}

	/**@return The mouse click input associated with this event.*/
	public MouseClickInput getInput()
	{
		return new MouseClickInput(getButton(), getCount(), getKeys().toArray(new Key[getKeys().size()]));	//return new mouse click input based upon this event
	}

}
