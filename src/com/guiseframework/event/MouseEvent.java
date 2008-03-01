package com.guiseframework.event;

import com.globalmentor.event.TargetedEvent;
import com.guiseframework.geometry.*;

/**An event providing mouse information.
@author Garret Wilson
*/
public interface MouseEvent extends GestureInputEvent, TargetedEvent
{

	/**@return The absolute bounds of the event target.*/
	public Rectangle getTargetBounds();

	/**@return The absolute bounds of the viewport.*/
	public Rectangle getViewportBounds();

	/**@return The position of the mouse relative to the viewport.*/
	public Point getMousePosition();

}
