package com.guiseframework.component.layout;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.guiseframework.geometry.Extent;

/**Constraints on individual component region layout.
@author Garret Wilson
*/
public class RegionConstraints extends AbstractConstraints
{

	/**The layout region for the associated component.*/
	private final Region region;
		
		/**The layout region for the associated component.*/
		public Region getRegion() {return region;}

	/**The preferred width of the region, or <code>null</code> if no preferred width has been specified.*/
	private final Extent preferredWidth;

		/**@return The preferred width of the region, or <code>null</code> if no preferred width has been specified.*/
		public Extent getPreferredWidth() {return preferredWidth;}

	/**The preferred height of the region, or <code>null</code> if no preferred height has been specified.*/
	private Extent preferredHeight;

		/**@return The preferred height of the region , or <code>null</code> if no preferred height has been specified.*/
		public Extent getPreferredHeight() {return preferredHeight;}

	/**Region constructor.
	@param region The layout region for the associated component.
	@exception NullPointerException if the given region is <code>null</code>.
	*/
	public RegionConstraints(final Region region)
	{
		this(region, null, null);	//construct the region with no preferred extents
	}

	/**Preferred extents constructor.
	@param region The layout region for the associated component.
	@param preferredWidth The preferred width of the region, or <code>null</code> there is no width preference.		
	@param preferredHeight The preferred height of the region, or <code>null</code> there is no height preference.		
	@exception NullPointerException if the given region is <code>null</code>.
	*/
	public RegionConstraints(final Region region, final Extent preferredWidth, final Extent preferredHeight)
	{
		this.region=checkInstance(region, "Region cannot be null.");
		this.preferredWidth=preferredWidth;
		this.preferredHeight=preferredHeight;
	}
}
