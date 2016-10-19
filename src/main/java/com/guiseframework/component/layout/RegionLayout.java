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

import static com.globalmentor.java.Arrays.fill;
import static com.globalmentor.java.Classes.*;
import static com.globalmentor.java.Objects.*;

import com.globalmentor.java.Objects;
import com.guiseframework.component.Component;
import com.guiseframework.component.LayoutComponent;
import com.guiseframework.geometry.Extent;

/**
 * A layout that defines locations of components in internationalized relative terms. This layout uses default constraints of {@link Region#CENTER}.
 * <p>
 * The region will span two of the components, if present, across the perpendicular flow. Which flow is spanned across the other is determined by setting
 * {@link #setSpanFlow(Flow)}. For example, in a right-to-left top-to-bottom orientation, a span flow of {@link Flow#LINE} (the default) will result in the top
 * and bottom components spanning across the space used by the left, center, and right components.
 * </p>
 * @author Garret Wilson
 * @see Region
 */
public class RegionLayout extends AbstractLayout<RegionConstraints> {

	/** The bound property of the fixed setting. */
	public static final String FIXED_PROPERTY = getPropertyName(RegionLayout.class, "fixed");
	/** The bound property of the line alignment. */
	public static final String LINE_ALIGNMENT_PROPERTY = getPropertyName(RegionLayout.class, "lineAlignment");
	/** The bound property of the page alignment. */
	public static final String PAGE_ALIGNMENT_PROPERTY = getPropertyName(RegionLayout.class, "pageAlignment");
	/** The line extent (width in left-to-right top-to-bottom orientation) bound property. */
	public static final String LINE_EXTENT_PROPERTY = getPropertyName(RegionLayout.class, "lineExtent");
	/** The page extent (height in left-to-right top-to-bottom orientation) bound property. */
	public static final String PAGE_EXTENT_PROPERTY = getPropertyName(RegionLayout.class, "pageExtent");
	/** The bound property of the line near padding extent. */
	public static final String PADDING_LINE_NEAR_EXTENT_PROPERTY = getPropertyName(RegionLayout.class, "paddingLineNearExtent");
	/** The bound property of the line far padding extent. */
	public static final String PADDING_LINE_FAR_EXTENT_PROPERTY = getPropertyName(RegionLayout.class, "paddingLineFarExtent");
	/** The bound property of the page near padding extent. */
	public static final String PADDING_PAGE_NEAR_EXTENT_PROPERTY = getPropertyName(RegionLayout.class, "paddingPageNearExtent");
	/** The bound property of the page far padding extent. */
	public static final String PADDING_PAGE_FAR_EXTENT_PROPERTY = getPropertyName(RegionLayout.class, "paddingPageFarExtent");
	/** The bound property of the span flow. */
	public static final String SPAN_FLOW_PROPERTY = getPropertyName(RegionLayout.class, "spanFlow");

	/** Whether the sizes of the regions are fixed or will dynamically change to support content. */
	private boolean fixed = false;

	/** @return whether the sizes of the regions are fixed or will dynamically change to support the given content. */
	public boolean isFixed() {
		return fixed;
	}

	/**
	 * Sets whether the sizes of the regions are fixed or will dynamically change to support the given content. This is a bound property of type
	 * <code>Boolean</code>.
	 * @param newFixed <code>true</code> if the sizes of the regions are fixed, or <code>false</code> if the regions will dynamically change to support the given
	 *          content.
	 * @see #FIXED_PROPERTY
	 */
	public void setFixed(final boolean newFixed) {
		if(fixed != newFixed) { //if the value is really changing
			final boolean oldFixed = fixed; //get the old value
			fixed = newFixed; //actually change the value
			firePropertyChange(FIXED_PROPERTY, Boolean.valueOf(oldFixed), Boolean.valueOf(newFixed)); //indicate that the value changed
		}
	}

	/** The array of default region alignments. */
	private double[] alignments = fill(new double[Flow.values().length], 0);

	/** The properties corresponding to the region alignments. */
	private static final String[] ALIGNMENT_PROPERTIES;

