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

import com.guiseframework.component.layout.Flow;
import com.guiseframework.converter.AbstractStringLiteralConverter;
import com.guiseframework.model.*;

import static com.globalmentor.java.Classes.*;
import static com.globalmentor.java.Objects.*;

/**Control to allow selection of one or more values from a list using a tabbed interface.
@param <V> The type of values to select.
@author Garret Wilson
*/
public class TabControl<V> extends AbstractListSelectControl<V>
{

	/**The axis bound property.*/
	public final static String AXIS_PROPERTY=getPropertyName(TabControl.class, "axis");
	/**The maximum tab count bound property.*/
	public final static String MAX_TAB_COUNT_PROPERTY=getPropertyName(TabControl.class, "maxTabCount");

	/**The flow axis.*/
	private Flow axis;

		/**@return The flow axis.*/
		public Flow getAxis() {return axis;}

		/**Sets the flow axis.
		This is a bound property
		@param newAxis The flow axis.
		@throws NullPointerException if the given axis is <code>null</code>.
		@see #AXIS_PROPERTY
		*/
		public void setAxis(final Flow newAxis)
		{
			if(axis!=checkInstance(newAxis, "Flow axis cannot be null."))	//if the value is really changing
			{
				final Flow oldAxis=axis;	//get the old value
				axis=newAxis;	//actually change the value
				firePropertyChange(AXIS_PROPERTY, oldAxis, newAxis);	//indicate that the value changed
			}
		}

	/**The estimated number of tabs requested to be visible, or -1 if no tab count is specified.*/
	private int maxTabCount;

		/**@return The estimated number of tabs requested to be visible, or -1 if no tab count is specified.*/
		public int getMaxTabCount() {return maxTabCount;}

		/**Sets the estimated number of tabs requested to be visible.
		This is a bound property of type <code>Integer</code>.
		@param newMaxTabCount The new requested number of visible tabs, or -1 if no tab count is specified.
		@see #MAX_TAB_COUNT_PROPERTY
		*/
		public void setMaxTabCount(final int newMaxTabCount)
		{
			if(maxTabCount!=newMaxTabCount)	//if the value is really changing
			{
				final int oldMaxTabCount=maxTabCount;	//get the old value
				maxTabCount=newMaxTabCount;	//actually change the value
				firePropertyChange(MAX_TAB_COUNT_PROPERTY, new Integer(oldMaxTabCount), new Integer(newMaxTabCount));	//indicate that the value changed
			}			
		}

	/**Value class and axis constructor with a default data model to represent a given type.
	@param valueClass The class indicating the type of value held in the model.
	@param axis The axis along which the tabs are oriented.
	@throws NullPointerException if the given value class and/or axis is <code>null</code>.
	*/
	public TabControl(final Class<V> valueClass, final Flow axis)
	{
		this(new DefaultListSelectModel<V>(valueClass), axis);	//construct the class with a default model TODO probably use a single list selection strategy
	}

	/**Value class, axis, and maximum tab count constructor with a default data model to represent a given type with multiple selection.
	@param valueClass The class indicating the type of value held in the model.
	@param axis The axis along which the tabs are oriented.
	@param maxTabCount The requested number of visible rows, or -1 if no row count is specified.
	@throws NullPointerException if the given value class and/or axis is <code>null</code>.
	*/
	public TabControl(final Class<V> valueClass, final Flow axis, final int maxTabCount)
	{
		this(new DefaultListSelectModel<V>(valueClass), axis, maxTabCount);	//construct the class with a default model and the maximum tab count
	}

	/**Value class, selection strategy, and axis constructor with a default data model to represent a given type.
	@param valueClass The class indicating the type of value held in the model.
	@param selectionStrategy The strategy for selecting values in the model.
	@param axis The axis along which the tabs are oriented.
	@throws NullPointerException if the given value class, selection strategy, and/or axis is <code>null</code>.
	*/
	public TabControl(final Class<V> valueClass, final ListSelectionPolicy<V> selectionStrategy, final Flow axis)
	{
		this(new DefaultListSelectModel<V>(valueClass, selectionStrategy), axis);	//construct the class with a default model
	}

