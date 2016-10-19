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

package com.guiseframework.model.ui;

import java.net.URI;
import java.util.*;

import com.globalmentor.java.Objects;
import com.guiseframework.component.layout.*;
import com.guiseframework.event.*;
import com.guiseframework.geometry.*;
import com.guiseframework.model.Displayable;
import com.guiseframework.style.Color;
import com.guiseframework.style.FontStyle;
import com.guiseframework.style.LineStyle;

import static com.globalmentor.java.Arrays.*;
import static com.globalmentor.java.Objects.*;
import static com.guiseframework.theme.Theme.*;

/**
 * An abstract implementation of presentation-related information.
 * @author Garret Wilson
 */
public abstract class AbstractPresentationModel extends GuiseBoundPropertyObject implements PresentationModel {

	/** The background color of the component, or <code>null</code> if no background color is specified for this component. */
	private Color backgroundColor = null;

	/** @return The background color of the component, or <code>null</code> if no background color is specified for this component. */
	public Color getBackgroundColor() {
		return backgroundColor;
	}

	/**
	 * Sets the background color of the component. This is a bound property.
	 * @param newBackgroundColor The background color of the component, or <code>null</code> if the default background color should be used.
	 * @see #BACKGROUND_COLOR_PROPERTY
	 */
	public void setBackgroundColor(final Color newBackgroundColor) {
		if(!Objects.equals(backgroundColor, newBackgroundColor)) { //if the value is really changing
			final Color oldBackgroundColor = backgroundColor; //get the old value
			backgroundColor = newBackgroundColor; //actually change the value
			firePropertyChange(BACKGROUND_COLOR_PROPERTY, oldBackgroundColor, newBackgroundColor); //indicate that the value changed
		}
	}

	/** The array of border colors. */
	private Color[] borderColors = fill(new Color[Border.values().length], null);

	/** The properties corresponding to the border colors. */
	private static final String[] BORDER_COLOR_PROPERTIES;

	static {
		BORDER_COLOR_PROPERTIES = new String[Border.values().length]; //create the array of properties and fill it with corresponding properties
		BORDER_COLOR_PROPERTIES[Border.LINE_NEAR.ordinal()] = BORDER_LINE_NEAR_COLOR_PROPERTY;
		BORDER_COLOR_PROPERTIES[Border.LINE_FAR.ordinal()] = BORDER_LINE_FAR_COLOR_PROPERTY;
		BORDER_COLOR_PROPERTIES[Border.PAGE_NEAR.ordinal()] = BORDER_PAGE_NEAR_COLOR_PROPERTY;
		BORDER_COLOR_PROPERTIES[Border.PAGE_FAR.ordinal()] = BORDER_PAGE_FAR_COLOR_PROPERTY;
	}

	/**
	 * Returns the border color of the indicated border.
	 * @param border The border for which a border color should be returned.
	 * @return The border color of the given border, or <code>null</code> if the default border color should be used.
	 */
	public Color getBorderColor(final Border border) {
		return borderColors[border.ordinal()];
	}

	/**
	 * Returns the border color of the line near page near border.
	 * @return The border color of the border, or <code>null</code> if the default border color should be used.
	 */
	public Color BorderLineNearColor() {
		return getBorderColor(Border.LINE_NEAR);
	}

	/**
	 * Returns the border color of the line far page near border.
	 * @return The border color of the border, or <code>null</code> if the default border color should be used.
	 */
	public Color BorderLineFarColor() {
		return getBorderColor(Border.LINE_FAR);
	}

	/**
	 * Returns the border color of the line near page far border.
	 * @return The border color of the border, or <code>null</code> if the default border color should be used.
	 */
	public Color BorderPageNearColor() {
		return getBorderColor(Border.PAGE_NEAR);
	}

	/**
	 * Returns the border color of the line far page far border.
	 * @return The border color of the border, or <code>null</code> if the default border color should be used.
	 */
	public Color BorderPageFarColor() {
		return getBorderColor(Border.PAGE_FAR);
	}

	/**
	 * Sets the border color of a given border. The border color of each border represents a bound property.
	 * @param border The border for which the border color should be set.
	 * @param newBorderColor The border color, or <code>null</code> if the default border color should be used.
	 * @throws NullPointerException if the given border is <code>null</code>.
	 * @see #BORDER_LINE_NEAR_COLOR_PROPERTY
	 * @see #BORDER_LINE_FAR_COLOR_PROPERTY
	 * @see #BORDER_PAGE_NEAR_COLOR_PROPERTY
	 * @see #BORDER_PAGE_FAR_COLOR_PROPERTY
	 */
	public void setBorderColor(final Border border, final Color newBorderColor) {
		final int borderOrdinal = checkInstance(border, "Border cannot be null").ordinal(); //get the ordinal of the border
		final Color oldBorderColor = borderColors[borderOrdinal]; //get the old value
		if(!Objects.equals(oldBorderColor, newBorderColor)) { //if the value is really changing
			borderColors[borderOrdinal] = newBorderColor; //actually change the value
			firePropertyChange(BORDER_COLOR_PROPERTIES[borderOrdinal], oldBorderColor, newBorderColor); //indicate that the value changed
		}
	}

	/**
	 * Sets the border COLOR of the line near border. This is a bound property.
	 * @param newBorderColor The border color, or <code>null</code> if the default border color should be used.
	 * @see #BORDER_LINE_NEAR_COLOR_PROPERTY
	 */
	public void setBorderLineNearColor(final Color newBorderColor) {
		setBorderColor(Border.LINE_NEAR, newBorderColor);
	}

	/**
	 * Sets the border color of the line far border. This is a bound property.
	 * @param newBorderColor The border color, or <code>null</code> if the default border color should be used.
	 * @see #BORDER_LINE_FAR_COLOR_PROPERTY
	 */
	public void setBorderLineFarColor(final Color newBorderColor) {
		setBorderColor(Border.LINE_FAR, newBorderColor);
	}

	/**
	 * Sets the border color of the page near border. This is a bound property.
	 * @param newBorderColor The border color, or <code>null</code> if the default border color should be used.
	 * @see #BORDER_PAGE_NEAR_COLOR_PROPERTY
	 */
	public void setBorderPageNearColor(final Color newBorderColor) {
		setBorderColor(Border.PAGE_NEAR, newBorderColor);
	}

	/**
	 * Sets the border color of the page far border. This is a bound property.
	 * @param newBorderColor The border color, or <code>null</code> if the default border color should be used.
	 * @see #BORDER_PAGE_FAR_COLOR_PROPERTY
	 */
	public void setBorderPageFarColor(final Color newBorderColor) {
		setBorderColor(Border.PAGE_FAR, newBorderColor);
	}

	/**
	 * Sets the border color of all borders. The border color of each border represents a bound property. This is a convenience method that calls
	 * {@link #setBorderColor(Border, Color)} for each border.
	 * @param newBorderColor The border color, or <code>null</code> if the default border color should be used.
	 * @see #BORDER_LINE_NEAR_COLOR_PROPERTY
	 * @see #BORDER_LINE_FAR_COLOR_PROPERTY
	 * @see #BORDER_PAGE_NEAR_COLOR_PROPERTY
	 * @see #BORDER_PAGE_FAR_COLOR_PROPERTY
	 */
	public void setBorderColor(final Color newBorderColor) {
		for(final Border border : Border.values()) { //for each border
			setBorderColor(border, newBorderColor); //set this border color
		}
	}

