package com.guiseframework.component.layout;

import static com.garretwilson.lang.ClassUtilities.getPropertyName;
import static com.garretwilson.lang.ObjectUtilities.checkInstance;

import com.guiseframework.component.Component;

/**A layout that defines locations of components in internationalized relative terms.
This layout uses default constraints of {@link Region#CENTER}.
<p>The region will span two of the components, if present, across the perpendicular flow.
Which flow is spanned across the other is determined by setting {@link #setSpanFlow(Flow)}.
For example, in a right-to-left top-to-bottom orientation, a span flow of {@link Flow#LINE} (the default)
will result in the top and bottom components spanning across the space used by the left, center, and right components.</p> 
@author Garret Wilson
@see Region
*/
public class RegionLayout extends AbstractLayout<RegionConstraints>
{

	/**The bound property of the fixed setting.*/
	public final static String FIXED_PROPERTY=getPropertyName(RegionLayout.class, "fixed");
	/**The bound property of the span flow.*/
	public final static String SPAN_FLOW_PROPERTY=getPropertyName(RegionLayout.class, "spanFlow");

	/**Whether the sizes of the regions are fixed or will dynamically change to support content.*/
	private boolean fixed=false;

		/**@return whether the sizes of the regions are fixed or will dynamically change to support the given content.*/
		public boolean isFixed() {return fixed;}

		/**Sets whether the sizes of the regions are fixed or will dynamically change to support the given content.
		This is a bound property of type <code>Boolean</code>.
		@param newFixed <code>true</code> if the sizes of the regions are fixed, or <code>false</code> if the regions will dynamically change to support the given content.
		@see #FIXED_PROPERTY
		*/
		public void setFixed(final boolean newFixed)
		{
			if(fixed!=newFixed)	//if the value is really changing
			{
				final boolean oldFixed=fixed;	//get the old value
				fixed=newFixed;	//actually change the value
				firePropertyChange(FIXED_PROPERTY, Boolean.valueOf(oldFixed), Boolean.valueOf(newFixed));	//indicate that the value changed
			}
		}

	/**The logical axis which will span components across the other logical axis.*/
	private Flow spanFlow;

		/**@return The logical axis which will span components across the other logical axis.*/
		public Flow getSpanFlow() {return spanFlow;}

		/**Sets the logical axis which will span components across the other logical axis.
		This is a bound property.
		@param newSpanFlow The logical axis which will span components across the other logical axis.
		@exception NullPointerException if the given span flow is <code>null</code>.
		@see #SPAN_FLOW_PROPERTY 
		*/
		public void setSpanFlow(final Flow newSpanFlow)
		{
			if(spanFlow!=checkInstance(newSpanFlow, "Span flow cannot be null."))	//if the value is really changing
			{
				final Flow oldSpanFlow=spanFlow;	//get the old value
				spanFlow=newSpanFlow;	//actually change the value
				firePropertyChange(SPAN_FLOW_PROPERTY, oldSpanFlow, newSpanFlow);	//indicate that the value changed
			}
		}

	/**@return The class representing the type of constraints appropriate for this layout.*/
	public Class<? extends RegionConstraints> getConstraintsClass() {return RegionConstraints.class;}

	/**Default constructor with {@link Flow#LINE} span flow.*/
	public RegionLayout()
	{
		this(Flow.LINE);	//construct the class with line span flow
	}

	/**Span flow constructor.
	@param spanFlow The logical axis which will span components across the other logical axis.
	@exception NullPointerException if the given span flow is <code>null</code>.
	*/
	public RegionLayout(final Flow spanFlow)
	{
		super();	//construct the parent class
		this.spanFlow=checkInstance(spanFlow, "Span flow cannot be null.");
	}

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
