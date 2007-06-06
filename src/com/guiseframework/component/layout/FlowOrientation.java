package com.guiseframework.component.layout;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.guiseframework.geometry.Axis;
import com.guiseframework.geometry.CompassPoint;

/**The orientation of a particular flow; its axis and direction.
@author Garret Wilson
*/
public enum FlowOrientation
{

	/**{@link Flow.Direction#INCREASING} flow on the {@link Axis#X} axis.*/
	LEFT_TO_RIGHT(Axis.X, Flow.Direction.INCREASING),
	/**{@link Flow.Direction#DECREASING} flow on the {@link Axis#X} axis.*/
	RIGHT_TO_LEFT(Axis.X, Flow.Direction.DECREASING),
	/**{@link Flow.Direction#INCREASING} flow on the {@link Axis#Y} axis.*/
	TOP_TO_BOTTOM(Axis.Y, Flow.Direction.INCREASING),
	/**{@link Flow.Direction#DECREASING} flow on the {@link Axis#Y} axis.*/
	BOTTOM_TO_TOP(Axis.Y, Flow.Direction.DECREASING);
	/**{@link Flow.Direction#INCREASING} flow on the {@link Axis#Z} axis.*/
//TODO fix when compass point issue is resolved	FRONT_TO_BACK(Axis.Z, Flow.Direction.INCREASING),
	/**{@link Flow.Direction#DECREASING} flow on the {@link Axis#Z} axis.*/
//TODO fix when compass point issue is resolved	BACK_TO_FRONT(Axis.Z, Flow.Direction.DECREASING);

	/**The axis of the flow.*/
	private Axis axis;

		/**@return The axis of the flow.*/
		public Axis getAxis() {return axis;}

	/**The direction of the flow on the axis.*/
	private Flow.Direction direction;

		/**@return The direction of the flow on the axis.*/
		public Flow.Direction getDirection() {return direction;}

	/**The compass points for each end.*/
	private CompassPoint[] compassPoints=new CompassPoint[Flow.End.values().length];

	/**Axis and direction constructor.
	@param axis The axis of the flow.
	@param direction The direction of the flow on the axis.
	@exception NullPointerException if the given axis and/or direction is <code>null</code>.
	*/
	private FlowOrientation(final Axis axis, final Flow.Direction direction)
	{
		this.axis=checkInstance(axis, "Axis cannot be null.");
		this.direction=checkInstance(direction, "Direction cannot be null.");
		switch(axis)	//see which axis this is
		{
			case X:
				switch(direction)	//see which direction this is
				{
					case INCREASING:
						compassPoints[Flow.End.NEAR.ordinal()]=CompassPoint.WEST;
						compassPoints[Flow.End.FAR.ordinal()]=CompassPoint.EAST;
						break;
					case DECREASING:
						compassPoints[Flow.End.NEAR.ordinal()]=CompassPoint.EAST;
						compassPoints[Flow.End.FAR.ordinal()]=CompassPoint.WEST;
						break;
					default:
						throw new AssertionError("Unrecognized direciton: "+direction);
				}
				break;
			case Y:
				switch(direction)	//see which direction this is
				{
					case INCREASING:
						compassPoints[Flow.End.NEAR.ordinal()]=CompassPoint.NORTH;
						compassPoints[Flow.End.FAR.ordinal()]=CompassPoint.SOUTH;
						break;
					case DECREASING:
						compassPoints[Flow.End.NEAR.ordinal()]=CompassPoint.SOUTH;
						compassPoints[Flow.End.FAR.ordinal()]=CompassPoint.NORTH;
						break;
					default:
						throw new AssertionError("Unrecognized direciton: "+direction);
				}
				break;
			default:
				throw new AssertionError("Unrecognized axis: "+axis);
		}
	}

	/**Retrieves a cardinal compass point indicating the absolute direction based upon the given flow end.
	@param end The end of the flow.
	@return The cardinal compass point indicating the absolute direction of the given end.
	@exception NullPointerException if the given end is <code>null</code>.
	@see CompassPoint#NORTH
	@see CompassPoint#EAST
	@see CompassPoint#SOUTH
	@see CompassPoint#WEST
	*/
	public CompassPoint getCompassPoint(final Flow.End end)
	{
		return compassPoints[end.ordinal()];	//return the compass point for this end
	}

}
