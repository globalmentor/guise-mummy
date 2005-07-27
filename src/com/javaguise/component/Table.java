package com.javaguise.component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.garretwilson.lang.ObjectUtilities;
import com.javaguise.model.*;
import com.javaguise.session.GuiseSession;
import com.javaguise.validator.ValidationException;
import com.javaguise.validator.Validator;

/**A table component.
The generic constructors are to be preferred. They create a genericized model and ensure that all the column types extend the table model cell types,
but because of erasure the models created are otherwise identical.
@author Garret Wilson
*/
public class Table extends AbstractModelComponent<TableModel, Table>
{

	/**@return An iterator to child components.*/
//TODO del	public Iterator<Component<?>> iterator() {return cellComponentMap.values().iterator();}

	/**@return Whether this component has children. This implementation delegates to the cell component map.*/
//TODO del	public boolean hasChildren() {return !cellComponentMap.isEmpty();}

	/**The map of cell representation strategies for columns.*/
	private final Map<TableColumnModel<?>, CellRepresentationStrategy<?>> columnCellRepresentationStrategyMap=new ConcurrentHashMap<TableColumnModel<?>, CellRepresentationStrategy<?>>();

	/**Installs the given cell representation strategy to produce representation components for the given column.
	@param <V> The type of value the column represents.
	@param column The column with which the strategy should be associated.
	@param cellRepresentationStrategy The strategy for generating components to represent values in the given column.
	@return The representation strategy previously associated with the given column.
	*/	
	@SuppressWarnings("unchecked")	//we check the generic types before putting them in the map, so it's fine to cast the retrieved values
	public <V> CellRepresentationStrategy<? super V> setCellRepresentationStrategy(final TableColumnModel<V> column, CellRepresentationStrategy<V> cellRepresentationStrategy)
	{
		return (CellRepresentationStrategy<? super V>)columnCellRepresentationStrategyMap.put(column, cellRepresentationStrategy);	//associate the strategy with the column in the map
	}

	/**Returns the given cell representation strategy assigned to produce representation components for the given column.
	@param <V> The type of value the column represents.
	@param column The column with which the strategy should be associated.
	@return The strategy for generating components to represent values in the given column.
	*/	
	@SuppressWarnings("unchecked")	//we check the generic types before putting them in the map, so it's fine to cast the retrieved values
	public <V> CellRepresentationStrategy<? super V> getCellRepresentationStrategy(final TableColumnModel<V> column)
	{
		return (CellRepresentationStrategy<? super V>)columnCellRepresentationStrategyMap.get(column);	//return the strategy linked to the column in the map
	}
	
	/**Session, value class, and column names constructor with a default ID and default data model.
	@param <C> The type of values in all the cells in the table.
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
	@param session The Guise session that owns this component.
	@param columns The models representing the table columns.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	@exception IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	*/
	public Table(final GuiseSession<?> session, final TableColumnModel<?>... columns)
	{
		this(session, (String)null, columns);	//construct the class, indicating that a default ID should be generated
	}

	/**Session, value class, table data, and column names constructor with a default ID and default data model.
	@param <C> The type of values in all the cells in the table.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of values held in the model.
	@param rowValues The two-dimensional list of values, where the first index represents the row and the second represents the column, or <code>null</code> if no default values should be given.
	@param columnNames The names to serve as label headers for the columns.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	@exception IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	@exception ClassCastException if one of the values in a row is not compatible with the type of its column.
	*/
	public <C> Table(final GuiseSession<?> session, final Class<C> valueClass, final C[][] rowValues, final String... columnNames)
	{
		this(session, null, valueClass, rowValues, columnNames);	//construct the class, indicating that a default ID should be generated
	}

	/**Session, value class, table data, and columns constructor with a default ID and default data model.
	@param session The Guise session that owns this component.
	@param rowValues The two-dimensional list of values, where the first index represents the row and the second represents the column, or <code>null</code> if no default values should be given.
	@param columns The models representing the table columns.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	@exception IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	@exception ClassCastException if one of the values in a row is not compatible with the type of its column.
	*/
	public Table(final GuiseSession<?> session, final Object[][] rowValues, final TableColumnModel<?>... columns)
	{
		this(session, null, rowValues, columns);	//construct the class, indicating that a default ID should be generated
	}

