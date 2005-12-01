package com.javaguise.component;

import com.javaguise.component.layout.Flow;
import com.javaguise.converter.AbstractStringLiteralConverter;
import com.javaguise.model.*;
import com.javaguise.session.GuiseSession;
import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;

/**Control to allow selection of one or more values from a list using a tabbed interface.
@param <V> The type of values to select.
@author Garret Wilson
*/
public class TabControl<V> extends AbstractListSelectControl<V, TabControl<V>>
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
		@exception NullPointerException if the given axis is <code>null</code>.
		@see #AXIS_PROPERTY
		*/
		public void setAxis(final Flow newAxis)
		{
			if(axis!=checkNull(newAxis, "Flow axis cannot be null."))	//if the value is really changing
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

	/**Session and model constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@param axis The axis along which the tabs are oriented.
	@exception NullPointerException if the given session, model, and/or axis is <code>null</code>.
	*/
	public TabControl(final GuiseSession session, final ListSelectModel<V> model, final Flow axis)
	{
		this(session, null, model, axis);	//construct the class, indicating that a default ID should be generated
	}

	/**Session, model, and maximum tab count constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@param axis The axis along which the tabs are oriented.
	@exception NullPointerException if the given session, model, and/or axis is <code>null</code>.
	*/
	public TabControl(final GuiseSession session, final ListSelectModel<V> model, final Flow axis, final int maxTabCount)
	{
		this(session, null, model, axis, maxTabCount);	//construct the class, indicating that a default ID should be generated
	}

	/**Session, model, and value representation strategy constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	@param axis The axis along which the tabs are oriented.
	@exception NullPointerException if the given session, model, value representation strategy, and/or axis is <code>null</code>.
	*/
	public TabControl(final GuiseSession session, final ListSelectModel<V> model, final ValueRepresentationStrategy<V> valueRepresentationStrategy, final Flow axis)
	{
		this(session, null, model, valueRepresentationStrategy, axis);	//construct the class, indicating that a default ID should be generated
	}

	/**Session, model, value representation strategy, and maximum count constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	@param axis The axis along which the tabs are oriented.
	@param maxTabCount The requested number of visible rows, or -1 if no row count is specified.
	@exception NullPointerException if the given session, model, value representation strategy, and/or axis is <code>null</code>.
	*/
	public TabControl(final GuiseSession session, final ListSelectModel<V> model, final ValueRepresentationStrategy<V> valueRepresentationStrategy, final Flow axis, final int maxTabCount)
	{
		this(session, null, model, valueRepresentationStrategy, axis, maxTabCount);	//construct the class, indicating that a default ID should be generated
	}
		
	/**Session constructor with a default data model to represent a given type with multiple selection.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of value held in the model.
	@param axis The axis along which the tabs are oriented.
	@exception NullPointerException if the given session, value class, and/or axis is <code>null</code>.
	*/
	public TabControl(final GuiseSession session, final Class<V> valueClass, final Flow axis)
	{
		this(session, null, valueClass, axis);	//construct the component, indicating that a default ID should be used
	}

	/**Session and maximum tab count constructor with a default data model to represent a given type with multiple selection.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of value held in the model.
	@param axis The axis along which the tabs are oriented.
	@param maxTabCount The requested number of visible rows, or -1 if no row count is specified.
	@exception NullPointerException if the given session, value class, and/or axis is <code>null</code>.
	*/
	public TabControl(final GuiseSession session, final Class<V> valueClass, final Flow axis, final int maxTabCount)
	{
		this(session, null, valueClass, axis, maxTabCount);	//construct the component, indicating the maximum tab count and that a default ID should be used
	}

	/**Session and selection strategy constructor with a default data model to represent a given type.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of value held in the model.
	@param selectionStrategy The strategy for selecting values in the model.
	@param axis The axis along which the tabs are oriented.
	@exception NullPointerException if the given session, value class, selection strategy, and/or axis is <code>null</code>.
	*/
	public TabControl(final GuiseSession session, final Class<V> valueClass, final ListSelectionPolicy<V> selectionStrategy, final Flow axis)
	{
		this(session, null, valueClass, selectionStrategy, axis);	//construct the component, indicating that a default ID should be used
	}

	/**Session, selection strategy, and maximum tab count constructor with a default data model to represent a given type.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of value held in the model.
	@param selectionStrategy The strategy for selecting values in the model.
	@param axis The axis along which the tabs are oriented.
	@param maxTabCount The requested number of visible rows, or -1 if no row count is specified.
	@exception NullPointerException if the given session, value class, selection strategy, and/or axis is <code>null</code>.
	*/
	public TabControl(final GuiseSession session, final Class<V> valueClass, final ListSelectionPolicy<V> selectionStrategy, final Flow axis, final int maxTabCount)
	{
		this(session, null, valueClass, selectionStrategy, axis, maxTabCount);	//construct the component, indicating the maximum tab count and that a default ID should be used
	}

	/**Session and ID constructor with a default data model to represent a given type with multiple selection.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param valueClass The class indicating the type of value held in the model.
	@param axis The axis along which the tabs are oriented.
	@exception NullPointerException if the given session, value class, and/or axis is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public TabControl(final GuiseSession session, final String id, final Class<V> valueClass, final Flow axis)
	{
		this(session, id, new DefaultListSelectModel<V>(session, valueClass), axis);	//construct the class with a default model
	}

	/**Session, ID, and maximum tab count constructor with a default data model to represent a given type with multiple selection.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param valueClass The class indicating the type of value held in the model.
	@param axis The axis along which the tabs are oriented.
	@param maxTabCount The requested number of visible rows, or -1 if no row count is specified.
	@exception NullPointerException if the given session, value class, and/or axis is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public TabControl(final GuiseSession session, final String id, final Class<V> valueClass, final Flow axis, final int maxTabCount)
	{
		this(session, id, new DefaultListSelectModel<V>(session, valueClass), axis, maxTabCount);	//construct the class with a default model and the maximum tab count
	}

	/**Session and ID constructor with a default data model to represent a given type.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param valueClass The class indicating the type of value held in the model.
	@param selectionStrategy The strategy for selecting values in the model.
	@param axis The axis along which the tabs are oriented.
	@exception NullPointerException if the given session, value class, selection strategy, and/or axis is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public TabControl(final GuiseSession session, final String id, final Class<V> valueClass, final ListSelectionPolicy<V> selectionStrategy, final Flow axis)
	{
		this(session, id, new DefaultListSelectModel<V>(session, valueClass, selectionStrategy), axis);	//construct the class with a default model
	}

	/**Session, ID, and maximum tab count constructor with a default data model to represent a given type.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param valueClass The class indicating the type of value held in the model.
	@param selectionStrategy The strategy for selecting values in the model.
	@param axis The axis along which the tabs are oriented.
	@param maxTabCount The requested number of visible tabs, or -1 if no maximum tab count is specified.
	@exception NullPointerException if the given session, value class, selection strategy, and/or axis is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public TabControl(final GuiseSession session, final String id, final Class<V> valueClass, final ListSelectionPolicy<V> selectionStrategy, final Flow axis, final int maxTabCount)
	{
		this(session, id, new DefaultListSelectModel<V>(session, valueClass, selectionStrategy), axis, maxTabCount);	//construct the class with a default model and the maximum tab count
	}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param axis The axis along which the tabs are oriented.
	@exception NullPointerException if the given session, model and/or axis is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public TabControl(final GuiseSession session, final String id, final ListSelectModel<V> model, final Flow axis)
	{
		this(session, id, model, axis, -1);	//construct the class with no maximum tab count
	}

	/**Session, ID, model, and maximumn tab count constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param axis The axis along which the tabs are oriented.
	@param maxTabCount The requested number of visible tabs, or -1 if no maximum tab count is specified.
	@exception NullPointerException if the given session, model, and/or axis is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public TabControl(final GuiseSession session, final String id, final ListSelectModel<V> model, final Flow axis, final int maxTabCount)
	{
		this(session, id, model, new DefaultValueRepresentationStrategy<V>(session, AbstractStringLiteralConverter.getInstance(session, model.getValueClass())), axis, maxTabCount);	//construct the class with a default representation strategy
	}

	/**Session, ID, model, and value representation strategy constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	@param axis The axis along which the tabs are oriented.
	@exception NullPointerException if the given session, model, value representation strategy, and/or axis is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public TabControl(final GuiseSession session, final String id, final ListSelectModel<V> model, final ValueRepresentationStrategy<V> valueRepresentationStrategy, final Flow axis)
	{
		this(session, id, model, valueRepresentationStrategy, axis, -1);	//construct the class with no maximum tab count
	}

	/**Session, ID, model, value representation strategy, and maximum tab count constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	@param axis The axis along which the tabs are oriented.
	@param maxTabCount The requested number of visible tabs, or -1 if no maximum tab count is specified.
	@exception NullPointerException if the given session, model, value representation strategy, and/or axis is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public TabControl(final GuiseSession session, final String id, final ListSelectModel<V> model, final ValueRepresentationStrategy<V> valueRepresentationStrategy, final Flow axis, final int maxTabCount)
	{
		super(session, id, model, valueRepresentationStrategy);	//construct the parent class
		this.axis=checkNull(axis, "Flow axis cannot be null.");
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

}
