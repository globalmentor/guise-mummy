package com.javaguise.component;

import static com.garretwilson.lang.ObjectUtilities.checkNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.javaguise.component.ListSelectControl.ValueRepresentationStrategy;
import com.javaguise.model.DefaultLabelModel;
import com.javaguise.model.DefaultMessageModel;
import com.javaguise.model.DefaultTableModel;
import com.javaguise.model.LabelModel;
import com.javaguise.model.ListSelectModel;
import com.javaguise.model.TableColumnModel;
import com.javaguise.model.TableModel;
import com.javaguise.session.GuiseSession;

/**A table component.
The generic constructors are to be preferred. They create a genericized model and ensure that all the column types extend the table model cell types,
but because of erasure the models created are otherwise identical.
@author Garret Wilson
*/
public class Table extends AbstractModelComponent<TableModel<?>, Table>
{

	/**@return An iterator to child components.*/
//TODO del	public Iterator<Component<?>> iterator() {return cellComponentMap.values().iterator();}

	/**@return Whether this component has children. This implementation delegates to the cell component map.*/
//TODO del	public boolean hasChildren() {return !cellComponentMap.isEmpty();}

	/**The map of value representation strategies for columns.*/
	private final Map<TableColumnModel<?>, ValueRepresentationStrategy<?>> columnValueRepresentationStrategyMap=new ConcurrentHashMap<TableColumnModel<?>, ValueRepresentationStrategy<?>>();

	/**Installs the given value representation strategy to produce representation components for the given column.
	@param <V> The type of value the column represents.
	@param column The column with which the strategy should be associated.
	@param valueRepresentationStrategy The strategy for generating components to represent values in the given column.
	@return The representation strategy previously associated with the given column.
	*/	
	@SuppressWarnings("unchecked")	//we check the generic types before putting them in the map, so it's fine to cast the retrieved values
	public <V> ValueRepresentationStrategy<? super V> setValueRepresentationStrategy(final TableColumnModel<V> column, ValueRepresentationStrategy<V> valueRepresentationStrategy)
	{
		return (ValueRepresentationStrategy<? super V>)columnValueRepresentationStrategyMap.put(column, valueRepresentationStrategy);	//associate the strategy with the column in the map
	}

	/**Returns the given value representation strategy assigned to produce representation components for the given column.
	@param <V> The type of value the column represents.
	@param column The column with which the strategy should be associated.
	@return valueRepresentationStrategy The strategy for generating components to represent values in the given column.
	*/	
	@SuppressWarnings("unchecked")	//we check the generic types before putting them in the map, so it's fine to cast the retrieved values
	public <V> ValueRepresentationStrategy<? super V> getValueRepresentationStrategy(final TableColumnModel<V> column)
	{
		return (ValueRepresentationStrategy<? super V>)columnValueRepresentationStrategyMap.get(column);	//return the strategy linked to the column in the map
	}

	/**Session, value class, and column names constructor with a default ID and default data model.
	@param session The Guise session that owns this component.
	@param columnNames The names to serve as label headers for the columns.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	@exception IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	*/
	public <C> Table(final GuiseSession<?> session, final String... columnNames)
	{
		this(session, (String)null, columnNames);	//construct the class, indicating that a default ID should be generated
	}

	/**Session, value class, and columns constructor with a default ID and default data model.
	@param session The Guise session that owns this component.
	@param columns The models representing the table columns.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	@exception IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	*/
	public Table(final GuiseSession<?> session, final TableColumnModel<? extends Object>... columns)
	{
		this(session, (String)null, columns);	//construct the class, indicating that a default ID should be generated
	}

	/**Session, value class, table data, and column names constructor with a default ID and default data model.
	@param session The Guise session that owns this component.
	@param rowValues The two-dimensional list of values, where the first index represents the row and the second represents the column, or <code>null</code> if no default values should be given.
	@param columnNames The names to serve as label headers for the columns.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	@exception IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	*/
	public Table(final GuiseSession<?> session, final Object[][] rowValues, final String... columnNames)
	{
		this(session, (String)null, rowValues, columnNames);	//construct the class, indicating that a default ID should be generated
	}

	/**Session, value class, table data, and columns constructor with a default ID and default data model.
	@param session The Guise session that owns this component.
	@param rowValues The two-dimensional list of values, where the first index represents the row and the second represents the column, or <code>null</code> if no default values should be given.
	@param columns The models representing the table columns.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	@exception IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	*/
	public Table(final GuiseSession<?> session, final Object[][] rowValues, final TableColumnModel<? extends Object>... columns)
	{
		this(session, (String)null, rowValues, columns);	//construct the class, indicating that a default ID should be generated
	}
	
	/**Session, ID, value class, and column names constructor with a default data model.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param columnNames The names to serve as label headers for the columns.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	@exception IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	*/
	public <C> Table(final GuiseSession<?> session, final String id, final String... columnNames)
	{
		this(session, id, Object.class, columnNames);	//construct the class with object values
	}

