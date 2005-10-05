package com.javaguise.model;

import java.net.URI;
import java.util.MissingResourceException;

import javax.mail.internet.ContentType;

import static com.garretwilson.io.ContentTypeConstants.*;
import static com.garretwilson.lang.ClassUtilities.*;

import com.garretwilson.text.xml.xhtml.XHTMLConstants;

/**A model for an identifier such as text and/or an icon.
@author Garret Wilson
*/
public interface LabelModel extends Model
{
	/**The icon bound property.*/
	public final static String ICON_PROPERTY=getPropertyName(LabelModel.class, "icon");
	/**The icon resource key bound property.*/
	public final static String ICON_RESOURCE_KEY_PROPERTY=getPropertyName(LabelModel.class, "iconResourceKey");
	/**The label bound property.*/
	public final static String LABEL_PROPERTY=getPropertyName(LabelModel.class, "label");
	/**The label content type bound property.*/
	public final static String LABEL_CONTENT_TYPE_PROPERTY=getPropertyName(LabelModel.class, "labelContentType");
	/**The label resource key bound property.*/
	public final static String LABEL_RESOURCE_KEY_PROPERTY=getPropertyName(LabelModel.class, "labelResourceKey");

	/**Determines the URI of the icon.
	If an icon is specified, it will be used; otherwise, a value will be loaded from the resources if possible.
	@return The icon URI, or <code>null</code> if there is no icon URI.
	@exception MissingResourceException if there was an error loading the value from the resources.
	@see #getIconResourceKey()
	*/
	public URI getIcon() throws MissingResourceException;

	/**Sets the URI of the icon.
	This is a bound property of type <code>URI</code>.
	@param newIcon The new URI of the icon.
	@see LabelModel#ICON_PROPERTY
	*/
	public void setIcon(final URI newIcon);

	/**@return The icon URI resource key, or <code>null</code> if there is no icon URI resource specified.*/
	public String getIconResourceKey();

	/**Sets the key identifying the URI of the icon in the resources.
	This is a bound property.
	@param newIconResourceKey The new icon URI resource key.
	@see LabelModel#ICON_RESOURCE_KEY_PROPERTY
	*/
	public void setIconResourceKey(final String newIconResourceKey);

	/**Determines the text of the label.
	If a label is specified, it will be used; otherwise, a value will be loaded from the resources if possible.
	@return The label text, or <code>null</code> if there is no label text.
	@exception MissingResourceException if there was an error loading the value from the resources.
	@see #getLabelResourceKey()
	*/
	public String getLabel() throws MissingResourceException;

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

	/**@return <code>true</code> if this model has label information, such as an icon or a label string.*/
	public boolean hasLabel();
}