	/** The array of border extents. */
	private Extent[] borderExtents = fill(new Extent[Border.values().length], Extent.ZERO_EXTENT1);

	/** The properties corresponding to the border extents. */
	private static final String[] BORDER_EXTENT_PROPERTIES;

	static {
		BORDER_EXTENT_PROPERTIES = new String[Border.values().length]; //create the array of properties and fill it with corresponding properties
		BORDER_EXTENT_PROPERTIES[Border.LINE_NEAR.ordinal()] = BORDER_LINE_NEAR_EXTENT_PROPERTY;
		BORDER_EXTENT_PROPERTIES[Border.LINE_FAR.ordinal()] = BORDER_LINE_FAR_EXTENT_PROPERTY;
		BORDER_EXTENT_PROPERTIES[Border.PAGE_NEAR.ordinal()] = BORDER_PAGE_NEAR_EXTENT_PROPERTY;
		BORDER_EXTENT_PROPERTIES[Border.PAGE_FAR.ordinal()] = BORDER_PAGE_FAR_EXTENT_PROPERTY;
	}

	/**
	 * Returns the border extent of the indicated border.
	 * @param border The border for which a border extent should be returned.
	 * @return The border extent of the given border.
	 */
	public Extent getBorderExtent(final Border border) {
		return borderExtents[border.ordinal()];
	}

	/**
	 * Returns the border extent of the line near page near border.
	 * @return The border extent of the given border.
	 */
	public Extent BorderLineNearExtent() {
		return getBorderExtent(Border.LINE_NEAR);
	}

	/**
	 * Returns the border extent of the line far page near border.
	 * @return The border extent of the given border.
	 */
	public Extent BorderLineFarExtent() {
		return getBorderExtent(Border.LINE_FAR);
	}

	/**
	 * Returns the border extent of the line near page far border.
	 * @return The border extent of the given border.
	 */
	public Extent BorderPageNearExtent() {
		return getBorderExtent(Border.PAGE_NEAR);
	}

	/**
	 * Returns the border extent of the line far page far border.
	 * @return The border extent of the given border.
	 */
	public Extent BorderPageFarExtent() {
		return getBorderExtent(Border.PAGE_FAR);
	}

	/**
	 * Sets the border extent of a given border. The border extent of each border represents a bound property.
	 * @param border The border for which the border extent should be set.
	 * @param newBorderExtent The border extent.
	 * @throws NullPointerException if the given border and/or border extent is <code>null</code>.
	 * @see #BORDER_LINE_NEAR_EXTENT_PROPERTY
	 * @see #BORDER_LINE_FAR_EXTENT_PROPERTY
	 * @see #BORDER_PAGE_NEAR_EXTENT_PROPERTY
	 * @see #BORDER_PAGE_FAR_EXTENT_PROPERTY
	 */
	public void setBorderExtent(final Border border, final Extent newBorderExtent) {
		final int borderOrdinal = checkInstance(border, "Border cannot be null").ordinal(); //get the ordinal of the border
		final Extent oldBorderExtent = borderExtents[borderOrdinal]; //get the old value
		if(!oldBorderExtent.equals(checkInstance(newBorderExtent, "Border extent cannot be null."))) { //if the value is really changing
			borderExtents[borderOrdinal] = newBorderExtent; //actually change the value
			firePropertyChange(BORDER_EXTENT_PROPERTIES[borderOrdinal], oldBorderExtent, newBorderExtent); //indicate that the value changed
		}
	}

	/**
	 * Sets the border extent of the line near border. This is a bound property.
	 * @param newBorderExtent The border extent.
	 * @throws NullPointerException if the given border extent is <code>null</code>.
	 * @see #BORDER_LINE_NEAR_EXTENT_PROPERTY
	 */
	public void setBorderLineNearExtent(final Extent newBorderExtent) {
		setBorderExtent(Border.LINE_NEAR, newBorderExtent);
	}

	/**
	 * Sets the border extent of the line far border. This is a bound property.
	 * @param newBorderExtent The border extent.
	 * @throws NullPointerException if the given border extent is <code>null</code>.
	 * @see #BORDER_LINE_FAR_EXTENT_PROPERTY
	 */
	public void setBorderLineFarExtent(final Extent newBorderExtent) {
		setBorderExtent(Border.LINE_FAR, newBorderExtent);
	}

	/**
	 * Sets the border extent of the page near border. This is a bound property.
	 * @param newBorderExtent The border extent.
	 * @throws NullPointerException if the given border extent is <code>null</code>.
	 * @see #BORDER_PAGE_NEAR_EXTENT_PROPERTY
	 */
	public void setBorderPageNearExtent(final Extent newBorderExtent) {
		setBorderExtent(Border.PAGE_NEAR, newBorderExtent);
	}

	/**
	 * Sets the border extent of the page far border. This is a bound property.
	 * @param newBorderExtent The border extent.
	 * @throws NullPointerException if the given border extent is <code>null</code>.
	 * @see #BORDER_PAGE_FAR_EXTENT_PROPERTY
	 */
	public void setBorderPageFarExtent(final Extent newBorderExtent) {
		setBorderExtent(Border.PAGE_FAR, newBorderExtent);
	}

	/**
	 * Sets the border extent of all borders. The border extent of each border represents a bound property. This is a convenience method that calls
	 * {@link #setBorderExtent(Border, Extent)} for each border.
	 * @param newBorderExtent The border extent.
	 * @throws NullPointerException if the given border extent is <code>null</code>.
	 * @see #BORDER_LINE_NEAR_EXTENT_PROPERTY
	 * @see #BORDER_LINE_FAR_EXTENT_PROPERTY
	 * @see #BORDER_PAGE_NEAR_EXTENT_PROPERTY
	 * @see #BORDER_PAGE_FAR_EXTENT_PROPERTY
	 */
	public void setBorderExtent(final Extent newBorderExtent) {
		for(final Border border : Border.values()) { //for each border
			setBorderExtent(border, newBorderExtent); //set this border extent
		}
	}

	/** The array of border styles. */
	private LineStyle[] borderStyles = fill(new LineStyle[Border.values().length], LineStyle.SOLID);

	/** The properties corresponding to the border styles. */
	private static final String[] BORDER_STYLE_PROPERTIES;

	static {
		BORDER_STYLE_PROPERTIES = new String[Border.values().length]; //create the array of properties and fill it with corresponding properties
		BORDER_STYLE_PROPERTIES[Border.LINE_NEAR.ordinal()] = BORDER_LINE_NEAR_STYLE_PROPERTY;
		BORDER_STYLE_PROPERTIES[Border.LINE_FAR.ordinal()] = BORDER_LINE_FAR_STYLE_PROPERTY;
		BORDER_STYLE_PROPERTIES[Border.PAGE_NEAR.ordinal()] = BORDER_PAGE_NEAR_STYLE_PROPERTY;
		BORDER_STYLE_PROPERTIES[Border.PAGE_FAR.ordinal()] = BORDER_PAGE_FAR_STYLE_PROPERTY;
	}

	/**
	 * Returns the border style of the indicated border.
	 * @param border The border for which a border style should be returned.
	 * @return The border style of the given border.
	 */
	public LineStyle getBorderStyle(final Border border) {
		return borderStyles[border.ordinal()];
	}

	/**
	 * Returns the border style of the line near page near border.
	 * @return The border style of the given border.
	 */
	public LineStyle BorderLineNearStyle() {
		return getBorderStyle(Border.LINE_NEAR);
	}

