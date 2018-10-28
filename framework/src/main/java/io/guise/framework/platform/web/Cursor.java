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

package io.guise.framework.platform.web;

import java.net.URI;
import java.util.*;

import static java.util.Collections.*;
import static java.util.Objects.*;

import com.globalmentor.io.Images;
import com.globalmentor.lex.Identifier;
import com.globalmentor.w3c.spec.CSS;

import io.guise.framework.GuiseApplication;
import io.guise.framework.component.layout.*;
import io.guise.framework.geometry.CompassPoint;

import static com.globalmentor.io.Files.*;
import static com.globalmentor.java.Enums.*;
import static io.guise.framework.GuiseApplication.*;

/**
 * Standard Guise cursors that are converted to predefined CSS cursors. These cursors allow predefined dummy URIs to be mapped to predefined CSS cursors. These
 * URIs are constructed by using the path {@link GuiseApplication#GUISE_ROOT_THEME_CURSORS_PATH} with the serialized form of the enum appended.
 * @author Garret Wilson
 * @see <a href="http://www.w3.org/TR/CSS21/ui.html">CSS 2.1 User Interface: Cursors</a>
 */
public enum Cursor implements Identifier {

	/** A simple crosshair (e.g., short line segments resembling a "+" sign). */
	CROSSHAIR(CSS.Cursor.CROSSHAIR),
	/** The platform-dependent default cursor. Often rendered as an arrow. */
	DEFAULT(CSS.Cursor.DEFAULT),
	/** Help is available for the object under the cursor. Often rendered as a question mark or a balloon. */
	HELP(CSS.Cursor.HELP),
	/** Indicates something is to be moved. */
	MOVE(CSS.Cursor.MOVE),
	/** The cursor is a pointer that indicates a link. */
	POINTER(CSS.Cursor.POINTER),
	/**
	 * A progress indicator. The program is performing some processing, but is different from {@link #WAIT} in that the user may still interact with the program.
	 * Often rendered as a spinning beach ball, or an arrow with a watch or hourglass.
	 */
	PROGRESS(CSS.Cursor.PROGRESS),
	/** Indicate that some edge is to be moved from the east of the box in left-to-right top-to-bottom orientation. */
	RESIZE_LINE_FAR(CSS.Cursor.E_RESIZE),
	/** Indicate that some edge is to be moved from the south-east corner of the box in left-to-right top-to-bottom orientation. */
	RESIZE_LINE_FAR_PAGE_FAR(CSS.Cursor.SE_RESIZE),
	/** Indicate that some edge is to be moved from the north-east corner of the box in left-to-right top-to-bottom orientation. */
	RESIZE_LINE_FAR_PAGE_NEAR(CSS.Cursor.NE_RESIZE),
	/** Indicate that some edge is to be moved from the west of the box in left-to-right top-to-bottom orientation. */
	RESIZE_LINE_NEAR(CSS.Cursor.W_RESIZE),
	/** Indicate that some edge is to be moved from the south-west corner of the box in left-to-right top-to-bottom orientation. */
	RESIZE_LINE_NEAR_PAGE_FAR(CSS.Cursor.SW_RESIZE),
	/** Indicate that some edge is to be moved from the north-west corner of the box in left-to-right top-to-bottom orientation. */
	RESIZE_LINE_NEAR_PAGE_NEAR(CSS.Cursor.NW_RESIZE),
	/** Indicate that some edge is to be moved from the north of the box in left-to-right top-to-bottom orientation. */
	RESIZE_PAGE_NEAR(CSS.Cursor.N_RESIZE),
	/** Indicate that some edge is to be moved from the south corner of the box in left-to-right top-to-bottom orientation. */
	RESIZE_PAGE_FAR(CSS.Cursor.S_RESIZE),
	/** Indicates text that may be selected. Often rendered as an I-beam. */
	TEXT(CSS.Cursor.TEXT),
	/** Indicates that the program is busy and the user should wait. Often rendered as a watch or hourglass. */
	WAIT(CSS.Cursor.WAIT);

	/** @return The URI of the predefined cursor relative to the application. */
	public URI getURI() {
		return URI.create(addExtension(GUISE_ROOT_THEME_CURSORS_PATH + getSerializationName(this), Images.PNG_NAME_EXTENSION));
	}

