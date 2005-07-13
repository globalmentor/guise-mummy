package com.javaguise.model;

import static com.garretwilson.lang.ClassUtilities.*;

import com.javaguise.component.Component;

/**A column in a table.
@param <V> The type of values contained in the table column.
@author Garret Wilson
*/
public interface TableColumnModel<V> extends LabelModel
{

	/**The value representation strategy bound property.*/
//TODO del when works	public final static String VALUE_REPRESENTATION_STRATEGY_PROPERTY=getPropertyName(TableColumnModel.class, "valueRepresentationStrategy");

	/**@return The class representing the type of values this model can hold.*/
	public Class<V> getValueClass();

	/**@return The strategy used to generate a component to represent each value in the model.*/
//TODO del when works	public ValueRepresentationStrategy<V, ? extends Component<?>> getValueRepresentationStrategy();

	/**Sets the strategy used to generate a component to represent each value in the model.
	This is a bound property
	@param newValueRepresentationStrategy The new strategy to create components to represent this model's values.
	@exception NullPointerException if the provided value representation strategy is <code>null</code>.
	@see TableColumnModel#VALUE_REPRESENTATION_STRATEGY_PROPERTY
	*/
//TODO del when works	public void setValueRepresentationStrategy(final ValueRepresentationStrategy<V, ? extends Component<?>> newValueRepresentationStrategy);

}
