package com.garretwilson.guise.model;

import java.io.*;
import java.util.MissingResourceException;

import javax.mail.internet.ContentType;

import com.garretwilson.guise.session.GuiseSession;
import com.garretwilson.io.*;
import static com.garretwilson.io.ContentTypeConstants.*;
import static com.garretwilson.io.WriterUtilities.*;
import com.garretwilson.lang.ObjectUtilities;
import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.net.URIUtilities.*;
import static com.garretwilson.text.CharacterEncodingConstants.*;
import static com.garretwilson.text.TextUtilities.*;

/**The default implementation of a model for text and an associated label.
@author Garret Wilson
*/
public class DefaultTextModel extends AbstractModel implements TextModel
{

	/**A preinstantiated shared default content type of <code>text/plain</code>.*/
	protected final static ContentType TEXT_PLAIN_CONTENT_TYPE=new ContentType(TEXT, PLAIN_SUBTYPE, null);

	/**The content type of the text.*/
	private ContentType contentType=TEXT_PLAIN_CONTENT_TYPE;

		/**@return The content type of the text.*/
		public ContentType getContentType() {return contentType;}

		/**Sets the content type of the text.
		This is a bound property.
		@param newContentType The new text content type.
		@exception NullPointerException if the given content type is <code>null</code>.
		@exception IllegalArgumentException if the given content type is not a text content type.
		@see TextModel#CONTENT_TYPE_PROPERTY
		*/
		public void setContentType(final ContentType newContentType)
		{
			checkNull(newContentType, "Content type cannot be null.");
			if(contentType!=newContentType)	//if the value is really changing
			{
				final ContentType oldContentType=contentType;	//get the old value
				if(!isText(newContentType))	//if the new content type is not a text content type
				{
					throw new IllegalArgumentException("Content type "+newContentType+" is not a text content type.");
				}
				contentType=newContentType;	//actually change the value
				firePropertyChange(CONTENT_TYPE_PROPERTY, oldContentType, newContentType);	//indicate that the value changed
			}			
		}

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
			try
			{
				return getString(text, getTextResourceKey());	//get the value or the resource, if available
			}
			catch(final MissingResourceException missingResourceException)	//if the resource does not exist
			{
				final String resourceKey=getTextResourceKey();	//get the resource key for the text, if there is one
				if(resourceKey!=null && isPath(resourceKey) && !isAbsolutePath(resourceKey))	//if the resource key is a relative path
				{
					final String applicationResourcePath=getSession().getApplication().getLocaleResourcePath(resourceKey, getSession().getLocale());	//try to get a locale-sensitive path to the resource
					if(applicationResourcePath!=null)	//if there is a path to the resource
					{
						final InputStream inputStream=getSession().getApplication().getResourceAsStream(applicationResourcePath);	//get a stream to the resource
						if(inputStream!=null)	//if we got a stream to the resource (we always should, as we already checked to see which path represents an existing resource)
						{
							try
							{
								try
								{
									final StringWriter stringWriter=new StringWriter();	//create a new string writer to receive the resource contents
									final Reader resourceReader=new BOMInputStreamReader(new BufferedInputStream(inputStream), UTF_8);	//get an input reader to the file, defaulting to UTF-8 if we don't know its encoding
									write(resourceReader, stringWriter);	//copy the resource to the string
									return stringWriter.toString();	//return the string read from the resource
								}
								finally
								{
									inputStream.close();	//always close the input stream
								}
							}
							catch(final IOException ioException)	//if there is an I/O error, convert it to a missing resource exception
							{
								throw (MissingResourceException)new MissingResourceException(ioException.getMessage(), missingResourceException.getClassName(), missingResourceException.getKey()).initCause(ioException);
							}
						}
					}
				}
				throw missingResourceException;	//if we couldn't find an application resource, throw the original missing resource exception
			}
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