	static {
		ALIGNMENT_PROPERTIES = new String[Flow.values().length]; //create the array of properties and fill it with corresponding properties
		ALIGNMENT_PROPERTIES[Flow.LINE.ordinal()] = LINE_ALIGNMENT_PROPERTY;
		ALIGNMENT_PROPERTIES[Flow.PAGE.ordinal()] = PAGE_ALIGNMENT_PROPERTY;
	}

	/**
	 * Returns the default alignment of the indicated flow relative to the beginning of the alignment axis.
	 * @param flow The flow for which an alignment should be returned.
	 * @return The alignment of the given flow.
	 */
	public double getAlignment(final Flow flow) {
		return alignments[flow.ordinal()];
	}

	/** @return The default alignment of the line flow relative to the beginning of the alignment axis. */
	public double getLineAlignment() {
		return getAlignment(Flow.LINE);
	}

	/** @return The default alignment of the page flow relative to the beginning of the alignment axis. */
	public double getPageAlignment() {
		return getAlignment(Flow.PAGE);
	}

	/**
	 * Sets the alignment of a given flow. This method also acts as a convenience method by unconditionally updating the flow alignment of the region constraints
	 * of any child components of this layout's owner. The alignment of each flow represents a bound property of type {@link Double}.
	 * @param flow The flow for which the alignment should be set.
	 * @param newAlignment The alignment of the region relative to the beginning of the given flow.
	 * @throws NullPointerException if the given flow is <code>null</code>.
	 * @see #LINE_ALIGNMENT_PROPERTY
	 * @see #PAGE_ALIGNMENT_PROPERTY
	 * @see RegionConstraints#setAlignment(Flow, double)
	 */
	public void setAlignment(final Flow flow, final double newAlignment) {
		final int flowOrdinal = checkInstance(flow, "Flow cannot be null").ordinal(); //get the ordinal of the flow
		final double oldAlignment = alignments[flowOrdinal]; //get the old value
		if(oldAlignment != newAlignment) { //if the value is really changing
			alignments[flowOrdinal] = newAlignment; //actually change the value
			firePropertyChange(ALIGNMENT_PROPERTIES[flowOrdinal], oldAlignment, newAlignment); //indicate that the value changed
		}
		final LayoutComponent owner = getOwner(); //get the owner of this layout, if any
		if(owner != null) { //if this layout has an owner
			for(final Component component : getOwner().getChildComponents()) { //for all child components of the owner
				getConstraints(component).setAlignment(flow, newAlignment); //update this child component's constraints with the new alignment value
			}
		}
	}

	/**
	 * Sets the default alignment of the line flow. For example, in a left-to-right top-to-bottom orientation, line alignments of 0.0, 0.5, and 1.0 would be
	 * equivalent to what are commonly known as <dfn>left</dfn>, <dfn>center</dfn>, and <dfn>right</dfn> alignments, respectively. This method also acts as a
	 * convenience method by unconditionally updating the line alignment of the region constraints of any child components of this layout's owner. This is a bound
	 * property of type {@link Double}.
	 * @param newAlignment The line alignment of the region relative to the beginning of the alignment axis.
	 * @see #LINE_ALIGNMENT_PROPERTY
	 */
	public void setLineAlignment(final double newAlignment) {
		setAlignment(Flow.LINE, newAlignment);
	}

	/**
	 * Sets the alignment of the page flow. For example, in a left-to-right top-to-bottom orientation, page alignments of 0.0, 0.5, and 1.0 would be equivalent to
	 * what are commonly known as <dfn>top</dfn>, <dfn>middle</dfn>, and <dfn>bottom</dfn> alignments, respectively. This method also acts as a convenience method
	 * by unconditionally updating the page alignment of the region constraints of any child components of this layout's owner. This is a bound property of type
	 * {@link Double}.
	 * @param newAlignment The page alignment of the region relative to the beginning of the alignment axis.
	 * @see #PAGE_ALIGNMENT_PROPERTY
	 */
	public void setPageAlignment(final double newAlignment) {
		setAlignment(Flow.PAGE, newAlignment);
	}

	/** The array of default region extents. */
	private Extent[] extents = fill(new Extent[Flow.values().length], null);

	/** The properties corresponding to the default region extents. */
	private static final String[] EXTENT_PROPERTIES;

