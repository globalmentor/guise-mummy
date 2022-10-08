/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.model.ui;

import java.net.URI;
import java.util.*;

import static java.util.Objects.*;

import com.globalmentor.java.Objects;

import io.guise.framework.component.layout.*;
import io.guise.framework.event.*;
import io.guise.framework.geometry.*;
import io.guise.framework.model.Displayable;
import io.guise.framework.style.Color;
import io.guise.framework.style.FontStyle;
import io.guise.framework.style.LineStyle;

import static com.globalmentor.java.Arrays.*;
import static io.guise.framework.theme.Theme.*;

/**
 * An abstract implementation of presentation-related information.
 * @author Garret Wilson
 */
public abstract class AbstractPresentationModel extends GuiseBoundPropertyObject implements PresentationModel {

	/** The background color of the component, or <code>null</code> if no background color is specified for this component. */
	private Color backgroundColor = null;

	@Override
	public Color getBackgroundColor() {
		return backgroundColor;
	}

	@Override
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

	@Override
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

	@Override
	public Color BorderLineFarColor() {
		return getBorderColor(Border.LINE_FAR);
	}

	@Override
	public Color BorderPageNearColor() {
		return getBorderColor(Border.PAGE_NEAR);
	}

	@Override
	public Color BorderPageFarColor() {
		return getBorderColor(Border.PAGE_FAR);
	}

	@Override
	public void setBorderColor(final Border border, final Color newBorderColor) {
		final int borderOrdinal = requireNonNull(border, "Border cannot be null").ordinal(); //get the ordinal of the border
		final Color oldBorderColor = borderColors[borderOrdinal]; //get the old value
		if(!Objects.equals(oldBorderColor, newBorderColor)) { //if the value is really changing
			borderColors[borderOrdinal] = newBorderColor; //actually change the value
			firePropertyChange(BORDER_COLOR_PROPERTIES[borderOrdinal], oldBorderColor, newBorderColor); //indicate that the value changed
		}
	}

	@Override
	public void setBorderLineNearColor(final Color newBorderColor) {
		setBorderColor(Border.LINE_NEAR, newBorderColor);
	}

	@Override
	public void setBorderLineFarColor(final Color newBorderColor) {
		setBorderColor(Border.LINE_FAR, newBorderColor);
	}

	@Override
	public void setBorderPageNearColor(final Color newBorderColor) {
		setBorderColor(Border.PAGE_NEAR, newBorderColor);
	}

	@Override
	public void setBorderPageFarColor(final Color newBorderColor) {
		setBorderColor(Border.PAGE_FAR, newBorderColor);
	}

	@Override
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

	@Override
	public Extent getBorderExtent(final Border border) {
		return borderExtents[border.ordinal()];
	}

	@Override
	public Extent BorderLineNearExtent() {
		return getBorderExtent(Border.LINE_NEAR);
	}

	@Override
	public Extent BorderLineFarExtent() {
		return getBorderExtent(Border.LINE_FAR);
	}

	@Override
	public Extent BorderPageNearExtent() {
		return getBorderExtent(Border.PAGE_NEAR);
	}

	@Override
	public Extent BorderPageFarExtent() {
		return getBorderExtent(Border.PAGE_FAR);
	}

	@Override
	public void setBorderExtent(final Border border, final Extent newBorderExtent) {
		final int borderOrdinal = requireNonNull(border, "Border cannot be null").ordinal(); //get the ordinal of the border
		final Extent oldBorderExtent = borderExtents[borderOrdinal]; //get the old value
		if(!oldBorderExtent.equals(requireNonNull(newBorderExtent, "Border extent cannot be null."))) { //if the value is really changing
			borderExtents[borderOrdinal] = newBorderExtent; //actually change the value
			firePropertyChange(BORDER_EXTENT_PROPERTIES[borderOrdinal], oldBorderExtent, newBorderExtent); //indicate that the value changed
		}
	}

	@Override
	public void setBorderLineNearExtent(final Extent newBorderExtent) {
		setBorderExtent(Border.LINE_NEAR, newBorderExtent);
	}

	@Override
	public void setBorderLineFarExtent(final Extent newBorderExtent) {
		setBorderExtent(Border.LINE_FAR, newBorderExtent);
	}

	@Override
	public void setBorderPageNearExtent(final Extent newBorderExtent) {
		setBorderExtent(Border.PAGE_NEAR, newBorderExtent);
	}

	@Override
	public void setBorderPageFarExtent(final Extent newBorderExtent) {
		setBorderExtent(Border.PAGE_FAR, newBorderExtent);
	}

	@Override
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