	/**
	 * Returns the border style of the line far page near border.
	 * @return The border style of the given border.
	 */
	public LineStyle BorderLineFarStyle() {
		return getBorderStyle(Border.LINE_FAR);
	}

	/**
	 * Returns the border style of the line near page far border.
	 * @return The border style of the given border.
	 */
	public LineStyle BorderPageNearStyle() {
		return getBorderStyle(Border.PAGE_NEAR);
	}

	/**
	 * Returns the border style of the line far page far border.
	 * @return The border style of the given border.
	 */
	public LineStyle BorderPageFarStyle() {
		return getBorderStyle(Border.PAGE_FAR);
	}

	/**
	 * Sets the border style of a given border. The border style of each border represents a bound property.
	 * @param border The border for which the border style should be set.
	 * @param newBorderStyle The border style.
	 * @throws NullPointerException if the given border and/or border style is <code>null</code>.
	 * @see #BORDER_LINE_NEAR_STYLE_PROPERTY
	 * @see #BORDER_LINE_FAR_STYLE_PROPERTY
	 * @see #BORDER_PAGE_NEAR_STYLE_PROPERTY
	 * @see #BORDER_PAGE_FAR_STYLE_PROPERTY
	 */
	public void setBorderStyle(final Border border, final LineStyle newBorderStyle) {
		final int borderOrdinal = checkInstance(border, "Border cannot be null").ordinal(); //get the ordinal of the border
		final LineStyle oldBorderStyle = borderStyles[borderOrdinal]; //get the old value
		if(oldBorderStyle != checkInstance(newBorderStyle, "Border style cannot be null.")) { //if the value is really changing
			borderStyles[borderOrdinal] = newBorderStyle; //actually change the value
			firePropertyChange(BORDER_STYLE_PROPERTIES[borderOrdinal], oldBorderStyle, newBorderStyle); //indicate that the value changed
		}
	}

	/**
	 * Sets the border style of the line near border. This is a bound property.
	 * @param newBorderStyle The border style.
	 * @throws NullPointerException if the given border style is <code>null</code>.
	 * @see #BORDER_LINE_NEAR_STYLE_PROPERTY
	 */
	public void setBorderLineNearStyle(final LineStyle newBorderStyle) {
		setBorderStyle(Border.LINE_NEAR, newBorderStyle);
	}

	/**
	 * Sets the border style of the line far border. This is a bound property.
	 * @param newBorderStyle The border style.
	 * @throws NullPointerException if the given border style is <code>null</code>.
	 * @see #BORDER_LINE_FAR_STYLE_PROPERTY
	 */
	public void setBorderLineFarStyle(final LineStyle newBorderStyle) {
		setBorderStyle(Border.LINE_FAR, newBorderStyle);
	}

	/**
	 * Sets the border style of the page near border. This is a bound property.
	 * @param newBorderStyle The border style.
	 * @throws NullPointerException if the given border style is <code>null</code>.
	 * @see #BORDER_PAGE_NEAR_STYLE_PROPERTY
	 */
	public void setBorderPageNearStyle(final LineStyle newBorderStyle) {
		setBorderStyle(Border.PAGE_NEAR, newBorderStyle);
	}

	/**
	 * Sets the border style of the page far border. This is a bound property.
	 * @param newBorderStyle The border style.
	 * @throws NullPointerException if the given border style is <code>null</code>.
	 * @see #BORDER_PAGE_FAR_STYLE_PROPERTY
	 */
	public void setBorderPageFarStyle(final LineStyle newBorderStyle) {
		setBorderStyle(Border.PAGE_FAR, newBorderStyle);
	}

	/**
	 * Sets the border style of all borders. The border style of each border represents a bound property. This is a convenience method that calls
	 * {@link #setBorderStyle(Border, LineStyle)} for each border.
	 * @param newBorderStyle The border style.
	 * @throws NullPointerException if the given border style is <code>null</code>.
	 * @see #BORDER_LINE_NEAR_STYLE_PROPERTY
	 * @see #BORDER_LINE_FAR_STYLE_PROPERTY
	 * @see #BORDER_PAGE_NEAR_STYLE_PROPERTY
	 * @see #BORDER_PAGE_FAR_STYLE_PROPERTY
	 */
	public void setBorderStyle(final LineStyle newBorderStyle) {
		for(final Border border : Border.values()) { //for each border
			setBorderStyle(border, newBorderStyle); //set this border style
		}
	}

	/** The array of dimensions each defining a corner arc by two radiuses. */
	private Dimensions[] cornerArcSizes = fill(new Dimensions[Corner.values().length], Dimensions.ZERO_DIMENSIONS);

	/** The properties corresponding to the corner arc sizes. */
	private static final String[] CORNER_ARC_SIZE_PROPERTIES;

	static {
		CORNER_ARC_SIZE_PROPERTIES = new String[Corner.values().length]; //create the array of properties and fill it with corresponding properties
		CORNER_ARC_SIZE_PROPERTIES[Corner.LINE_NEAR_PAGE_NEAR.ordinal()] = CORNER_LINE_NEAR_PAGE_NEAR_ARC_SIZE_PROPERTY;
		CORNER_ARC_SIZE_PROPERTIES[Corner.LINE_FAR_PAGE_NEAR.ordinal()] = CORNER_LINE_FAR_PAGE_NEAR_ARC_SIZE_PROPERTY;
		CORNER_ARC_SIZE_PROPERTIES[Corner.LINE_NEAR_PAGE_FAR.ordinal()] = CORNER_LINE_NEAR_PAGE_FAR_ARC_SIZE_PROPERTY;
		CORNER_ARC_SIZE_PROPERTIES[Corner.LINE_FAR_PAGE_FAR.ordinal()] = CORNER_LINE_FAR_PAGE_FAR_ARC_SIZE_PROPERTY;
	}

	/**
	 * Returns the arc size for the indicated corner.
	 * @param corner The corner for which an arc size should be returned.
	 * @return The dimensions indicating the two radiuses of the given corner arc, or dimensions of zero if the corner should not be rounded.
	 */
	public Dimensions getCornerArcSize(final Corner corner) {
		return cornerArcSizes[corner.ordinal()];
	}

	/**
	 * Returns the arc size for the line near page near corner.
	 * @return The dimensions indicating the two radiuses of the corner arc, or dimensions of zero if the corner should not be rounded.
	 */
	public Dimensions getCornerLineNearPageNearArcSize() {
		return getCornerArcSize(Corner.LINE_NEAR_PAGE_NEAR);
	}

	/**
	 * Returns the arc size for the line far page near corner.
	 * @return The dimensions indicating the two radiuses of the corner arc, or dimensions of zero if the corner should not be rounded.
	 */
	public Dimensions getCornerLineFarPageNearArcSize() {
		return getCornerArcSize(Corner.LINE_FAR_PAGE_NEAR);
	}

	/**
	 * Returns the arc size for the line near page far corner.
	 * @return The dimensions indicating the two radiuses of the corner arc, or dimensions of zero if the corner should not be rounded.
	 */
	public Dimensions getCornerLineNearPageFarArcSize() {
		return getCornerArcSize(Corner.LINE_NEAR_PAGE_FAR);
	}

