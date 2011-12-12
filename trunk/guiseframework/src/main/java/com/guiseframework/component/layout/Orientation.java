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

package com.guiseframework.component.layout;

import java.util.*;

import com.globalmentor.java.Objects;
import com.guiseframework.geometry.Axis;
import com.guiseframework.geometry.CompassPoint;
import com.guiseframework.geometry.Side;

import static com.globalmentor.iso.ISO639.*;
import static com.globalmentor.java.Objects.*;

/**Encapsulates internationalized orientation of objects.
Static preinstantiated orientation objects are provided for common orientations.
@author Garret Wilson
*/
public class Orientation
{

	/**Left-to-right line, top-to-bottom page orientation (e.g. English).*/
	public final static Orientation LEFT_TO_RIGHT_TOP_TO_BOTTOM=new Orientation(FlowOrientation.LEFT_TO_RIGHT, FlowOrientation.TOP_TO_BOTTOM);
	
	/**Right-to-left line, top-to-bottom page orientation (e.g. Arabic).*/
	public final static Orientation RIGHT_TO_LEFT_TOP_TO_BOTTOM=new Orientation(FlowOrientation.RIGHT_TO_LEFT, FlowOrientation.TOP_TO_BOTTOM);

	/**Top-to-bottom line, right-to-left page orientation (e.g. Chinese).*/
	public final static Orientation TOP_TO_BOTTOM_RIGHT_TO_LEFT=new Orientation(FlowOrientation.TOP_TO_BOTTOM, FlowOrientation.RIGHT_TO_LEFT);

	/**The orientation of each flow.*/
	private final FlowOrientation[] orientations=new FlowOrientation[Flow.values().length];

		/**Determines the orientation for the particular flow.
		@param flow The flow (line or page).
		@return The orientation for the specified flow.
		@exception NullPointerException if the given flow is <code>null</code>.
		*/
		public FlowOrientation getOrientation(final Flow flow)
		{
			return orientations[flow.ordinal()];	//get the orientation for this flow
		}
		
		/**Determines the axis for the particular flow.
		@param flow The flow (line or page).
		@return The axis for the specified flow.
		@exception NullPointerException if the given flow is <code>null</code>.
		*/
		public Axis getAxis(final Flow flow)
		{
			return getOrientation(flow).getAxis();	//get the axis of the flow orientation
		}

		/**Determines the flow (line or page) that is aligned to the given axis.
		@param axis The axis for which flow should be determined.
		@return The flow that is aligned to the given axis.
		@exception NullPointerException if the given axis is <code>null</code>.
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

		/**Determines the direction of the particular flow.
		@param flow The flow (line or page).
		@return The direction of the specified flow.
		@exception NullPointerException if the given flow is <code>null</code>.
		*/
		public Flow.Direction getDirection(final Flow flow)
		{
			return getOrientation(flow).getDirection();	//get the direction of the flow orientation
		}
		
	/**The side for each border.*/
	private final Side[] sides=new Side[4];

		/**Determines the side for the particular border.
		@param border The logical border.
		@return The side for the specified border.
		@exception NullPointerException if the given border is <code>null</code>.
		*/
		public Side getSide(final Border border)
		{
			return sides[border.ordinal()];	//get the side for this border
		}

		/**Determines the border that appears on the given side.
		@param side The side for which the border should be determined.
		@return The border that appears on the given side.
		@exception NullPointerException if the given side is <code>null</code>.
		*/
		public Border getBorder(final Side side)
		{
			for(final Border border:Border.values())	//for each border
			{
				if(getSide(border)==side)	//if the border is on the requested side
				{
					return border;	//return this border
				}
			}
			throw new IllegalArgumentException("Unsupported orientation side: "+side);	//hopefully they won't pass Side.FRONT or Side.BACK, because orientations don't support the Z axis
		}

	/**Retrieves a cardinal compass point indicating the absolute direction based upon the given flow and end.
	@param flow The flow for which the compass point should be returned
	@param end The end of the flow requested.
	@return The cardinal compass point indicating the absolute direction of the given flow end.
	@exception NullPointerException if the given flow and/or end is <code>null</code>.
	@see CompassPoint#NORTH
	@see CompassPoint#EAST
	@see CompassPoint#SOUTH
	@see CompassPoint#WEST
	*/
	public CompassPoint getCompassPoint(final Flow flow, final Flow.End end)
	{
		return getOrientation(flow).getCompassPoint(end);	//return the compass point for this flow end
	}

