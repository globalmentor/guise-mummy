/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guiseframework.component;

import com.guiseframework.model.*;

/**A control to allow selection by the user of a value from a list.
@param <V> The type of values to select.
@author Garret Wilson
*/
public interface ListSelectControl<V> extends SelectControl<V>, ListSelectModel<V>
{

	/**@return The strategy used to generate a component to represent each value in the model.*/
	public ValueRepresentationStrategy<V> getValueRepresentationStrategy();

	/**Sets the strategy used to generate a component to represent each value in the model.
	This is a bound property
	@param newValueRepresentationStrategy The new strategy to create components to represent this model's values.
	@throws NullPointerException if the provided value representation strategy is <code>null</code>.
	@see SelectControl#VALUE_REPRESENTATION_STRATEGY_PROPERTY
	*/
	public void setValueRepresentationStrategy(final ValueRepresentationStrategy<V> newValueRepresentationStrategy);

	/**Retrieves the component for the given value.
	@param value The value for which a representation component should be returned.
	@return The child component representing the given value.
	*/
	public Component getComponent(final V value);

	/**@return The strategy used to generate a component to represent each value in the model.*/
//TODO del when works	public ValueRepresentationStrategy<V, R> getValueRepresentationStrategy();

	/**Sets the strategy used to generate a component to represent each value in the model.
	This is a bound property
	@param newValueRepresentationStrategy The new strategy to create components to represent this model's values.
	@throws NullPointerException if the provided value representation strategy is <code>null</code>.
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
	@param <VV> The type of value the strategy is to represent.
	@author Garret Wilson
	*/
	public interface ValueRepresentationStrategy<VV>
	{
		/**Creates a component for the given list value.
		@param model The model containing the value.
		@param value The value for which a component should be created.
		@param index The index of the value within the list, or -1 if the value is not in the list (e.g. for representing no selection).
		@param selected <code>true</code> if the value is selected.
		@param focused <code>true</code> if the value has the focus.
		@return A new component to represent the given value.
		*/
		public Component createComponent(final ListSelectModel<VV> model, final VV value, final int index, final boolean selected, final boolean focused);
	}
}
