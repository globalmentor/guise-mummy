package com.guiseframework.component.layout;

import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;

import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.component.Component;
import com.guiseframework.geometry.Extent;

/**A layout that flows information along an axis.
@param <T> The type of layout constraints associated with each component.
@author Garret Wilson
*/
public abstract class AbstractFlowLayout<T extends AbstractFlowConstraints> extends AbstractLayout<T>
{

	/**The bound property of the flow.*/
	public final static String FLOW_PROPERTY=getPropertyName(AbstractFlowLayout.class, "flow");
	/**The bound property of the gap after flowed components.*/
	public final static String GAP_AFTER_PROPERTY=getPropertyName(AbstractFlowLayout.class, "gapAfter");
	/**The bound property of the gap before flowed components.*/
	public final static String GAP_BEFORE_PROPERTY=getPropertyName(AbstractFlowLayout.class, "gapBefore");
	/**The bound property of the gap between flowed components.*/
	public final static String GAP_BETWEEN_PROPERTY=getPropertyName(AbstractFlowLayout.class, "gapBetween");

	/**The logical axis (line or page) along which information is flowed.*/
	private Flow flow;

		/**@return The logical axis (line or page) along which information is flowed.*/
		public Flow getFlow() {return flow;}

		/**Sets the logical axis (line or page) along which information is flowed.
		This is a bound property.
		@param newFlow The logical axis along which information is flowed.
		@exception NullPointerException if the given flow is <code>null</code>.
		@see #FLOW_PROPERTY 
		*/
		public void setFlow(final Flow newFlow)
		{
			if(flow!=checkInstance(newFlow, "Flow cannot be null."))	//if the value is really changing
			{
				final Flow oldFlow=flow;	//get the old value
				flow=newFlow;	//actually change the value
				firePropertyChange(FLOW_PROPERTY, oldFlow, newFlow);	//indicate that the value changed
			}
		}

	/**The gap after flowed components.*/
	private Extent gapAfter=Extent.ZERO_EXTENT1;

		/**@return The gap after flowed components.*/
		public Extent getGapAfter() {return gapAfter;}

		/**Sets the gap after flowed components.
		This is a bound property.
		@param newGapAfter The gap after flowed components.
		@exception NullPointerException if the given gap is <code>null</code>.
		@see #GAP_AFTER_PROPERTY 
		*/
		public void setGapAfter(final Extent newGapAfter)
		{
			if(!gapAfter.equals(checkInstance(newGapAfter, "Gap after cannot be null.")))	//if the value is really changing
			{
				final Extent oldGapAfter=gapAfter;	//get the old value
				gapAfter=newGapAfter;	//actually change the value
				firePropertyChange(GAP_AFTER_PROPERTY, oldGapAfter, newGapAfter);	//indicate that the value changed
			}			
		}

	/**The gap before flowed components.*/
	private Extent gapBefore=Extent.ZERO_EXTENT1;

		/**@return The gap before flowed components.*/
		public Extent getGapBefore() {return gapBefore;}

		/**Sets the gap before flowed components.
		This is a bound property.
		@param newGapBefore The gap before flowed components.
		@exception NullPointerException if the given gap is <code>null</code>.
		@see #GAP_BEFORE_PROPERTY 
		*/
		public void setGapBefore(final Extent newGapBefore)
		{
			if(!gapBefore.equals(checkInstance(newGapBefore, "Gap before cannot be null.")))	//if the value is really changing
			{
				final Extent oldGapBefore=gapBefore;	//get the old value
				gapBefore=newGapBefore;	//actually change the value
				firePropertyChange(GAP_BEFORE_PROPERTY, oldGapBefore, newGapBefore);	//indicate that the value changed
			}			
		}

	/**The gap between flowed components.*/
	private Extent gapBetween=Extent.ZERO_EXTENT1;

		/**@return The gap between flowed components.*/
		public Extent getGapBetween() {return gapBetween;}

		/**Sets the gap between flowed components.
		This is a bound property.
		@param newGapBetween The gap between flowed components.
		@exception NullPointerException if the given gap is <code>null</code>.
		@see #GAP_BETWEEN_PROPERTY 
		*/
		public void setGapBetween(final Extent newGapBetween)
		{
			if(!gapBetween.equals(checkInstance(newGapBetween, "Gap between cannot be null.")))	//if the value is really changing
			{
				final Extent oldGapBetween=gapBetween;	//get the old value
				gapBetween=newGapBetween;	//actually change the value
				firePropertyChange(GAP_BETWEEN_PROPERTY, oldGapBetween, newGapBetween);	//indicate that the value changed
			}			
		}

	/**Sets the gap before, between, and after flowed components.
	This is a convenience method that sets each of the gaps to the same value.
	Each gap represents a bound property.
	@param newGap The gap before, between, and after flowed components.
	@exception NullPointerException if the given gap is <code>null</code>.
	@see #GAP_BEFORE_PROPERTY 
	@see #GAP_BETWEEN_PROPERTY 
	@see #GAP_AFTER_PROPERTY 
	*/
	public void setGap(final Extent newGap)
	{
		setGapBefore(newGap);	//set each of the gaps to the same value
		setGapBetween(newGap);
		setGapAfter(newGap);
	}

	/**Flow constructor.
	@param flow The logical axis (line or page) along which information is flowed.
	@exception NullPointerException if the flow axis is <code>null</code>.
	*/
	public AbstractFlowLayout(final Flow flow)
	{
		this.flow=checkInstance(flow, "Flow cannot be null.");	//store the flow
	}

}
