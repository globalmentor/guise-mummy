package com.guiseframework.model;

import java.util.*;
import static java.util.Collections.*;

import static com.globalmentor.util.Arrays.*;

import com.globalmentor.util.SynchronizedListDecorator;

/**The default implementation of a table model.
The model is thread-safe, synchronized on itself. Any iteration over values should include synchronization on the instance of this class. 
@author Garret Wilson
*/
public class DefaultTableModel extends AbstractTableModel
{

	/**The list of value lists for rows.*/
	private final List<List<Object>> valueRowLists;

	/**@return The number of rows in this table.*/
	public int getRowCount() {return valueRowLists.size();}

	/**Constructs a default table model indicating the type of values it can hold, using default column models.
	@param <C> The type of values in all the cells in the table.
	@param valueClass The class indicating the type of values held in the model.
	@param columnNames The names to serve as label headers for the columns.
	@exception NullPointerException if the given class object is <code>null</code>.
	*/
	public <C> DefaultTableModel(final Class<C> valueClass, final String... columnNames)
	{
		this(valueClass, null, columnNames);	//construct the class with no values
	}

	/**Constructs a default table model indicating the type of values it can hold along with column definitions.
	@param columns The models representing the table columns.
	@exception NullPointerException if the given class object is <code>null</code>.
	*/
	public DefaultTableModel(final TableColumnModel<?>... columns)
	{
		this(null, columns);	//construct the class with no values
	}

	/**Constructs a default table model indicating the type of values it can hold and column names.
	@param <C> The type of values in all the cells in the table.
	@param valueClass The class indicating the type of values held in the model.
	@param rowValues The two-dimensional list of values, where the first index represents the row and the second represents the column, or <code>null</code> if no default values should be given.
	@param columnNames The names to serve as label headers for the columns.
	@exception NullPointerException if the given class object is <code>null</code>.
	@exception IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	*/
	public <C> DefaultTableModel(final Class<C> valueClass, final C[][] rowValues, final String... columnNames)
	{
		this(rowValues, (TableColumnModel<?>[])createDefaultColumns(valueClass, columnNames));	//create default columns for the column names
	}

	/**Constructs a default table model indicating the type of values it can hold.
	@param rowValues The two-dimensional list of values, where the first index represents the row and the second represents the column, or <code>null</code> if no default values should be given.
	@param columns The models representing the table columns.
	@exception IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	@exception ClassCastException if one of the values in a row is not compatible with the type of its column.
	*/
	public DefaultTableModel(final Object[][] rowValues, final TableColumnModel<?>... columns)
	{
		super(columns);	//construct the parent class
		valueRowLists=new SynchronizedListDecorator<List<Object>>(new ArrayList<List<Object>>(), this);	//create a list of value lists, synchronizing all access on this object
		if(rowValues!=null)	//if table data was given
		{
			synchronized(this)	//synchronize on this object out of consistency (even though it is highly unlikely another thread could have access to our data as the class is not yet constructed)
			{
				for(final Object[] values:rowValues)	//for each row of given data
				{
					if(values.length!=columns.length)	//if the number of columns doesn't match the columns of data that were supplied
					{
						throw new IllegalArgumentException("Received "+columns.length+" columns but encountered row with "+values.length+" values.");
					}
					for(int columnIndex=columns.length-1; columnIndex>=0; --columnIndex)	//for each column, make sure the given value is of an allowed type
					{
						columns[columnIndex].getValueClass().cast(values[columnIndex]);	//make sure this value can be cast to the column type
					}
					final List<Object> valueList=new SynchronizedListDecorator<Object>(new ArrayList<Object>(values.length), this);	//create a list of value, synchronizing all access on this object
					addAll(valueList, values);	//add all this row's values to the list
					valueRowLists.add(valueList);	//add this row to the list of row lists
				}
			}
		}
	}

	/**Creates default columns with the given column names.
	@param <C> The type of values contained in the columns.
	@param valueClass The class representing the values contained in the columns.
	@param columnNames The names to serve as label headers for the columns.
	@return An array of default columns.
	*/
	@SuppressWarnings("unchecked")	//as generics are only one-deep for class objects, we must cast the generic type of the default table column model class
	public static <C> TableColumnModel<C>[] createDefaultColumns(final Class<C> valueClass, final String... columnNames)
	{
		final DefaultTableColumnModel<C>[] columns=createArray((Class<DefaultTableColumnModel<C>>)(Object)DefaultTableColumnModel.class, columnNames.length);	//create an array of default table columns TODO find out why we need the double-cast
		for(int i=columns.length-1; i>=0; --i)	//for each column
		{
			final DefaultTableColumnModel<C> column=new DefaultTableColumnModel<C>(valueClass);	//create a new default table column
			column.setLabel(columnNames[i]);	//set the column label
			columns[i]=column;	//store the column
		}
		return columns;	//return the columns we created
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
		final int columnIndex=getColumnIndex(column);	//get the index of this column
		if(columnIndex<0)	//if this column isn't in this table
		{
			throw new IllegalArgumentException("Table column "+column+" not in table.");
		}
		synchronized(this)	//don't allow others to change the table data while we access the values
		{
			return column.getValueClass().cast(valueRowLists.get(rowIndex).get(columnIndex));	//get the value in the given row and column, cast to the appropriate type
		}
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
		final int columnIndex=getColumnIndex(column);	//get the index of this column
		if(columnIndex<0)	//if this column isn't in this table
		{
			throw new IllegalArgumentException("Table column "+column+" not in table.");
		}
		synchronized(this)	//don't allow others to change the table data while we access the values
		{
			valueRowLists.get(rowIndex).set(columnIndex, newCellValue);	//set the value in the given row and column
		}
	}

}
