package com.garretwilson.guise.component.layout;

import static com.garretwilson.lang.ObjectUtilities.*;

/**A layout that flows information along an axis.
@author Garret Wilson
*/
public class FlowLayout implements Layout
{

	/**The axis along which information is flowed.*/
	private final Axis axis;

		/**@return The axis along which information is flowed.*/
		public Axis getAxis() {return axis;}

	/**Axis constructor.
	@param axis The axis along which information is flowed.
	@exception NullPointerException if the axis is <code>null</code>.
	*/
	public FlowLayout(final Axis axis)
	{
		this.axis=checkNull(axis, "Axis cannot be null.");	//store the axis
	}

}