	/**
	 * The CSS cursor to which this cursor corresponds. Some cursors may need further checks to determine the correct CSS cursor based upon component
	 * orientation.
	 */
	private final CSS.Cursor cssCursor;

	/**
	 * CSS cursor constructor.
	 * @param cssCursor The CSS cursor to which this cursor corresponds.
	 * @throws NullPointerException if the given CSS cursor is <code>null</code>.
	 */
	private Cursor(final CSS.Cursor cssCursor) {
		this.cssCursor = requireNonNull(cssCursor, "CSS cursor cannot be null.");
	}

	/**
	 * Retrieves the CSS cursor that corresponds to the predefined cursor based in the given component orientation.
	 * @param orientation The component orientation for which the cursor should be returned.
	 * @return The CSS cursor corresponding to this cursor in the given orientation.
	 * @throws NullPointerException if the given orientation is <code>null</code>.
	 */
	public CSS.Cursor getCSSCursor(final Orientation orientation) {
		requireNonNull(orientation, "Orientation cannot be null.");
		final CompassPoint resizeCompassPoint; //we'll determine a esize compass point if needed
		switch(this) {
			case RESIZE_LINE_NEAR:
				resizeCompassPoint = orientation.getCompassPoint(Flow.End.NEAR, null);
				break;
			case RESIZE_LINE_NEAR_PAGE_NEAR:
				resizeCompassPoint = orientation.getCompassPoint(Flow.End.NEAR, Flow.End.NEAR);
				break;
			case RESIZE_LINE_NEAR_PAGE_FAR:
				resizeCompassPoint = orientation.getCompassPoint(Flow.End.NEAR, Flow.End.FAR);
				break;
			case RESIZE_LINE_FAR:
				resizeCompassPoint = orientation.getCompassPoint(Flow.End.FAR, null);
				break;
			case RESIZE_LINE_FAR_PAGE_NEAR:
				resizeCompassPoint = orientation.getCompassPoint(Flow.End.FAR, Flow.End.NEAR);
				break;
			case RESIZE_LINE_FAR_PAGE_FAR:
				resizeCompassPoint = orientation.getCompassPoint(Flow.End.FAR, Flow.End.FAR);
				break;
			case RESIZE_PAGE_NEAR:
				resizeCompassPoint = orientation.getCompassPoint((Flow.End)null, Flow.End.NEAR);
				break;
			case RESIZE_PAGE_FAR:
				resizeCompassPoint = orientation.getCompassPoint((Flow.End)null, Flow.End.FAR);
				break;
			default: //for all the other cursors
				return cssCursor; //return the non-directional cursor
		}
		switch(resizeCompassPoint) { //choose a CSS resize cursor based upon the compass point
			case NORTH:
				return CSS.Cursor.N_RESIZE;
			case NORTHEAST:
				return CSS.Cursor.NE_RESIZE;
			case EAST:
				return CSS.Cursor.NE_RESIZE;
			case SOUTHEAST:
				return CSS.Cursor.SE_RESIZE;
			case SOUTH:
				return CSS.Cursor.S_RESIZE;
			case SOUTHWEST:
				return CSS.Cursor.SW_RESIZE;
			case WEST:
				return CSS.Cursor.W_RESIZE;
			case NORTHWEST:
				return CSS.Cursor.NW_RESIZE;
			default:
				throw new AssertionError("Unsupported resize compass point: " + resizeCompassPoint);
		}
	}

	/**
	 * The read-only map of cursors mapped to application-relative URIs.
	 * @see Cursor#getURI()
	 */
	private static Map<URI, Cursor> uriCursorMap;

	static {
		final Map<URI, Cursor> uriCursorMap = new HashMap<URI, Cursor>(); //create a new map of cursors mapped to URIs
		for(final Cursor cursor : Cursor.values()) { //for each cursor
			uriCursorMap.put(cursor.getURI(), cursor); //create a mapping from the URI to this cursor
		}
		Cursor.uriCursorMap = unmodifiableMap(uriCursorMap); //store a read-only version of the cursor map
	}

	/**
	 * Determines a predefined cursor from the given URI
	 * @param uri The URI of the predefined cursor relative to the application.
	 * @return The predefined cursor indicated by the given URI, or <code>null</code> if the given URI does not identify a predefined cursor.
	 */
	public static Cursor getCursor(final URI uri) {
		return uriCursorMap.get(uri); //look up the corresponding cursor, if possible
	}
}