	/**
	 * Returns the arc size for the line far page far corner.
	 * @return The dimensions indicating the two radiuses of the corner arc, or dimensions of zero if the corner should not be rounded.
	 */
	public Dimensions getCornerLineFarPageFarArcSize() {
		return getCornerArcSize(Corner.LINE_FAR_PAGE_FAR);
	}

	/**
	 * Sets the arc size of a given corner. The radius of each corner represents a bound property.
	 * @param corner The corner for which the arc size should be set.
	 * @param newCornerArcSize The dimensions indicating the two radiuses of the corner, or dimensions of zero if the corner should not be rounded.
	 * @throws NullPointerException if the given corner and/or arc size is <code>null</code>.
	 * @see #CORNER_LINE_NEAR_PAGE_NEAR_ARC_SIZE_PROPERTY
	 * @see #CORNER_LINE_FAR_PAGE_NEAR_ARC_SIZE_PROPERTY
	 * @see #CORNER_LINE_NEAR_PAGE_FAR_ARC_SIZE_PROPERTY
	 * @see #CORNER_LINE_FAR_PAGE_FAR_ARC_SIZE_PROPERTY
	 */
	public void setCornerArcSize(final Corner corner, final Dimensions newCornerArcSize) {
		final int cornerOrdinal = checkInstance(corner, "Corner cannot be null").ordinal(); //get the ordinal of the corner
		final Dimensions oldCornerArcSize = cornerArcSizes[cornerOrdinal]; //get the old value
		if(!Objects.equals(oldCornerArcSize, checkInstance(newCornerArcSize, "Corner arc size cannot be null"))) { //if the value is really changing TODO decide if null dimensions should be accepted
			cornerArcSizes[cornerOrdinal] = newCornerArcSize; //actually change the value
			firePropertyChange(CORNER_ARC_SIZE_PROPERTIES[cornerOrdinal], oldCornerArcSize, newCornerArcSize); //indicate that the value changed
		}
	}

	/**
	 * Sets the arc size of the line near page near corner. This is a bound property.
	 * @param newCornerArcSize The dimensions indicating the two radiuses of the corner, or dimensions of zero if the corner should not be rounded.
	 * @throws NullPointerException if the given size is <code>null</code>.
	 * @see #CORNER_LINE_NEAR_PAGE_NEAR_ARC_SIZE_PROPERTY
	 */
	public void setCornerLineNearPageNearArcSize(final Dimensions newCornerArcSize) {
		setCornerArcSize(Corner.LINE_NEAR_PAGE_NEAR, newCornerArcSize);
	}

	/**
	 * Sets the arc size of the line far page near corner. This is a bound property.
	 * @param newCornerArcSize The dimensions indicating the two radiuses of the corner, or dimensions of zero if the corner should not be rounded.
	 * @throws NullPointerException if the given size is <code>null</code>.
	 * @see #CORNER_LINE_FAR_PAGE_NEAR_ARC_SIZE_PROPERTY
	 */
	public void setCornerLineFarPageNearArcSize(final Dimensions newCornerArcSize) {
		setCornerArcSize(Corner.LINE_FAR_PAGE_NEAR, newCornerArcSize);
	}

	/**
	 * Sets the arc size of the line near page far corner. This is a bound property.
	 * @param newCornerArcSize The dimensions indicating the two radiuses of the corner, or dimensions of zero if the corner should not be rounded.
	 * @throws NullPointerException if the given size is <code>null</code>.
	 * @see #CORNER_LINE_NEAR_PAGE_FAR_ARC_SIZE_PROPERTY
	 */
	public void setCornerLineNearPageFarArcSize(final Dimensions newCornerArcSize) {
		setCornerArcSize(Corner.LINE_NEAR_PAGE_FAR, newCornerArcSize);
	}

	/**
	 * Sets the arc size of the line far page far corner. This is a bound property.
	 * @param newCornerArcSize The dimensions indicating the two radiuses of the corner, or dimensions of zero if the corner should not be rounded.
	 * @throws NullPointerException if the given size is <code>null</code>.
	 * @see #CORNER_LINE_FAR_PAGE_FAR_ARC_SIZE_PROPERTY
	 */
	public void setCornerLineFarPageFarArcSize(final Dimensions newCornerArcSize) {
		setCornerArcSize(Corner.LINE_FAR_PAGE_FAR, newCornerArcSize);
	}

	/**
	 * Sets the arc size of all corners. The radius of each corner represents a bound property. This is a convenience method that calls
	 * {@link #setCornerArcSize(Corner, Dimensions)} for each corner.
	 * @param newCornerArcSize The dimensions indicating the two radiuses of the corners, or dimensions of zero if the corners should not be rounded.
	 * @throws NullPointerException if the given arc size is <code>null</code>.
	 * @see #CORNER_LINE_NEAR_PAGE_NEAR_ARC_SIZE_PROPERTY
	 * @see #CORNER_LINE_FAR_PAGE_NEAR_ARC_SIZE_PROPERTY
	 * @see #CORNER_LINE_NEAR_PAGE_FAR_ARC_SIZE_PROPERTY
	 * @see #CORNER_LINE_FAR_PAGE_FAR_ARC_SIZE_PROPERTY
	 */
	public void setCornerArcSize(final Dimensions newCornerArcSize) {
		for(final Corner corner : Corner.values()) { //for each corner
			setCornerArcSize(corner, newCornerArcSize); //set this corner arc size
		}
	}

	/** The cursor URI, which may be a resource URI. */
	private URI cursor = CURSOR_DEFAULT;

	/** @return The cursor URI, which may be a resource URI. */
	public URI getCursor() {
		return cursor;
	}

	/**
	 * Sets the URI of the cursor. This is a bound property.
	 * @param newCursor The new URI of the cursor, which may be a resource URI.
	 * @throws NullPointerException if the given cursor URI is <code>null</code>.
	 * @see #CURSOR_PROPERTY
	 */
	public void setCursor(final URI newCursor) {
		if(!cursor.equals(checkInstance(newCursor, "Cursor URI cannot be null."))) { //if the value is really changing
			final URI oldCursor = cursor; //get the old value
			cursor = newCursor; //actually change the value
			firePropertyChange(CURSOR_PROPERTY, oldCursor, newCursor); //indicate that the value changed
		}
	}

	/** The array of component extents. */
	private Extent[] extents = fill(new Extent[Flow.values().length], null);

	/** The properties corresponding to the component extents. */
	private static final String[] EXTENT_PROPERTIES;

	static {
		EXTENT_PROPERTIES = new String[Flow.values().length]; //create the array of properties and fill it with corresponding properties
		EXTENT_PROPERTIES[Flow.LINE.ordinal()] = LINE_EXTENT_PROPERTY;
		EXTENT_PROPERTIES[Flow.PAGE.ordinal()] = PAGE_EXTENT_PROPERTY;
	}

	/**
	 * Returns the extent of the indicated flow.
	 * @param flow The flow for which an extent should be returned.
	 * @return The extent of the given flow.
	 */
	public Extent getExtent(final Flow flow) {
		return extents[flow.ordinal()];
	}

	/**
	 * Returns the extent of the line flow. In left-to-right top-to-bottom orientation, this is commonly known as the <dfn>width</dfn>.
	 * @return The extent of the flow, or <code>null</code> if no preferred extent has been specified
	 */
	public Extent getLineExtent() {
		return getExtent(Flow.LINE);
	}

