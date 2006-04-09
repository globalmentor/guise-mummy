package com.guiseframework.component.layout;

import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;

import com.guiseframework.component.Component;

/**A layout that flows information along an axis.
@param <T> The type of layout constraints associated with each component.
@author Garret Wilson
*/
public abstract class AbstractFlowLayout<T extends AbstractFlowConstraints> extends AbstractLayout<T>
{

	/**The bound property of the flow.*/
	public final static String FLOW_PROPERTY=getPropertyName(Component.class, "flow");

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

	/**Flow constructor.
	@param flow The logical axis (line or page) along which information is flowed.
	@exception NullPointerException if the flow axis is <code>null</code>.
	*/
	public AbstractFlowLayout(final Flow flow)
	{
		this.flow=checkInstance(flow, "Flow cannot be null.");	//store the flow
	}

}
