package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;

import com.guiseframework.model.ValueModel;

/**A control to accept input by the user of a value.
@param <V> The type of value to represent.
@author Garret Wilson
*/
public interface ValueControl<V, C extends ValueControl<V, C>> extends Control<C>, ValueModel<V>
{

	/**The converter bound property.*/
	public final static String CONVERTER_PROPERTY=getPropertyName(ValueControl.class, "converter");

	/**@return Whether the value is editable and the control will allow the the user to change the value.*/
	public boolean isEditable();

	/**Sets whether the value is editable and the control will allow the the user to change the value.
	This is a bound property of type <code>Boolean</code>.
	@param newEditable <code>true</code> if the control should allow the user to change the value.
	@see #EDITABLE_PROPERTY
	*/
	public void setEditable(final boolean newEditable);

	/**@return The value model used by this component.*/
//TODO del	public ValueModel<V> getValueModel();

}