	/**
	 * Returns the extent of the page flow. In left-to-right top-to-bottom orientation, this is commonly known as the <dfn>height</dfn>.
	 * @return The extent of the flow, or <code>null</code> if no preferred extent has been specified
	 */
	public Extent getPageExtent() {
		return getExtent(Flow.PAGE);
	}

	/**
	 * Sets the extent of a given flow. The extent of each flow represents a bound property.
	 * @param flow The flow for which the extent should be set.
	 * @param newExtent The new requested extent of the component, or <code>null</code> there is no extent preference.
	 * @throws NullPointerException if the given flow is <code>null</code>.
	 * @see #LINE_EXTENT_PROPERTY
	 * @see #PAGE_EXTENT_PROPERTY
	 */
	public void setExtent(final Flow flow, final Extent newExtent) {
		final int flowOrdinal = checkInstance(flow, "Flow cannot be null").ordinal(); //get the ordinal of the flow
		final Extent oldExtent = extents[flowOrdinal]; //get the old value
		if(!Objects.equals(oldExtent, newExtent)) { //if the value is really changing
			extents[flowOrdinal] = newExtent; //actually change the value
			firePropertyChange(EXTENT_PROPERTIES[flowOrdinal], oldExtent, newExtent); //indicate that the value changed
		}
	}

	/**
	 * Sets the extent of the line flow. In left-to-right top-to-bottom orientation, this is commonly known as the <dfn>width</dfn>. This is a bound property.
	 * @param newExtent The new requested extent of the component, or <code>null</code> there is no extent preference.
	 * @see #LINE_EXTENT_PROPERTY
	 */
	public void setLineExtent(final Extent newExtent) {
		setExtent(Flow.LINE, newExtent);
	}

	/**
	 * Sets the extent of the page flow. In left-to-right top-to-bottom orientation, this is commonly known as the <dfn>height</dfn>. This is a bound property.
	 * @param newExtent The new requested extent of the component, or <code>null</code> there is no extent preference.
	 * @see #PAGE_EXTENT_PROPERTY
	 */
	public void setPageExtent(final Extent newExtent) {
		setExtent(Flow.PAGE, newExtent);
	}

	/** The prioritized list of font family names, or <code>null</code> if no font family names have been specified. */
	private List<String> fontFamilies = null;

	/** @return The prioritized list of font family names, or <code>null</code> if no font family names have been specified. */
	public List<String> getFontFamilies() {
		return fontFamilies;
	}

	/**
	 * Sets the font families of the component. This is a bound property.
	 * @param newFontFamilies The new prioritized list of font family names, or <code>null</code> if no font family names are specified.
	 * @see #FONT_FAMILIES_PROPERTY
	 */
	public void setFontFamilies(final List<String> newFontFamilies) {
		if(!Objects.equals(fontFamilies, newFontFamilies)) { //if the value is really changing
			final List<String> oldFontFamilies = fontFamilies; //get the old value
			fontFamilies = newFontFamilies; //actually change the value
			firePropertyChange(FONT_FAMILIES_PROPERTY, oldFontFamilies, newFontFamilies); //indicate that the value changed
		}
	}

	/** The size of the font from baseline to baseline, or <code>null</code> if no font size has been specified. */
	private Extent fontSize = null;

	/** @return The size of the font from baseline to baseline, or <code>null</code> if no font size has been specified. */
	public Extent getFontSize() {
		return fontSize;
	}

	/**
	 * Sets the font size of the component. This is a bound property.
	 * @param newFontSize The new size of the font from baseline to baseline, or <code>null</code> there is no font size specified.
	 * @see #FONT_SIZE_PROPERTY
	 */
	public void setFontSize(final Extent newFontSize) {
		if(!Objects.equals(fontSize, newFontSize)) { //if the value is really changing
			final Extent oldFontSize = fontSize; //get the old value
			fontSize = newFontSize; //actually change the value
			firePropertyChange(FONT_SIZE_PROPERTY, oldFontSize, newFontSize); //indicate that the value changed
		}
	}

	/** The style of the font. */
	private FontStyle fontStyle = FontStyle.NORMAL;

	/** @return The style of the font. */
	public FontStyle getFontStyle() {
		return fontStyle;
	}

	/**
	 * Sets the style of the font. This is a bound property.
	 * @param newFontStyle The style of the font.
	 * @throws NullPointerException if the given font style is <code>null</code>.
	 * @see #FONT_STYLE_PROPERTY
	 */
	public void setFontStyle(final FontStyle newFontStyle) {
		if(fontStyle != checkInstance(newFontStyle, "Font style cannot be null.")) { //if the value is really changing
			final FontStyle oldFontStyle = fontStyle; //get the current value
			fontStyle = newFontStyle; //update the value
			firePropertyChange(FONT_STYLE_PROPERTY, oldFontStyle, newFontStyle);
		}
	}

	/** The weight of the font relative to a normal value of 0.5. */
	private double fontWeight = 0.5;

	/** @return The weight of the font relative to a normal value of 0.5. */
	public double getFontWeight() {
		return fontWeight;
	}

	/**
	 * Sets the weight of the font. The weight of the font relative to a normal value of 0.5. A font weight of 0.75 is equivalent to a bold font. It is
	 * recommended that the constant variables {@link #FONT_WEIGHT_NORMAL} and {@link #FONT_WEIGHT_BOLD} be used for the most compatibility across platforms. This
	 * is a bound property of type {@link Double}.
	 * @param newFontWeight The weight of the font relative to a normal value of 0.5.
	 * @see #FONT_WEIGHT_PROPERTY
	 * @see #FONT_WEIGHT_NORMAL
	 * @see #FONT_WEIGHT_BOLD
	 */
	public void setFontWeight(final double newFontWeight) {
		if(fontWeight != newFontWeight) { //if the value is really changing
			final double oldFontWeight = fontWeight; //get the current value
			fontWeight = newFontWeight; //update the value
			firePropertyChange(FONT_WEIGHT_PROPERTY, Double.valueOf(oldFontWeight), Double.valueOf(newFontWeight));
		}
	}

	/** The prioritized list of label font family names, or <code>null</code> if no label font family names have been specified. */
	private List<String> labelFontFamilies = null;

	/** @return The prioritized list of label font family names, or <code>null</code> if no label font family names have been specified. */
	public List<String> getLabelFontFamilies() {
		return labelFontFamilies;
	}

	/**
	 * Sets the font families of the label. This is a bound property.
	 * @param newLabelFontFamilies The new prioritized list of label font family names, or <code>null</code> if no label font family names are specified.
	 * @see #LABEL_FONT_FAMILIES_PROPERTY
	 */
	public void setLabelFontFamilies(final List<String> newLabelFontFamilies) {
		if(!Objects.equals(labelFontFamilies, newLabelFontFamilies)) { //if the value is really changing
			final List<String> oldLabelFontFamilies = labelFontFamilies; //get the old value
			labelFontFamilies = newLabelFontFamilies; //actually change the value
			firePropertyChange(LABEL_FONT_FAMILIES_PROPERTY, oldLabelFontFamilies, newLabelFontFamilies); //indicate that the value changed
		}
	}

	/** The size of the label font from baseline to baseline, or <code>null</code> if no label font size has been specified. */
	private Extent labelFontSize = null;

	/** @return The size of the label font from baseline to baseline, or <code>null</code> if no label font size has been specified. */
	public Extent getLabelFontSize() {
		return labelFontSize;
	}

