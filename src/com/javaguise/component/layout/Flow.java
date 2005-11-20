package com.javaguise.component.layout;

/**Indicates a logical flow axis.
@author Garret Wilson
*/
public enum Flow
{
	/**Flow along a line; the X axis in left-to-right, top-to-bottom orientation.*/
	LINE,
	
	/**Flow along a page; the Y axis in left-to-right, top-to-bottom orientation.*/
	PAGE;

	/**The flow direction for a line or page, relative to the origin in the top, left-hand corner of the area.
	@author Garret Wilson
	*/
	public enum Direction
	{
		/**Left-to-right lines or top-to-bottom pages.*/
		INCREASING,

		/**Right-to-left lines or bottom-to-top pages.*/
		DECREASING;

		/**Determines the end at which the given direction would arrive relative to this direction.
		For example, if this is an increasing direction and a decreasing direction is given, the decreasing direction would arrive at the near end of the flow.
		@param direction The relative direction or which the end should be returned.
		@return The end of the flow at which the given direction would arrive relative to this direction.
		*/ 
		public End getEnd(final Direction direction)
		{
			return direction==this ? End.FAR : End.NEAR;	//if both directions are the same, the direction will end up at the far end
		}		
	}

	/**Indicates an end of a flow, relative to a direction.
	@author Garret Wilson
	@see Direction
	*/
	public enum End
	{
		/**The near end of the flow.*/
		NEAR,
		
		/**The far end of the flow.*/
		FAR;
	}
}