	/**Retrieves a cardinal or ordinal compass point indicating the absolute direction based upon the given line and/or page ends.
	Each, but not both, of the ends may be <code>null</code>.
	@param lineEnd The end of the line flow, or <code>null</code> if a cardinal direction is requested and a page end is provided.
	@param pageEnd The end of the page flow, or <code>null</code> if a cardinal direction is requested and a line end is provided.
	@return The cardinal or ordinal compass point indicating the absolute direction of the given line and page end.
	@exception NullPointerException if both the given line end and the given page end are <code>null</code>.
	@see CompassPoint#NORTH
	@see CompassPoint#NORTHEAST
	@see CompassPoint#EAST
	@see CompassPoint#SOUTHEAST
	@see CompassPoint#SOUTH
	@see CompassPoint#SOUTHWEST
	@see CompassPoint#WEST
	@see CompassPoint#NORTHWEST
	*/
	public CompassPoint getCompassPoint(final Flow.End lineEnd, final Flow.End pageEnd)
	{
		if(lineEnd!=null)	//if there is a line end
		{
			final CompassPoint lineCompassPoint=getCompassPoint(Flow.LINE, lineEnd);	//get the compass point for this line end
			if(pageEnd!=null)	//if there is a page end
			{
				final CompassPoint pageCompassPoint=getCompassPoint(Flow.PAGE, pageEnd);	//return the compass point for this page end
				final CompassPoint latitudeCompassPoint=getFlow(Axis.X)==Flow.LINE ? lineCompassPoint : pageCompassPoint;	//get the correct compass points for latitude and longitude
				final CompassPoint longitudeCompassPoint=getFlow(Axis.Y)==Flow.PAGE ? pageCompassPoint : lineCompassPoint;
				return CompassPoint.getOrdinalCompassPoint(latitudeCompassPoint, longitudeCompassPoint);	//return the ordinal compass point for these two cardinal compass points
			}
			else	//if there is no page end
			{
				return lineCompassPoint;	//return line compass point
			}
		}
		else	//if there is no line end
		{
			if(pageEnd!=null)	//if there is a page end
			{
				return getCompassPoint(Flow.PAGE, pageEnd);	//return the compass point for this page end
			}
			else	//if both arguments are null
			{
				throw new NullPointerException("Line end and page end cannot both be null.");
			}
		}
	}

	/**Flow orientation constructor.
	@param lineOrientation The orientation of the line.
	@param pageOrientation The orientation of the page.
	@exception NullPointerException if the line orientation and/or page orientation is <code>null</code>.
	@exception IllegalArgumentException if the line orientation and/or page orientation uses the {@link Axis#Z} axis.
	@exception IllegalArgumentException if both flow orientations specify the same axis.
	*/
	public Orientation(final FlowOrientation lineOrientation, final FlowOrientation pageOrientation)
	{
		orientations[Flow.LINE.ordinal()]=checkInstance(lineOrientation, "Line orientation cannot be null.");
		if(lineOrientation.getAxis()==Axis.Z)	//if the line is flowing on the Z axis
		{
			throw new IllegalArgumentException("Lines cannot flow on the Z axis.");
		}
		orientations[Flow.PAGE.ordinal()]=checkInstance(pageOrientation, "Page orientation cannot be null.");
		if(pageOrientation.getAxis()==Axis.Z)	//if the page is flowing on the Z axis
		{
			throw new IllegalArgumentException("Pages cannot flow on the Z axis.");
		}
		if(lineOrientation.getAxis()==pageOrientation.getAxis())	//if both axes are the same
		{
			throw new IllegalArgumentException("Line orientation and page orientation must use different axes.");
		}
		final Axis lineAxis=lineOrientation.getAxis();	//get the axis of the line
		final Flow.Direction lineDirection=lineOrientation.getDirection();	//get the direction of the line flow
		final Flow.Direction pageDirection=pageOrientation.getDirection();	//get the direction of the page flow
		sides[Border.LINE_NEAR.ordinal()]=lineAxis==Axis.X ? (lineDirection==Flow.Direction.INCREASING ? Side.LEFT : Side.RIGHT) : (lineDirection==Flow.Direction.INCREASING ? Side.TOP : Side.BOTTOM);
		sides[Border.LINE_FAR.ordinal()]=lineAxis==Axis.X ? (lineDirection==Flow.Direction.INCREASING ? Side.RIGHT : Side.LEFT) : (lineDirection==Flow.Direction.INCREASING ? Side.BOTTOM : Side.TOP);
		sides[Border.PAGE_NEAR.ordinal()]=lineAxis==Axis.X ? (pageDirection==Flow.Direction.INCREASING ? Side.TOP : Side.BOTTOM) : (pageDirection==Flow.Direction.INCREASING ? Side.LEFT : Side.RIGHT);
		sides[Border.PAGE_FAR.ordinal()]=lineAxis==Axis.X ? (pageDirection==Flow.Direction.INCREASING ? Side.BOTTOM : Side.TOP) : (pageDirection==Flow.Direction.INCREASING ? Side.RIGHT : Side.LEFT);
	}

	/**The lazily-created set of right-to-left, top-to-bottom languages.*/
	private static Set<String> rightToLeftTopToBottomLanguages;

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
			rightToLeftTopToBottomLanguages.add(ARABIC_CODE);	//populate the set with appropriate languages
			rightToLeftTopToBottomLanguages.add(FARSI_CODE);
			rightToLeftTopToBottomLanguages.add(HEBREW_CODE);
			rightToLeftTopToBottomLanguages.add(HEBREW_OBSOLETE_CODE);
			rightToLeftTopToBottomLanguages.add(URDU_CODE);
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
		for(int orientationIndex=orientations.length-1; orientationIndex>=0; --orientationIndex)	//check each flow orientation
		{
			if(orientations[orientationIndex]!=orientation.orientations[orientationIndex])	//if a flow orientation doesn't match
			{
				return false;	//the objects aren't equal
			}
		}
		return true;	//the objects passed all the tests
	}

	/**@return A hash code for the cell.*/
  public int hashCode()
  {
  	return Objects.getHashCode((Object[])orientations);	//generate a hash code
  }
}
