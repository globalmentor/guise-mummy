package com.guiseframework.model;

import javax.mail.internet.ContentType;

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

		/**@return The text, or <code>null</code> if there is no text.*/
		public String getText() {return text;}

		/**Sets the text.
		This is a bound property.
		@param newText The new text.
		@see #TEXT_PROPERTY
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
		@param newTextContentType The new text content type.
		@exception NullPointerException if the given content type is <code>null</code>.
		@exception IllegalArgumentException if the given content type is not a text content type.
		@see #TEXT_CONTENT_TYPE_PROPERTY
		*/
		public void setTextContentType(final ContentType newTextContentType)
		{
			checkInstance(newTextContentType, "Content type cannot be null.");
			if(textContentType!=newTextContentType)	//if the value is really changing
			{
				final ContentType oldTextContentType=textContentType;	//get the old value
				if(!isText(newTextContentType))	//if the new content type is not a text content type
				{
					throw new IllegalArgumentException("Content type "+newTextContentType+" is not a text content type.");
				}
				textContentType=newTextContentType;	//actually change the value
				firePropertyChange(TEXT_CONTENT_TYPE_PROPERTY, oldTextContentType, newTextContentType);	//indicate that the value changed
			}			
		}

	/**The text resource key, or <code>null</code> if there is no text resource specified.*/
	private String textResourceKey=null;

		/**@return The text resource key, or <code>null</code> if there is no text resource specified.*/
		public String getTextResourceKey() {return textResourceKey;}

		/**Sets the key identifying the text in the resources.
		This is a bound property.
		@param newTextResourceKey The new text resource key.
		@see #TEXT_RESOURCE_KEY_PROPERTY
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

}
