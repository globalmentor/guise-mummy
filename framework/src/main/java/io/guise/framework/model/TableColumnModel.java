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

import static com.globalmentor.java.Classes.*;

import io.guise.framework.component.EditComponent;
import io.guise.framework.validator.Validator;

/**
 * A column in a table.
 * @param <V> The type of values contained in the table column.
 * @author Garret Wilson
 */
public interface TableColumnModel<V> extends InfoModel {

	/** The bound property of the column style ID. */
	public static final String STYLE_ID_PROPERTY = getPropertyName(TableColumnModel.class, "styleID");
	/** The validator bound property. */
	public static final String VALIDATOR_PROPERTY = getPropertyName(TableColumnModel.class, "validator");
	/** The bound property of whether the column is visible. */
	public static final String VISIBLE_PROPERTY = getPropertyName(TableColumnModel.class, "visible");

	/** @return The class representing the type of values this model can hold. */
	public Class<V> getValueClass();

	/** @return Whether the cells in this table column model are editable and will allow the the user to change their values. */
	public boolean isEditable();

	/**
	 * Sets whether the cells in this table column model are editable and will allow the the user to change their values. This is a bound property of type
	 * <code>Boolean</code>.
	 * @param newEditable <code>true</code> if the table column cells should allow the user to change their values.
	 * @see EditComponent#EDITABLE_PROPERTY
	 */
	public void setEditable(final boolean newEditable);

	/** @return The style identifier, or <code>null</code> if there is no style ID. */
	public String getStyleID();

	/**
	 * Identifies the style for the column. This is a bound property.
	 * @param newStyleID The style identifier, or <code>null</code> if there is no style ID.
	 * @see #STYLE_ID_PROPERTY
	 */
	public void setStyleID(final String newStyleID);

	/** @return The validator for cells in this column, or <code>null</code> if no validator is installed. */
	public Validator<V> getValidator();

	/**
	 * Sets the validator. This is a bound property
	 * @param newValidator The validator for cells in this column, or <code>null</code> if no validator should be used.
	 * @see #VALIDATOR_PROPERTY
	 */
	public void setValidator(final Validator<V> newValidator);

	/** @return Whether the column is visible. */
	public boolean isVisible();

	/**
	 * Sets whether the column is visible. This is a bound property of type <code>Boolean</code>.
	 * @param newVisible <code>true</code> if the column should be visible, else <code>false</code>.
	 * @see #VISIBLE_PROPERTY
	 */
	public void setVisible(final boolean newVisible);

}