	/**
	 * Sets the label font size of the component. This is a bound property.
	 * @param newLabelFontSize The new size of the label font from baseline to baseline, or <code>null</code> there is no label font size specified.
	 * @see #LABEL_FONT_SIZE_PROPERTY
	 */
	public void setLabelFontSize(final Extent newLabelFontSize) {
		if(!Objects.equals(labelFontSize, newLabelFontSize)) { //if the value is really changing
			final Extent oldLabelFontSize = labelFontSize; //get the old value
			labelFontSize = newLabelFontSize; //actually change the value
			firePropertyChange(LABEL_FONT_SIZE_PROPERTY, oldLabelFontSize, newLabelFontSize); //indicate that the value changed
		}
	}

	/** The style of the label font. */
	private FontStyle labelFontStyle = FontStyle.NORMAL;

	/** @return The style of the label font. */
	public FontStyle getLabelFontStyle() {
		return labelFontStyle;
	}

	/**
	 * Sets the style of the label font. This is a bound property.
	 * @param newLabelFontStyle The style of the label font.
	 * @throws NullPointerException if the given label font style is <code>null</code>.
	 * @see #LABEL_FONT_STYLE_PROPERTY
	 */
	public void setLabelFontStyle(final FontStyle newLabelFontStyle) {
		if(labelFontStyle != checkInstance(newLabelFontStyle, "Label font style cannot be null.")) { //if the value is really changing
			final FontStyle oldLabelFontStyle = labelFontStyle; //get the current value
			labelFontStyle = newLabelFontStyle; //update the value
			firePropertyChange(LABEL_FONT_STYLE_PROPERTY, oldLabelFontStyle, newLabelFontStyle);
		}
	}

	/** The weight of the label font relative to a normal value of 0.5. */
	private double labelFontWeight = 0.5;

	/** @return The weight of the label font relative to a normal value of 0.5. */
	public double getLabelFontWeight() {
		return labelFontWeight;
	}

	/**
	 * Sets the weight of the label font. The weight of the label font relative to a normal value of 0.5. A font weight of 0.75 is equivalent to a bold font. It
	 * is recommended that the constant variables {@link #FONT_WEIGHT_NORMAL} and {@link #FONT_WEIGHT_BOLD} be used for the most compatibility across platforms.
	 * This is a bound property of type {@link Double}.
	 * @param newLabelFontWeight The weight of the label font relative to a normal value of 0.5.
	 * @see #FONT_WEIGHT_PROPERTY
	 * @see #FONT_WEIGHT_NORMAL
	 * @see #FONT_WEIGHT_BOLD
	 */
	public void setLabelFontWeight(final double newLabelFontWeight) {
		if(labelFontWeight != newLabelFontWeight) { //if the value is really changing
			final double oldLabelFontWeight = labelFontWeight; //get the current value
			labelFontWeight = newLabelFontWeight; //update the value
			firePropertyChange(LABEL_FONT_WEIGHT_PROPERTY, Double.valueOf(oldLabelFontWeight), Double.valueOf(newLabelFontWeight));
		}
	}

	/** The text color of the label, or <code>null</code> if no text color is specified for the label. */
	private Color labelTextColor = null;

	/** @return The text color of the label, or <code>null</code> if no text color is specified for the label. */
	public Color getLabelTextColor() {
		return labelTextColor;
	}

	/**
	 * Sets the text color of the label. This is a bound property.
	 * @param newLabelTextColor The text color of the label, or <code>null</code> if the default text color should be used.
	 * @see #LABEL_TEXT_COLOR_PROPERTY
	 */
	public void setLabelTextColor(final Color newLabelTextColor) {
		if(!Objects.equals(labelTextColor, newLabelTextColor)) { //if the value is really changing
			final Color oldLabelTextColor = labelTextColor; //get the old value
			labelTextColor = newLabelTextColor; //actually change the value
			firePropertyChange(LABEL_TEXT_COLOR_PROPERTY, oldLabelTextColor, newLabelTextColor); //indicate that the value changed
		}
	}

	/** The array of margin extents. */
	private Extent[] marginExtents = fill(new Extent[Border.values().length], Extent.ZERO_EXTENT1);

	/** The properties corresponding to the margin extents. */
	private static final String[] MARGIN_EXTENT_PROPERTIES;

	static {
		MARGIN_EXTENT_PROPERTIES = new String[Border.values().length]; //create the array of properties and fill it with corresponding properties
		MARGIN_EXTENT_PROPERTIES[Border.LINE_NEAR.ordinal()] = MARGIN_LINE_NEAR_EXTENT_PROPERTY;
		MARGIN_EXTENT_PROPERTIES[Border.LINE_FAR.ordinal()] = MARGIN_LINE_FAR_EXTENT_PROPERTY;
		MARGIN_EXTENT_PROPERTIES[Border.PAGE_NEAR.ordinal()] = MARGIN_PAGE_NEAR_EXTENT_PROPERTY;
		MARGIN_EXTENT_PROPERTIES[Border.PAGE_FAR.ordinal()] = MARGIN_PAGE_FAR_EXTENT_PROPERTY;
	}

	/**
	 * Returns the margin extent of the indicated border.
	 * @param border The border for which a margin extent should be returned.
	 * @return The margin extent of the given border.
	 */
	public Extent getMarginExtent(final Border border) {
		return marginExtents[border.ordinal()];
	}

	/**
	 * Returns the margin extent of the line near page near border.
	 * @return The margin extent of the given border.
	 */
	public Extent getMarginLineNearExtent() {
		return getMarginExtent(Border.LINE_NEAR);
	}

	/**
	 * Returns the margin extent of the line far page near border.
	 * @return The margin extent of the given border.
	 */
	public Extent getMarginLineFarExtent() {
		return getMarginExtent(Border.LINE_FAR);
	}

	/**
	 * Returns the margin extent of the line near page far border.
	 * @return The margin extent of the given border.
	 */
	public Extent getMarginPageNearExtent() {
		return getMarginExtent(Border.PAGE_NEAR);
	}

	/**
	 * Returns the margin extent of the line far page far border.
	 * @return The margin extent of the given border.
	 */
	public Extent getMarginPageFarExtent() {
		return getMarginExtent(Border.PAGE_FAR);
	}

	/**
	 * Sets the margin extent of a given border. The margin extent of each border represents a bound property.
	 * @param border The border for which the margin extent should be set.
	 * @param newMarginExtent The margin extent.
	 * @throws NullPointerException if the given border and/or margin extent is <code>null</code>.
	 * @see #MARGIN_LINE_NEAR_EXTENT_PROPERTY
	 * @see #MARGIN_LINE_FAR_EXTENT_PROPERTY
	 * @see #MARGIN_PAGE_NEAR_EXTENT_PROPERTY
	 * @see #MARGIN_PAGE_FAR_EXTENT_PROPERTY
	 */
	public void setMarginExtent(final Border border, final Extent newMarginExtent) {
		final int borderOrdinal = checkInstance(border, "Border cannot be null").ordinal(); //get the ordinal of the border
		final Extent oldMarginExtent = marginExtents[borderOrdinal]; //get the old value
		if(!oldMarginExtent.equals(checkInstance(newMarginExtent, "margin extent cannot be null."))) { //if the value is really changing
			marginExtents[borderOrdinal] = newMarginExtent; //actually change the value
			firePropertyChange(MARGIN_EXTENT_PROPERTIES[borderOrdinal], oldMarginExtent, newMarginExtent); //indicate that the value changed
		}
	}