	static {
		EXTENT_PROPERTIES = new String[Flow.values().length]; //create the array of properties and fill it with corresponding properties
		EXTENT_PROPERTIES[Flow.LINE.ordinal()] = LINE_EXTENT_PROPERTY;
		EXTENT_PROPERTIES[Flow.PAGE.ordinal()] = PAGE_EXTENT_PROPERTY;
	}

	/**
	 * Returns the default extent of the indicated flow.
	 * @param flow The flow for which an extent should be returned.
	 * @return The extent of the given flow.
	 */
	public Extent getExtent(final Flow flow) {
		return extents[flow.ordinal()];
	}

	/**
	 * Returns the default extent of the line flow. In left-to-right top-to-bottom orientation, this is commonly known as the <dfn>width</dfn>.
	 * @return The extent of the flow, or <code>null</code> if no preferred extent has been specified
	 */
	public Extent getLineExtent() {
		return getExtent(Flow.LINE);
	}

	/**
	 * Returns the default extent of the page flow. In left-to-right top-to-bottom orientation, this is commonly known as the <dfn>height</dfn>.
	 * @return The extent of the flow, or <code>null</code> if no preferred extent has been specified
	 */
	public Extent getPageExtent() {
		return getExtent(Flow.PAGE);
	}

	/**
	 * Sets the default extent of a given flow. This method also acts as a convenience method by unconditionally updating the extent of any child components of
	 * this layout's owner. The extent of each flow represents a bound property.
	 * @param flow The flow for which the extent should be set.
	 * @param newExtent The new requested extent of the region, or <code>null</code> there is no extent preference.
	 * @throws NullPointerException if the given flow is <code>null</code>.
	 * @see #LINE_EXTENT_PROPERTY
	 * @see #PAGE_EXTENT_PROPERTY
	 * @see RegionConstraints#setExtent(Flow, Extent)
	 */
	public void setExtent(final Flow flow, final Extent newExtent) {
		final int flowOrdinal = checkInstance(flow, "Flow cannot be null").ordinal(); //get the ordinal of the flow
		final Extent oldExtent = extents[flowOrdinal]; //get the old value
		if(!Objects.equals(oldExtent, newExtent)) { //if the value is really changing
			extents[flowOrdinal] = newExtent; //actually change the value
			firePropertyChange(EXTENT_PROPERTIES[flowOrdinal], oldExtent, newExtent); //indicate that the value changed
		}
		final LayoutComponent owner = getOwner(); //get the owner of this layout, if any
		if(owner != null) { //if this layout has an owner
			for(final Component component : getOwner().getChildComponents()) { //for all child components of the owner
				getConstraints(component).setExtent(flow, newExtent); //update this child component's constraints with the new extent value
			}
		}
	}

	/**
	 * Sets the default extent of the line flow. In left-to-right top-to-bottom orientation, this is commonly known as the <dfn>width</dfn>. This method also acts
	 * as a convenience method by unconditionally updating the extent of any child components of this layout's owner. This is a bound property.
	 * @param newExtent The new requested extent of the region, or <code>null</code> there is no extent preference.
	 * @see #LINE_EXTENT_PROPERTY
	 */
	public void setLineExtent(final Extent newExtent) {
		setExtent(Flow.LINE, newExtent);
	}

	/**
	 * Sets the default extent of the page flow. In left-to-right top-to-bottom orientation, this is commonly known as the <dfn>height</dfn>. This method also
	 * acts as a convenience method by unconditionally updating the extent of any child components of this layout's owner. This is a bound property.
	 * @param newExtent The new requested extent of the region, or <code>null</code> there is no extent preference.
	 * @see #PAGE_EXTENT_PROPERTY
	 */
	public void setPageExtent(final Extent newExtent) {
		setExtent(Flow.PAGE, newExtent);
	}

	/** The array of default padding extents. */
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
	 * Returns the default padding extent of the indicated border.
	 * @param border The border for which a padding extent should be returned.
	 * @return The padding extent of the given border.
	 */
	public Extent getPaddingExtent(final Border border) {
		return paddingExtents[border.ordinal()];
	}

	/**
	 * Returns the default padding extent of the line near page near border.
	 * @return The padding extent of the given border.
	 */
	public Extent getPaddingLineNearExtent() {
		return getPaddingExtent(Border.LINE_NEAR);
	}

