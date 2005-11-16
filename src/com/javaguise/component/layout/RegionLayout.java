package com.javaguise.component.layout;

import java.util.*;
import static com.garretwilson.lang.ObjectUtilities.*;
import com.javaguise.component.Component;
import com.javaguise.geometry.Extent;
import com.javaguise.session.GuiseSession;

/**A layout that defines locations of components in internationalized relative terms.
This layout uses default constraints of {@link Region#CENTER}.
@author Garret Wilson
@see Region
*/
public class RegionLayout extends AbstractLayout<RegionLayout.Constraints>
{

	/**Default constraints for the beginning of a line; "left" in left-to-right, top-to-botom orientation.*/
	public final static Constraints LINE_START_CONSTRAINTS=new Constraints(Region.LINE_START);

	/**Default constraints for the end of a line; "right" in left-to-right, top-to-botom orientation.*/
	public final static Constraints LINE_END_CONSTRAINTS=new Constraints(Region.LINE_END);

	/**Default constraints for the beginning of a page; "top" in left-to-right, top-to-botom orientation.*/
	public final static Constraints PAGE_START_CONSTRAINTS=new Constraints(Region.PAGE_START);

	/**Default constraints for the end of a page; "bottom" in left-to-right, top-to-botom orientation.*/
	public final static Constraints PAGE_END_CONSTRAINTS=new Constraints(Region.PAGE_END);

	/**Default constraints for the center region.*/
	public final static Constraints CENTER_CONSTRAINTS=new Constraints(Region.CENTER);

	/**Session constructor.
	@param session The Guise session that owns this layout.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public RegionLayout(final GuiseSession session)
	{
		super(session);	//construct the parent class
	}

	/**Creates default constraints for the container.
	This implementation returns {@link #CENTER_CONSTRAINTS}.
	@return New default constraints for the given component.
	*/
	public Constraints createDefaultConstraints()
	{
		return CENTER_CONSTRAINTS;	//default to the center region
	}

	/**Metadata about individual component layout.
	@author Garret Wilson
	*/
	public static class Constraints extends AbstractLayout.AbstractConstraints
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
		@param region The layout region for the associated component.
		@exception NullPointerException if the given region is <code>null</code>.
		*/
		public Constraints(final Region region)
		{
			this(region, null, null);	//construct the region with no preferred extents
		}

		/**Preferred extents constructor.
		@param region The layout region for the associated component.
		@param preferredWidth The preferred width of the region, or <code>null</code> there is no width preference.		
		@param preferredHeight The preferred height of the region, or <code>null</code> there is no height preference.		
		@exception NullPointerException if the given region is <code>null</code>.
		*/
		public Constraints(final Region region, final Extent preferredWidth, final Extent preferredHeight)
		{
			this.region=checkNull(region, "Region cannot be null.");
			this.preferredWidth=preferredWidth;
			this.preferredHeight=preferredHeight;
		}
	}

	/**Retrieves a component for a given region.
	@param region The region for which a component should be returned.
	@return The component with which the given region is associated, or <code>null</code> if no component has the given region specified.
	*/
	public Component<?> getComponent(final Region region)	//TODO later use reverse maps or something similar for quicker lookup
	{
		for(final Map.Entry<Component<?>, Constraints> entry:componentConstraintsMap.entrySet())	//for each entry in the map of components and constraints
		{
			if(entry.getValue().getRegion()==region)	//if this component is in the correct region
			{
				return entry.getKey();	//return the component
			}
		}
		return null;	//indicate that no component has the given region
	}

}
