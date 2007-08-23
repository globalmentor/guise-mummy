package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;

import com.guiseframework.model.ValueModel;

/**A control to accept input by the user of a value.
@param <V> The type of value to represent.
@author Garret Wilson
*/
public interface ValueControl<V> extends Control, /*TODO del if not wanted EditComponent, */ValueModel<V>, ValuedComponent<V>
{

	/**The converter bound property.*/
	public final static String CONVERTER_PROPERTY=getPropertyName(ValueControl.class, "converter");

}
