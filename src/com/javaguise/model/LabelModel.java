package com.javaguise.model;

import java.util.MissingResourceException;

import javax.mail.internet.ContentType;

import static com.garretwilson.io.ContentTypeConstants.*;
import static com.garretwilson.lang.ClassUtilities.*;
import com.garretwilson.text.xml.xhtml.XHTMLConstants;

/**A model for an identifier such as text and/or an icon.
@author Garret Wilson
*/
public interface LabelModel extends Model	//TODO add icon and hasLabel() method, which checks for label text and/or icon
{
	/**The label bound property.*/
	public final static String LABEL_PROPERTY=getPropertyName(LabelModel.class, "label");
	/**The label content type bound property.*/
	public final static String LABEL_CONTENT_TYPE_PROPERTY=getPropertyName(LabelModel.class, "labelContentType");
	/**The label resource key bound property.*/
	public final static String LABEL_RESOURCE_KEY_PROPERTY=getPropertyName(LabelModel.class, "labelResourceKey");

	/**A content type of <code>text/plain</code>.*/
	public final static ContentType PLAIN_TEXT_CONTENT_TYPE=new ContentType(TEXT, PLAIN_SUBTYPE, null);

	/**A content type of <code>application/xhtml+xml</code>.*/
	public final static ContentType XHTML_CONTENT_TYPE=XHTMLConstants.XHTML_CONTENT_TYPE;

	/**Determines the text of the label.
	If a label is specified, it will be used; otherwise, a value will be loaded from the resources if possible.
	@return The label text, or <code>null</code> if there is no label text.
	@exception MissingResourceException if there was an error loading the value from the resources.
	@see #getLabelResourceKey()
	@see #getPlainLabel()
	*/
	public String getLabel() throws MissingResourceException;

	/**Determines the plain text of the label, with no markup.
	If a label is specified, it will be used; otherwise, a value will be loaded from the resources if possible.
	@return The label plain text, or <code>null</code> if there is no label text.
	@exception MissingResourceException if there was an error loading the value from the resources.
	@see #getLabel()
	*/
	public String getPlainLabel() throws MissingResourceException;

	/**Sets the text of the label.
	This is a bound property.
	@param newLabel The new text of the label.
	@see #LABEL_PROPERTY
	*/
	public void setLabel(final String newLabel);

	/**@return The content type of the label text.*/
	public ContentType getLabelContentType();

	/**Sets the content type of the label text.
	This is a bound property.
	@param newLabelContentType The new label text content type.
	@exception NullPointerException if the given content type is <code>null</code>.
	@exception IllegalArgumentException if the given content type is not a text content type.
	@see #LABEL_CONTENT_TYPE_PROPERTY
	*/
	public void setLabelContentType(final ContentType newLabelContentType);

	/**@return The label text resource key, or <code>null</code> if there is no label text resource specified.*/
	public String getLabelResourceKey();

	/**Sets the key identifying the text of the label in the resources.
	This is a bound property.
	@param newLabelResourceKey The new label text resource key.
	@see #LABEL_RESOURCE_KEY_PROPERTY
	*/
	public void setLabelResourceKey(final String newLabelResourceKey);
}