	/**Session, value class, and model constructor with a default ID.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	*/
	public Table(final GuiseSession<?> session, final TableModel model)
	{
		this(session, null, model);	//construct the class, indicating that a default ID should be generated
	}
	
	/**Session, ID, value class, and column names constructor with a default data model.
	@param <C> The type of values in all the cells in the table.
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
		this(session, id, new DefaultTableModel(session, valueClass, null, columnNames));	//construct the class with no default data
	}

	/**Session, ID, value class, and columns constructor with a default data model.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param columns The models representing the table columns.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	@exception IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	*/
	public Table(final GuiseSession<?> session, final String id, final TableColumnModel<?>... columns)
	{
		this(session, id, new DefaultTableModel(session, null, columns));	//construct the class with no default data
	}

	/**Session, ID, value class, table data, and column names constructor with a default data model.
	@param <C> The type of values in all the cells in the table.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param valueClass The class indicating the type of values held in the model.
	@param rowValues The two-dimensional list of values, where the first index represents the row and the second represents the column, or <code>null</code> if no default values should be given.
	@param columnNames The names to serve as label headers for the columns.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	@exception IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	@exception ClassCastException if one of the values in a row is not compatible with the type of its column.
	*/
	public <C> Table(final GuiseSession<?> session, final String id, final Class<C> valueClass, final C[][] rowValues, final String... columnNames)
	{
		this(session, id, new DefaultTableModel(session, valueClass, rowValues, columnNames));	//construct the class with a default model
	}

	/**Session, ID, value class, table data, and columns constructor with a default data model.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param rowValues The two-dimensional list of values, where the first index represents the row and the second represents the column, or <code>null</code> if no default values should be given.
	@param columns The models representing the table columns.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	@exception IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	@exception ClassCastException if one of the values in a row is not compatible with the type of its column.
	*/
	public Table(final GuiseSession<?> session, final String id, final Object[][] rowValues, final TableColumnModel<?>... columns)
	{
		this(session, id, new DefaultTableModel(session, rowValues, columns));	//construct the class with a default model
	}

	/**Session, ID, value class, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Table(final GuiseSession<?> session, final String id, final TableModel model)
	{
		super(session, id, model);	//construct the parent class
		for(final TableColumnModel<?> column:model.getColumns())	//install a default cell representation strategy for each column
		{
			installDefaultCellRepresentationStrategy(column);	//create and install a default representation strategy for this column
		}
	}

	/**Installs a default cell representation strategy for the given column.
	@param <T> The type of value contained in the column.
	@param column The table column for which a default cell representation strategy should be installed.
	*/
	private <T> void installDefaultCellRepresentationStrategy(final TableColumnModel<T> column)
	{
		setCellRepresentationStrategy(column, new DefaultCellRepresentationStrategy<T>(getSession()));	//create a default cell representation strategy
	}

	/**A strategy for generating components to represent table cell model values.
	The component ID should reflect a unique identifier for the cell.
	@param <V> The type of value the strategy is to represent.
	@author Garret Wilson
	*/
	public interface CellRepresentationStrategy<V>
	{
		/**Creates a component to represent the given cell.
		@param <C> The type of value contained in the column.
		@param model The model containing the value.
		@param rowIndex The zero-based row index of the value.
		@param column The column of the value.
		@param editable Whether values in this column are editable.
		@param selected <code>true</code> if the value is selected.
		@param focused <code>true</code> if the value has the focus.
		@return A new component to represent the given value, or <code>null</code> if the provided value is <code>null</code>.
		*/
		public <C extends V> Component<?> createComponent(final TableModel model, final int rowIndex, final TableColumnModel<C> column, final boolean editable, final boolean selected, final boolean focused);
	}

	/**A default table cell representation strategy.
	A message component will be generated using the cell's value as its message.
	The message's ID will be in the form "cell-<var>rowIndex</var>-<var>columnIndex</var>.
	@param <V> The type of value the strategy is to represent.
	@see Message
	@see Object#toString() 
	@author Garret Wilson
	*/
	public static class DefaultCellRepresentationStrategy<V> implements CellRepresentationStrategy<V>
	{

