package com.guiseframework.model;

import static com.garretwilson.lang.ClassUtilities.*;

import java.net.URI;

import javax.mail.internet.ContentType;

import com.guiseframework.component.Labelable;
import com.guiseframework.validator.Validator;

/**A column in a table.
@param <V> The type of values contained in the table column.
@author Garret Wilson
*/
public interface TableColumnModel<V> extends ControlModel, Labelable
{

	/**The label icon bound property.*/
	public final static String LABEL_ICON_PROPERTY=getPropertyName(TableColumnModel.class, "labelIcon");
	/**The label icon resource key bound property.*/
	public final static String LABEL_ICON_RESOURCE_KEY_PROPERTY=getPropertyName(TableColumnModel.class, "labelIconResourceKey");
	/**The label text bound property.*/
	public final static String LABEL_TEXT_PROPERTY=getPropertyName(TableColumnModel.class, "labelText");
	/**The label text content type bound property.*/
	public final static String LABEL_TEXT_CONTENT_TYPE_PROPERTY=getPropertyName(TableColumnModel.class, "labelTextContentType");
	/**The label text resource key bound property.*/
	public final static String LABEL_TEXT_RESOURCE_KEY_PROPERTY=getPropertyName(TableColumnModel.class, "labelTextResourceKey");
	/**The bound property of the column style ID.*/
	public final static String STYLE_ID_PROPERTY=getPropertyName(TableColumnModel.class, "styleID");
	/**The validator bound property.*/
	public final static String VALIDATOR_PROPERTY=getPropertyName(TableColumnModel.class, "validator");
	/**The bound property of whether the column is visible.*/
	public final static String VISIBLE_PROPERTY=getPropertyName(TableColumnModel.class, "visible");

	/**@return The class representing the type of values this model can hold.*/
	public Class<V> getValueClass();

	/**@return The label icon URI, or <code>null</code> if there is no icon URI.*/
	public URI getIcon();

	/**Sets the URI of the label icon.
	This is a bound property of type <code>URI</code>.
	@param newLabelIcon The new URI of the label icon.
	@see #LABEL_ICON_PROPERTY
	*/
	public void setIcon(final URI newLabelIcon);

	/**@return The label icon URI resource key, or <code>null</code> if there is no icon URI resource specified.*/
	public String getIconResourceKey();

	/**Sets the key identifying the URI of the label icon in the resources.
	This is a bound property.
	@param newIconResourceKey The new label icon URI resource key.
	@see #LABEL_ICON_RESOURCE_KEY_PROPERTY
	*/
	public void setIconResourceKey(final String newIconResourceKey);

	/**@return The label text, or <code>null</code> if there is no label text.*/
	public String getLabel();

	/**Sets the text of the label.
	This is a bound property.
	@param newLabelText The new text of the label.
	@see #LABEL_TEXT_PROPERTY
	*/
	public void setLabel(final String newLabelText);

	/**@return The content type of the label text.*/
	public ContentType getLabelContentType();

	/**Sets the content type of the label text.
	This is a bound property.
	@param newLabelTextContentType The new label text content type.
	@exception NullPointerException if the given content type is <code>null</code>.
	@exception IllegalArgumentException if the given content type is not a text content type.
	@see #LABEL_TEXT_CONTENT_TYPE_PROPERTY
	*/
	public void setLabelContentType(final ContentType newLabelTextContentType);

	/**@return The label text resource key, or <code>null</code> if there is no label text resource specified.*/
	public String getLabelResourceKey();

	/**Sets the key identifying the text of the label in the resources.
	This is a bound property.
	@param newLabelTextResourceKey The new label text resource key.
	@see #LABEL_TEXT_RESOURCE_KEY_PROPERTY
	*/
	public void setLabelResourceKey(final String newLabelTextResourceKey);

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
