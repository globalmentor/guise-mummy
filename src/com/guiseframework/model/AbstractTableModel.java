package com.guiseframework.model;

import java.util.*;
import static java.util.Collections.*;
import java.util.concurrent.CopyOnWriteArrayList;

import com.garretwilson.util.CollectionUtilities;

/**An abstract implementation of a table model.
The table model is editable by default.
@author Garret Wilson
*/
public abstract class AbstractTableModel extends AbstractModel implements TableModel
{

	/**The list of table column models.*/
	private final List<TableColumnModel<?>> tableColumnModels=new CopyOnWriteArrayList<TableColumnModel<?>>();

	/**@return A read-only list of table columns in physical order.*/ 
	public List<TableColumnModel<?>> getColumns() {return unmodifiableList(tableColumnModels);}

	/**The list of table column models in logical order.*/
	private final List<TableColumnModel<?>> logicalTableColumnModels=new CopyOnWriteArrayList<TableColumnModel<?>>();

		/**Determines the logical index of the given table column.
		@param column One of the table columns.
		@return The zero-based logical index of the column within the table, or -1 if the column is not one of the model's columns.
		*/
		public int getColumnIndex(final TableColumnModel<?> column) {return logicalTableColumnModels.indexOf(column);}
	
	/**@return The number of columns in this table.*/
	public int getColumnCount() {return logicalTableColumnModels.size();}

	/**Adds a column to the table.
	@param column The column to add.
	*/
	protected void addColumn(final TableColumnModel<?> column)	//TODO synchronize access
	{
		tableColumnModels.add(column);	//add this column to the list of columns
		logicalTableColumnModels.add(column);	//add this column to the list of columns in logical order
	}

	/**Clears all columns from the table.*/
	protected void clearColumns()	//TODO synchronize access
	{
		tableColumnModels.clear();	//clear the columns
		logicalTableColumnModels.clear();	//clear the logical columns		
	}
	
	/**Columns constructor.
	@param columns The models representing the table columns.
	*/
	public AbstractTableModel(final TableColumnModel<?>... columns)
	{
		super();	//construct the parent class
		CollectionUtilities.addAll(tableColumnModels, columns);	//add all the columns to our list of table columns
		CollectionUtilities.addAll(logicalTableColumnModels, columns);	//add all the columns to our logical list of table columns
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

	/**Sets the cell value for the given cell.
	This method delegates to {@link #setCellValue(int, TableColumnModel, Object)}.
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

}
