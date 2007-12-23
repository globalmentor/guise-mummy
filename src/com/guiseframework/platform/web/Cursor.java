package com.guiseframework.platform.web;

import java.net.URI;
import java.util.*;
import static java.util.Collections.*;

import static com.garretwilson.lang.EnumUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;

import static com.garretwilson.io.FileConstants.*;
import static com.garretwilson.io.Files.*;
import com.garretwilson.text.xml.stylesheets.css.XMLCSSCursor;

import com.guiseframework.component.layout.*;
import com.guiseframework.geometry.CompassPoint;

import com.guiseframework.GuiseApplication;
import static com.guiseframework.GuiseApplication.*;

/**Standard Guise cursors that are converted to predefined CSS cursors.
These cursors allow predefined dummy URIs to be mapped to predefined CSS cursors.
These URIs are constructed by using the path {@value GuiseApplication#GUISE_ROOT_THEME_CURSORS_PATH} with the serialized form of the enum appended.
@author Garret Wilson
@see <a href="http://www.w3.org/TR/CSS21/ui.html">CSS 2.1 User Interface: Cursors</a>
*/
public enum Cursor
{

	/**A simple crosshair (e.g., short line segments resembling a "+" sign).*/
	CROSSHAIR(XMLCSSCursor.CROSSHAIR),
	/**The platform-dependent default cursor. Often rendered as an arrow.*/
	DEFAULT(XMLCSSCursor.DEFAULT),
	/**Help is available for the object under the cursor. Often rendered as a question mark or a balloon.*/
	HELP(XMLCSSCursor.HELP),
	/**Indicates something is to be moved.*/
	MOVE(XMLCSSCursor.MOVE),
	/**The cursor is a pointer that indicates a link.*/
	POINTER(XMLCSSCursor.POINTER),
	/**A progress indicator. The program is performing some processing, but is different from {@link #WAIT} in that the user may still interact with the program. Often rendered as a spinning beach ball, or an arrow with a watch or hourglass.
	PROGRESS(XMLCSSCursor.PROGRESS),
	/**Indicate that some edge is to be moved from the east of the box in left-to-right top-to-bottom orientation.*/
	RESIZE_LINE_FAR(XMLCSSCursor.E_RESIZE),
	/**Indicate that some edge is to be moved from the south-east corner of the box in left-to-right top-to-bottom orientation.*/
	RESIZE_LINE_FAR_PAGE_FAR(XMLCSSCursor.SE_RESIZE),
	/**Indicate that some edge is to be moved from the north-east corner of the box in left-to-right top-to-bottom orientation.*/
	RESIZE_LINE_FAR_PAGE_NEAR(XMLCSSCursor.NE_RESIZE),
	/**Indicate that some edge is to be moved from the west of the box in left-to-right top-to-bottom orientation.*/
	RESIZE_LINE_NEAR(XMLCSSCursor.W_RESIZE),
	/**Indicate that some edge is to be moved from the south-west corner of the box in left-to-right top-to-bottom orientation.*/
	RESIZE_LINE_NEAR_PAGE_FAR(XMLCSSCursor.SW_RESIZE),
	/**Indicate that some edge is to be moved from the north-west corner of the box in left-to-right top-to-bottom orientation.*/
	RESIZE_LINE_NEAR_PAGE_NEAR(XMLCSSCursor.NW_RESIZE),
	/**Indicate that some edge is to be moved from the north of the box in left-to-right top-to-bottom orientation.*/
	RESIZE_PAGE_NEAR(XMLCSSCursor.N_RESIZE),
	/**Indicate that some edge is to be moved from the south corner of the box in left-to-right top-to-bottom orientation.*/
	RESIZE_PAGE_FAR(XMLCSSCursor.S_RESIZE),
	/**Indicates text that may be selected. Often rendered as an I-beam.*/
	TEXT(XMLCSSCursor.TEXT),
	/**Indicates that the program is busy and the user should wait. Often rendered as a watch or hourglass.*/
	WAIT(XMLCSSCursor.WAIT);

	/**@return The URI of the predefined cursor relative to the application.*/
	public URI getURI()
	{
		return URI.create(addExtension(GUISE_ROOT_THEME_CURSORS_PATH+getSerializationName(this), PNG_EXTENSION));
	}

