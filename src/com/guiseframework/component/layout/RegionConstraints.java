package com.guiseframework.component.layout;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.guiseframework.GuiseSession;
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

	/**Constructor.
	@param session The Guise session that owns these constraints.
	@param region The layout region for the associated component.
	@exception NullPointerException if the given session and/or region is <code>null</code>.
	*/
	public RegionConstraints(final GuiseSession session, final Region region)
	{
		this(session, region, null, null);	//construct the region with no preferred extents
	}

	/**Preferred extents constructor.
	@param session The Guise session that owns these constraints.
	@param region The layout region for the associated component.
	@param preferredWidth The preferred width of the region, or <code>null</code> there is no width preference.		
	@param preferredHeight The preferred height of the region, or <code>null</code> there is no height preference.		
	@exception NullPointerException if the given session and/or region is <code>null</code>.
	*/
	public RegionConstraints(final GuiseSession session, final Region region, final Extent preferredWidth, final Extent preferredHeight)
	{
		super(session);	//construct the parent class
		this.region=checkNull(region, "Region cannot be null.");
		this.preferredWidth=preferredWidth;
		this.preferredHeight=preferredHeight;
	}
}
