package com.guiseframework.component;

import com.guiseframework.component.layout.Flow;
import com.guiseframework.converter.AbstractStringLiteralConverter;
import com.guiseframework.model.*;

import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;

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

	/**Whether the value is editable and the control will allow the the user to change the value.*/
	private boolean editable=true;

		/**@return Whether the value is editable and the control will allow the the user to change the value.*/
		public boolean isEditable() {return editable;}

		/**Sets whether the value is editable and the control will allow the the user to change the value.
		This is a bound property of type <code>Boolean</code>.
		@param newEditable <code>true</code> if the control should allow the user to change the value.
		@see #EDITABLE_PROPERTY
		*/
		public void setEditable(final boolean newEditable)
		{
			if(editable!=newEditable)	//if the value is really changing
			{
				final boolean oldEditable=editable;	//get the old value
				editable=newEditable;	//actually change the value
				firePropertyChange(EDITABLE_PROPERTY, Boolean.valueOf(oldEditable), Boolean.valueOf(newEditable));	//indicate that the value changed
			}			
		}

	/**The flow axis.*/
	private Flow axis;

		/**@return The flow axis.*/
		public Flow getAxis() {return axis;}

		/**Sets the flow axis.
		This is a bound property
		@param newAxis The flow axis.
		@exception NullPointerException if the given axis is <code>null</code>.
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
	@exception NullPointerException if the given value class and/or axis is <code>null</code>.
	*/
	public TabControl(final Class<V> valueClass, final Flow axis)
	{
		this(new DefaultListSelectModel<V>(valueClass), axis);	//construct the class with a default model TODO probably use a single list selection strategy
	}

	/**Value class, axis, and maximum tab count constructor with a default data model to represent a given type with multiple selection.
	@param valueClass The class indicating the type of value held in the model.
	@param axis The axis along which the tabs are oriented.
	@param maxTabCount The requested number of visible rows, or -1 if no row count is specified.
	@exception NullPointerException if the given value class and/or axis is <code>null</code>.
	*/
	public TabControl(final Class<V> valueClass, final Flow axis, final int maxTabCount)
	{
		this(new DefaultListSelectModel<V>(valueClass), axis, maxTabCount);	//construct the class with a default model and the maximum tab count
	}

	/**Value class, selection strategy, and axis constructor with a default data model to represent a given type.
	@param valueClass The class indicating the type of value held in the model.
	@param selectionStrategy The strategy for selecting values in the model.
	@param axis The axis along which the tabs are oriented.
	@exception NullPointerException if the given value class, selection strategy, and/or axis is <code>null</code>.
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
	@exception NullPointerException if the given value class, selection strategy, and/or axis is <code>null</code>.
	*/
	public TabControl(final Class<V> valueClass, final ListSelectionPolicy<V> selectionStrategy, final Flow axis, final int maxTabCount)
	{
		this(new DefaultListSelectModel<V>(valueClass, selectionStrategy), axis, maxTabCount);	//construct the class with a default model and the maximum tab count
	}

	/**List select model and axis constructor.
	@param listSelectModel The component list select model.
	@param axis The axis along which the tabs are oriented.
	@exception NullPointerException if the given list select model and/or axis is <code>null</code>.
	*/
	public TabControl(final ListSelectModel<V> listSelectModel, final Flow axis)
	{
		this(listSelectModel, axis, -1);	//construct the class with no maximum tab count
	}

	/**List select model, axis, and maximum tab count constructor.
	@param listSelectModel The component list select model.
	@param axis The axis along which the tabs are oriented.
	@param maxTabCount The requested number of visible tabs, or -1 if no maximum tab count is specified.
	@exception NullPointerException if the given list select model and/or axis is <code>null</code>.
	*/
	public TabControl(final ListSelectModel<V> listSelectModel, final Flow axis, final int maxTabCount)
	{
		this(listSelectModel, new DefaultValueRepresentationStrategy<V>(AbstractStringLiteralConverter.getInstance(listSelectModel.getValueClass())), axis, maxTabCount);	//construct the class with a default representation strategy
	}

	/**List select model, value representation strategy, and axis constructor.
	@param listSelectModel The component list select model.
	@param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	@param axis The axis along which the tabs are oriented.
	@exception NullPointerException if the given list select model, value representation strategy, and/or axis is <code>null</code>.
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
	@exception NullPointerException if the given list select model, value representation strategy, and/or axis is <code>null</code>.
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
