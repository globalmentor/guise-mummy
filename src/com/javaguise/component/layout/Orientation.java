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

	/**Indicates an axis; for example, a direction of layout.
	@author Garret Wilson
	*/
	public enum Axis
	{
		/**Layout along a line; the X axis in left-to-right, top-to-botom orientation.*/
		X,
		
		/**Layout along a page; the Y axis in left-to-right, top-to-botom orientation.*/
		Y;		
	}

	/**Left-to-right line, top-to-bottom page orientation (e.g. English).*/
	public final static Orientation LEFT_TO_RIGHT_TOP_TO_BOTTOM=new Orientation(Direction.LEFT_TO_RIGHT, Direction.TOP_TO_BOTTOM);
	
	/**Right-to-left line, top-to-bottom page orientation (e.g. Arabic).*/
	public final static Orientation RIGHT_TO_LEFT_TOP_TO_BOTTOM=new Orientation(Direction.RIGHT_TO_LEFT, Direction.TOP_TO_BOTTOM);

	/**Top-to-bottom line, right-to-left page orientation (e.g. Chinese).*/
	public final static Orientation TOP_TO_BOTTOM_RIGHT_TO_LEFT=new Orientation(Direction.TOP_TO_BOTTOM, Direction.RIGHT_TO_LEFT);

	/**The writing direction for a line or page.
	@author Garret Wilson
	*/
	public enum Direction
	{
		/**Left-to-right orientation (e.g. English lines).*/
		LEFT_TO_RIGHT,

		/**Right-to-left orientation (e.g. Farsi lines).*/
		RIGHT_TO_LEFT,
		
		/**Top-to-bottom orientation (e.g. Chinese lines).*/
		TOP_TO_BOTTOM,

		/**Bottom-to-top orientation.*/
		BOTTOM_TO_TOP;		
	}

	/**The direction of lines.*/
	private final Direction lineDirection;

		/**@return The direction of lines.*/
		public Direction getLineDirection() {return lineDirection;}

	/**The direction of pages.*/
	private final Direction pageDirection;

		/**@return The direction of pages.*/
		public Direction getPageDirection() {return pageDirection;}

	/**The lazily-created set of right-to-left, top-to-bottom languages.*/
	private static Set<String> rightToLeftTopToBottomLanguages;

	/**Constructor.
	@param lineDirection The direction of lines.
	@param pageDirection The direction of pages.
	@exception NullPointerException if the horizontal and/or vertical orientation is <code>null</code>.
	*/
	public Orientation(final Direction lineDirection, final Direction pageDirection)
	{
		this.lineDirection=checkNull(lineDirection, "Line direction cannot be null.");
		this.pageDirection=checkNull(pageDirection, "Page direction cannot be null.");
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
		if(object instanceof Orientation)	//if the object is an orientation
		{
			final Orientation orientation=(Orientation)object;	//cast the object to an orientation
			return getLineDirection()==orientation.getLineDirection() && getPageDirection().equals(orientation.getPageDirection());	//compare line and page directions
		}
		else	//if the object is not an orientation
		{
			return false;	//the objects aren't equal
		}			
	}

	/**@return A hash code for the cell.*/
  public int hashCode()
  {
  	return ObjectUtilities.hashCode(getPageDirection(), getLineDirection());	//generate a hash code
  }
}