	/**Value class, selection strategy, axis, and maximum tab count constructor with a default data model to represent a given type.
	@param valueClass The class indicating the type of value held in the model.
	@param selectionStrategy The strategy for selecting values in the model.
	@param axis The axis along which the tabs are oriented.
	@param maxTabCount The requested number of visible tabs, or -1 if no maximum tab count is specified.
	@throws NullPointerException if the given value class, selection strategy, and/or axis is <code>null</code>.
	*/
	public TabControl(final Class<V> valueClass, final ListSelectionPolicy<V> selectionStrategy, final Flow axis, final int maxTabCount)
	{
		this(new DefaultListSelectModel<V>(valueClass, selectionStrategy), axis, maxTabCount);	//construct the class with a default model and the maximum tab count
	}

	/**List select model and axis constructor.
	@param listSelectModel The component list select model.
	@param axis The axis along which the tabs are oriented.
	@throws NullPointerException if the given list select model and/or axis is <code>null</code>.
	*/
	public TabControl(final ListSelectModel<V> listSelectModel, final Flow axis)
	{
		this(listSelectModel, axis, -1);	//construct the class with no maximum tab count
	}

	/**List select model, axis, and maximum tab count constructor.
	@param listSelectModel The component list select model.
	@param axis The axis along which the tabs are oriented.
	@param maxTabCount The requested number of visible tabs, or -1 if no maximum tab count is specified.
	@throws NullPointerException if the given list select model and/or axis is <code>null</code>.
	*/
	public TabControl(final ListSelectModel<V> listSelectModel, final Flow axis, final int maxTabCount)
	{
		this(listSelectModel, new DefaultValueRepresentationStrategy<V>(AbstractStringLiteralConverter.getInstance(listSelectModel.getValueClass())), axis, maxTabCount);	//construct the class with a default representation strategy
	}

	/**List select model, value representation strategy, and axis constructor.
	@param listSelectModel The component list select model.
	@param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	@param axis The axis along which the tabs are oriented.
	@throws NullPointerException if the given list select model, value representation strategy, and/or axis is <code>null</code>.
	*/
	public TabControl(final ListSelectModel<V> listSelectModel, final ValueRepresentationStrategy<V> valueRepresentationStrategy, final Flow axis)
	{
		this(listSelectModel, valueRepresentationStrategy, axis, -1);	//construct the class with no maximum tab count
	}

	/**List select model, value representation strategy, axis, and maximum tab count constructor.
	@param listSelectModel The component list select model.
	@param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	@param axis The axis along which the tabs are oriented.
	@param maxTabCount The requested number of visible tabs, or -1 if no maximum tab count is specified.
	@throws NullPointerException if the given list select model, value representation strategy, and/or axis is <code>null</code>.
	*/
	public TabControl(final ListSelectModel<V> listSelectModel, final ValueRepresentationStrategy<V> valueRepresentationStrategy, final Flow axis, final int maxTabCount)
	{
		super(listSelectModel, valueRepresentationStrategy);	//construct the parent class
		this.axis=checkInstance(axis, "Flow axis cannot be null.");
		this.maxTabCount=maxTabCount;	//save the maximum tab count
	}

	/**A convenience base strategy for generating components to represents model values in a list select control.
	The component ID should reflect a unique identifier of the item
	@param <RR> The type of value the strategy is to represent.
	@author Garret Wilson
	*/
/*TODO del when works
	public abstract static class AbstractValueRepresentationStrategy<RR> implements ValueRepresentationStrategy<RR, ModelComponent<? extends LabelModel, ?>>
	{
	}
*/

	/**Returns a list of tabs.
	This method along with {@link #setTabs()} provides a <code>tabs</code> property for alternate tabs access.
	@return A list of tabs in order.
	@see #iterator()
	*/
/*TODO del if not needed
	public List<V> getTabs()
	{
		return getThis();	//return the control itself, which is a list of tabs
	}
*/

	/**Sets the tabs in this control.
	This method along with {@link #getTabs()} provides a <code>tabs</code> property for alternate tabs access.
	@param tabs The new tabs for this tab control in order.
	@see #clear()
	@see #add(Object)
	*/
/*TODO del if not needed
	public void setTabs(final List<V> tabs)
	{
		clear();	//remove all tabs from the control 
		for(final V tab:tabs)	//for each child
		{
			add(tab);	//add this tab
		}
	}
*/
}
