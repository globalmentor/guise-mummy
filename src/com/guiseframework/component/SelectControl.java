package com.guiseframework.component;

import com.guiseframework.model.*;

import static com.garretwilson.lang.ClassUtilities.*;

/**A control to allow selection by the user of a value from a collection.
@param <V> The type of values to select.
@author Garret Wilson
*/
public interface SelectControl<V, C extends SelectControl<V, C>> extends Control<C>
{
	/**The value representation strategy bound property.*/
	public final static String VALUE_REPRESENTATION_STRATEGY_PROPERTY=getPropertyName(SelectControl.class, "valueRepresentationStrategy");

	/**@return The select model used by this component.*/
	public SelectModel<V> getSelectModel();

}
