package com.javaguise.component.layout;

import java.util.*;

import com.garretwilson.lang.ObjectUtilities;
import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.util.LanguageConstants.*;

/**Encapsulates internationalized orientation of objects.
Static preinstantiated orientation objects are provided for common orientations.
@author Garret Wilson
*/
public class Orientation
{

	/**Indicates a logical flow axis.
	@author Garret Wilson
	*/
	public enum Flow
	{
		/**Flow along a line; the X axis in left-to-right, top-to-botom orientation.*/
		LINE,
		
		/**Flow along a page; the Y axis in left-to-right, top-to-botom orientation.*/
		PAGE;		
	}

	/**Left-to-right line, top-to-bottom page orientation (e.g. English).*/
	public final static Orientation LEFT_TO_RIGHT_TOP_TO_BOTTOM=new Orientation(Axis.X, Direction.INCREASING, Direction.INCREASING);
	
	/**Right-to-left line, top-to-bottom page orientation (e.g. Arabic).*/
	public final static Orientation RIGHT_TO_LEFT_TOP_TO_BOTTOM=new Orientation(Axis.X, Direction.DECREASING, Direction.INCREASING);

	/**Top-to-bottom line, right-to-left page orientation (e.g. Chinese).*/
	public final static Orientation TOP_TO_BOTTOM_RIGHT_TO_LEFT=new Orientation(Axis.Y, Direction.INCREASING, Direction.DECREASING);

	/**The flow direction for a line or page, relative to the origin in the top, left-hand corner of the area.
	@author Garret Wilson
	*/
	public enum Direction
	{
		/**Left-to-right lines or top-to-bottom pages.*/
		INCREASING,

		/**Right-to-left lines or bottom-to-top pages.*/
		DECREASING;
	}

	/**The axis for each flow (line and page).*/
	private final Axis[] axes=new Axis[2];

		/**Determines the axis for the particular flow.
		@param flow The flow (line or page).
		@return The axis for the specified flow.
		*/
		public Axis getAxis(final Flow flow)
		{
			return axes[flow.ordinal()];	//get the axis for this flow
		}

		/**Determines the flow (line or page) that is aligned to the given axis.
		@param axis The axis for which flow should be determined.
		@return The flow that is aligned to the given axis.
		*/
		public Flow getFlow(final Axis axis)
		{
			for(final Flow flow:Flow.values())	//for each flow
			{
				if(getAxis(flow)==axis)	//if the flow is on the requested axis
				{
					return flow;	//return this flow
				}
			}
			throw new IllegalArgumentException("Unsupported orientation axis: "+axis);	//hopefully they won't pass Axis.Z, because orientations don't support the Z axis
		}

	/**The direction for each flow (line and page).*/
	private final Direction[] directions=new Direction[2];

		/**Determines the direction of the particular flow.
		@param flow The flow (line or page).
		@return The direction of the specified flow.
		*/
		public Direction getDirection(final Flow flow)
		{
			return directions[flow.ordinal()];	//get the direction for this flow
		}

	/**The lazily-created set of right-to-left, top-to-bottom languages.*/
	private static Set<String> rightToLeftTopToBottomLanguages;

	/**Constructor.
	@param lineAxis The axis of lines in this orientation.
	@param lineDirection The direction of lines.
	@param pageDirection The direction of pages.
	@exception NullPointerException if the line axis, line direction, and/or page direction is <code>null</code>.
	*/
	public Orientation(final Axis lineAxis, final Direction lineDirection, final Direction pageDirection)
	{
		axes[Flow.LINE.ordinal()]=checkNull(lineAxis, "Line axiscannot be null.");	//set the line axis
		axes[Flow.PAGE.ordinal()]=lineAxis==Axis.X ? Axis.Y : Axis.X;	//the page axis will be the perpendicular axis
		directions[Flow.LINE.ordinal()]=checkNull(lineDirection, "Line direction cannot be null.");	//set the line direction
		directions[Flow.PAGE.ordinal()]=checkNull(pageDirection, "Page direction cannot be null.");	//set the page direction
	}

	/**Retrieves the default orientation for a particular locale.
	This method currently supports the following locales:
	<dl>
		<dt>Arabic (<code>ar</code>)</dt> <dd>Right-to-left, top-to-bottom.</dd>
		<dt>Farsi (<code>fa</code>)</dt> <dd>Right-to-left, top-to-bottom.</dd>
		<dt>Hebrew (<code>he</code>, <code>iw</code>)</dt> <dd>Right-to-left, top-to-bottom.</dd>
		<dt>Urdu (<code>ur</code>)</dt> <dd>Right-to-left, top-to-bottom.</dd>
	</dl>
	All other languages will be assumed left-to-right, top-to-bottom.
	@param locale The locale for which an orientation should be returned.
	@return The default orientation for the given locale, or {@link #LEFT_TO_RIGHT_TOP_TO_BOTTOM} by default.
	*/
	public static Orientation getOrientation(final Locale locale)	//TODO update
	{
		if(rightToLeftTopToBottomLanguages==null)	//if we haven't yet created our language lookup set
		{
			rightToLeftTopToBottomLanguages=new HashSet<String>();	//create a new set for holding language strings
			rightToLeftTopToBottomLanguages.add(ARABIC);	//populate the set with appropriate languages
			rightToLeftTopToBottomLanguages.add(FARSI);
			rightToLeftTopToBottomLanguages.add(HEBREW);
			rightToLeftTopToBottomLanguages.add(HEBREW_OBSOLETE);
			rightToLeftTopToBottomLanguages.add(URDU);
		}
		final String language=locale.getLanguage();	//get the locale language
		if(rightToLeftTopToBottomLanguages.contains(language))	//if our right-to-left, top-to-bottom language set contains this language
		{
			return RIGHT_TO_LEFT_TOP_TO_BOTTOM;	//return the right-to-left, top-to-bottom orientation
		}
		return LEFT_TO_RIGHT_TOP_TO_BOTTOM;	//default to left-to-right, top-to-bottom
	}

  /**Determines whether the given object is equal to this object.
	@param object The object to compare to this object.
	@return <code>true</code> if the given object is another orientation with the same line and page directions.
	*/
	public boolean equals(final Object object)
	{
		if(!(object instanceof Orientation))	//if the object is not an orientation
		{
			return false;	//the objects aren't equal
		}
		final Orientation orientation=(Orientation)object;	//cast the object to an orientation
		for(int axisIndex=axes.length-1; axisIndex>=0; --axisIndex)	//check each axis
		{
			if(axes[axisIndex]!=orientation.axes[axisIndex])	//if an axis doesn't match
			{
				return false;	//the objects aren't equal
			}
		}
		for(int directionIndex=directions.length-1; directionIndex>=0; --directionIndex)	//check each direction
		{
			if(directions[directionIndex]!=orientation.directions[directionIndex])	//if an direction doesn't match
			{
				return false;	//the objects aren't equal
			}
		}
		return true;	//the objects passed all the tests
	}

	/**@return A hash code for the cell.*/
  public int hashCode()
  {
  	return ObjectUtilities.hashCode(axes[0], axes[1], directions[0], directions[1]);	//generate a hash code
  }
}
