package com.guiseframework.component;

import com.guiseframework.GuiseSession;
import com.guiseframework.converter.AbstractStringLiteralConverter;
import com.guiseframework.model.*;

import static com.garretwilson.lang.ClassUtilities.*;

/**Control to allow selection of one or more values from a list.
@param <V> The type of values to select.
@author Garret Wilson
*/
public class ListControl<V> extends AbstractListSelectControl<V, ListControl<V>> implements ValueControl<V, ListControl<V>>
{

	/**The row count bound property.*/
	public final static String ROW_COUNT_PROPERTY=getPropertyName(ListControl.class, "rowCount");

	/**Whether the value is editable and the control will allow the the user to change the value.*/
	private boolean editable=true;	//TODO fix or del if not needed

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

	/**The estimated number of rows requested to be visible, or -1 if no row count is specified.*/
	private int rowCount;

		/**@return The estimated number of rows requested to be visible, or -1 if no row count is specified.*/
		public int getRowCount() {return rowCount;}

		/**Sets the estimated number of rows requested to be visible.
		This is a bound property of type <code>Integer</code>.
		@param newRowCount The new requested number of visible rows, or -1 if no row count is specified.
		@see #ROW_COUNT_PROPERTY
		*/
		public void setRowCount(final int newRowCount)
		{
			if(rowCount!=newRowCount)	//if the value is really changing
			{
				final int oldRowCount=rowCount;	//get the old value
				rowCount=newRowCount;	//actually change the value
				firePropertyChange(ROW_COUNT_PROPERTY, new Integer(oldRowCount), new Integer(newRowCount));	//indicate that the value changed
			}			
		}

	/**Session and model constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	*/
	public ListControl(final GuiseSession session, final ListSelectModel<V> model)
	{
		this(session, null, model);	//construct the class, indicating that a default ID should be generated
	}

	/**Session, model, and row count constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	*/
	public ListControl(final GuiseSession session, final ListSelectModel<V> model, final int rowCount)
	{
		this(session, null, model, rowCount);	//construct the class, indicating that a default ID should be generated
	}

	/**Session, model, and value representation strategy constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	@exception NullPointerException if the given session, model, and/or value representation strategy is <code>null</code>.
	*/
	public ListControl(final GuiseSession session, final ListSelectModel<V> model, final ValueRepresentationStrategy<V> valueRepresentationStrategy)
	{
		this(session, null, model, valueRepresentationStrategy);	//construct the class, indicating that a default ID should be generated
	}

	/**Session, model, value representation strategy, and row count constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	@param rowCount The requested number of visible rows, or -1 if no row count is specified.
	@exception NullPointerException if the given session, model, and/or value representation strategy is <code>null</code>.
	*/
	public ListControl(final GuiseSession session, final ListSelectModel<V> model, final ValueRepresentationStrategy<V> valueRepresentationStrategy, final int rowCount)
	{
		this(session, null, model, valueRepresentationStrategy, rowCount);	//construct the class, indicating that a default ID should be generated
	}
		
	/**Session constructor with a default data model to represent a given type with multiple selection.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of value held in the model.
	@exception NullPointerException if the given session and/or value class is <code>null</code>.
	*/
	public ListControl(final GuiseSession session, final Class<V> valueClass)
	{
		this(session, null, valueClass);	//construct the component, indicating that a default ID should be used
	}

	/**Session and row count constructor with a default data model to represent a given type with multiple selection.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of value held in the model.
	@param rowCount The requested number of visible rows, or -1 if no row count is specified.
	@exception NullPointerException if the given session and/or value class is <code>null</code>.
	*/
	public ListControl(final GuiseSession session, final Class<V> valueClass, final int rowCount)
	{
		this(session, null, valueClass, rowCount);	//construct the component, indicating the row count and that a default ID should be used
	}

	/**Session and selection strategy constructor with a default data model to represent a given type.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of value held in the model.
	@param selectionStrategy The strategy for selecting values in the model.
	@exception NullPointerException if the given session, value class, and/or selection strategy is <code>null</code>.
	*/
	public ListControl(final GuiseSession session, final Class<V> valueClass, final ListSelectionPolicy<V> selectionStrategy)
	{
		this(session, null, valueClass, selectionStrategy);	//construct the component, indicating that a default ID should be used
	}

