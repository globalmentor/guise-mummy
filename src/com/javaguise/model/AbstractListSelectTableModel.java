package com.javaguise.model;

import java.util.*;

import static java.util.Collections.*;

import java.util.concurrent.CopyOnWriteArrayList;

import com.garretwilson.util.CollectionUtilities;
import com.garretwilson.util.SynchronizedListDecorator;
import com.javaguise.GuiseSession;

/**An abstract implementation of a table model representing selectable list values.
The model is thread-safe, synchronized on itself. Any iteration over values should include synchronization on the instance of this class. 
@param <V> The type of values representing table rows.
@author Garret Wilson
*/
public abstract class AbstractListSelectTableModel<V> extends DefaultListSelectModel<V> implements TableModel
{

	/**Whether the table is editable and the cells will allow the the user to change their values, if their respective columns are designated as editable as well.*/
	private boolean editable=true;

		/**@return Whether the table is editable and the cells will allow the the user to change their values, if their respective columns are designated as editable as well.*/
		public boolean isEditable() {return editable;}

		/**Sets whether the table is editable and the cells will allow the the user to change their values, if their respective columns are designated as editable as well.
		This is a bound property of type <code>Boolean</code>.
		@param newEditable <code>true</code> if the cells should allow the user to change their values if their respective columns are also designated as editable.
		@see TableModel#EDITABLE_PROPERTY
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

	/**The list of table column models in logical order.*/
	private final List<TableColumnModel<?>> logicalTableColumnModels=new CopyOnWriteArrayList<TableColumnModel<?>>();

		/**Determines the logical index of the given table column.
		@param column One of the table columns.
		@return The zero-based logical index of the column within the table, or -1 if the column is not one of the model's columns.
		*/
		public int getColumnIndex(final TableColumnModel<?> column) {return logicalTableColumnModels.indexOf(column);}
	
	/**The list of table column models.*/
	private final List<TableColumnModel<?>> tableColumnModels=new CopyOnWriteArrayList<TableColumnModel<?>>();

	/**@return A read-only list of table columns in physical order.*/ 
	public List<TableColumnModel<?>> getColumns() {return unmodifiableList(tableColumnModels);}

	/**@return The number of rows in this table.*/
	public int getRowCount() {return size();}

	/**@return The number of columns in this table.*/
	public int getColumnCount() {return tableColumnModels.size();}

	/**The list of value model arrays for each row.*/
//TODO fix	private final List<List<ValueModel<V>>> valueModelLists;
	private final List<ValueModel<Object>[]> valueModelRowArrays;

	/**Constructs a list select table model indicating the type of values it can hold, using a default multiple selection strategy.
	@param session The Guise session that owns this model.
	@param valueClass The class indicating the type of values held in the model.
	@param columns The models representing the table columns.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	*/
	public AbstractListSelectTableModel(final GuiseSession session, final Class<V> valueClass, final TableColumnModel<?>... columns)
	{
		this(session, valueClass, new MultipleListSelectionPolicy<V>(), columns);	//construct the class with a multiple selection strategy
	}

	/**Constructs a list select table model indicating the type of values it can hold.
	The selection strategy is not added as a listener to this model but is rather notified manually so that the event won't be delayed and/or sent out of order
	@param session The Guise session that owns this model.
	@param valueClass The class indicating the type of values held in the model.
	@param listSelectionStrategy The strategy for selecting values in the model.
	@param columns The models representing the table columns.
	@exception NullPointerException if the given session, class object, and/or selection strategy is <code>null</code>.
	*/
	public AbstractListSelectTableModel(final GuiseSession session, final Class<V> valueClass, final ListSelectionPolicy<V> listSelectionStrategy, final TableColumnModel<?>... columns)
	{
		super(session, valueClass, listSelectionStrategy);	//construct the parent class
		CollectionUtilities.addAll(logicalTableColumnModels, columns);	//add all the columns to our logical list of table columns
		CollectionUtilities.addAll(tableColumnModels, columns);	//add all the columns to our list of table columns
		valueModelRowArrays=new SynchronizedListDecorator<ValueModel<Object>[]>(new ArrayList<ValueModel<Object>[]>(), this);	//create a list of value model arrays, synchronizing all access on this object
	}

