package com.javaguise.component;

import com.javaguise.model.*;
import com.javaguise.session.GuiseSession;
import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.checkNull;

/**Control to allow selection of one or more values from a list.
@param <V> The type of values to select.
@author Garret Wilson
*/
public class ListControl<V> extends AbstractListSelectControl<V, ListSelectModel<V>, ListControl<V>>
{

	/**The row count bound property.*/
	public final static String ROW_COUNT_PROPERTY=getPropertyName(ListControl.class, "rowCount");

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
	public ListControl(final GuiseSession session, final Class<V> valueClass, final ListSelectionStrategy<V> selectionStrategy)
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
	public ListControl(final GuiseSession session, final Class<V> valueClass, final ListSelectionStrategy<V> selectionStrategy, final int rowCount)
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
	public ListControl(final GuiseSession session, final String id, final Class<V> valueClass, final ListSelectionStrategy<V> selectionStrategy)
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
	public ListControl(final GuiseSession session, final String id, final Class<V> valueClass, final ListSelectionStrategy<V> selectionStrategy, final int rowCount)
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
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public ListControl(final GuiseSession session, final String id, final ListSelectModel<V> model, final int rowCount)
	{
		this(session, id, model, new DefaultValueRepresentationStrategy<V>(session), rowCount);	//construct the class with a default representation strategy
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

	/**A default list select value representation strategy.
	A label component will be generated containing the default string representation of a value.
	The label's ID will be generated by appending the hexadecimal representation of the object's hash code to the word "hash".
	@param <VV> The type of value the strategy is to represent.
	@see Label
	@see Object#toString() 
	@see Object#hashCode() 
	@author Garret Wilson
	*/
	public static class DefaultValueRepresentationStrategy<VV> implements ValueRepresentationStrategy<VV>
	{

		/**The Guise session that owns this representation strategy.*/
		private final GuiseSession session;

			/**@return The Guise session that owns this representation strategy.*/
			public GuiseSession getSession() {return session;}

		/**Session constructor.
		@param session The Guise session that owns this representation strategy.
		@exception NullPointerException if the given session is <code>null</code>.
		*/
		public DefaultValueRepresentationStrategy(final GuiseSession session)
		{
			this.session=checkNull(session, "Session cannot be null");	//save the session
		}

		/**Creates a component for the given list value.
		This implementation returns a label with string value of the given value using the object's <code>toString()</code> method.
		The label's ID is set to the hexadecimal representation of the object's hash code appended to the word "hash".
		@param model The model containing the value.
		@param value The value for which a component should be created.
		@param index The index of the value within the list.
		@param selected <code>true</code> if the value is selected.
		@param focused <code>true</code> if the value has the focus.
		@return A new component to represent the given value, or <code>null</code> if the provided value is <code>null</code>.
		*/
		public Label createComponent(final ListSelectModel<VV> model, final VV value, final int index, final boolean selected, final boolean focused)
		{
			return value!=null	//if there is a value
					? new Label(getSession(), getID(value), new DefaultLabelModel(getSession(), value.toString()))	//generate a label containing the value's string value
					: null;	//otherwise return null
		}

		/**Determines an identifier for the given object.
		This implementation returns the hexadecimal representation of the object's hash code appended to the word "hash".
		@param value The value for which an identifier should be returned.
		@return A string identifying the value, or <code>null</code> if the provided value is <code>null</code>.
		@see Component#getID()
		*/
		public String getID(final VV value)
		{
			return value!=null ? "hash"+Integer.toHexString(value.hashCode()) : null;	//if a value is given return the word "hash" followed by a hexadecimal representation of the value's hash code
		}
	}

}
