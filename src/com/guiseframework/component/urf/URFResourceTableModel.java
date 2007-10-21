package com.guiseframework.component.urf;

import java.net.URI;

import com.garretwilson.urf.*;
import com.guiseframework.model.*;

/**A table model based upon a list of URF resource.
This table recognizes {@link URFResourceURITableColumnModel} columns.
As for other columns, this table only recognizes {@link URFPropertyTableColumnModel} columns and will throw an exception if such a column is not found,
so subclasses using non-URF property columns should override {@link #getCellValue(URFResource, int, TableColumnModel)}.
and {@link #setCellValue(URFResource, int, TableColumnModel, Object)} to handle those custom columns before delegating to this class.
@author Garret Wilson
*/
public class URFResourceTableModel extends AbstractListSelectTableModel<URFResource>
{
	/**Constructs a table model indicating the type of values it can hold, using a default multiple selection strategy.
	@param columns The models representing the table columns.
	*/
	public URFResourceTableModel(final TableColumnModel<?>... columns)
	{
		super(URFResource.class, columns);	//construct the parent class
	}

	/**Returns the value's property for the given column.
	@param <C> The type of cell values in the given column.
	@param resource The resource in this list select model.
	@param rowIndex The zero-based row index of the value.
	@param column The column for which a value should be returned.
	@return The value in the cell at the given row and column, or <code>null</code> if there is no value in that cell.
	@exception IndexOutOfBoundsException if the given row index represents an invalid location for the table.
	@exception IllegalArgumentException if the given column is not one of this table's columns.
	@exception ClassCastException if the given column is not a {@link URFPropertyTableColumnModel} or one of the other column types supported by this class.
	*/
	protected <C> C getCellValue(final URFResource resource, final int rowIndex, final TableColumnModel<C> column)
	{
		if(column instanceof URFResourceURITableColumnModel)	//if this is the reference URI column
		{
			return column.getValueClass().cast(resource.getURI());	//return the reference URI
		}
		else	//if this is some other column type
		{
			final URFPropertyTableColumnModel<?> propertyColumn=(URFPropertyTableColumnModel<?>)column;	//cast the column
			final URI propertyURI=propertyColumn.getPropertyURI();	//get the URI of the property
			return column.getValueClass().cast(resource.getPropertyValue(propertyURI));	//get this property value and cast and return the value
		}
	}

	/**Sets the value's property for the given column.
	@param <C> The type of cell values in the given column.
	@param resource The resource in this list select model.
	@param rowIndex The zero-based row index of the value.
	@param column The column for which a value should be returned.
	@param newCellValue The value to place in the cell at the given row and column, or <code>null</code> if there should be no value in that cell.
	@exception IndexOutOfBoundsException if the given row index represents an invalid location for the table.
	@exception IllegalArgumentException if the given column is not one of this table's columns.
	@exception ClassCastException if the given column is not a {@link URFPropertyTableColumnModel} or one of the other column types supported by this class or the new cell value is not an {@link URFResource}.
	*/
	protected <C> void setCellValue(final URFResource resource, final int rowIndex, final TableColumnModel<C> column, final C newCellValue)
	{
		if(column instanceof URFResourceURITableColumnModel)	//if this is the reference URI column
		{
			throw new UnsupportedOperationException("Changing resource reference URI is not yet permitted.");
		}
		else	//if this is some other column type
		{
			final URFPropertyTableColumnModel<?> propertyColumn=(URFPropertyTableColumnModel<?>)column;	//cast the column
			final URI propertyURI=propertyColumn.getPropertyURI();	//get the URI of the property
			resource.setPropertyValue(propertyURI, (URFResource)newCellValue);
		}
	}

}
