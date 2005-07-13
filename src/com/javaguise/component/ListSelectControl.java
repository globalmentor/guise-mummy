package com.javaguise.component;

import com.javaguise.model.*;

/**A control to allow selection by the user of a value from a list.
@param <V> The type of values to select.
@param <M> The type of select model used.
@author Garret Wilson
*/
public interface ListSelectControl<V, M extends ListSelectModel<V>, C extends SelectControl<V, M, C>> extends SelectControl<V, M, C>
{

	/**@return The strategy used to generate a component to represent each value in the model.*/
	public ValueRepresentationStrategy<V> getValueRepresentationStrategy();

	/**Sets the strategy used to generate a component to represent each value in the model.
	This is a bound property
	@param newValueRepresentationStrategy The new strategy to create components to represent this model's values.
	@exception NullPointerException if the provided value representation strategy is <code>null</code>.
	@see SelectControl#VALUE_REPRESENTATION_STRATEGY_PROPERTY
	*/
	public void setValueRepresentationStrategy(final ValueRepresentationStrategy<V> newValueRepresentationStrategy);

	/**@return The strategy used to generate a component to represent each value in the model.*/
//TODO del when works	public ValueRepresentationStrategy<V, R> getValueRepresentationStrategy();

	/**Sets the strategy used to generate a component to represent each value in the model.
	This is a bound property
	@param newValueRepresentationStrategy The new strategy to create components to represent this model's values.
	@exception NullPointerException if the provided value representation strategy is <code>null</code>.
	@see SelectControl#VALUE_REPRESENTATION_STRATEGY_PROPERTY
	*/
//TODO del when works	public void setValueRepresentationStrategy(final ValueRepresentationStrategy<V, R> newValueRepresentationStrategy);

	/**A strategy for generating components to represent model values.
	The component ID should reflect a unique identifier of the item
	@param <VV> The type of value the strategy is to represent.
	@param <RR> The type of component created for the value used to represent the value.
	@author Garret Wilson
	*/
/*TODO del if not needed
	public interface ValueRepresentationStrategy<VV, RR extends Component<?>> extends ComponentFactory<VV, RR>
	{
*/
		/**Determines an identier for the given object.
		This value must be equal to the ID of the component returned by the {@link ComponentFactory#createComponent(VV)} method.
		@param value The value for which an identifier should be returned.
		@return A string identifying the value, or <code>null</code> if the provided value is <code>null</code>.
		@see Component#getID()
		*/
//TODO del if not needed		public String getID(final VV value);
//TODO del if not needed	}

	/**A strategy for generating components to represent list select model values.
	The component ID should reflect a unique identifier of the item
	@param <VV> The type of value the strategy is to represent.
	@author Garret Wilson
	*/
	public interface ValueRepresentationStrategy<VV>
	{
		/**Creates a component for the given list value.
		@param model The model containing the value.
		@param value The value for which a component should be created.
		@param index The index of the value within the list.
		@param selected <code>true</code> if the value is selected.
		@param focused <code>true</code> if the value has the focus.
		@return A new component to represent the given value, or <code>null</code> if the provided value is <code>null</code>.
		*/
		public ModelComponent<? extends LabelModel, ?> createComponent(final ListSelectModel<VV> model, final VV value, final int index, final boolean selected, final boolean focused);

		/**Determines an identier for the given object.
		This value must be equal to the ID of the component returned by the {@link #createComponent(VV)} method.
		@param value The value for which an identifier should be returned.
		@return A string identifying the value, or <code>null</code> if the provided value is <code>null</code>.
		@see Component#getID()
		*/
		public String getID(final VV value);	//TODO del this method and keep the components around in the controller
	}
}
