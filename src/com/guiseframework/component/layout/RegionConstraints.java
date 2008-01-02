package com.guiseframework.component.layout;

import static com.garretwilson.util.ArrayUtilities.*;
import static com.globalmentor.java.ClassUtilities.*;
import static com.globalmentor.java.Objects.*;

import com.globalmentor.java.Objects;
import com.guiseframework.geometry.Extent;

/**Constraints on individual component region layout.
@author Garret Wilson
*/
public class RegionConstraints extends AbstractConstraints
{

	/**The bound property of the line alignment.*/
	public final static String LINE_ALIGNMENT_PROPERTY=getPropertyName(RegionConstraints.class, "lineAlignment");
	/**The bound property of the page alignment.*/
	public final static String PAGE_ALIGNMENT_PROPERTY=getPropertyName(RegionConstraints.class, "pageAlignment");
	/**The line extent (width in left-to-right top-to-bottom orientation) bound property.*/
	public final static String LINE_EXTENT_PROPERTY=getPropertyName(RegionConstraints.class, "lineExtent");
	/**The page extent (height in left-to-right top-to-bottom orientation) bound property.*/
	public final static String PAGE_EXTENT_PROPERTY=getPropertyName(RegionConstraints.class, "pageExtent");
	/**The bound property of the line near padding extent.*/
	public final static String PADDING_LINE_NEAR_EXTENT_PROPERTY=getPropertyName(RegionConstraints.class, "paddingLineNearExtent");
	/**The bound property of the line far padding extent.*/
	public final static String PADDING_LINE_FAR_EXTENT_PROPERTY=getPropertyName(RegionConstraints.class, "paddingLineFarExtent");
	/**The bound property of the page near padding extent.*/
	public final static String PADDING_PAGE_NEAR_EXTENT_PROPERTY=getPropertyName(RegionConstraints.class, "paddingPageNearExtent");
	/**The bound property of the page far padding extent.*/
	public final static String PADDING_PAGE_FAR_EXTENT_PROPERTY=getPropertyName(RegionConstraints.class, "paddingPageFarExtent");

	/**The layout region for the associated component.*/
	private final Region region;
		
		/**The layout region for the associated component.*/
		public Region getRegion() {return region;}

	/**The array of region alignments.*/
	private double[] alignments=fill(new double[Flow.values().length], 0);

	/**The properties corresponding to the region alignments.*/
	private final static String[] ALIGNMENT_PROPERTIES;

		static
		{
			ALIGNMENT_PROPERTIES=new String[Flow.values().length];	//create the array of properties and fill it with corresponding properties
			ALIGNMENT_PROPERTIES[Flow.LINE.ordinal()]=LINE_ALIGNMENT_PROPERTY;
			ALIGNMENT_PROPERTIES[Flow.PAGE.ordinal()]=PAGE_ALIGNMENT_PROPERTY;
		}

		/**Returns the alignment of the indicated flow relative to the beginning of the alignment axis.
		@param flow The flow for which an alignment should be returned.
		@return The alignment of the given flow.
		*/
		public double getAlignment(final Flow flow) {return alignments[flow.ordinal()];}

		/**@return The alignment of the line flow relative to the beginning of the alignment axis.*/
		public double getLineAlignment() {return getAlignment(Flow.LINE);}

		/**@return The alignment of the page flow relative to the beginning of the alignment axis.*/
		public double getPageAlignment() {return getAlignment(Flow.PAGE);}

		/**Sets the alignment of a given flow.
		The alignment of each flow represents a bound property of type {@link Double}.
		@param flow The flow for which the alignment should be set.
		@param newAlignment The alignment of the region relative to the beginning of the given flow.
		@exception NullPointerException if the given flow is <code>null</code>. 
		@see #LINE_ALIGNMENT_PROPERTY
		@see #PAGE_ALIGNMENT_PROPERTY
		*/
		public void setAlignment(final Flow flow, final double newAlignment)
		{
			final int flowOrdinal=checkInstance(flow, "Flow cannot be null").ordinal();	//get the ordinal of the flow
			final double oldAlignment=alignments[flowOrdinal];	//get the old value
			if(oldAlignment!=newAlignment)	//if the value is really changing
			{
				alignments[flowOrdinal]=newAlignment;	//actually change the value
				firePropertyChange(ALIGNMENT_PROPERTIES[flowOrdinal], oldAlignment, newAlignment);	//indicate that the value changed
			}			
		}

		/**Sets the alignment of the line flow.
		For example, in a left-to-right top-to-bottom orientation,
		line alignments of 0.0, 0.5, and 1.0 would be equivalent to what are commonly known as <dfn>left</dfn>, <dfn>center</dfn>, and <dfn>right</dfn> alignments, respectively. 
		This is a bound property of type {@link Double}.
		@param newAlignment The line alignment of the region relative to the beginning of the alignment axis.
		@see #LINE_ALIGNMENT_PROPERTY
		*/
		public void setLineAlignment(final double newAlignment) {setAlignment(Flow.LINE, newAlignment);}

