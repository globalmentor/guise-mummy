package com.guiseframework.event;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.guiseframework.geometry.*;

/**An event providing mouse information.
@author Garret Wilson
*/
public class MouseEvent extends AbstractInputEvent
{

	/**The absolute bounds of the event source.*/
	private final Rectangle sourceBounds;

		/**@return The absolute bounds of the event source.*/
		public Rectangle getSourceBounds() {return sourceBounds;}

	/**The absolute bounds of the viewport.*/
	private final Rectangle viewportBounds;

		/**@return The absolute bounds of the viewport.*/
		public Rectangle getViewportBounds() {return viewportBounds;}

	/**The position of the mouse relative to the viewport.*/
	private final Point mousePosition;

		/**@return The position of the mouse relative to the viewport.*/
		public Point getMousePosition() {return mousePosition;}

	/**Source constructor.
	@param source The object on which the event initially occurred.
	@param sourceBounds The absolute bounds of the event source.
	@param viewportBounds The absolute bounds of the viewport.
	@param mousePosition The position of the mouse relative to the viewport.
	@param keys The keys that were pressed when this event was generated.
	@exception NullPointerException if the given source, source bounds, viewport bounds, mouse position, and/or keys is <code>null</code>.
	*/
	public MouseEvent(final Object source, final Rectangle sourceBounds, final Rectangle viewportBounds, final Point mousePosition, final Key... keys)
	{
		super(source, keys);	//construct the parent class
		this.sourceBounds=checkInstance(sourceBounds, "Source bounds cannot be null");
		this.viewportBounds=checkInstance(viewportBounds, "Viewport bounds cannot be null");
		this.mousePosition=checkInstance(mousePosition, "Mouse position cannot be null");
	}

}
