package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;

/**A control to allow selection by the user of a value from a collection.
@param <V> The type of values to select.
@author Garret Wilson
*/
public interface SelectControl<V, C extends SelectControl<V, C>> extends ValueControl<V, C>
{
	/**The value representation strategy bound property.*/
	public final static String VALUE_REPRESENTATION_STRATEGY_PROPERTY=getPropertyName(SelectControl.class, "valueRepresentationStrategy");

}