	/**Session, ID, value class, and columns constructor with a default data model.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param columns The models representing the table columns.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	@exception IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	*/
	public Table(final GuiseSession<?> session, final String id, final TableColumnModel<? extends Object>... columns)
	{
		this(session, id, Object.class, columns);	//construct the class with object values
	}

	/**Session, ID, value class, table data, and column names constructor with a default data model.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param rowValues The two-dimensional list of values, where the first index represents the row and the second represents the column, or <code>null</code> if no default values should be given.
	@param columnNames The names to serve as label headers for the columns.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	@exception IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	*/
	public Table(final GuiseSession<?> session, final String id, final Object[][] rowValues, final String... columnNames)
	{
		this(session, id, Object.class, rowValues, columnNames);	//construct the class with object values
	}

	/**Session, ID, value class, table data, and columns constructor with a default data model.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param rowValues The two-dimensional list of values, where the first index represents the row and the second represents the column, or <code>null</code> if no default values should be given.
	@param columns The models representing the table columns.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	@exception IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	*/
	public Table(final GuiseSession<?> session, final String id, final Object[][] rowValues, final TableColumnModel<? extends Object>... columns)
	{
		this(session, id, Object.class, rowValues, columns);	//construct the class with object values
	}
	
	/**Session, value class, and column names constructor with a default ID and default data model.
	@param <C> The type of values contained in all the table's cells.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of values held in the model.
	@param columnNames The names to serve as label headers for the columns.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	@exception IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	*/
	public <C> Table(final GuiseSession<?> session, final Class<C> valueClass, final String... columnNames)
	{
		this(session, null, valueClass, columnNames);	//construct the class, indicating that a default ID should be generated
	}

	/**Session, value class, and columns constructor with a default ID and default data model.
	@param <C> The type of values contained in all the table's cells.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of values held in the model.
	@param columns The models representing the table columns.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	@exception IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	*/
	public <C> Table(final GuiseSession<?> session, final Class<C> valueClass, final TableColumnModel<? extends C>... columns)
	{
		this(session, null, valueClass, columns);	//construct the class, indicating that a default ID should be generated
	}

	/**Session, value class, table data, and column names constructor with a default ID and default data model.
	@param <C> The type of values contained in all the table's cells.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of values held in the model.
	@param rowValues The two-dimensional list of values, where the first index represents the row and the second represents the column, or <code>null</code> if no default values should be given.
	@param columnNames The names to serve as label headers for the columns.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	@exception IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	*/
	public <C> Table(final GuiseSession<?> session, final Class<C> valueClass, final C[][] rowValues, final String... columnNames)
	{
		this(session, null, valueClass, rowValues, columnNames);	//construct the class, indicating that a default ID should be generated
	}

	/**Session, value class, table data, and columns constructor with a default ID and default data model.
	@param <C> The type of values contained in all the table's cells.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of values held in the model.
	@param rowValues The two-dimensional list of values, where the first index represents the row and the second represents the column, or <code>null</code> if no default values should be given.
	@param columns The models representing the table columns.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	@exception IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	*/
	public <C> Table(final GuiseSession<?> session, final Class<C> valueClass, final C[][] rowValues, final TableColumnModel<? extends C>... columns)
	{
		this(session, null, valueClass, rowValues, columns);	//construct the class, indicating that a default ID should be generated
	}

	/**Session, value class, and model constructor with a default ID.
	@param <C> The type of values contained in all the table's cells.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	*/
	public <C> Table(final GuiseSession<?> session, final TableModel<C> model)
	{
		this(session, null, model);	//construct the class, indicating that a default ID should be generated
	}
	
	/**Session, ID, value class, and column names constructor with a default data model.
	@param <C> The type of values contained in all the table's cells.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param valueClass The class indicating the type of values held in the model.
	@param columnNames The names to serve as label headers for the columns.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	@exception IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	*/
	public <C> Table(final GuiseSession<?> session, final String id, final Class<C> valueClass, final String... columnNames)
	{
		this(session, id, new DefaultTableModel<C>(session, valueClass, null, columnNames));	//construct the class with no default data
	}

	/**Session, ID, value class, and columns constructor with a default data model.
	@param <C> The type of values contained in all the table's cells.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param valueClass The class indicating the type of values held in the model.
	@param columns The models representing the table columns.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	@exception IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	*/
	public <C> Table(final GuiseSession<?> session, final String id, final Class<C> valueClass, final TableColumnModel<? extends C>... columns)
	{
		this(session, id, new DefaultTableModel<C>(session, valueClass, null, columns));	//construct the class with no default data
	}

	/**Session, ID, value class, table data, and column names constructor with a default data model.
	@param <C> The type of values contained in all the table's cells.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param valueClass The class indicating the type of values held in the model.
	@param rowValues The two-dimensional list of values, where the first index represents the row and the second represents the column, or <code>null</code> if no default values should be given.
	@param columnNames The names to serve as label headers for the columns.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	@exception IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	*/
	public <C> Table(final GuiseSession<?> session, final String id, final Class<C> valueClass, final C[][] rowValues, final String... columnNames)
	{
		this(session, id, new DefaultTableModel<C>(session, valueClass, rowValues, columnNames));	//construct the class with a default model
	}

