package com.guiseframework.platform;

import static com.globalmentor.java.Objects.*;

/**Indicates that a drop action has occurred after a drag on the platform.
The drag target serves as the source of the event.
@author Garret Wilson
*/
public class PlatformDropEvent extends AbstractDepictEvent
{

	/**The source of the drag-drop gesture.*/
	private final DepictedObject dragSource;

		/**@return The source of the drag-drop gesture.*/
		public DepictedObject getDragSource() {return dragSource;}

	/**@return The target of the drag-drop gesture.*/
	public DepictedObject getDropTarget() {return getDepictedObject();}
	
//TODO add support for mouse position

	/**Drag source and drop target constructor.
	@param dragSource The source of the drag-drop gesture.
	@param dropTarget The target of the drag-drop gesture.
	@exception NullPointerException if the given drag source and/or drop target is <code>null</code>.
	*/
	public PlatformDropEvent(final DepictedObject dragSource, final DepictedObject dropTarget)
	{
		super(dropTarget);	//construct the parent class with the drop target as the source of the event
		this.dragSource=checkInstance(dragSource, "Drag source cannot be null.");
	}
}
