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

import static java.util.Objects.*;

/**
 * A model of a table. The model is thread-safe, synchronized on itself. Any iteration over values should include synchronization on the instance of this class.
 * @author Garret Wilson
 */
public interface TableModel extends Model { //TODO maybe specify row count to be a bound property

	/**
	 * Determines the logical index of the given table column.
	 * @param column One of the table columns.
	 * @return The zero-based logical index of the column within the table, or -1 if the column is not one of the model's columns.
	 */
	public int getColumnIndex(final TableColumnModel<?> column);

	/** @return A read-only list of table columns in physical order. */
	public List<TableColumnModel<?>> getColumns();

	/** @return The number of rows in this table. */
	public int getRowCount();

	/** @return The number of columns in this table. */
	public int getColumnCount();

	/**
	 * Returns the cell value for the given cell. This method delegates to {@link #getCellValue(int, TableColumnModel)}.
	 * @param <C> The type of cell value.
	 * @param cell The cell containing the row index and column information.
	 * @return The value in the cell at the given row and column, or <code>null</code> if there is no value in that cell.
	 * @throws IndexOutOfBoundsException if the given row index represents an invalid location for the table.
	 * @throws IllegalArgumentException if the given column is not one of this table's columns.
	 */
	public <C> C getCellValue(final Cell<C> cell);

	/**
	 * Returns the cell value at the given row and column.
	 * @param <C> The type of cell values in the given column.
	 * @param rowIndex The zero-based row index.
	 * @param column The column for which a value should be returned.
	 * @return The value in the cell at the given row and column, or <code>null</code> if there is no value in that cell.
	 * @throws IndexOutOfBoundsException if the given row index represents an invalid location for the table.
	 * @throws IllegalArgumentException if the given column is not one of this table's columns.
	 */
	public <C> C getCellValue(final int rowIndex, final TableColumnModel<C> column);

	/**
	 * Sets the cell value for the given cell. This method delegates to {@link #setCellValue(int, TableColumnModel, Object)}.
	 * @param <C> The type of cell value.
	 * @param cell The cell containing the row index and column information.
	 * @param newCellValue The value to place in the cell at the given row and column, or <code>null</code> if there should be no value in that cell.
	 * @throws IndexOutOfBoundsException if the given row index represents an invalid location for the table.
	 * @throws IllegalArgumentException if the given column is not one of this table's columns.
	 */
	public <C> void setCellValue(final Cell<C> cell, final C newCellValue);

	/**
	 * Sets the cell value at the given row and column.
	 * @param <C> The type of cell values in the given column.
	 * @param rowIndex The zero-based row index.
	 * @param column The column for which a value should be returned.
	 * @param newCellValue The value to place in the cell at the given row and column, or <code>null</code> if there should be no value in that cell.
	 * @throws IndexOutOfBoundsException if the given row index represents an invalid location for the table.
	 * @throws IllegalArgumentException if the given column is not one of this table's columns.
	 */
	public <C> void setCellValue(final int rowIndex, final TableColumnModel<C> column, final C newCellValue);

	/**
	 * A lightweight class representing a row and column in a table. This class is useful as a map key, for instance.
	 * @param <C> The type of value contained in the cell.
	 * @author Garret Wilson
	 */
	public static class Cell<C> {

		/** The zero-based row index. */
		private final int rowIndex;

		/** @return The zero-based row index. */
		public int getRowIndex() {
			return rowIndex;
		}

		/** The column. */
		private final TableColumnModel<C> column;

		/** @return The column. */
		public TableColumnModel<C> getColumn() {
			return column;
		}

		/**
		 * Row and column constructor
		 * @param rowIndex The zero-based cell row index.
		 * @param column The cell column.
		 * @throws NullPointerException if the given column is null.
		 */
		public Cell(final int rowIndex, final TableColumnModel<C> column) {
			this.rowIndex = rowIndex;
			this.column = requireNonNull(column, "Column cannot be null.");
		}

		/**
		 * Determines whether the given object is equal to this object.
		 * @param object The object to compare to this object.
		 * @return <code>true</code> if the given object is another cell with the same row index and column.
		 */
		public boolean equals(final Object object) {
			if(object instanceof Cell) { //if the object is a cell
				final Cell<?> cell = (Cell<?>)object; //cast the object to a cell
				return getRowIndex() == cell.getRowIndex() && getColumn().equals(cell.getColumn()); //compare row index and column
			} else { //if the object is not a cell
				return false; //the objects aren't equal
			}
		}

		/** @return A hash code for the cell. */
		public int hashCode() {
			return hash(rowIndex, column); //generate a hash code
		}

	}

}
