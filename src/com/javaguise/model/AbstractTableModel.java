package com.javaguise.model;

import java.util.*;
import static java.util.Collections.*;
import java.util.concurrent.CopyOnWriteArrayList;

import com.javaguise.model.TableModel.Cell;
import com.javaguise.session.GuiseSession;

/**An abstract implementation of a table model.
The model is thread-safe, synchronized on itself. Any iteration over values should include synchronization on the instance of this class. 
The table model is editable by default.
@author Garret Wilson
*/
public abstract class AbstractTableModel extends AbstractControlModel implements TableModel
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

	/**@return The number of columns in this table.*/
	public int getColumnCount() {return logicalTableColumnModels.size();}

	/**Session constructor.
	@param session The Guise session that owns this model.
	@param columns The models representing the table columns.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public AbstractTableModel(final GuiseSession<?> session, final TableColumnModel<?>... columns)
	{
		this(session, null, columns);	//construct the class with no label
	}

	/**Session and label constructor.
	@param session The Guise session that owns this model.
	@param label The text of the label.
	@param columns The models representing the table columns.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public AbstractTableModel(final GuiseSession<?> session, final String label, final TableColumnModel<?>... columns)
	{
		super(session, label);	//construct the parent class
		addAll(logicalTableColumnModels, columns);	//add all the columns to our logical list of table columns
		addAll(tableColumnModels, columns);	//add all the columns to our list of table columns
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

}