		/**The Guise session that owns this representation strategy.*/
		private final GuiseSession<?> session;

			/**@return The Guise session that owns this representation strategy.*/
			public GuiseSession<?> getSession() {return session;}

		/**Session constructor.
		@param session The Guise session that owns this representation strategy.
		@exception NullPointerException if the given session is <code>null</code>.
		*/
		public DefaultCellRepresentationStrategy(final GuiseSession<?> session)
		{
			this.session=checkNull(session, "Session cannot be null");	//save the session
		}

		/**Creates a component for the given cell.
		This implementation returns a message with string value of the given value using the object's <code>toString()</code> method.
		The label's ID is set to the hexadecimal representation of the object's hash code appended to the word "hash".
		@param <C> The type of value contained in the column.
		@param model The model containing the value.
		@param rowIndex The zero-based row index of the value.
		@param column The column of the value.
		@param editable Whether values in this column are editable.
		@param selected <code>true</code> if the value is selected.
		@param focused <code>true</code> if the value has the focus.
		@return A new component to represent the given value, or <code>null</code> if the provided value is <code>null</code>.
		*/
		@SuppressWarnings("unchecked")	//we check the type of the column value class, so the casts are safe
		public <C extends V> Component<?> createComponent(final TableModel model, final int rowIndex, final TableColumnModel<C> column, final boolean editable, final boolean selected, final boolean focused)
		{
			final GuiseSession<?> session=getSession();	//get the session
			final TableModel.Cell<C> cell=new TableModel.Cell<C>(rowIndex, column);	//create a cell to represent the row and column
			final int columnIndex=model.getColumnIndex(column);	//get the logical index of the given column
			final String id=new StringBuilder("cell-").append(rowIndex).append('-').append(columnIndex).toString();	//generate an ID for this row and column
			if(editable)	//if the component should be editable
			{
				final ValueModel<C> valueModel=new DefaultCellValueModel<C>(session, model, cell);	//create a new value model for the cell
				final Class<C> valueClass=column.getValueClass();	//get the value class of the column
				if(Boolean.class.isAssignableFrom(valueClass))	//if the value class is subclass of Boolean
				{
					return new CheckControl(session, id, (ValueModel<Boolean>)(Object)valueModel);	//create a new check control for the Boolean value model TODO find out why JDK 1.5.0_03 requires the intermediate Object cast
				}
				else	//for all other values
				{
					return new TextControl<C>(session, id, valueModel);	//generate a message containing a text input control for the value model
				}
			}
			else	//if the component should not be editable, return a message control
			{
				return new Message(session, id, new DefaultCellMessageModel<C>(session, model, cell));	//create a message component containing a message model representing the value's string value				
			}
		}
	}

	/**A message model that returns a default representation of the cell in a message.
	@param <C> The type of value in the cell.
	@author Garret Wilson
	*/
	public static class DefaultCellMessageModel<C> extends DefaultMessageModel
	{
		/**The table model of the cell.*/
		private final TableModel model;

			/**@return The table model of the cell.*/
			protected TableModel getModel() {return model;}

		/**The cell being represented*/
		private TableModel.Cell<C> cell;

			/**@return The cell being represented*/
			protected TableModel.Cell<C> getCell() {return cell;}

		/**Constructs a default message model for a cell.
		@param session The Guise session that owns this model.
		@param model The table model of the cell.
		@param cell The cell being represented.
		@exception NullPointerException if the given session, table model and/or cell is <code>null</code>.
		*/
		public DefaultCellMessageModel(final GuiseSession<?> session, final TableModel model, final TableModel.Cell<C> cell)
		{
			super(session);	//construct the parent class
			this.model=checkNull(model, "Table model cannot be null");
			this.cell=checkNull(cell, "Cell cannot be null");
		}

		/**Determines the message text of the cell.
		This implementation returns a message with string value of the given value using the object's <code>toString()</code> method.
		@return The message text of the cell.
		*/
		public String getMessage()
		{
			final TableModel.Cell<C> cell=getCell();	//get our current cell
			final C value=getModel().getCellValue(cell.getRowIndex(), cell.getColumn());	//get the value from the table model
			return value!=null ? value.toString() : null;	//if there is a value, return value's string value
		}

