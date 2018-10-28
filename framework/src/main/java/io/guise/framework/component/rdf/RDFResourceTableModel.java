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

package io.guise.framework.component.rdf;

import java.net.URI;

import com.globalmentor.rdf.*;

import io.guise.framework.model.*;

/**
 * A table model based upon a list of RDF resource. This table recognizes {@link RDFResourceURITableColumnModel} columns. As for other columns, this table only
 * recognizes {@link RDFPropertyTableColumnModel} columns and will throw an exception if such a column is not found, so subclasses using non-RDF property
 * columns should override {@link #getCellValue(RDFResource, int, TableColumnModel)}. and {@link #setCellValue(RDFResource, int, TableColumnModel, Object)} to
 * handle those custom columns before delegating to this class.
 * @author Garret Wilson
 */
public class RDFResourceTableModel extends AbstractListSelectTableModel<RDFResource> {

	/**
	 * Constructs a table model indicating the type of values it can hold, using a default multiple selection strategy.
	 * @param columns The models representing the table columns.
	 */
	public RDFResourceTableModel(final TableColumnModel<?>... columns) {
		super(RDFResource.class, columns); //construct the parent class
	}

	@Override
	protected <C> C getCellValue(final RDFResource resource, final int rowIndex, final TableColumnModel<C> column) {
		if(column instanceof RDFResourceURITableColumnModel) { //if this is the reference URI column
			return column.getValueClass().cast(resource.getURI()); //return the reference URI
		} else { //if this is some other column type
			final RDFPropertyTableColumnModel<?> propertyColumn = (RDFPropertyTableColumnModel<?>)column; //cast the column
			final URI propertyURI = propertyColumn.getPropertyURI(); //get the URI of the property
			return column.getValueClass().cast(resource.getPropertyValue(propertyURI)); //get this property value and cast and return the value
		}
	}

	@Override
	protected <C> void setCellValue(final RDFResource resource, final int rowIndex, final TableColumnModel<C> column, final C newCellValue) {
		if(column instanceof RDFResourceURITableColumnModel) { //if this is the reference URI column
			throw new UnsupportedOperationException("Changing resource reference URI is not yet permitted.");
		} else { //if this is some other column type
			final RDFPropertyTableColumnModel<?> propertyColumn = (RDFPropertyTableColumnModel<?>)column; //cast the column
			final URI propertyURI = propertyColumn.getPropertyURI(); //get the URI of the property
			resource.setProperty(propertyURI, (RDFObject)newCellValue);
		}
	}

}