	/**Session, selection strategy, and row count constructor with a default data model to represent a given type.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of value held in the model.
	@param selectionStrategy The strategy for selecting values in the model.
	@param rowCount The requested number of visible rows, or -1 if no row count is specified.
	@exception NullPointerException if the given session, value class, and/or selection strategy is <code>null</code>.
	*/
	public ListControl(final GuiseSession session, final Class<V> valueClass, final ListSelectionPolicy<V> selectionStrategy, final int rowCount)
	{
		this(session, null, valueClass, selectionStrategy, rowCount);	//construct the component, indicating the row count and that a default ID should be used
	}

	/**Session and ID constructor with a default data model to represent a given type with multiple selection.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param valueClass The class indicating the type of value held in the model.
	@exception NullPointerException if the given session and/or value class is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public ListControl(final GuiseSession session, final String id, final Class<V> valueClass)
	{
		this(session, id, new DefaultListSelectModel<V>(session, valueClass));	//construct the class with a default model
	}

	/**Session, ID, and row count constructor with a default data model to represent a given type with multiple selection.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param valueClass The class indicating the type of value held in the model.
	@param rowCount The requested number of visible rows, or -1 if no row count is specified.
	@exception NullPointerException if the given session and/or value class is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public ListControl(final GuiseSession session, final String id, final Class<V> valueClass, final int rowCount)
	{
		this(session, id, new DefaultListSelectModel<V>(session, valueClass), rowCount);	//construct the class with a default model and the row count
	}

	/**Session and ID constructor with a default data model to represent a given type.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param valueClass The class indicating the type of value held in the model.
	@param selectionStrategy The strategy for selecting values in the model.
	@exception NullPointerException if the given session, value class, and/or selection strategy is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public ListControl(final GuiseSession session, final String id, final Class<V> valueClass, final ListSelectionPolicy<V> selectionStrategy)
	{
		this(session, id, new DefaultListSelectModel<V>(session, valueClass, selectionStrategy));	//construct the class with a default model
	}

	/**Session, ID, and row count constructor with a default data model to represent a given type.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param valueClass The class indicating the type of value held in the model.
	@param selectionStrategy The strategy for selecting values in the model.
	@param rowCount The requested number of visible rows, or -1 if no row count is specified.
	@exception NullPointerException if the given session, value class, and/or selection strategy is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public ListControl(final GuiseSession session, final String id, final Class<V> valueClass, final ListSelectionPolicy<V> selectionStrategy, final int rowCount)
	{
		this(session, id, new DefaultListSelectModel<V>(session, valueClass, selectionStrategy), rowCount);	//construct the class with a default model and the row count
	}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public ListControl(final GuiseSession session, final String id, final ListSelectModel<V> model)
	{
		this(session, id, model, -1);	//construct the class with no row count
	}

	/**Session, ID, model, and row count constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param rowCount The requested number of visible rows, or -1 if no row count is specified.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public ListControl(final GuiseSession session, final String id, final ListSelectModel<V> model, final int rowCount)
	{
		this(session, id, model, new DefaultValueRepresentationStrategy<V>(session, AbstractStringLiteralConverter.getInstance(session, model.getValueClass())), rowCount);	//construct the class with a default representation strategy
	}

	/**Session, ID, model, and value representation strategy constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	@exception NullPointerException if the given session, model, and/or value representation strategy is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public ListControl(final GuiseSession session, final String id, final ListSelectModel<V> model, final ValueRepresentationStrategy<V> valueRepresentationStrategy)
	{
		this(session, id, model, valueRepresentationStrategy, -1);	//construct the class with no row count
	}

	/**Session, ID, model, value representation strategy, and row count constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	@param rowCount The requested number of visible rows, or -1 if no row count is specified.
	@exception NullPointerException if the given session, model, and/or value representation strategy is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public ListControl(final GuiseSession session, final String id, final ListSelectModel<V> model, final ValueRepresentationStrategy<V> valueRepresentationStrategy, final int rowCount)
	{
		super(session, id, model, valueRepresentationStrategy);	//construct the parent class
		this.rowCount=rowCount;	//save the row count
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
