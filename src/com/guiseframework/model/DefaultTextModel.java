package com.guiseframework.model;

import javax.mail.internet.ContentType;

import com.globalmentor.java.Objects;

import static com.globalmentor.java.Objects.*;
import static com.globalmentor.text.TextUtilities.*;

/**The default implementation of a model for text and an associated label.
@author Garret Wilson
*/
public class DefaultTextModel extends AbstractModel implements TextModel
{

	/**The text, which may include a resource reference, or <code>null</code> if there is no text.*/
	private String text;

		/**@return The text, which may include a resource reference, or <code>null</code> if there is no text.*/
		public String getText() {return text;}

		/**Sets the text.
		This is a bound property.
		@param newText The new text, which may include a resource reference.
		@see #TEXT_PROPERTY
		*/
		public void setText(final String newText)
		{
			if(!Objects.equals(text, newText))	//if the value is really changing
			{
				final String oldText=text;	//get the old value
				text=newText;	//actually change the value
				firePropertyChange(TEXT_PROPERTY, oldText, newText);	//indicate that the value changed
			}			
		}

	/**The content type of the text.*/
	private ContentType textContentType;

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

	/**Default constructor.*/
	public DefaultTextModel()
	{
		this(null);	//construct the class with no text
	}

	/**Text constructor with a default {@link Model#PLAIN_TEXT_CONTENT_TYPE} content type.
	@param text The text, which may include a resource reference, or <code>null</code> if there is no text.
	*/
	public DefaultTextModel(final String text)
	{
		this(text, PLAIN_TEXT_CONTENT_TYPE);	//construct the class with a plain text content type
	}

	/**Text and content type constructor
	@param text The text, which may include a resource reference, or <code>null</code> if there is no text.
	@param textContentType The content type of the text.
	@exception NullPointerException if the given content type is <code>null</code>.
	@exception IllegalArgumentException if the given content type is not a text content type.
	*/
	public DefaultTextModel(final String text, final ContentType textContentType)
	{
		this.text=text;	//save the text
		if(!isText(checkInstance(textContentType, "Content type cannot be null.")))	//if the content type is not a text content type
		{
			throw new IllegalArgumentException("Content type "+textContentType+" is not a text content type.");
		}
		this.textContentType=textContentType;	//save the text content type
	}
}