	/**The XML CSS cursor to which this cursor corresponds.
	Some cursors may need further checks to determine the correct CSS cursor based upon component orientation.
	*/
	private final XMLCSSCursor cssCursor;

	/**CSS cursor constructor.
	@param cssCursor The XML CSS cursor to which this cursor corresponds.
	@exception NullPointerException if the given CSS cursor is <code>null</code>.
	*/
	private Cursor(final XMLCSSCursor cssCursor)
	{
		this.cssCursor=checkInstance(cssCursor, "CSS cursor cannot be null.");
	}

	/**Retrieves the CSS cursor that corresponds to the predefined cursor based in the given component orientation.
	@param orientation The component orientation for which the cursor should be returned.
	@return The CSS cursor corresponding to this cursor in the given orientation.
	@exception NullPointerException if the given orientation is <code>null</code>.
	*/
	public XMLCSSCursor getCSSCursor(final Orientation orientation)
	{
		checkInstance(orientation, "Orientation cannot be null.");
		final CompassPoint resizeCompassPoint;	//we'll determine a esize compass point if needed
		switch(this)
		{

			case RESIZE_LINE_NEAR:
				resizeCompassPoint=orientation.getCompassPoint(Flow.End.NEAR, null);
				break;
			case RESIZE_LINE_NEAR_PAGE_NEAR:
				resizeCompassPoint=orientation.getCompassPoint(Flow.End.NEAR, Flow.End.NEAR);
				break;
			case RESIZE_LINE_NEAR_PAGE_FAR:
				resizeCompassPoint=orientation.getCompassPoint(Flow.End.NEAR, Flow.End.FAR);
				break;
			case RESIZE_LINE_FAR:
				resizeCompassPoint=orientation.getCompassPoint(Flow.End.FAR, null);
				break;
			case RESIZE_LINE_FAR_PAGE_NEAR:
				resizeCompassPoint=orientation.getCompassPoint(Flow.End.FAR, Flow.End.NEAR);
				break;
			case RESIZE_LINE_FAR_PAGE_FAR:
				resizeCompassPoint=orientation.getCompassPoint(Flow.End.FAR, Flow.End.FAR);
				break;
			case RESIZE_PAGE_NEAR:
				resizeCompassPoint=orientation.getCompassPoint((Flow.End)null, Flow.End.NEAR);
				break;
			case RESIZE_PAGE_FAR:
				resizeCompassPoint=orientation.getCompassPoint((Flow.End)null, Flow.End.FAR);
				break;
			default:	//for all the other cursors
				return cssCursor;	//return the non-directional cursor
		}
		switch(resizeCompassPoint)	//choose a CSS resize cursor based upon the compass point
		{
			case NORTH:
				return XMLCSSCursor.N_RESIZE;
			case NORTHEAST:
				return XMLCSSCursor.NE_RESIZE;
			case EAST:
				return XMLCSSCursor.NE_RESIZE;
			case SOUTHEAST:
				return XMLCSSCursor.SE_RESIZE;
			case SOUTH:
				return XMLCSSCursor.S_RESIZE;
			case SOUTHWEST:
				return XMLCSSCursor.SW_RESIZE;
			case WEST:
				return XMLCSSCursor.W_RESIZE;
			case NORTHWEST:
				return XMLCSSCursor.NW_RESIZE;
			default:
				throw new AssertionError("Unsupported resize compass point: "+resizeCompassPoint);
		}
	}

	/**The read-only map of cursors mapped to application-relative URIs.
	@see Cursor#getURI()
	*/  
	private static Map<URI, Cursor> uriCursorMap;

	static
	{
		final Map<URI, Cursor> uriCursorMap=new HashMap<URI, Cursor>();	//create a new map of cursors mapped to URIs
		for(final Cursor cursor:Cursor.values())	//for each cursor
		{
			uriCursorMap.put(cursor.getURI(), cursor);	//create a mapping from the URI to this cursor
		}
		Cursor.uriCursorMap=unmodifiableMap(uriCursorMap);	//store a read-only version of the cursor map
	}
	
	/**Determines a predefined cursor from the given URI
	@param uri The URI of the predefined cursor relative to the application.
	@return The predefined cursor indicated by the given URI, or <code>null</code> if the given URI does not identify a predefined cursor.
	*/
	public static Cursor getCursor(final URI uri)
	{
		return uriCursorMap.get(uri);	//look up the corresponding cursor, if possible
	}
}
