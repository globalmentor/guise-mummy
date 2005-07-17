package com.javaguise.component.layout;

import java.util.*;
import static com.garretwilson.lang.ObjectUtilities.*;
import com.javaguise.component.Component;

/**A layout that defines locations of components in internationalized relative terms.
This layout uses default constraints of {@link Region#CENTER}.
@author Garret Wilson
@see Region
*/
public class RegionLayout extends AbstractLayout<RegionLayout.Constraints>
{

	/**Default constraints for the center region.*/
	public final static Constraints CENTER_CONSTRAINTS=new Constraints(Region.CENTER);

	/**Constructor.*/
	public RegionLayout()
	{
	}

	/**Creates default constraints for the given component.
	This implementation returns {@link #CENTER_CONSTRAINTS}.
	@param component The component for which constraints should be provided.
	@return New default constraints for the given component.
	*/
	public Constraints createDefaultConstraints(final Component<?> component)
	{
		return CENTER_CONSTRAINTS;	//default to the center region
	}

	/**Metadata about individual component flow.
	@author Garret Wilson
	*/
	public static class Constraints implements Layout.Constraints
	{

		/**The layout region for the associated component.*/
		private final Region region;
			
			/**The layout region for the associated component.*/
			public Region getRegion() {return region;}

		/**Constructor.
		@param region The layout region for the associated component.
		@exception NullPointerException if the given region is <code>null</code>.
		*/
		public Constraints(final Region region)
		{
			this.region=checkNull(region, "Region cannot be null.");
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
