package com.javaguise.model;

import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.checkNull;
import static com.garretwilson.text.TextUtilities.isText;

import java.util.MissingResourceException;

import javax.mail.internet.ContentType;

import com.garretwilson.beans.*;
import com.garretwilson.lang.ObjectUtilities;
import com.garretwilson.text.TextConstants;
import com.garretwilson.text.xml.xhtml.XHTMLConstants;
import com.javaguise.session.GuiseSession;
import com.javaguise.validator.ValidationException;

/**Base interface for all component models.
A model should never fire a model-related event directly. It should rather create a postponed event and queue that event with the session.
@author Garret Wilson
@see GuiseSession#queueEvent(com.garretwilson.event.PostponedEvent)
*/
public interface Model extends PropertyBindable
{

	/**The description bound property.*/
	public final static String DESCRIPTION_PROPERTY=getPropertyName(Model.class, "description");
	/**The description content type bound property.*/
	public final static String DESCRIPTION_CONTENT_TYPE_PROPERTY=getPropertyName(Model.class, "descriptionContentType");
	/**The description resource key bound property.*/
	public final static String DESCRIPTION_RESOURCE_KEY_PROPERTY=getPropertyName(Model.class, "descriptionResourceKey");
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

	/**Validates the contents of this model, throwing an exception if the model is not valid.
	@exception ValidationException if the contents of this model are not valid.	
	*/
	public void validate() throws ValidationException;

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

	/**Determines the description text, such as might appear in a flyover.
	If a description is specified, it will be used; otherwise, a value will be loaded from the resources if possible.
	@return The description text, such as might appear in a flyover, or <code>null</code> if there is no description.
	@exception MissingResourceException if there was an error loading the value from the resources.
	@see #getDescriptionResourceKey()
	*/
	public String getDescription() throws MissingResourceException;

	/**Sets the description text, such as might appear in a flyover.
	This is a bound property.
	@param newDescription The new text of the description, such as might appear in a flyover.
	@see #DESCRIPTION_PROPERTY
	*/
	public void setDescription(final String newDescription);

	/**@return The content type of the description text.*/
	public ContentType getDescriptionContentType();

	/**Sets the content type of the description text.
	This is a bound property.
	@param newDescriptionContentType The new description text content type.
	@exception NullPointerException if the given content type is <code>null</code>.
	@exception IllegalArgumentException if the given content type is not a text content type.
	@see #DESCRIPTION_CONTENT_TYPE_PROPERTY
	*/
	public void setDescriptionContentType(final ContentType newDescriptionContentType);

	/**@return The description text resource key, or <code>null</code> if there is no description text resource specified.*/
	public String getDescriptionResourceKey();

	/**Sets the key identifying the text of the description in the resources.
	This is a bound property.
	@param newDescriptionResourceKey The new description text resource key.
	@see #DESCRIPTION_RESOURCE_KEY_PROPERTY
	*/
	public void setDescriptionResourceKey(final String newDescriptionResourceKey);

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
