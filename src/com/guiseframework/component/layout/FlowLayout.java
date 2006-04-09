package com.guiseframework.component.layout;

/**A layout that flows information along an axis.
@author Garret Wilson
*/
public class FlowLayout extends AbstractFlowLayout<FlowConstraints>
{

	/**@return The class representing the type of constraints appropriate for this layout.*/
	public Class<? extends FlowConstraints> getConstraintsClass() {return FlowConstraints.class;}

	/**Default constructor with {@link Flow#PAGE} layout.*/
	public FlowLayout()
	{
		this(Flow.PAGE);	//construct the class with page flow layout
	}

	/**Flow constructor.
	@param flow The logical axis (line or page) along which information is flowed.
	@exception NullPointerException if the flow axis is <code>null</code>.
	*/
	public FlowLayout(final Flow flow)
	{
		super(flow);	//construct the parent class
	}

	/**Creates default constraints for the container.
	@return New default constraints for the container.
	@exception IllegalStateException if this layout does not support default constraints.
	*/
	public FlowConstraints createDefaultConstraints()
	{
		return new FlowConstraints();	//return a default constraints object
	}
}
