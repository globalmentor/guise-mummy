package com.javaguise.event;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.javaguise.geometry.*;
import com.javaguise.session.GuiseSession;

/**An event providing mouse information.
@param <S> The type of the event source.
@author Garret Wilson
*/
public class MouseEvent<S> extends GuiseEvent<S>
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

	/**Session and source constructor.
	@param session The Guise session in which this event was generated.
	@param source The object on which the event initially occurred.
	@param sourceBounds The absolute bounds of the event source.
	@param viewportBounds The absolute bounds of the viewport.
	@param mousePosition The position of the mouse relative to the viewport.
	@exception NullPointerException if the given session, source, source bounds, viewport bounds and/or mouse position is <code>null</code>.
	*/
	public MouseEvent(final GuiseSession session, final S source, final Rectangle sourceBounds, final Rectangle viewportBounds, final Point mousePosition)
	{
		super(session, source);	//construct the parent class
		this.sourceBounds=checkNull(sourceBounds, "Source bounds cannot be null");
		this.viewportBounds=checkNull(viewportBounds, "Viewport bounds cannot be null");
		this.mousePosition=checkNull(mousePosition, "Mouse position cannot be null");
	}

}