		/**Sets the alignment of the page flow.
		For example, in a left-to-right top-to-bottom orientation,
		page alignments of 0.0, 0.5, and 1.0 would be equivalent to what are commonly known as <dfn>top</dfn>, <dfn>middle</dfn>, and <dfn>bottom</dfn> alignments, respectively. 
		This is a bound property of type {@link Double}.
		@param newAlignment The page alignment of the region relative to the beginning of the alignment axis.
		@see #PAGE_ALIGNMENT_PROPERTY
		*/
		public void setPageAlignment(final double newAlignment) {setAlignment(Flow.PAGE, newAlignment);}
		
	/**The array of region extents.*/
	private Extent[] extents=fill(new Extent[Flow.values().length], null);

	/**The properties corresponding to the region extents.*/
	private final static String[] EXTENT_PROPERTIES;

		static
		{
			EXTENT_PROPERTIES=new String[Flow.values().length];	//create the array of properties and fill it with corresponding properties
			EXTENT_PROPERTIES[Flow.LINE.ordinal()]=LINE_EXTENT_PROPERTY;
			EXTENT_PROPERTIES[Flow.PAGE.ordinal()]=PAGE_EXTENT_PROPERTY;
		}

		/**Returns the extent of the indicated flow.
		@param flow The flow for which an extent should be returned.
		@return The extent of the given flow.
		*/
		public Extent getExtent(final Flow flow) {return extents[flow.ordinal()];}

		/**Returns the extent of the line flow.
		In left-to-right top-to-bottom orientation, this is commonly known as the <dfn>width</dfn>.
		@return The extent of the flow, or <code>null</code> if no preferred extent has been specified
		*/
		public Extent getLineExtent() {return getExtent(Flow.LINE);}

		/**Returns the extent of the page flow.
		In left-to-right top-to-bottom orientation, this is commonly known as the <dfn>height</dfn>.
		@return The extent of the flow, or <code>null</code> if no preferred extent has been specified
		*/
		public Extent getPageExtent() {return getExtent(Flow.PAGE);}

		/**Sets the extent of a given flow.
		The extent of each flow represents a bound property.
		@param flow The flow for which the extent should be set.
		@param newExtent The new requested extent of the region, or <code>null</code> there is no extent preference.
		@exception NullPointerException if the given flow is <code>null</code>. 
		@see #LINE_EXTENT_PROPERTY
		@see #PAGE_EXTENT_PROPERTY
		*/
		public void setExtent(final Flow flow, final Extent newExtent)
		{
			final int flowOrdinal=checkInstance(flow, "Flow cannot be null").ordinal();	//get the ordinal of the flow
			final Extent oldExtent=extents[flowOrdinal];	//get the old value
			if(!Objects.equals(oldExtent, newExtent))	//if the value is really changing
			{
				extents[flowOrdinal]=newExtent;	//actually change the value
				firePropertyChange(EXTENT_PROPERTIES[flowOrdinal], oldExtent, newExtent);	//indicate that the value changed
			}			
		}

		/**Sets the extent of the line flow.
		In left-to-right top-to-bottom orientation, this is commonly known as the <dfn>width</dfn>.
		This is a bound property.
		@param newExtent The new requested extent of the region, or <code>null</code> there is no extent preference.
		@see #LINE_EXTENT_PROPERTY
		*/
		public void setLineExtent(final Extent newExtent) {setExtent(Flow.LINE, newExtent);}

		/**Sets the extent of the page flow.
		In left-to-right top-to-bottom orientation, this is commonly known as the <dfn>height</dfn>.
		This is a bound property.
		@param newExtent The new requested extent of the region, or <code>null</code> there is no extent preference.
		@see #PAGE_EXTENT_PROPERTY
		*/
		public void setPageExtent(final Extent newExtent) {setExtent(Flow.PAGE, newExtent);}

	/**The array of padding extents.*/
	private Extent[] paddingExtents=fill(new Extent[Border.values().length], Extent.ZERO_EXTENT1);

	/**The properties corresponding to the padding extents.*/
	private final static String[] PADDING_EXTENT_PROPERTIES;

		static
		{
			PADDING_EXTENT_PROPERTIES=new String[Border.values().length];	//create the array of properties and fill it with corresponding properties
			PADDING_EXTENT_PROPERTIES[Border.LINE_NEAR.ordinal()]=PADDING_LINE_NEAR_EXTENT_PROPERTY;
			PADDING_EXTENT_PROPERTIES[Border.LINE_FAR.ordinal()]=PADDING_LINE_FAR_EXTENT_PROPERTY;
			PADDING_EXTENT_PROPERTIES[Border.PAGE_NEAR.ordinal()]=PADDING_PAGE_NEAR_EXTENT_PROPERTY;
			PADDING_EXTENT_PROPERTIES[Border.PAGE_FAR.ordinal()]=PADDING_PAGE_FAR_EXTENT_PROPERTY;
		}

		/**Returns the padding extent of the indicated border.
		@param border The border for which a padding extent should be returned.
		@return The padding extent of the given border.
		*/
		public Extent getPaddingExtent(final Border border) {return paddingExtents[border.ordinal()];}

