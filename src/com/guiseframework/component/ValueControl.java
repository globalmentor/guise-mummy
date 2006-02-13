package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;

import com.guiseframework.model.ValueModel;

/**A control to accept input by the user of a value.
@param <V> The type of value to represent.
@author Garret Wilson
*/
public interface ValueControl<V, C extends ValueControl<V, C>> extends Control<C>
{

	/**The converter bound property.*/
	public final static String CONVERTER_PROPERTY=getPropertyName(ValueControl.class, "converter");

	/**@return The data model used by this component.*/
	public ValueModel<V> getModel();

}