	/**
	 * Sets the margin extent of the line near border. This is a bound property.
	 * @param newMarginExtent The margin extent.
	 * @throws NullPointerException if the given margin extent is <code>null</code>.
	 * @see #MARGIN_LINE_NEAR_EXTENT_PROPERTY
	 */
	public void setMarginLineNearExtent(final Extent newMarginExtent) {
		setMarginExtent(Border.LINE_NEAR, newMarginExtent);
	}

	/**
	 * Sets the margin extent of the line far border. This is a bound property.
	 * @param newMarginExtent The margin extent, or <code>null</code> if the default margin extent should be used.
	 * @throws NullPointerException if the given margin extent is <code>null</code>.
	 * @see #MARGIN_LINE_FAR_EXTENT_PROPERTY
	 */
	public void setMarginLineFarExtent(final Extent newMarginExtent) {
		setMarginExtent(Border.LINE_FAR, newMarginExtent);
	}

	/**
	 * Sets the margin extent of the page near border. This is a bound property.
	 * @param newMarginExtent The margin extent, or <code>null</code> if the default margin extent should be used.
	 * @throws NullPointerException if the given margin extent is <code>null</code>.
	 * @see #MARGIN_PAGE_NEAR_EXTENT_PROPERTY
	 */
	public void setMarginPageNearExtent(final Extent newMarginExtent) {
		setMarginExtent(Border.PAGE_NEAR, newMarginExtent);
	}

	/**
	 * Sets the margin extent of the page far border. This is a bound property.
	 * @param newMarginExtent The margin extent, or <code>null</code> if the default margin extent should be used.
	 * @throws NullPointerException if the given margin extent is <code>null</code>.
	 * @see #MARGIN_PAGE_FAR_EXTENT_PROPERTY
	 */
	public void setMarginPageFarExtent(final Extent newMarginExtent) {
		setMarginExtent(Border.PAGE_FAR, newMarginExtent);
	}

	/**
	 * Sets the margin extent of all borders. The margin extent of each border represents a bound property. This is a convenience method that calls
	 * {@link #setMarginExtent(Border, Extent)} for each border.
	 * @param newMarginExtent The margin extent.
	 * @throws NullPointerException if the given margin extent is <code>null</code>.
	 * @see #MARGIN_LINE_NEAR_EXTENT_PROPERTY
	 * @see #MARGIN_LINE_FAR_EXTENT_PROPERTY
	 * @see #MARGIN_PAGE_NEAR_EXTENT_PROPERTY
	 * @see #MARGIN_PAGE_FAR_EXTENT_PROPERTY
	 */
	public void setMarginExtent(final Extent newMarginExtent) {
		for(final Border border : Border.values()) { //for each border
			setMarginExtent(border, newMarginExtent); //set this margin extent
		}
	}

	/** The opacity of the entire component in the range (0.0-1.0), with a default of 1.0. */
	private double opacity = 1.0f;

	/** @return The opacity of the entire component in the range (0.0-1.0), with a default of 1.0. */
	public double getOpacity() {
		return opacity;
	}

	/**
	 * Sets the opacity of the entire component. This is a bound property of type {@link Double}.
	 * @param newOpacity The new opacity of the entire component in the range (0.0-1.0).
	 * @throws IllegalArgumentException if the given opacity is not within the range (0.0-1.0).
	 * @see #OPACITY_PROPERTY
	 */
	public void setOpacity(final double newOpacity) {
		if(newOpacity < 0.0f || newOpacity > 1.0f) { //if the new opacity is out of range
			throw new IllegalArgumentException("Opacity " + newOpacity + " is not within the allowed range.");
		}
		if(opacity != newOpacity) { //if the value is really changing
			final double oldOpacity = opacity; //get the old value
			opacity = newOpacity; //actually change the value
			firePropertyChange(OPACITY_PROPERTY, Double.valueOf(oldOpacity), Double.valueOf(newOpacity)); //indicate that the value changed
		}
	}

	/** The array of padding extents. */
	private Extent[] paddingExtents = fill(new Extent[Border.values().length], Extent.ZERO_EXTENT1);

	/** The properties corresponding to the padding extents. */
	private static final String[] PADDING_EXTENT_PROPERTIES;

	static {
		PADDING_EXTENT_PROPERTIES = new String[Border.values().length]; //create the array of properties and fill it with corresponding properties
		PADDING_EXTENT_PROPERTIES[Border.LINE_NEAR.ordinal()] = PADDING_LINE_NEAR_EXTENT_PROPERTY;
		PADDING_EXTENT_PROPERTIES[Border.LINE_FAR.ordinal()] = PADDING_LINE_FAR_EXTENT_PROPERTY;
		PADDING_EXTENT_PROPERTIES[Border.PAGE_NEAR.ordinal()] = PADDING_PAGE_NEAR_EXTENT_PROPERTY;
		PADDING_EXTENT_PROPERTIES[Border.PAGE_FAR.ordinal()] = PADDING_PAGE_FAR_EXTENT_PROPERTY;
	}

	/**
	 * Returns the padding extent of the indicated border.
	 * @param border The border for which a padding extent should be returned.
	 * @return The padding extent of the given border.
	 */
	public Extent getPaddingExtent(final Border border) {
		return paddingExtents[border.ordinal()];
	}

	/**
	 * Returns the padding extent of the line near page near border.
	 * @return The padding extent of the given border.
	 */
	public Extent getPaddingLineNearExtent() {
		return getPaddingExtent(Border.LINE_NEAR);
	}

	/**
	 * Returns the padding extent of the line far page near border.
	 * @return The padding extent of the given border.
	 */
	public Extent getPaddingLineFarExtent() {
		return getPaddingExtent(Border.LINE_FAR);
	}

	/**
	 * Returns the padding extent of the line near page far border.
	 * @return The padding extent of the given border.
	 */
	public Extent getPaddingPageNearExtent() {
		return getPaddingExtent(Border.PAGE_NEAR);
	}

	/**
	 * Returns the padding extent of the line far page far border.
	 * @return The padding extent of the given border.
	 */
	public Extent getPaddingPageFarExtent() {
		return getPaddingExtent(Border.PAGE_FAR);
	}

	/**
	 * Sets the padding extent of a given border. The padding extent of each border represents a bound property.
	 * @param border The border for which the padding extent should be set.
	 * @param newPaddingExtent The padding extent.
	 * @throws NullPointerException if the given border and/or padding extent is <code>null</code>.
	 * @see #PADDING_LINE_NEAR_EXTENT_PROPERTY
	 * @see #PADDING_LINE_FAR_EXTENT_PROPERTY
	 * @see #PADDING_PAGE_NEAR_EXTENT_PROPERTY
	 * @see #PADDING_PAGE_FAR_EXTENT_PROPERTY
	 */
	public void setPaddingExtent(final Border border, final Extent newPaddingExtent) {
		final int borderOrdinal = checkInstance(border, "Border cannot be null").ordinal(); //get the ordinal of the border
		final Extent oldPaddingExtent = paddingExtents[borderOrdinal]; //get the old value
		if(!oldPaddingExtent.equals(checkInstance(newPaddingExtent, "Padding extent cannot be null."))) { //if the value is really changing
			paddingExtents[borderOrdinal] = newPaddingExtent; //actually change the value
			firePropertyChange(PADDING_EXTENT_PROPERTIES[borderOrdinal], oldPaddingExtent, newPaddingExtent); //indicate that the value changed
		}
	}

