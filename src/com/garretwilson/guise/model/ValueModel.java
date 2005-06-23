package com.garretwilson.guise.model;

import static com.garretwilson.lang.ClassUtilities.*;

/**A model for user input of a value.
@author Garret Wilson
*/
public interface ValueModel<V> extends Model
{

	/**The value bound property.*/
	public final static String VALUE_PROPERTY=getPropertyName(ValueModel.class, "value");

	/**@return The input value, or <code>null</code> if there is no input value.*/
	public V getValue();

	/**Sets the input value.
	This is a bound property that only fires a change event when the new value is different via the <code>equals()</code> method.
	@param newValue The input value of the model.
	@see #VALUE_PROPERTY
	*/
	public void setValue(final V newValue);

	/**@return The class representing the type of value this model can hold.*/
	public Class<V> getValueClass();

}
