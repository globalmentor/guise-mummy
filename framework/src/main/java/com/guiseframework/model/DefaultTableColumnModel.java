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

package com.guiseframework.model;

import java.net.URI;

import com.guiseframework.component.EditComponent;
import com.guiseframework.validator.Validator;

/**
 * The default implementation of a column in a table. The table column model by default is not editable.
 * @param <V> The type of values contained in the table column.
 * @author Garret Wilson
 */
public class DefaultTableColumnModel<V> extends DefaultInfoModel implements TableColumnModel<V> {

	/** The class representing the type of values this model can hold. */
	private final Class<V> valueClass;

	@Override
	public Class<V> getValueClass() {
		return valueClass;
	}

	/** Whether the cells in this table column model are editable and will allow the the user to change their values. */
	private boolean editable = false;

	@Override
	public boolean isEditable() {
		return editable;
	}

	@Override
	public void setEditable(final boolean newEditable) {
		if(editable != newEditable) { //if the value is really changing
			final boolean oldEditable = editable; //get the old value
			editable = newEditable; //actually change the value
			//TODO important: fix				firePropertyChange(EDITABLE_PROPERTY, Boolean.valueOf(oldEditable), Boolean.valueOf(newEditable));	//indicate that the value changed
		}
	}

	/** The style identifier, or <code>null</code> if there is no style ID. */
	private String styleID = null;

	@Override
	public String getStyleID() {
		return styleID;
	}

	@Override
	public void setStyleID(final String newStyleID) {
		if(styleID != newStyleID) { //if the value is really changing
			final String oldStyleID = styleID; //get the current value
			styleID = newStyleID; //update the value
			firePropertyChange(STYLE_ID_PROPERTY, oldStyleID, newStyleID);
		}
	}

	/** The validator for cells in this column, or <code>null</code> if no validator is installed. */
	private Validator<V> validator;

	@Override
	public Validator<V> getValidator() {
		return validator;
	}

	@Override
	public void setValidator(final Validator<V> newValidator) {
		if(validator != newValidator) { //if the value is really changing
			final Validator<V> oldValidator = validator; //get the old value
			validator = newValidator; //actually change the value
			firePropertyChange(ValueModel.VALIDATOR_PROPERTY, oldValidator, newValidator); //indicate that the value changed
		}
	}

	/** Whether the column is visible. */
	private boolean visible = true;

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public void setVisible(final boolean newVisible) {
		if(visible != newVisible) { //if the value is really changing
			final boolean oldVisible = visible; //get the current value
			visible = newVisible; //update the value
			firePropertyChange(VISIBLE_PROPERTY, Boolean.valueOf(oldVisible), Boolean.valueOf(newVisible));
		}
	}

	/**
	 * Session and value class constructor.
	 * @param session The Guise session that owns this column.
	 * @param valueClass The class indicating the type of values held in the model.
	 * @throws NullPointerException if the given session, and/or value class is <code>null</code>.
	 */
	/*TODO del; the ID apparently is not used
		public DefaultTableColumnModel(final GuiseSession session, final Class<V> valueClass)
		{
			this(session, null, valueClass);	//construct the class indicating that a default ID should be generated
		}
	*/

	/**
	 * Value class constructor.
	 * @param valueClass The class indicating the type of values held in the model.
	 * @throws NullPointerException if the given value class is <code>null</code>.
	 */
	public DefaultTableColumnModel(final Class<V> valueClass) {
		this(valueClass, null); //construct the class with no label
	}

	/**
	 * Value class and label constructor.
	 * @param valueClass The class indicating the type of values held in the model.
	 * @param label The text of the label, or <code>null</code> if there should be no label.
	 */
	public DefaultTableColumnModel(final Class<V> valueClass, final String label) {
		this(valueClass, label, null); //construct the label model with no icon
	}

	/**
	 * Value class, label, and glyph URI constructor.
	 * @param valueClass The class indicating the type of values held in the model.
	 * @param label The text of the label, or <code>null</code> if there should be no label.
	 * @param glyphURI The glyph URI, which may be a resource URI, or <code>null</code> if there is no glyph URI.
	 */
	public DefaultTableColumnModel(final Class<V> valueClass, final String label, final URI glyphURI) {
		super(label, glyphURI); //construct the parent class
		this.valueClass = valueClass; //save the value class
	}

}
