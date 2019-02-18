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

package io.guise.framework.component.urf;

import java.net.URI;

import org.urframework.*;

import static com.globalmentor.java.Objects.*;
import com.globalmentor.net.Resource;

import io.guise.framework.model.*;

/**
 * A table model based upon a list of URF resource. This table recognizes {@link URFResourceURITableColumnModel} columns. As for other columns, this table only
 * recognizes {@link URFPropertyTableColumnModel} columns and will throw an exception if such a column is not found, so subclasses using non-URF property
 * columns should override {@link #getCellValue(URFResource, int, TableColumnModel)} and use {@link #setCellValue(URFResource, int, TableColumnModel, Object)}
 * to handle those custom columns before delegating to this class. For {@link URFPropertyTableColumnModel} columns, if the
 * {@link TableColumnModel#getValueClass()} is {@link URFResource}, then the value will be used as-is. Otherwise, it will be attempted to be converted to the
 * correct type using {@link URF#asObject(Resource)}. If the returned object is not of the correct type, the value will be ignored rather than a
 * {@link ClassCastException} thrown, as a common use case is to display loosely-coupled URF resources that could have various value types. Values to be set, on
 * the other hand, must be of the appropriate value or a {@link ClassCastException} will be thrown. Setting non-resource values is not yet supported.
 * @author Garret Wilson
 */
public class URFResourceTableModel extends AbstractListSelectTableModel<URFResource> {

	/**
	 * Constructs a table model indicating the type of values it can hold, using a default multiple selection strategy.
	 * @param columns The models representing the table columns.
	 */
	public URFResourceTableModel(final TableColumnModel<?>... columns) {
		super(URFResource.class, columns); //construct the parent class
	}

	@Override
	protected <C> C getCellValue(final URFResource resource, final int rowIndex, final TableColumnModel<C> column) { //TODO return Optional<>?
		final Class<C> valueClass = column.getValueClass(); //get the type of value for the column
		final C value; //we'll determine the value to use
		if(column instanceof URFResourceURITableColumnModel) { //if this is the reference URI column
			value = valueClass.cast(resource.getURI()); //use the reference URI
		} else { //if this is some other column type
			final URFPropertyTableColumnModel<?> propertyColumn = (URFPropertyTableColumnModel<?>)column; //cast the column
			final URI propertyURI = propertyColumn.getPropertyURI(); //get the URI of the property
			final URFResource propertyValue = resource.getPropertyValue(propertyURI); //get the property value
			if(URFResource.class.isAssignableFrom(valueClass)) { //if this is just an URF resource property column
				value = column.getValueClass().cast(propertyValue); //use the value as-is
			} else { //if the column expects another type
				value = asInstance(URF.asObject(propertyValue), valueClass).orElse(null); //try to convert the property value to the correct type
			}
		}
		return value; //return the value we determined
	}

	@Override
	protected <C> void setCellValue(final URFResource resource, final int rowIndex, final TableColumnModel<C> column, final C newCellValue) {
		final Class<C> valueClass = column.getValueClass(); //get the type of value for the coloumn
		final URFResource propertyValue; //we'll determine the property value to use
		if(column instanceof URFResourceURITableColumnModel) { //if this is the reference URI column
			throw new UnsupportedOperationException("Changing resource reference URI is not yet permitted.");
		} else { //if this is some other column type
			final URFPropertyTableColumnModel<?> propertyColumn = (URFPropertyTableColumnModel<?>)column; //cast the column
			final URI propertyURI = propertyColumn.getPropertyURI(); //get the URI of the property
			if(URFResource.class.isAssignableFrom(valueClass)) { //if this is just an URF resource property column
				propertyValue = (URFResource)newCellValue; //cast the value
			} else { //if this is a non-resource property column
				throw new UnsupportedOperationException("Changing resource reference URI is not yet permitted.");
			}
			resource.setPropertyValue(propertyURI, propertyValue);
		}
	}

}
