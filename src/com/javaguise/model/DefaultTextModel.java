package com.javaguise.model;

import java.util.MissingResourceException;

import javax.mail.internet.ContentType;

import com.javaguise.session.GuiseSession;
import static com.garretwilson.io.ContentTypeConstants.*;
import static com.garretwilson.io.ContentTypeUtilities.*;
import com.garretwilson.lang.ObjectUtilities;
import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.text.TextUtilities.*;

/**The default implementation of a model for text and an associated label.
@author Garret Wilson
*/
public class DefaultTextModel extends AbstractModel implements TextModel
{

	/**The text, or <code>null</code> if there is no text.*/
	private String text=null;

		/**Determines the text.
		If text is specified, it will be used; otherwise, a value will be loaded from the resources if possible.
		@return The text, or <code>null</code> if there is no text.
		@exception MissingResourceException if there was an error loading the value from the resources.
		@see #getTextResourceKey()
		*/
		public String getText() throws MissingResourceException
		{
			return getString(text, getTextResourceKey());	//get the value or the resource, if available
		}

		/**Sets the text.
		This is a bound property.
		@param newText The new text.
		@see TextModel#TEXT_PROPERTY
		*/
		public void setText(final String newText)
		{
			if(!ObjectUtilities.equals(text, newText))	//if the value is really changing
			{
				final String oldText=text;	//get the old value
				text=newText;	//actually change the value
				firePropertyChange(TEXT_PROPERTY, oldText, newText);	//indicate that the value changed
			}			
		}

	/**The content type of the text.*/
	private ContentType textContentType=PLAIN_TEXT_CONTENT_TYPE;

		/**@return The content type of the text.*/
		public ContentType getTextContentType() {return textContentType;}

		/**Sets the content type of the text.
		This is a bound property.
		@param newContentType The new text content type.
		@exception NullPointerException if the given content type is <code>null</code>.
		@exception IllegalArgumentException if the given content type is not a text content type.
		@see TextModel#TEXT_CONTENT_TYPE_PROPERTY
		*/
		public void setTextContentType(final ContentType newContentType)
		{
			checkNull(newContentType, "Content type cannot be null.");
			if(textContentType!=newContentType)	//if the value is really changing
			{
				final ContentType oldContentType=textContentType;	//get the old value
				if(!isText(newContentType))	//if the new content type is not a text content type
				{
					throw new IllegalArgumentException("Content type "+newContentType+" is not a text content type.");
				}
				textContentType=newContentType;	//actually change the value
				firePropertyChange(TEXT_CONTENT_TYPE_PROPERTY, oldContentType, newContentType);	//indicate that the value changed
			}			
		}

	/**The text resource key, or <code>null</code> if there is no text resource specified.*/
	private String textResourceKey=null;

		/**@return The text resource key, or <code>null</code> if there is no text resource specified.*/
		public String getTextResourceKey() {return textResourceKey;}

		/**Sets the key identifying the text in the resources.
		This is a bound property.
		@param newTextResourceKey The new text resource key.
		@see TextModel#TEXT_RESOURCE_KEY_PROPERTY
		*/
		public void setTextResourceKey(final String newTextResourceKey)
		{
			if(!ObjectUtilities.equals(textResourceKey, newTextResourceKey))	//if the value is really changing
			{
				final String oldTextResourceKey=textResourceKey;	//get the old value
				textResourceKey=newTextResourceKey;	//actually change the value
				firePropertyChange(TEXT_RESOURCE_KEY_PROPERTY, oldTextResourceKey, newTextResourceKey);	//indicate that the value changed
			}
		}

	/**Session constructor.
	@param session The Guise session that owns this model.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public DefaultTextModel(final GuiseSession<?> session)
	{
		super(session);	//construct the parent class
	}

}
