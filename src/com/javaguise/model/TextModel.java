package com.javaguise.model;

import java.util.MissingResourceException;
import javax.mail.internet.ContentType;

import static com.garretwilson.io.ContentTypeConstants.*;
import static com.garretwilson.lang.ClassUtilities.*;
import com.garretwilson.text.xml.xhtml.XHTMLConstants;

/**A model for text and an associated label.
This model only supports text content types, including:
<ul>
	<li><code>text/*</code></li>
	<li><code>application/xml</code></li>
	<li><code>application/*+xml</code></li>
</ul>
<p>The model defaults to a content type of <code>text/plain</code>.</p>
@author Garret Wilson
*/
public interface TextModel extends Model
{
	/**The content type bound property.*/
	public final static String TEXT_CONTENT_TYPE_PROPERTY=getPropertyName(TextModel.class, "contentType");
	/**The text bound property.*/
	public final static String TEXT_PROPERTY=getPropertyName(TextModel.class, "text");
	/**The text resource key bound property.*/
	public final static String TEXT_RESOURCE_KEY_PROPERTY=getPropertyName(MessageModel.class, "textResourceKey");

	/**A content type of <code>text/plain</code>.*/
	public final static ContentType PLAIN_TEXT_CONTENT_TYPE=new ContentType(TEXT, PLAIN_SUBTYPE, null);

	/**A content type of <code>application/xhtml+xml</code>.*/
	public final static ContentType XHTML_CONTENT_TYPE=XHTMLConstants.XHTML_CONTENT_TYPE;

	/**Determines the text.
	If text is specified, it will be used; otherwise, a value will be loaded from the resources if possible.
	@return The text, or <code>null</code> if there is no text.
	@exception MissingResourceException if there was an error loading the value from the resources.
	@see #getTextResourceKey()
	*/
	public String getText() throws MissingResourceException;

	/**Sets the text.
	This is a bound property.
	@param newText The new text.
	@see #TEXT_PROPERTY
	*/
	public void setText(final String newText);

	/**@return The content type of the text.*/
	public ContentType getTextContentType();

	/**Sets the content type of the text.
	This is a bound property.
	@param newContentType The new text content type.
	@exception NullPointerException if the given content type is <code>null</code>.
	@exception IllegalArgumentException if the given content type is not a text content type.
	@see #TEXT_CONTENT_TYPE_PROPERTY
	*/
	public void setTextContentType(final ContentType newContentType);

	/**@return The text resource key, or <code>null</code> if there is no text resource specified.*/
	public String getTextResourceKey();

	/**Sets the key identifying the text in the resources.
	This is a bound property.
	@param newTextResourceKey The new text resource key.
	@see #TEXT_RESOURCE_KEY_PROPERTY
	*/
	public void setTextResourceKey(final String newTextResourceKey);

}
