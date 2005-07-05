package com.garretwilson.guise.component;

import com.garretwilson.guise.model.*;
import static com.garretwilson.lang.ClassUtilities.*;

/**A control to allow selection by the user of a value from a collection.
@param <V> The type of values to select.
@param <R> The type of component allowed to represent each value.
@author Garret Wilson
*/
public interface SelectControl<V, R extends Component<?>, C extends SelectControl<V, R, C>> extends Control<SelectModel<V>, C>
{
	/**The value representation strategy bound property.*/
	public final static String VALUE_REPRESENTATION_STRATEGY_PROPERTY=getPropertyName(SelectControl.class, "valueRepresentationStrategy");

	/**@return The strategy used to generate a component to represent each value in the model.*/
	public ValueRepresentationStrategy<V, R> getValueRepresentationStrategy();

	/**Sets the strategy used to generate a component to represent each value in the model.
	This is a bound property
	@param newValueRepresentationStrategy The new strategy to create components to represent this model's values.
	@exception NullPointerException if the provided value representation strategy is <code>null</code>.
	@see SelectControl#VALUE_REPRESENTATION_STRATEGY_PROPERTY
	*/
	public void setValueRepresentationStrategy(final ValueRepresentationStrategy<V, R> newValueRepresentationStrategy);

	/**A strategy for generating components to represent model values.
	The component ID should reflect a unique identifier of the item
	@param <VV> The type of value the strategy is to represent.
	@param <RR> The type of component created for the value used to represent the value.
	@author Garret Wilson
	*/
	public interface ValueRepresentationStrategy<VV, RR extends Component<?>> extends ComponentFactory<VV, RR>
	{
	}
}
