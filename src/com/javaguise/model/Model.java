package com.javaguise.model;

import static com.garretwilson.lang.ClassUtilities.*;

import java.util.MissingResourceException;

import javax.mail.internet.ContentType;

import com.garretwilson.beans.*;
import com.garretwilson.text.TextConstants;
import com.garretwilson.text.xml.xhtml.XHTMLConstants;
import com.javaguise.session.GuiseSession;

/**Base interface for all component models.
A model should never fire a model-related event directly. It should rather create a postponed event and queue that event with the session.
@author Garret Wilson
@see GuiseSession#queueModelEvent(com.garretwilson.event.PostponedEvent)
*/
public interface Model extends PropertyBindable
{

	/**The info bound property.*/
	public final static String INFO_PROPERTY=getPropertyName(Model.class, "info");
	/**The info content type bound property.*/
	public final static String INFO_CONTENT_TYPE_PROPERTY=getPropertyName(Model.class, "infoContentType");
	/**The info resource key bound property.*/
	public final static String INFO_RESOURCE_KEY_PROPERTY=getPropertyName(Model.class, "infoResourceKey");

	/**A content type of <code>text/plain</code>.*/
	public final static ContentType PLAIN_TEXT_CONTENT_TYPE=TextConstants.TEXT_PLAIN_CONTENT_TYPE;

	/**A content type of <code>application/xhtml+xml</code>.*/
	public final static ContentType XHTML_CONTENT_TYPE=XHTMLConstants.XHTML_CONTENT_TYPE;

	/**@return The Guise session that owns this model.*/
	public GuiseSession getSession();

	/**@return Whether the contents of this model are valid.*/
	public boolean isValid();

	/**Determines the advisory information text, such as might appear in a tooltip.
	If information is specified, it will be used; otherwise, a value will be loaded from the resources if possible.
	@return The advisory information text, such as might appear in a tooltip, or <code>null</code> if there is no advisory information.
	@exception MissingResourceException if there was an error loading the value from the resources.
	@see #getInfoResourceKey()
	*/
	public String getInfo() throws MissingResourceException;

	/**Sets the advisory information text, such as might appear in a tooltip.
	This is a bound property.
	@param newInfo The new text of the advisory information text, such as might appear in a tooltip.
	@see #INFO_PROPERTY
	*/
	public void setInfo(final String newInfo);

	/**@return The content type of the advisory information text.*/
	public ContentType getInfoContentType();

	/**Sets the content type of the advisory information text.
	This is a bound property.
	@param newInfoContentType The new advisory information text content type.
	@exception NullPointerException if the given content type is <code>null</code>.
	@exception IllegalArgumentException if the given content type is not a text content type.
	@see #INFO_CONTENT_TYPE_PROPERTY
	*/
	public void setInfoContentType(final ContentType newInfoContentType);

	/**@return The advisory information text resource key, or <code>null</code> if there is no advisory information text resource specified.*/
	public String getInfoResourceKey();

	/**Sets the key identifying the text of the advisory information in the resources.
	This is a bound property.
	@param newInfoResourceKey The new advisory information text resource key.
	@see Model#INFO_RESOURCE_KEY_PROPERTY
	*/
	public void setInfoResourceKey(final String newInfoResourceKey);
}
