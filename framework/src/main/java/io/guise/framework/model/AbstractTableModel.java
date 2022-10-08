/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
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

/**
 * An abstract implementation of a table model. The table model is editable by default.
 * @author Garret Wilson
 */
public abstract class AbstractTableModel extends AbstractModel implements TableModel {

	/** The list of table column models. */
	private final List<TableColumnModel<?>> tableColumnModels = new CopyOnWriteArrayList<TableColumnModel<?>>();

	@Override
	public List<TableColumnModel<?>> getColumns() {
		return unmodifiableList(tableColumnModels);
	}

	/** The list of table column models in logical order. */
	private final List<TableColumnModel<?>> logicalTableColumnModels = new CopyOnWriteArrayList<TableColumnModel<?>>();

	@Override
	public int getColumnIndex(final TableColumnModel<?> column) {
		return logicalTableColumnModels.indexOf(column);
	}

	@Override
	public int getColumnCount() {
		return logicalTableColumnModels.size();
	}

	/**
	 * Adds a column to the table.
	 * @param column The column to add.
	 */
	protected void addColumn(final TableColumnModel<?> column) { //TODO synchronize access
		tableColumnModels.add(column); //add this column to the list of columns
		logicalTableColumnModels.add(column); //add this column to the list of columns in logical order
	}

	/** Clears all columns from the table. */
	protected void clearColumns() { //TODO synchronize access
		tableColumnModels.clear(); //clear the columns
		logicalTableColumnModels.clear(); //clear the logical columns		
	}

	/**
	 * Columns constructor.
	 * @param columns The models representing the table columns.
	 */
	public AbstractTableModel(final TableColumnModel<?>... columns) {
		super(); //construct the parent class
		addAll(tableColumnModels, columns); //add all the columns to our list of table columns
		addAll(logicalTableColumnModels, columns); //add all the columns to our logical list of table columns
	}

	@Override
	public <C> C getCellValue(final Cell<C> cell) {
		return getCellValue(cell.getRowIndex(), cell.getColumn()); //return the cell value for the cell row index and column
	}

	@Override
	public <C> void setCellValue(final Cell<C> cell, final C newCellValue) {
		setCellValue(cell.getRowIndex(), cell.getColumn(), newCellValue); //set the cell value for the cell row index and column
	}

}
