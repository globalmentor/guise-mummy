package com.garretwilson.guise.model;

import static com.garretwilson.lang.ClassUtilities.*;

/**A model for an identifier such as text and/or an icon.
@author Garret Wilson
*/
public interface LabelModel extends Model
{
	/**The label bound property.*/
	public final static String LABEL_PROPERTY=getPropertyName(LabelModel.class, "label");

	/**@return The label text, or <code>null</code> if there is no label text.*/
	public String getLabel();

	/**Sets the text of the label.
	This is a bound property.
	@param newLabel The new text of the label.
	@see #LABEL_PROPERTY
	*/
	public void setLabel(final String newLabel);
}
