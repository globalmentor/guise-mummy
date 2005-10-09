package com.javaguise.component;

import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;

import com.javaguise.component.layout.*;
import com.javaguise.model.DefaultValueModel;
import com.javaguise.model.ValueModel;
import com.javaguise.session.GuiseSession;

/**A value control that represents its value by a slider.
@author Garret Wilson
@param <V> The type of value the slider represents.
*/
public class SliderControl<V extends Number> extends AbstractValueControl<V, SliderControl<V>>
{

	/**The axis bound property.*/
	public final static String AXIS_PROPERTY=getPropertyName(SliderControl.class, "axis");

	/**The flow axis.*/
	private Orientation.Flow axis;

		/**@return The flow axis.*/
		public Orientation.Flow getAxis() {return axis;}

		/**Sets the flow axis.
		This is a bound property
		@param newAxis The flow axis.
		@exception NullPointerException if the given axis is <code>null</code>.
		@see #AXIS_PROPERTY
		*/
		public void setAxis(final Orientation.Flow newAxis)
		{
			if(axis!=checkNull(newAxis, "Flow axis cannot be null."))	//if the value is really changing
			{
				final Orientation.Flow oldAxis=axis;	//get the old value
				axis=newAxis;	//actually change the value
				firePropertyChange(AXIS_PROPERTY, oldAxis, newAxis);	//indicate that the value changed
			}
		}

	/**Session, model, and axis constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@param axis The axis along which the slider is oriented.
	@exception NullPointerException if the given session, axis, and/or model is <code>null</code>.
	*/
	public SliderControl(final GuiseSession session, final ValueModel<V> model, final Orientation.Flow axis)
	{
		this(session, null, model, axis);	//construct the component, indicating that a default ID should be used
	}

	/**Session and axis constructor with a default data model to represent a given type.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of value held in the model.
	@param axis The axis along which the slider is oriented.
	@exception NullPointerException if the given session, value class, and/or axis is <code>null</code>.
	@exception IllegalArgumentException if no default converter is available for the given value class.
	*/
	public SliderControl(final GuiseSession session, final Class<V> valueClass, final Orientation.Flow axis)
	{
		this(session, null, valueClass, axis);	//construct the component, indicating that a default ID should be used
	}

	/**Session, ID and axis constructor with a default data model to represent a given type.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param valueClass The class indicating the type of value held in the model.
	@param axis The axis along which the slider is oriented.
	@exception NullPointerException if the given session, value class, and/or axis is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	@exception IllegalArgumentException if no default converter is available for the given value class.
	*/
	public SliderControl(final GuiseSession session, final String id, final Class<V> valueClass, final Orientation.Flow axis)
	{
		this(session, id, new DefaultValueModel<V>(session, valueClass), axis);	//construct the class with a default model
	}

	/**Session, ID, model, and axis constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param axis The axis along which the slider is oriented.
	@exception NullPointerException if the given session, axis, and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public SliderControl(final GuiseSession session, final String id, final ValueModel<V> model, final Orientation.Flow axis)
	{
		super(session, id, model);	//construct the parent class
		this.axis=checkNull(axis, "Flow axis cannot be null.");
	}

}