	/**
	 * Returns the default padding extent of the line far page near border.
	 * @return The padding extent of the given border.
	 */
	public Extent getPaddingLineFarExtent() {
		return getPaddingExtent(Border.LINE_FAR);
	}

	/**
	 * Returns the default padding extent of the line near page far border.
	 * @return The padding extent of the given border.
	 */
	public Extent getPaddingPageNearExtent() {
		return getPaddingExtent(Border.PAGE_NEAR);
	}

	/**
	 * Returns the default padding extent of the line far page far border.
	 * @return The padding extent of the given border.
	 */
	public Extent getPaddingPageFarExtent() {
		return getPaddingExtent(Border.PAGE_FAR);
	}

	/**
	 * Sets the default padding extent of a given border. This method also acts as a convenience method by unconditionally updating the padding extent of the
	 * region constraints of any child components of this layout's owner. The padding extent of each border represents a bound property.
	 * @param border The border for which the padding extent should be set.
	 * @param newPaddingExtent The padding extent.
	 * @throws NullPointerException if the given border and/or padding extent is <code>null</code>.
	 * @see #PADDING_LINE_NEAR_EXTENT_PROPERTY
	 * @see #PADDING_LINE_FAR_EXTENT_PROPERTY
	 * @see #PADDING_PAGE_NEAR_EXTENT_PROPERTY
	 * @see #PADDING_PAGE_FAR_EXTENT_PROPERTY
	 * @see RegionConstraints#setPaddingExtent(Border, Extent)
	 */
	public void setPaddingExtent(final Border border, final Extent newPaddingExtent) {
		final int borderOrdinal = checkInstance(border, "Border cannot be null").ordinal(); //get the ordinal of the border
		final Extent oldPaddingExtent = paddingExtents[borderOrdinal]; //get the old value
		if(!oldPaddingExtent.equals(checkInstance(newPaddingExtent, "Padding extent cannot be null."))) { //if the value is really changing
			paddingExtents[borderOrdinal] = newPaddingExtent; //actually change the value
			firePropertyChange(PADDING_EXTENT_PROPERTIES[borderOrdinal], oldPaddingExtent, newPaddingExtent); //indicate that the value changed
		}
		final LayoutComponent owner = getOwner(); //get the owner of this layout, if any
		if(owner != null) { //if this layout has an owner
			for(final Component component : getOwner().getChildComponents()) { //for all child components of the owner
				getConstraints(component).setPaddingExtent(border, newPaddingExtent); //update this child component's constraints with the new padding extent value
			}
		}
	}

	/**
	 * Sets the default padding extent of the line near border. This method also acts as a convenience method by unconditionally updating the padding extent of
	 * the region constraints of any child components of this layout's owner. This is a bound property.
	 * @param newPaddingExtent The padding extent.
	 * @throws NullPointerException if the given padding extent is <code>null</code>.
	 * @see #PADDING_LINE_NEAR_EXTENT_PROPERTY
	 */
	public void setPaddingLineNearExtent(final Extent newPaddingExtent) {
		setPaddingExtent(Border.LINE_NEAR, newPaddingExtent);
	}

	/**
	 * Sets the default padding extent of the line far border. This method also acts as a convenience method by unconditionally updating the padding extent of the
	 * region constraints of any child components of this layout's owner. This is a bound property.
	 * @param newPaddingExtent The padding extent, or <code>null</code> if the default padding extent should be used.
	 * @throws NullPointerException if the given padding extent is <code>null</code>.
	 * @see #PADDING_LINE_FAR_EXTENT_PROPERTY
	 */
	public void setPaddingLineFarExtent(final Extent newPaddingExtent) {
		setPaddingExtent(Border.LINE_FAR, newPaddingExtent);
	}

	/**
	 * Sets the default padding extent of the page near border. This method also acts as a convenience method by unconditionally updating the padding extent of
	 * the region constraints of any child components of this layout's owner. This is a bound property.
	 * @param newPaddingExtent The padding extent, or <code>null</code> if the default padding extent should be used.
	 * @throws NullPointerException if the given padding extent is <code>null</code>.
	 * @see #PADDING_PAGE_NEAR_EXTENT_PROPERTY
	 */
	public void setPaddingPageNearExtent(final Extent newPaddingExtent) {
		setPaddingExtent(Border.PAGE_NEAR, newPaddingExtent);
	}