		/**Sets the text of the message. This version throws an exception, as this model is read-only.
		@param newMessage The new text of the message.
		@exception UnsupportedOperationException because default cell message models are read-only.
		*/
		public void setMessage(final String newMessage)
		{
			throw new UnsupportedOperationException("Cell is read-only.");
		}

	}

	/**A value model that returns and updates a the value of the cell.
	@param <C> The type of value in the cell.
	@author Garret Wilson
	*/
	public static class DefaultCellValueModel<C> extends DefaultValueModel<C>
	{
		/**The table model of the cell.*/
		private final TableModel model;

			/**@return The table model of the cell.*/
			protected TableModel getModel() {return model;}

		/**The cell being represented*/
		private TableModel.Cell<C> cell;

			/**@return The cell being represented*/
			protected TableModel.Cell<C> getCell() {return cell;}

		/**Constructs a default value model for a cell.
		@param session The Guise session that owns this model.
		@param model The table model of the cell.
		@param cell The cell being represented.
		@exception NullPointerException if the given session, table model and/or cell is <code>null</code>.
		*/
		public DefaultCellValueModel(final GuiseSession<?> session, final TableModel model, final TableModel.Cell<C> cell)
		{
			super(session, checkNull(cell, "Cell cannot be null").getColumn().getValueClass());	//construct the parent class
			this.model=checkNull(model, "Table model cannot be null");
			this.cell=cell;
		}

		/**@return Whether the model's value is editable and the corresponding control will allow the the user to change the value.
		This version returns <code>true</code> if the model and column are both editable.*/
		public boolean isEditable() {return getModel().isEditable() && getCell().getColumn().isEditable();}

		/**Sets whether the model's value is editable and the corresponding control will allow the the user to change the value. This version throws an exception, as the editable status is read-only.
		@param newEditable <code>true</code> if the corresponding control should allow the user to change the value.
		*/
		public void setEditable(final boolean newEditable) {throw new UnsupportedOperationException("Editable is read-only.");}

		/**@return Whether the model is enabled and and the corresponding control can receive user input.
		This version returns <code>true</code> if the model and column are both enabled.*/
		public boolean isEnabled() {return getModel().isEnabled() && getCell().getColumn().isEnabled();}

		/**Sets whether the model is enabled and and the corresponding control can receive user input. This version throws an exception, as the enabled status is read-only.
		@param newEnabled <code>true</code> if the corresponding control should indicate and accept user input.
		*/
		public void setEnabled(final boolean newEnabled) {throw new UnsupportedOperationException("Enabled is read-only.");}

		/**@return The validator for this model, or <code>null</code> if no validator is installed.*/
		public Validator<C> getValidator() {return getCell().getColumn().getValidator();}	//return the validator from the column

		/**Sets the validator. This version throws an exception, as the validator is read-only.
		@param newValidator The validator for this model, or <code>null</code> if no validator should be used.
		*/
		public void setValidator(final Validator<C> newValidator) {throw new UnsupportedOperationException("Validator is read-only.");}

		/**@return The value from the table model cell, or <code>null</code> if there is no value in the cell.*/
		public C getValue() {return getModel().getCellValue(getCell());}	//return the value from the table model

		/**Sets the value in the cell.
		@param newValue The value of the cell.
		@exception ValidationException if the provided value is not valid.
		@see #getValidator()
		@see ValueModel#VALUE_PROPERTY
		*/
		public void setValue(final C newValue) throws ValidationException
		{
			final Validator<C> validator=getValidator();	//get the currently installed validator, if there is one
			if(validator!=null)	//if a validator is installed, always validate the value, even if it isn't changing, so that an initial value that may not be valid will throw an error when it's tried to be set to the same, but invalid, value
			{
				validator.validate(newValue);	//validate the new value, throwing an exception if anything is wrong
			}
			getModel().setCellValue(getCell(), newValue);	//set the value in the table model
		}

		/**Resets the value to a default value, which may be invalid according to any installed validators.
		No validation occurs.
		@see ValueModel#VALUE_PROPERTY
		*/
		public void resetValue()
		{
			getModel().setCellValue(getCell(), null);	//set a null value in the table model
		}

	}

}
