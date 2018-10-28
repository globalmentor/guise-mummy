/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.model;

import java.util.*;

import static java.util.Collections.*;

import java.util.concurrent.CopyOnWriteArrayList;

import com.globalmentor.collections.SynchronizedListDecorator;

/**
 * An abstract implementation of a table model representing selectable list values. The model is thread-safe, synchronized on itself. Any iteration over values
 * should include synchronization on the instance of this class.
 * @param <V> The type of values representing table rows.
 * @author Garret Wilson
 */
public abstract class AbstractListSelectTableModel<V> extends DefaultListSelectModel<V> implements TableModel {

	/**
	 * Whether the table is editable and the cells will allow the the user to change their values, if their respective columns are designated as editable as well.
	 */
	//TODO fix	private boolean editable=true;

	/**
	 * @return Whether the table is editable and the cells will allow the the user to change their values, if their respective columns are designated as editable
	 *         as well.
	 */
	//TODO fix		public boolean isEditable() {return editable;}

	/**
	 * Sets whether the table is editable and the cells will allow the the user to change their values, if their respective columns are designated as editable as
	 * well. This is a bound property of type <code>Boolean</code>.
	 * @param newEditable <code>true</code> if the cells should allow the user to change their values if their respective columns are also designated as editable.
	 * @see EditComponent#EDITABLE_PROPERTY
	 */
	/*TODO fix
			public void setEditable(final boolean newEditable)
			{
				if(editable!=newEditable) {	//if the value is really changing
					final boolean oldEditable=editable;	//get the old value
					editable=newEditable;	//actually change the value
					firePropertyChange(EDITABLE_PROPERTY, Boolean.valueOf(oldEditable), Boolean.valueOf(newEditable));	//indicate that the value changed
				}			
			}
	*/

	/** The list of table column models in logical order. */
	private final List<TableColumnModel<?>> logicalTableColumnModels = new CopyOnWriteArrayList<TableColumnModel<?>>();

	@Override
	public int getColumnIndex(final TableColumnModel<?> column) {
		return logicalTableColumnModels.indexOf(column);
	}

	/** The list of table column models. */
	private final List<TableColumnModel<?>> tableColumnModels = new CopyOnWriteArrayList<TableColumnModel<?>>();

	@Override
	public List<TableColumnModel<?>> getColumns() {
		return unmodifiableList(tableColumnModels);
	}

	@Override
	public int getRowCount() {
		return size();
	}

	@Override
	public int getColumnCount() {
		return tableColumnModels.size();
	}

	/** The list of value model arrays for each row. */
	//TODO fix	private final List<List<ValueModel<V>>> valueModelLists;
	private final List<ValueModel<Object>[]> valueModelRowArrays;

	/**
	 * Constructs a list select table model indicating the type of values it can hold, using a default multiple selection strategy.
	 * @param valueClass The class indicating the type of values held in the model.
	 * @param columns The models representing the table columns.
	 * @throws NullPointerException if the given value class is <code>null</code>.
	 */
	public AbstractListSelectTableModel(final Class<V> valueClass, final TableColumnModel<?>... columns) {
		this(valueClass, new MultipleListSelectionPolicy<V>(), columns); //construct the class with a multiple selection strategy
	}

	/**
	 * Constructs a list select table model indicating the type of values it can hold. The selection strategy is not added as a listener to this model but is
	 * rather notified manually so that the event won't be delayed and/or sent out of order
	 * @param valueClass The class indicating the type of values held in the model.
	 * @param listSelectionStrategy The strategy for selecting values in the model.
	 * @param columns The models representing the table columns.
	 * @throws NullPointerException if the given value class and/or selection strategy is <code>null</code>.
	 */
	public AbstractListSelectTableModel(final Class<V> valueClass, final ListSelectionPolicy<V> listSelectionStrategy, final TableColumnModel<?>... columns) {
		super(valueClass, listSelectionStrategy); //construct the parent class
		Collections.addAll(logicalTableColumnModels, columns); //add all the columns to our logical list of table columns
		Collections.addAll(tableColumnModels, columns); //add all the columns to our list of table columns
		valueModelRowArrays = new SynchronizedListDecorator<ValueModel<Object>[]>(new ArrayList<ValueModel<Object>[]>(), this); //create a list of value model arrays, synchronizing all access on this object
	}

	/**
	 * Returns the model representing the cell value at the given row and column.
	 * @param rowIndex The zero-based row index.
	 * @param colIndex The zero-based column index.
	 * @return The value model representing the value in the cell at the given row and column.
	 * @throws IndexOutOfBoundsException if the given row index and/or column index represents an invalid location for the table.
	 */
	public synchronized ValueModel<Object> getCellValueModel(final int rowIndex, final int colIndex) {
		return valueModelRowArrays.get(rowIndex)[colIndex]; //return the value model in the given row for the given column
	}

	@Override
	public <C> C getCellValue(final Cell<C> cell) {
		return getCellValue(cell.getRowIndex(), cell.getColumn()); //return the cell value for the cell row index and column
	}

	@Override
	public <C> C getCellValue(final int rowIndex, final TableColumnModel<C> column) {
		return getCellValue(get(rowIndex), rowIndex, column); //retrieve the value in the given row and ask for the corresponding cell value 
	}

	@Override
	public <C> void setCellValue(final Cell<C> cell, final C newCellValue) {
		setCellValue(cell.getRowIndex(), cell.getColumn(), newCellValue); //set the cell value for the cell row index and column
	}

	@Override
	public <C> void setCellValue(final int rowIndex, final TableColumnModel<C> column, final C newCellValue) {
		setCellValue(get(rowIndex), rowIndex, column, newCellValue); //retrieve the value in the given row and set the corresponding cell value 
	}

	/**
	 * Returns the value's property for the given column.
	 * @param <C> The type of cell values in the given column.
	 * @param value The value in this list select model.
	 * @param rowIndex The zero-based row index of the value.
	 * @param column The column for which a value should be returned.
	 * @return The value in the cell at the given row and column, or <code>null</code> if there is no value in that cell.
	 * @throws IndexOutOfBoundsException if the given row index represents an invalid location for the table.
	 * @throws IllegalArgumentException if the given column is not one of this table's columns.
	 */
	protected abstract <C> C getCellValue(final V value, final int rowIndex, final TableColumnModel<C> column);

	/**
	 * Sets the value's property for the given column.
	 * @param <C> The type of cell values in the given column.
	 * @param value The value in this list select model.
	 * @param rowIndex The zero-based row index of the value.
	 * @param column The column for which a value should be returned.
	 * @param newCellValue The value to place in the cell at the given row and column, or <code>null</code> if there should be no value in that cell.
	 * @throws IndexOutOfBoundsException if the given row index represents an invalid location for the table.
	 * @throws IllegalArgumentException if the given column is not one of this table's columns.
	 */
	protected abstract <C> void setCellValue(final V value, final int rowIndex, final TableColumnModel<C> column, final C newCellValue);

}
