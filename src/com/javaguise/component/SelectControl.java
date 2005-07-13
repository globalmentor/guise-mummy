package com.javaguise.component;

import com.javaguise.model.*;
import static com.garretwilson.lang.ClassUtilities.*;

/**A control to allow selection by the user of a value from a collection.
@param <V> The type of values to select.
@param <M> The type of select model used.
@author Garret Wilson
*/
public interface SelectControl<V, M extends SelectModel<V>, C extends SelectControl<V, M, C>> extends Control<M, C>
{
	/**The value representation strategy bound property.*/
	public final static String VALUE_REPRESENTATION_STRATEGY_PROPERTY=getPropertyName(SelectControl.class, "valueRepresentationStrategy");
}
