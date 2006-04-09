package com.guiseframework.component.layout;

import com.guiseframework.component.Component;

/**A layout that defines locations of components in internationalized relative terms.
This layout uses default constraints of {@link Region#CENTER}.
@author Garret Wilson
@see Region
*/
public class RegionLayout extends AbstractLayout<RegionConstraints>
{

	/**@return The class representing the type of constraints appropriate for this layout.*/
	public Class<? extends RegionConstraints> getConstraintsClass() {return RegionConstraints.class;}

	/**Creates default constraints for the container.
	This implementation returns {@link #CENTER_CONSTRAINTS}.
	@return New default constraints for the given component.
	*/
	public RegionConstraints createDefaultConstraints()
	{
		return new RegionConstraints(Region.CENTER);	//default to the center region
	}

	/**Retrieves a component for a given region.
	@param region The region for which a component should be returned.
	@return The component with which the given region is associated, or <code>null</code> if no component has the given region specified.
	*/
	public Component<?> getComponent(final Region region)	//TODO later use reverse maps or something similar for quicker lookup
	{
		for(final Component<?> childComponent:getContainer())	//for each child component in the container
		{
			final RegionConstraints constraints=(RegionConstraints)getConstraints(childComponent);	//get the constraints for this component TODO use covariants on each subclass; update getConstraints() to ensure correct type
			if(constraints.getRegion()==region)	//if this component is in the correct region
			{
				return childComponent;	//return the component
			}
		}
		return null;	//indicate that no component has the given region
	}

}