	@Override
	public LineStyle getBorderStyle(final Border border) {
		return borderStyles[border.ordinal()];
	}

	@Override
	public LineStyle BorderLineNearStyle() {
		return getBorderStyle(Border.LINE_NEAR);
	}

	@Override
	public LineStyle BorderLineFarStyle() {
		return getBorderStyle(Border.LINE_FAR);
	}

	@Override
	public LineStyle BorderPageNearStyle() {
		return getBorderStyle(Border.PAGE_NEAR);
	}

	@Override
	public LineStyle BorderPageFarStyle() {
		return getBorderStyle(Border.PAGE_FAR);
	}

	@Override
	public void setBorderStyle(final Border border, final LineStyle newBorderStyle) {
		final int borderOrdinal = requireNonNull(border, "Border cannot be null").ordinal(); //get the ordinal of the border
		final LineStyle oldBorderStyle = borderStyles[borderOrdinal]; //get the old value
		if(oldBorderStyle != requireNonNull(newBorderStyle, "Border style cannot be null.")) { //if the value is really changing
			borderStyles[borderOrdinal] = newBorderStyle; //actually change the value
			firePropertyChange(BORDER_STYLE_PROPERTIES[borderOrdinal], oldBorderStyle, newBorderStyle); //indicate that the value changed
		}
	}

	@Override
	public void setBorderLineNearStyle(final LineStyle newBorderStyle) {
		setBorderStyle(Border.LINE_NEAR, newBorderStyle);
	}

	@Override
	public void setBorderLineFarStyle(final LineStyle newBorderStyle) {
		setBorderStyle(Border.LINE_FAR, newBorderStyle);
	}

	@Override
	public void setBorderPageNearStyle(final LineStyle newBorderStyle) {
		setBorderStyle(Border.PAGE_NEAR, newBorderStyle);
	}

	@Override
	public void setBorderPageFarStyle(final LineStyle newBorderStyle) {
		setBorderStyle(Border.PAGE_FAR, newBorderStyle);
	}

	@Override
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

	@Override
	public Dimensions getCornerArcSize(final Corner corner) {
		return cornerArcSizes[corner.ordinal()];
	}

	@Override
	public Dimensions getCornerLineNearPageNearArcSize() {
		return getCornerArcSize(Corner.LINE_NEAR_PAGE_NEAR);
	}

	@Override
	public Dimensions getCornerLineFarPageNearArcSize() {
		return getCornerArcSize(Corner.LINE_FAR_PAGE_NEAR);
	}

	@Override
	public Dimensions getCornerLineNearPageFarArcSize() {
		return getCornerArcSize(Corner.LINE_NEAR_PAGE_FAR);
	}

	@Override
	public Dimensions getCornerLineFarPageFarArcSize() {
		return getCornerArcSize(Corner.LINE_FAR_PAGE_FAR);
	}

	@Override
	public void setCornerArcSize(final Corner corner, final Dimensions newCornerArcSize) {
		final int cornerOrdinal = requireNonNull(corner, "Corner cannot be null").ordinal(); //get the ordinal of the corner
		final Dimensions oldCornerArcSize = cornerArcSizes[cornerOrdinal]; //get the old value
		if(!Objects.equals(oldCornerArcSize, requireNonNull(newCornerArcSize, "Corner arc size cannot be null"))) { //if the value is really changing TODO decide if null dimensions should be accepted
			cornerArcSizes[cornerOrdinal] = newCornerArcSize; //actually change the value
			firePropertyChange(CORNER_ARC_SIZE_PROPERTIES[cornerOrdinal], oldCornerArcSize, newCornerArcSize); //indicate that the value changed
		}
	}

	@Override
	public void setCornerLineNearPageNearArcSize(final Dimensions newCornerArcSize) {
		setCornerArcSize(Corner.LINE_NEAR_PAGE_NEAR, newCornerArcSize);
	}

	@Override
	public void setCornerLineFarPageNearArcSize(final Dimensions newCornerArcSize) {
		setCornerArcSize(Corner.LINE_FAR_PAGE_NEAR, newCornerArcSize);
	}

	@Override
	public void setCornerLineNearPageFarArcSize(final Dimensions newCornerArcSize) {
		setCornerArcSize(Corner.LINE_NEAR_PAGE_FAR, newCornerArcSize);
	}

	@Override
	public void setCornerLineFarPageFarArcSize(final Dimensions newCornerArcSize) {
		setCornerArcSize(Corner.LINE_FAR_PAGE_FAR, newCornerArcSize);
	}

	@Override
	public void setCornerArcSize(final Dimensions newCornerArcSize) {
		for(final Corner corner : Corner.values()) { //for each corner
			setCornerArcSize(corner, newCornerArcSize); //set this corner arc size
		}
	}