	/**Returns the model representing the cell value at the given row and column.
	@param rowIndex The zero-based row index.
	@param colIndex The zero-based column index.
	@return The value model representing the value in the cell at the given row and column.
	@exception IndexOutOfBoundsException if the given row index and/or column index represents an invalid location for the table.
	*/
	public synchronized ValueModel<Object> getCellValueModel(final int rowIndex, final int colIndex)
	{
		return valueModelRowArrays.get(rowIndex)[colIndex];	//return the value model in the given row for the given column
	}

	/**Returns the cell value for the given cell.
	This method delegates to {@link #getCellValue(int, TableColumnModel)}.
	@param <C> The type of cell value.
	@param cell The cell containing the row index and column information.
	@return The value in the cell at the given row and column, or <code>null</code> if there is no value in that cell.
	@exception IndexOutOfBoundsException if the given row index represents an invalid location for the table.
	@exception IllegalArgumentException if the given column is not one of this table's columns.
	*/
	public <C> C getCellValue(final Cell<C> cell)
	{
		return getCellValue(cell.getRowIndex(), cell.getColumn());	//return the cell value for the cell row index and column
	}

	/**Returns the cell value at the given row and column.
	@param <C> The type of cell values in the given column.
	@param rowIndex The zero-based row index.
	@param column The column for which a value should be returned.
	@return The value in the cell at the given row and column, or <code>null</code> if there is no value in that cell.
	@exception IndexOutOfBoundsException if the given row index represents an invalid location for the table.
	@exception IllegalArgumentException if the given column is not one of this table's columns.
	*/
	public <C> C getCellValue(final int rowIndex, final TableColumnModel<C> column)
	{
		return getCellValue(get(rowIndex), rowIndex, column);	//retrieve the value in the given row and ask for the corresponding cell value 
	}

	/**Sets the cell value for the given cell.
	This method delegates to {@link #setCellValue(int, TableColumnModel, C)}.
	@param <C> The type of cell value.
	@param cell The cell containing the row index and column information.
	@param newCellValue The value to place in the cell at the given row and column, or <code>null</code> if there should be no value in that cell.
	@exception IndexOutOfBoundsException if the given row index represents an invalid location for the table.
	@exception IllegalArgumentException if the given column is not one of this table's columns.
	*/
	public <C> void setCellValue(final Cell<C> cell, final C newCellValue)
	{
		setCellValue(cell.getRowIndex(), cell.getColumn(), newCellValue);	//set the cell value for the cell row index and column
	}

	/**Sets the cell value at the given row and column.
	@param <C> The type of cell values in the given column.
	@param rowIndex The zero-based row index.
	@param column The column for which a value should be returned.
	@param newCellValue The value to place in the cell at the given row and column, or <code>null</code> if there should be no value in that cell.
	@exception IndexOutOfBoundsException if the given row index represents an invalid location for the table.
	@exception IllegalArgumentException if the given column is not one of this table's columns.
	*/
	public <C> void setCellValue(final int rowIndex, final TableColumnModel<C> column, final C newCellValue)
	{
		setCellValue(get(rowIndex), rowIndex, column, newCellValue);	//retrieve the value in the given row and set the corresponding cell value 
	}

	/**Returns the value's property for the given column.
	@param <C> The type of cell values in the given column.
	@param value The value in this list select model.
	@param rowIndex The zero-based row index of the value.
	@param column The column for which a value should be returned.
	@return The value in the cell at the given row and column, or <code>null</code> if there is no value in that cell.
	@exception IndexOutOfBoundsException if the given row index represents an invalid location for the table.
	@exception IllegalArgumentException if the given column is not one of this table's columns.
	*/
	protected abstract <C> C getCellValue(final V value, final int rowIndex, final TableColumnModel<C> column);

	/**Sets the value's property for the given column.
	@param <C> The type of cell values in the given column.
	@param value The value in this list select model.
	@param rowIndex The zero-based row index of the value.
	@param column The column for which a value should be returned.
	@param newCellValue The value to place in the cell at the given row and column, or <code>null</code> if there should be no value in that cell.
	@exception IndexOutOfBoundsException if the given row index represents an invalid location for the table.
	@exception IllegalArgumentException if the given column is not one of this table's columns.
	*/
	protected abstract <C> void setCellValue(final V value, final int rowIndex, final TableColumnModel<C> column, final C newCellValue);

}
