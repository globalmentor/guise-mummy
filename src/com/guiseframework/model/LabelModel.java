package com.guiseframework.model;

import java.net.URI;

import javax.mail.internet.ContentType;

import static com.garretwilson.lang.ClassUtilities.*;

/**A model for an identifier such as text and/or an icon.
@author Garret Wilson
*/
public interface LabelModel extends Model
{
	/**The icon bound property.*/
	public final static String GLYPH_URI_PROPERTY=getPropertyName(LabelModel.class, "glyphURI");
	/**The label bound property.*/
	public final static String LABEL_PROPERTY=getPropertyName(LabelModel.class, "label");
	/**The label content type bound property.*/
	public final static String LABEL_CONTENT_TYPE_PROPERTY=getPropertyName(LabelModel.class, "labelContentType");

	/**@return The icon URI, which may be a resource URI, or <code>null</code> if there is no icon URI.*/
	public URI getGlyphURI();

	/**Sets the URI of the icon.
	This is a bound property.
	@param newIcon The new URI of the icon, which may be a resource URI.
	@see #GLYPH_URI_PROPERTY
	*/
	public void setGlyphURI(final URI newIcon);

	/**@return The label text, which may include a resource reference, or <code>null</code> if there is no label text.*/
	public String getLabel();

	/**Sets the text of the label.
	This is a bound property.
	@param newLabelText The new text of the label, which may include a resource reference.
	@see #LABEL_PROPERTY
	*/
	public void setLabel(final String newLabelText);

	/**@return The content type of the label text.*/
	public ContentType getLabelContentType();

	/**Sets the content type of the label text.
	This is a bound property.
	@param newLabelTextContentType The new label text content type.
	@exception NullPointerException if the given content type is <code>null</code>.
	@exception IllegalArgumentException if the given content type is not a text content type.
	@see #LABEL_CONTENT_TYPE_PROPERTY
	*/
	public void setLabelContentType(final ContentType newLabelTextContentType);

}