		/**Returns the padding extent of the line near page near border.
		@return The padding extent of the given border.
		*/
		public Extent getPaddingLineNearExtent() {return getPaddingExtent(Border.LINE_NEAR);}

		/**Returns the padding extent of the line far page near border.
		@return The padding extent of the given border.
		*/
		public Extent getPaddingLineFarExtent() {return getPaddingExtent(Border.LINE_FAR);}

		/**Returns the padding extent of the line near page far border.
		@return The padding extent of the given border.
		*/
		public Extent getPaddingPageNearExtent() {return getPaddingExtent(Border.PAGE_NEAR);}

		/**Returns the padding extent of the line far page far border.
		@return The padding extent of the given border.
		*/
		public Extent getPaddingPageFarExtent() {return getPaddingExtent(Border.PAGE_FAR);}

		/**Sets the padding extent of a given border.
		The padding extent of each border represents a bound property.
		@param border The border for which the padding extent should be set.
		@param newPaddingExtent The padding extent.
		@exception NullPointerException if the given border and/or padding extent is <code>null</code>. 
		@see #PADDING_LINE_NEAR_EXTENT_PROPERTY
		@see #PADDING_LINE_FAR_EXTENT_PROPERTY
		@see #PADDING_PAGE_NEAR_EXTENT_PROPERTY
		@see #PADDING_PAGE_FAR_EXTENT_PROPERTY
		*/
		public void setPaddingExtent(final Border border, final Extent newPaddingExtent)
		{
			final int borderOrdinal=checkInstance(border, "Border cannot be null").ordinal();	//get the ordinal of the border
			final Extent oldPaddingExtent=paddingExtents[borderOrdinal];	//get the old value
			if(!oldPaddingExtent.equals(checkInstance(newPaddingExtent, "Padding extent cannot be null.")))	//if the value is really changing
			{
				paddingExtents[borderOrdinal]=newPaddingExtent;	//actually change the value
				firePropertyChange(PADDING_EXTENT_PROPERTIES[borderOrdinal], oldPaddingExtent, newPaddingExtent);	//indicate that the value changed
			}			
		}

		/**Sets the padding extent of the line near border.
		This is a bound property.
		@param newPaddingExtent The padding extent.
		@exception NullPointerException if the given padding extent is <code>null</code>. 
		@see #PADDING_LINE_NEAR_EXTENT_PROPERTY
		*/
		public void setPaddingLineNearExtent(final Extent newPaddingExtent) {setPaddingExtent(Border.LINE_NEAR, newPaddingExtent);}

		/**Sets the padding extent of the line far border.
		This is a bound property.
		@param newPaddingExtent The padding extent, or <code>null</code> if the default padding extent should be used.
		@exception NullPointerException if the given padding extent is <code>null</code>. 
		@see #PADDING_LINE_FAR_EXTENT_PROPERTY
		*/
		public void setPaddingLineFarExtent(final Extent newPaddingExtent) {setPaddingExtent(Border.LINE_FAR, newPaddingExtent);}

		/**Sets the padding extent of the page near border.
		This is a bound property.
		@param newPaddingExtent The padding extent, or <code>null</code> if the default padding extent should be used.
		@exception NullPointerException if the given padding extent is <code>null</code>. 
		@see #PADDING_PAGE_NEAR_EXTENT_PROPERTY
		*/
		public void setPaddingPageNearExtent(final Extent newPaddingExtent) {setPaddingExtent(Border.PAGE_NEAR, newPaddingExtent);}

		/**Sets the padding extent of the page far border.
		This is a bound property.
		@param newPaddingExtent The padding extent, or <code>null</code> if the default padding extent should be used.
		@exception NullPointerException if the given padding extent is <code>null</code>. 
		@see #PADDING_PAGE_FAR_EXTENT_PROPERTY
		*/
		public void setPaddingPageFarExtent(final Extent newPaddingExtent) {setPaddingExtent(Border.PAGE_FAR, newPaddingExtent);}

		/**Sets the padding extent of all borders.
		The padding extent of each border represents a bound property.
		This is a convenience method that calls {@link #setPaddingExtent(Border, Extent)} for each border.
		@param newPaddingExtent The padding extent.
		@exception NullPointerException if the given padding extent is <code>null</code>. 
		@see #PADDING_LINE_NEAR_EXTENT_PROPERTY
		@see #PADDING_LINE_FAR_EXTENT_PROPERTY
		@see #PADDING_PAGE_NEAR_EXTENT_PROPERTY
		@see #PADDING_PAGE_FAR_EXTENT_PROPERTY
		*/
		public void setPaddingExtent(final Extent newPaddingExtent)
		{
			for(final Border border:Border.values())	//for each border
			{
				setPaddingExtent(border, newPaddingExtent);	//set this padding extent
			}
		}

	/**Region constructor.
	@param region The layout region for the associated component.
	@exception NullPointerException if the given region is <code>null</code>.
	*/
	public RegionConstraints(final Region region)
	{
		this.region=checkInstance(region, "Region cannot be null.");
	}
}