	/**Session, ID, value class, table data, and columns constructor with a default data model.
	@param <C> The type of values contained in all the table's cells.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param valueClass The class indicating the type of values held in the model.
	@param rowValues The two-dimensional list of values, where the first index represents the row and the second represents the column, or <code>null</code> if no default values should be given.
	@param columns The models representing the table columns.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	@exception IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	*/
	public <C> Table(final GuiseSession<?> session, final String id, final Class<C> valueClass, final C[][] rowValues, final TableColumnModel<? extends C>... columns)
	{
		this(session, id, new DefaultTableModel<C>(session, valueClass, rowValues, columns));	//construct the class with a default model
	}

	/**Session, ID, value class, and model constructor.
	@param <C> The type of values contained in all the table's cells.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public <C> Table(final GuiseSession<?> session, final String id, final TableModel<C> model)
	{
		super(session, id, model);	//construct the parent class
		for(final TableColumnModel<? extends C> column:model.getColumns())	//install a default value representation strategy for each column
		{
			installDefaultValueRepresentationStrategy(column);	//create and install a default representation strategy for this column
		}
	}

	/**Installs a default value representation strategy for the given column.
	@param <T> The type of value contained in teh column.
	@param column The table column for which a default value representation strategy should be installed.
	*/
	private <T> void installDefaultValueRepresentationStrategy(final TableColumnModel<T> column)
	{
		setValueRepresentationStrategy(column, new DefaultValueRepresentationStrategy<T>(getSession()));	//create a default value representation strategy
	}

	/**A strategy for generating components to represent table cell model values.
	The component ID should reflect a unique identifier of the value.
	@param <V> The type of value the strategy is to represent.
	@author Garret Wilson
	*/
	public interface ValueRepresentationStrategy<V>
	{
		/**Creates a component for the given cell value.
		@param <T> The type of value contained in the column.
		@param model The model containing the value.
		@param value The value for which a component should be created.
		@param rowIndex The zero-based row index of the value.
		@param column The column of the value.
		@param editable Whether values in this column are editable.
		@param selected <code>true</code> if the value is selected.
		@param focused <code>true</code> if the value has the focus.
		@return A new component to represent the given value, or <code>null</code> if the provided value is <code>null</code>.
		*/
		public <T extends V> Component<?> createComponent(final TableModel<? super T> model, final V value, final int rowIndex, final TableColumnModel<T> column, final boolean editable, final boolean selected, final boolean focused);
	}

	/**A default table cell value representation strategy.
	A message component will be generated containing the default string representation of a value.
	The message's ID will be generated by appending the hexadecimal representation of the object's hash code to the word "hash".
	@param <V> The type of value the strategy is to represent.
	@see Message
	@see Object#toString() 
	@see Object#hashCode() 
	@author Garret Wilson
	*/
	public static class DefaultValueRepresentationStrategy<V> implements ValueRepresentationStrategy<V>
	{

		/**The Guise session that owns this representation strategy.*/
		private final GuiseSession<?> session;

			/**@return The Guise session that owns this representation strategy.*/
			public GuiseSession<?> getSession() {return session;}

		/**Session constructor.
		@param session The Guise session that owns this representation strategy.
		@exception NullPointerException if the given session is <code>null</code>.
		*/
		public DefaultValueRepresentationStrategy(final GuiseSession<?> session)
		{
			this.session=checkNull(session, "Session cannot be null");	//save the session
		}

		/**Creates a component for the given cell value.
		This implementation returns a message with string value of the given value using the object's <code>toString()</code> method.
		The label's ID is set to the hexadecimal representation of the object's hash code appended to the word "hash".
		@param <T> The type of value contained in the column.
		@param model The model containing the value.
		@param value The value for which a component should be created.
		@param rowIndex The zero-based row index of the value.
		@param column The column of the value.
		@param editable Whether values in this column are editable.
		@param selected <code>true</code> if the value is selected.
		@param focused <code>true</code> if the value has the focus.
		@return A new component to represent the given value, or <code>null</code> if the provided value is <code>null</code>.
		*/
		public <T extends V> Message createComponent(final TableModel<? super T> model, final V value, final int rowIndex, final TableColumnModel<T> column, final boolean editable, final boolean selected, final boolean focused)
		{
			return value!=null	//if there is a value
					? new Message(getSession(), getID(value), new DefaultMessageModel(getSession(), value.toString()))	//generate a label containing the value's string value
					: null;	//otherwise return null
		}

		/**Determines an identier for the given object.
		This implementation returns the hexadecimal representation of the object's hash code appended to the word "hash".
		@param value The value for which an identifier should be returned.
		@return A string identifying the value, or <code>null</code> if the provided value is <code>null</code>.
		@see Component#getID()
		*/
		protected String getID(final V value)	//TODO del and incorporate into createComponent()
		{
			return value!=null ? "hash"+Integer.toHexString(value.hashCode()) : null;	//if a value is given return the word "hash" followed by a hexadecimal representation of the value's hash code
		}
	}

}