	/**
	 * Sets the default padding extent of the page far border. This method also acts as a convenience method by unconditionally updating the padding extent of the
	 * region constraints of any child components of this layout's owner. This is a bound property.
	 * @param newPaddingExtent The padding extent, or <code>null</code> if the default padding extent should be used.
	 * @throws NullPointerException if the given padding extent is <code>null</code>.
	 * @see #PADDING_PAGE_FAR_EXTENT_PROPERTY
	 */
	public void setPaddingPageFarExtent(final Extent newPaddingExtent) {
		setPaddingExtent(Border.PAGE_FAR, newPaddingExtent);
	}

	/**
	 * Sets the default padding extent of all borders. This method also acts as a convenience method by unconditionally updating the padding extent of the region
	 * constraints of any child components of this layout's owner. The padding extent of each border represents a bound property. This is a convenience method
	 * that calls {@link #setPaddingExtent(Border, Extent)} for each border.
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

	/** The logical axis which will span components across the other logical axis. */
	private Flow spanFlow;

	/** @return The logical axis which will span components across the other logical axis. */
	public Flow getSpanFlow() {
		return spanFlow;
	}

	/**
	 * Sets the logical axis which will span components across the other logical axis. This is a bound property.
	 * @param newSpanFlow The logical axis which will span components across the other logical axis.
	 * @throws NullPointerException if the given span flow is <code>null</code>.
	 * @see #SPAN_FLOW_PROPERTY
	 */
	public void setSpanFlow(final Flow newSpanFlow) {
		if(spanFlow != checkInstance(newSpanFlow, "Span flow cannot be null.")) { //if the value is really changing
			final Flow oldSpanFlow = spanFlow; //get the old value
			spanFlow = newSpanFlow; //actually change the value
			firePropertyChange(SPAN_FLOW_PROPERTY, oldSpanFlow, newSpanFlow); //indicate that the value changed
		}
	}

	/** @return The class representing the type of constraints appropriate for this layout. */
	public Class<? extends RegionConstraints> getConstraintsClass() {
		return RegionConstraints.class;
	}

	/** Default constructor with {@link Flow#LINE} span flow. */
	public RegionLayout() {
		this(Flow.LINE); //construct the class with line span flow
	}

	/**
	 * Span flow constructor.
	 * @param spanFlow The logical axis which will span components across the other logical axis.
	 * @throws NullPointerException if the given span flow is <code>null</code>.
	 */
	public RegionLayout(final Flow spanFlow) {
		super(); //construct the parent class
		this.spanFlow = checkInstance(spanFlow, "Span flow cannot be null.");
	}

	/**
	 * Creates default constraints for the container. This implementation returns {@link Region#CENTER}. The new extents will have the default alignment,
	 * extent, and padding.
	 * @return New default constraints for the given component.
	 */
	public RegionConstraints createDefaultConstraints() {
		final RegionConstraints regionConstraints = new RegionConstraints(Region.CENTER); //create new constraints defaulting to the center region
		for(final Flow flow : Flow.values()) { //for each flow
			regionConstraints.setAlignment(flow, getAlignment(flow)); //set the default alignment
			regionConstraints.setExtent(flow, getExtent(flow)); //set the default extent
		}
		for(final Border border : Border.values()) { //for each border
			regionConstraints.setPaddingExtent(border, getPaddingExtent(border)); //set the default padding extent			
		}
		return regionConstraints; //return the new default region constraints
	}

	/**
	 * Retrieves a component for a given region.
	 * @param region The region for which a component should be returned.
	 * @return The component with which the given region is associated, or <code>null</code> if no component has the given region specified.
	 */
	public Component getComponent(final Region region) { //TODO later use reverse maps or something similar for quicker lookup
		for(final Component childComponent : getOwner().getChildComponents()) { //for each child component in the container
			final RegionConstraints constraints = (RegionConstraints)getConstraints(childComponent); //get the constraints for this component TODO use covariants on each subclass; update getConstraints() to ensure correct type
			if(constraints.getRegion() == region) { //if this component is in the correct region
				return childComponent; //return the component
			}
		}
		return null; //indicate that no component has the given region
	}

}