	/**
	 * Sets the padding extent of the line near border. This is a bound property.
	 * @param newPaddingExtent The padding extent.
	 * @throws NullPointerException if the given padding extent is <code>null</code>.
	 * @see #PADDING_LINE_NEAR_EXTENT_PROPERTY
	 */
	public void setPaddingLineNearExtent(final Extent newPaddingExtent) {
		setPaddingExtent(Border.LINE_NEAR, newPaddingExtent);
	}

	/**
	 * Sets the padding extent of the line far border. This is a bound property.
	 * @param newPaddingExtent The padding extent, or <code>null</code> if the default padding extent should be used.
	 * @throws NullPointerException if the given padding extent is <code>null</code>.
	 * @see #PADDING_LINE_FAR_EXTENT_PROPERTY
	 */
	public void setPaddingLineFarExtent(final Extent newPaddingExtent) {
		setPaddingExtent(Border.LINE_FAR, newPaddingExtent);
	}

	/**
	 * Sets the padding extent of the page near border. This is a bound property.
	 * @param newPaddingExtent The padding extent, or <code>null</code> if the default padding extent should be used.
	 * @throws NullPointerException if the given padding extent is <code>null</code>.
	 * @see #PADDING_PAGE_NEAR_EXTENT_PROPERTY
	 */
	public void setPaddingPageNearExtent(final Extent newPaddingExtent) {
		setPaddingExtent(Border.PAGE_NEAR, newPaddingExtent);
	}

	/**
	 * Sets the padding extent of the page far border. This is a bound property.
	 * @param newPaddingExtent The padding extent, or <code>null</code> if the default padding extent should be used.
	 * @throws NullPointerException if the given padding extent is <code>null</code>.
	 * @see #PADDING_PAGE_FAR_EXTENT_PROPERTY
	 */
	public void setPaddingPageFarExtent(final Extent newPaddingExtent) {
		setPaddingExtent(Border.PAGE_FAR, newPaddingExtent);
	}

	/**
	 * Sets the padding extent of all borders. The padding extent of each border represents a bound property. This is a convenience method that calls
	 * {@link #setPaddingExtent(Border, Extent)} for each border.
	 * @param newPaddingExtent The padding extent.
	 * @throws NullPointerException if the given padding extent is <code>null</code>.
	 * @see #PADDING_LINE_NEAR_EXTENT_PROPERTY
	 * @see #PADDING_LINE_FAR_EXTENT_PROPERTY
	 * @see #PADDING_PAGE_NEAR_EXTENT_PROPERTY
	 * @see #PADDING_PAGE_FAR_EXTENT_PROPERTY
	 */
	public void setPaddingExtent(final Extent newPaddingExtent) {
		for(final Border border : Border.values()) { //for each border
			setPaddingExtent(border, newPaddingExtent); //set this padding extent
		}
	}

	/** The style identifier, or <code>null</code> if there is no style ID. */
	private String styleID = null;

	/** @return The style identifier, or <code>null</code> if there is no style ID. */
	public String getStyleID() {
		return styleID;
	}

	/**
	 * Identifies the style for the component. This is a bound property.
	 * @param newStyleID The style identifier, or <code>null</code> if there is no style ID.
	 * @see #STYLE_ID_PROPERTY
	 */
	public void setStyleID(final String newStyleID) {
		if(!Objects.equals(styleID, newStyleID)) { //if the value is really changing
			final String oldStyleID = styleID; //get the current value
			styleID = newStyleID; //update the value
			firePropertyChange(STYLE_ID_PROPERTY, oldStyleID, newStyleID);
		}
	}

	/** Whether the component is visible. */
	private boolean visible = true;

	/**
	 * @return Whether the component is visible.
	 * @see #isDisplayed()
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Sets whether the component is visible. This is a bound property of type {@link Boolean}.
	 * @param newVisible <code>true</code> if the component should be visible, else <code>false</code>.
	 * @see #VISIBLE_PROPERTY
	 * @see #setDisplayed(boolean)
	 */
	public void setVisible(final boolean newVisible) {
		if(visible != newVisible) { //if the value is really changing
			final boolean oldVisible = visible; //get the current value
			visible = newVisible; //update the value
			firePropertyChange(VISIBLE_PROPERTY, Boolean.valueOf(oldVisible), Boolean.valueOf(newVisible));
		}
	}

	/** Whether the component is displayed or has no representation, taking up no space. */
	private boolean displayed = true;

	/**
	 * @return Whether the component is displayed or has no representation, taking up no space.
	 * @see #isVisible()
	 */
	public boolean isDisplayed() {
		return displayed;
	}

	/**
	 * Sets whether the component is displayed or has no representation, taking up no space. This is a bound property of type {@link Boolean}.
	 * @param newDisplayed <code>true</code> if the component should be displayed, else <code>false</code> if the component should take up no space.
	 * @see Displayable#DISPLAYED_PROPERTY
	 * @see #setVisible(boolean)
	 */
	public void setDisplayed(final boolean newDisplayed) {
		if(displayed != newDisplayed) { //if the value is really changing
			final boolean oldDisplayed = displayed; //get the current value
			displayed = newDisplayed; //update the value
			firePropertyChange(DISPLAYED_PROPERTY, Boolean.valueOf(oldDisplayed), Boolean.valueOf(newDisplayed));
		}
	}

	/** The text color of the component, or <code>null</code> if no text color is specified for this component. */
	private Color textColor = null;

	/** @return The text color of the component, or <code>null</code> if no text color is specified for this component. */
	public Color getTextColor() {
		return textColor;
	}

	/**
	 * Sets the text color of the component. This is a bound property.
	 * @param newTextColor The text color of the component, or <code>null</code> if the default text color should be used.
	 * @see #TEXT_COLOR_PROPERTY
	 */
	public void setTextColor(final Color newTextColor) {
		if(!Objects.equals(textColor, newTextColor)) { //if the value is really changing
			final Color oldTextColor = textColor; //get the old value
			textColor = newTextColor; //actually change the value
			firePropertyChange(TEXT_COLOR_PROPERTY, oldTextColor, newTextColor); //indicate that the value changed
		}
	}

	/** Whether tooltips are enabled for this component. */
	private boolean tooltipEnabled = true;

	/** @return Whether tooltips are enabled for this component. */
	public boolean isTooltipEnabled() {
		return tooltipEnabled;
	}

	/**
	 * Sets whether tooltips are enabled for this component. Tooltips contain information from the component model's "info" property. This is a bound property of
	 * type {@link Boolean}.
	 * @param newTooltipEnabled <code>true</code> if the component should display tooltips, else <code>false</code>.
	 * @see #TOOLTIP_ENABLED_PROPERTY
	 */
	public void setTooltipEnabled(final boolean newTooltipEnabled) {
		if(tooltipEnabled != newTooltipEnabled) { //if the value is really changing
			final boolean oldTooltipEnabled = tooltipEnabled; //get the current value
			tooltipEnabled = newTooltipEnabled; //update the value
			firePropertyChange(TOOLTIP_ENABLED_PROPERTY, Boolean.valueOf(oldTooltipEnabled), Boolean.valueOf(newTooltipEnabled));
		}
	}

	/** Default constructor. */
	public AbstractPresentationModel() {
		assert CORNER_ARC_SIZE_PROPERTIES.length == cornerArcSizes.length : "Number of available corners changed.";
	}

}