package com.javaguise.model;

import static com.garretwilson.lang.ClassUtilities.*;

import com.javaguise.validator.Validator;

/**A column in a table.
@param <V> The type of values contained in the table column.
@author Garret Wilson
*/
public interface TableColumnModel<V> extends ControlModel
{

	/**The bound property of the column style ID.*/
	public final static String STYLE_ID_PROPERTY=getPropertyName(TableColumnModel.class, "styleID");
	/**The validator bound property.*/
	public final static String VALIDATOR_PROPERTY=getPropertyName(TableColumnModel.class, "validator");
	/**The bound property of whether the component is visible.*/
	public final static String VISIBLE_PROPERTY=getPropertyName(TableColumnModel.class, "visible");

	/**@return The class representing the type of values this model can hold.*/
	public Class<V> getValueClass();

	/**@return Whether the cells in this table column model are editable and will allow the the user to change their values.*/
	public boolean isEditable();

	/**Sets whether the cells in this table column model are editable and will allow the the user to change their values.
	This is a bound property of type <code>Boolean</code>.
	@param newEditable <code>true</code> if the table column cells should allow the user to change their values.
	@see #EDITABLE_PROPERTY
	*/
	public void setEditable(final boolean newEditable);

	/**@return The style identifier, or <code>null</code> if there is no style ID.*/
	public String getStyleID();

	/**Identifies the style for the column.
	This is a bound property.
	@param newStyleID The style identifier, or <code>null</code> if there is no style ID.
	@see #STYLE_ID_PROPERTY
	*/
	public void setStyleID(final String newStyleID);

	/**@return The validator for cells in this column, or <code>null</code> if no validator is installed.*/
	public Validator<V> getValidator();

	/**Sets the validator.
	This is a bound property
	@param newValidator The validator for cells in this column, or <code>null</code> if no validator should be used.
	@see #VALIDATOR_PROPERTY
	*/
	public void setValidator(final Validator<V> newValidator);

	/**@return Whether the column is visible.*/
	public boolean isVisible();

	/**Sets whether the column is visible.
	This is a bound property of type <code>Boolean</code>.
	@param newVisible <code>true</code> if the column should be visible, else <code>false</code>.
	@see #VISIBLE_PROPERTY
	*/
	public void setVisible(final boolean newVisible);

}
