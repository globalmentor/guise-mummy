package com.guiseframework.component.layout;

import java.util.*;
import static com.garretwilson.lang.ObjectUtilities.*;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.Component;
import com.guiseframework.geometry.Extent;

/**A layout that defines locations of components in internationalized relative terms.
This layout uses default constraints of {@link Region#CENTER}.
@author Garret Wilson
@see Region
*/
public class RegionLayout extends AbstractLayout<RegionConstraints>
{

	/**Default constraints for the beginning of a line; "left" in left-to-right, top-to-bottom orientation.*/
//TODO del	public final static Constraints LINE_START_CONSTRAINTS=new Constraints(Region.LINE_START);

	/**Default constraints for the end of a line; "right" in left-to-right, top-to-bottom orientation.*/
//TODO del	public final static Constraints LINE_END_CONSTRAINTS=new Constraints(Region.LINE_END);

	/**Default constraints for the beginning of a page; "top" in left-to-right, top-to-bottom orientation.*/
//TODO del	public final static Constraints PAGE_START_CONSTRAINTS=new Constraints(Region.PAGE_START);

	/**Default constraints for the end of a page; "bottom" in left-to-right, top-to-bottom orientation.*/
//TODO del	public final static Constraints PAGE_END_CONSTRAINTS=new Constraints(Region.PAGE_END);

	/**Default constraints for the center region.*/
//TODO del	public final static Constraints CENTER_CONSTRAINTS=new Constraints(Region.CENTER);

	/**@return The class representing the type of constraints appropriate for this layout.*/
	public Class<? extends RegionConstraints> getConstraintsClass() {return RegionConstraints.class;}

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
	public RegionConstraints createDefaultConstraints()
	{
		return new RegionConstraints(getSession(), Region.CENTER);	//default to the center region
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
