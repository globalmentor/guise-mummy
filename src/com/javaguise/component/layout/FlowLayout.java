package com.javaguise.component.layout;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.javaguise.component.Component;

/**A layout that flows information along an axis.
@author Garret Wilson
*/
public class FlowLayout extends AbstractLayout<FlowLayout.Constraints>
{

//TODO change axis to a relative axis (i.e. page/line); in fact, switch to Orientation.Axis (page/line)
	
	/**The axis along which information is flowed.*/
	private final Orientation.Axis axis;

		/**@return The axis along which information is flowed.*/
		public Orientation.Axis getAxis() {return axis;}

	/**Axis constructor.
	@param axis The axis along which information is flowed.
	@exception NullPointerException if the axis is <code>null</code>.
	*/
	public FlowLayout(final Orientation.Axis axis)
	{
		this.axis=checkNull(axis, "Axis cannot be null.");	//store the axis
	}

	/**Creates default constraints for the given component.
	@param component The component for which constraints should be provided.
	@return New default constraints for the given component.
	@exception IllegalStateException if this layout does not support default constraints.
	*/
	public Constraints createDefaultConstraints(final Component<?> component)
	{
		return new Constraints();	//return a default constraints object
	}

	/**Metadata about individual component flow.
	@author Garret Wilson
	*/
	public static class Constraints
	{
	}

}
