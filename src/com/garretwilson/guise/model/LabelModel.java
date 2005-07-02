package com.garretwilson.guise.model;

import static com.garretwilson.lang.ClassUtilities.*;

/**A model for an identifier such as text and/or an icon.
@author Garret Wilson
*/
public interface LabelModel extends Model
{
	/**The label bound property.*/
	public final static String LABEL_PROPERTY=getPropertyName(LabelModel.class, "label");
	/**The label resource key bound property.*/
	public final static String LABEL_RESOURCE_KEY_PROPERTY=getPropertyName(LabelModel.class, "labelResourceKey");

	/**Determines the text of the label.
	If a label is specified, it will be used; otherwise, a value will be loaded from the resources if possible.
	@return The label text, or <code>null</code> if there is no label text.
	@exception java.util.MissingResourceException if there was an error loading the value from the resources.
	@see #getLabelResourceKey()
	*/
	public String getLabel();

	/**Sets the text of the label.
	This is a bound property.
	@param newLabel The new text of the label.
	@see #LABEL_PROPERTY
	*/
	public void setLabel(final String newLabel);

	/**@return The label text resource key, or <code>null</code> if there is no label text resource specified.*/
	public String getLabelResourceKey();

	/**Sets the key identifying the text of the label in the resources.
	This is a bound property.
	@param newLabelResourceKey The new label text resource key.
	@see #LABEL_RESOURCE_KEY_PROPERTY
	*/
	public void setLabelResourceKey(final String newLabelResourceKey);
}