	/** The cursor URI, which may be a resource URI. */
	private URI cursor = CURSOR_DEFAULT;

	@Override
	public URI getCursor() {
		return cursor;
	}

	@Override
	public void setCursor(final URI newCursor) {
		if(!cursor.equals(requireNonNull(newCursor, "Cursor URI cannot be null."))) { //if the value is really changing
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

	@Override
	public Extent getExtent(final Flow flow) {
		return extents[flow.ordinal()];
	}

	@Override
	public Extent getLineExtent() {
		return getExtent(Flow.LINE);
	}

	@Override
	public Extent getPageExtent() {
		return getExtent(Flow.PAGE);
	}

	@Override
	public void setExtent(final Flow flow, final Extent newExtent) {
		final int flowOrdinal = requireNonNull(flow, "Flow cannot be null").ordinal(); //get the ordinal of the flow
		final Extent oldExtent = extents[flowOrdinal]; //get the old value
		if(!Objects.equals(oldExtent, newExtent)) { //if the value is really changing
			extents[flowOrdinal] = newExtent; //actually change the value
			firePropertyChange(EXTENT_PROPERTIES[flowOrdinal], oldExtent, newExtent); //indicate that the value changed
		}
	}

	@Override
	public void setLineExtent(final Extent newExtent) {
		setExtent(Flow.LINE, newExtent);
	}

	@Override
	public void setPageExtent(final Extent newExtent) {
		setExtent(Flow.PAGE, newExtent);
	}

	/** The prioritized list of font family names, or <code>null</code> if no font family names have been specified. */
	private List<String> fontFamilies = null;

	@Override
	public List<String> getFontFamilies() {
		return fontFamilies;
	}

	@Override
	public void setFontFamilies(final List<String> newFontFamilies) {
		if(!Objects.equals(fontFamilies, newFontFamilies)) { //if the value is really changing
			final List<String> oldFontFamilies = fontFamilies; //get the old value
			fontFamilies = newFontFamilies; //actually change the value
			firePropertyChange(FONT_FAMILIES_PROPERTY, oldFontFamilies, newFontFamilies); //indicate that the value changed
		}
	}

	/** The size of the font from baseline to baseline, or <code>null</code> if no font size has been specified. */
	private Extent fontSize = null;

	@Override
	public Extent getFontSize() {
		return fontSize;
	}

	@Override
	public void setFontSize(final Extent newFontSize) {
		if(!Objects.equals(fontSize, newFontSize)) { //if the value is really changing
			final Extent oldFontSize = fontSize; //get the old value
			fontSize = newFontSize; //actually change the value
			firePropertyChange(FONT_SIZE_PROPERTY, oldFontSize, newFontSize); //indicate that the value changed
		}
	}

	/** The style of the font. */
	private FontStyle fontStyle = FontStyle.NORMAL;

	@Override
	public FontStyle getFontStyle() {
		return fontStyle;
	}

	@Override
	public void setFontStyle(final FontStyle newFontStyle) {
		if(fontStyle != requireNonNull(newFontStyle, "Font style cannot be null.")) { //if the value is really changing
			final FontStyle oldFontStyle = fontStyle; //get the current value
			fontStyle = newFontStyle; //update the value
			firePropertyChange(FONT_STYLE_PROPERTY, oldFontStyle, newFontStyle);
		}
	}

	/** The weight of the font relative to a normal value of 0.5. */
	private double fontWeight = 0.5;

	@Override
	public double getFontWeight() {
		return fontWeight;
	}

	@Override
	public void setFontWeight(final double newFontWeight) {
		if(fontWeight != newFontWeight) { //if the value is really changing
			final double oldFontWeight = fontWeight; //get the current value
			fontWeight = newFontWeight; //update the value
			firePropertyChange(FONT_WEIGHT_PROPERTY, Double.valueOf(oldFontWeight), Double.valueOf(newFontWeight));
		}
	}

	/** The prioritized list of label font family names, or <code>null</code> if no label font family names have been specified. */
	private List<String> labelFontFamilies = null;

	@Override
	public List<String> getLabelFontFamilies() {
		return labelFontFamilies;
	}

	@Override
	public void setLabelFontFamilies(final List<String> newLabelFontFamilies) {
		if(!Objects.equals(labelFontFamilies, newLabelFontFamilies)) { //if the value is really changing
			final List<String> oldLabelFontFamilies = labelFontFamilies; //get the old value
			labelFontFamilies = newLabelFontFamilies; //actually change the value
			firePropertyChange(LABEL_FONT_FAMILIES_PROPERTY, oldLabelFontFamilies, newLabelFontFamilies); //indicate that the value changed
		}
	}

	/** The size of the label font from baseline to baseline, or <code>null</code> if no label font size has been specified. */
	private Extent labelFontSize = null;

	@Override
	public Extent getLabelFontSize() {
		return labelFontSize;
	}

	@Override
	public void setLabelFontSize(final Extent newLabelFontSize) {
		if(!Objects.equals(labelFontSize, newLabelFontSize)) { //if the value is really changing
			final Extent oldLabelFontSize = labelFontSize; //get the old value
			labelFontSize = newLabelFontSize; //actually change the value
			firePropertyChange(LABEL_FONT_SIZE_PROPERTY, oldLabelFontSize, newLabelFontSize); //indicate that the value changed
		}
	}

	/** The style of the label font. */
	private FontStyle labelFontStyle = FontStyle.NORMAL;

	@Override
	public FontStyle getLabelFontStyle() {
		return labelFontStyle;
	}

	@Override
	public void setLabelFontStyle(final FontStyle newLabelFontStyle) {
		if(labelFontStyle != requireNonNull(newLabelFontStyle, "Label font style cannot be null.")) { //if the value is really changing
			final FontStyle oldLabelFontStyle = labelFontStyle; //get the current value
			labelFontStyle = newLabelFontStyle; //update the value
			firePropertyChange(LABEL_FONT_STYLE_PROPERTY, oldLabelFontStyle, newLabelFontStyle);
		}
	}

	/** The weight of the label font relative to a normal value of 0.5. */
	private double labelFontWeight = 0.5;

	@Override
	public double getLabelFontWeight() {
		return labelFontWeight;
	}

	@Override
	public void setLabelFontWeight(final double newLabelFontWeight) {
		if(labelFontWeight != newLabelFontWeight) { //if the value is really changing
			final double oldLabelFontWeight = labelFontWeight; //get the current value
			labelFontWeight = newLabelFontWeight; //update the value
			firePropertyChange(LABEL_FONT_WEIGHT_PROPERTY, Double.valueOf(oldLabelFontWeight), Double.valueOf(newLabelFontWeight));
		}
	}

	/** The text color of the label, or <code>null</code> if no text color is specified for the label. */
	private Color labelTextColor = null;

	@Override
	public Color getLabelTextColor() {
		return labelTextColor;
	}

	@Override
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

	@Override
	public Extent getMarginExtent(final Border border) {
		return marginExtents[border.ordinal()];
	}

	@Override
	public Extent getMarginLineNearExtent() {
		return getMarginExtent(Border.LINE_NEAR);
	}

	@Override
	public Extent getMarginLineFarExtent() {
		return getMarginExtent(Border.LINE_FAR);
	}

	@Override
	public Extent getMarginPageNearExtent() {
		return getMarginExtent(Border.PAGE_NEAR);
	}

	@Override
	public Extent getMarginPageFarExtent() {
		return getMarginExtent(Border.PAGE_FAR);
	}

	@Override
	public void setMarginExtent(final Border border, final Extent newMarginExtent) {
		final int borderOrdinal = requireNonNull(border, "Border cannot be null").ordinal(); //get the ordinal of the border
		final Extent oldMarginExtent = marginExtents[borderOrdinal]; //get the old value
		if(!oldMarginExtent.equals(requireNonNull(newMarginExtent, "margin extent cannot be null."))) { //if the value is really changing
			marginExtents[borderOrdinal] = newMarginExtent; //actually change the value
			firePropertyChange(MARGIN_EXTENT_PROPERTIES[borderOrdinal], oldMarginExtent, newMarginExtent); //indicate that the value changed
		}
	}

	@Override
	public void setMarginLineNearExtent(final Extent newMarginExtent) {
		setMarginExtent(Border.LINE_NEAR, newMarginExtent);
	}

	@Override
	public void setMarginLineFarExtent(final Extent newMarginExtent) {
		setMarginExtent(Border.LINE_FAR, newMarginExtent);
	}

	@Override
	public void setMarginPageNearExtent(final Extent newMarginExtent) {
		setMarginExtent(Border.PAGE_NEAR, newMarginExtent);
	}

	@Override
	public void setMarginPageFarExtent(final Extent newMarginExtent) {
		setMarginExtent(Border.PAGE_FAR, newMarginExtent);
	}

	@Override
	public void setMarginExtent(final Extent newMarginExtent) {
		for(final Border border : Border.values()) { //for each border
			setMarginExtent(border, newMarginExtent); //set this margin extent
		}
	}

	/** The opacity of the entire component in the range (0.0-1.0), with a default of 1.0. */
	private double opacity = 1.0f;

	@Override
	public double getOpacity() {
		return opacity;
	}

	@Override
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

	@Override
	public Extent getPaddingExtent(final Border border) {
		return paddingExtents[border.ordinal()];
	}

	@Override
	public Extent getPaddingLineNearExtent() {
		return getPaddingExtent(Border.LINE_NEAR);
	}

	@Override
	public Extent getPaddingLineFarExtent() {
		return getPaddingExtent(Border.LINE_FAR);
	}

	@Override
	public Extent getPaddingPageNearExtent() {
		return getPaddingExtent(Border.PAGE_NEAR);
	}

	@Override
	public Extent getPaddingPageFarExtent() {
		return getPaddingExtent(Border.PAGE_FAR);
	}

	@Override
	public void setPaddingExtent(final Border border, final Extent newPaddingExtent) {
		final int borderOrdinal = requireNonNull(border, "Border cannot be null").ordinal(); //get the ordinal of the border
		final Extent oldPaddingExtent = paddingExtents[borderOrdinal]; //get the old value
		if(!oldPaddingExtent.equals(requireNonNull(newPaddingExtent, "Padding extent cannot be null."))) { //if the value is really changing
			paddingExtents[borderOrdinal] = newPaddingExtent; //actually change the value
			firePropertyChange(PADDING_EXTENT_PROPERTIES[borderOrdinal], oldPaddingExtent, newPaddingExtent); //indicate that the value changed
		}
	}

	@Override
	public void setPaddingLineNearExtent(final Extent newPaddingExtent) {
		setPaddingExtent(Border.LINE_NEAR, newPaddingExtent);
	}

	@Override
	public void setPaddingLineFarExtent(final Extent newPaddingExtent) {
		setPaddingExtent(Border.LINE_FAR, newPaddingExtent);
	}

	@Override
	public void setPaddingPageNearExtent(final Extent newPaddingExtent) {
		setPaddingExtent(Border.PAGE_NEAR, newPaddingExtent);
	}

	@Override
	public void setPaddingPageFarExtent(final Extent newPaddingExtent) {
		setPaddingExtent(Border.PAGE_FAR, newPaddingExtent);
	}

	@Override
	public void setPaddingExtent(final Extent newPaddingExtent) {
		for(final Border border : Border.values()) { //for each border
			setPaddingExtent(border, newPaddingExtent); //set this padding extent
		}
	}

	/** The style identifier, or <code>null</code> if there is no style ID. */
	private String styleID = null;

	@Override
	public String getStyleID() {
		return styleID;
	}

	@Override
	public void setStyleID(final String newStyleID) {
		if(!Objects.equals(styleID, newStyleID)) { //if the value is really changing
			final String oldStyleID = styleID; //get the current value
			styleID = newStyleID; //update the value
			firePropertyChange(STYLE_ID_PROPERTY, oldStyleID, newStyleID);
		}
	}

	/** Whether the component is visible. */
	private boolean visible = true;

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public void setVisible(final boolean newVisible) {
		if(visible != newVisible) { //if the value is really changing
			final boolean oldVisible = visible; //get the current value
			visible = newVisible; //update the value
			firePropertyChange(VISIBLE_PROPERTY, Boolean.valueOf(oldVisible), Boolean.valueOf(newVisible));
		}
	}

	/** Whether the component is displayed or has no representation, taking up no space. */
	private boolean displayed = true;

	@Override
	public boolean isDisplayed() {
		return displayed;
	}

	@Override
	public void setDisplayed(final boolean newDisplayed) {
		if(displayed != newDisplayed) { //if the value is really changing
			final boolean oldDisplayed = displayed; //get the current value
			displayed = newDisplayed; //update the value
			firePropertyChange(DISPLAYED_PROPERTY, Boolean.valueOf(oldDisplayed), Boolean.valueOf(newDisplayed));
		}
	}

	/** The text color of the component, or <code>null</code> if no text color is specified for this component. */
	private Color textColor = null;

	@Override
	public Color getTextColor() {
		return textColor;
	}

	@Override
	public void setTextColor(final Color newTextColor) {
		if(!Objects.equals(textColor, newTextColor)) { //if the value is really changing
			final Color oldTextColor = textColor; //get the old value
			textColor = newTextColor; //actually change the value
			firePropertyChange(TEXT_COLOR_PROPERTY, oldTextColor, newTextColor); //indicate that the value changed
		}
	}

	/** Whether tooltips are enabled for this component. */
	private boolean tooltipEnabled = true;

	@Override
	public boolean isTooltipEnabled() {
		return tooltipEnabled;
	}

	@Override
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
