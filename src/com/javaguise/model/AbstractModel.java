package com.javaguise.model;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.text.TextUtilities.*;

import java.net.URI;
import java.util.MissingResourceException;

import javax.mail.internet.ContentType;

import com.garretwilson.event.EventListenerManager;
import com.garretwilson.lang.ObjectUtilities;
import com.javaguise.event.GuiseBoundPropertyObject;
import com.javaguise.session.GuiseSession;

/**A base abstract class implementing helpful functionality for models.
@author Garret Wilson
*/
public abstract class AbstractModel extends GuiseBoundPropertyObject implements Model
{
	/**The object managing event listeners.*/
	private final EventListenerManager eventListenerManager=new EventListenerManager();

		/**@return The object managing event listeners.*/
		protected EventListenerManager getEventListenerManager() {return eventListenerManager;}

	/**The advisory information text, such as might appear in a tooltip, or <code>null</code> if there is no advisory information.*/
	private String info=null;

		/**Determines the advisory information text, such as might appear in a tooltip.
		If information is specified, it will be used; otherwise, a value will be loaded from the resources if possible.
		@return The advisory information text, such as might appear in a tooltip, or <code>null</code> if there is no advisory information.
		@exception MissingResourceException if there was an error loading the value from the resources.
		@see #getInfoResourceKey()
		*/
		public String getInfo() throws MissingResourceException
		{
			return getString(info, getInfoResourceKey());	//get the value or the resource, if available
		}

		/**Sets the advisory information text, such as might appear in a tooltip.
		This is a bound property.
		@param newInfo The new text of the advisory information text, such as might appear in a tooltip.
		@see Model#INFO_PROPERTY
		*/
		public void setInfo(final String newInfo)
		{
			if(!ObjectUtilities.equals(info, newInfo))	//if the value is really changing
			{
				final String oldInfo=info;	//get the old value
				info=newInfo;	//actually change the value
				firePropertyChange(INFO_PROPERTY, oldInfo, newInfo);	//indicate that the value changed
			}			
		}

	/**The content type of the advisory information text.*/
	private ContentType infoContentType=PLAIN_TEXT_CONTENT_TYPE;

		/**@return The content type of the advisory information text.*/
		public ContentType getInfoContentType() {return infoContentType;}

		/**Sets the content type of the advisory information text.
		This is a bound property.
		@param newInfoContentType The new advisory information text content type.
		@exception NullPointerException if the given content type is <code>null</code>.
		@exception IllegalArgumentException if the given content type is not a text content type.
		@see Model#INFO_CONTENT_TYPE_PROPERTY
		*/
		public void setInfoContentType(final ContentType newInfoContentType)
		{
			checkNull(newInfoContentType, "Content type cannot be null.");
			if(infoContentType!=newInfoContentType)	//if the value is really changing
			{
				final ContentType oldInfoContentType=infoContentType;	//get the old value
				if(!isText(newInfoContentType))	//if the new content type is not a text content type
				{
					throw new IllegalArgumentException("Content type "+newInfoContentType+" is not a text content type.");
				}
				infoContentType=newInfoContentType;	//actually change the value
				firePropertyChange(INFO_CONTENT_TYPE_PROPERTY, oldInfoContentType, newInfoContentType);	//indicate that the value changed
			}			
		}

	/**The advisory information text resource key, or <code>null</code> if there is no advisory information text resource specified.*/
	private String infoResourceKey=null;

		/**@return The advisory information text resource key, or <code>null</code> if there is no advisory information text resource specified.*/
		public String getInfoResourceKey() {return infoResourceKey;}

		/**Sets the key identifying the text of the advisory information in the resources.
		This is a bound property.
		@param newInfoResourceKey The new advisory information text resource key.
		@see Model#INFO_RESOURCE_KEY_PROPERTY
		*/
		public void setInfoResourceKey(final String newInfoResourceKey)
		{
			if(!ObjectUtilities.equals(infoResourceKey, newInfoResourceKey))	//if the value is really changing
			{
				final String oldInfoResourceKey=infoResourceKey;	//get the old value
				infoResourceKey=newInfoResourceKey;	//actually change the value
				firePropertyChange(INFO_RESOURCE_KEY_PROPERTY, oldInfoResourceKey, newInfoResourceKey);	//indicate that the value changed
			}
		}

	/**Session constructor.
	@param session The Guise session that owns this model.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public AbstractModel(final GuiseSession session)
	{
		super(session);	//construct the parent class
	}

	/**Determines whether the contents of this model are valid.
	This version returns <code>true</code>.
	@return Whether the contents of this model are valid.
	*/
	public boolean isValid() {return true;}

	/**Determines the plain text form of the given text, based upon its content type.
	@param text The given text.
	@param contentType The content type of the text.
	@return The plain text form of the given text, based upon the given content type.
	@exception NullPointerException if the given text and/or content type is <code>null</code>.
	@exception IllegalArgumentException if the given content type is not a text content type.
	*/
	public static String getPlainText(final String text, final ContentType contentType)
	{
		checkNull(text, "Text cannot be null");
		checkNull(contentType, "Content Type cannot be null.");
		if(!isText(contentType))	//if the new content type is not a text content type
		{
			throw new IllegalArgumentException("Content type "+contentType+" is not a text content type.");
		}
		return text;	//TODO fix to actually convert to plain text
	}

	/**Determines a string value either explicitly set or stored in the resources.
	If a value is explicitly specified, it will be used; otherwise, a value will be loaded from the session resources if possible.
	@param value The value explicitly set, which will override any resource.
	@param resourceKey The key for looking up a resource if no value is explicitly set.
	@return The string value, or <code>null</code> if there is no value available, neither explicitly set nor in the resources.
	@exception MissingResourceException if there was an error loading the value from the resources.
	*/
	protected String getString(final String value, final String resourceKey) throws MissingResourceException
	{
		if(value!=null)	//if a value is provided
		{
			return value;	//return the specified value
		}
		else if(resourceKey!=null)	//if no value is provided, but if a resource key is provided
		{
			return getSession().getStringResource(resourceKey);	//lookup the value from the resources 
		}
		else	//if neither a value nor a resource key are provided
		{
			return null;	//there is no value available
		}
	}

	/**Retrieves a reader to a string value either explicitly set or stored in the resources.
	If a value is explicitly specified, it will be used; otherwise, a value will be loaded from the resources if possible.
	This method first looks at the session resources and then at the application resources.
	@param value The value explicitly set, which will override any resource.
	@param resourceKey The key for looking up a resource if no value is explicitly set.
	@return A reader to the string value, or <code>null</code> if there is no value available, neither explicitly set nor in the resources.
	@exception MissingResourceException if there was an error loading the value from the resources.
	@see #getString(String, String)
	*/
/*TODO del when works
	protected Reader getReader(final String value, final String resourceKey) throws MissingResourceException
	{
		try
		{
			final String string=getString(value, resourceKey);	//get the value or the resource, if available
			return string!=null ? new StringReader(string) : null;	//return a reader to the string, if we found one				
		}
		catch(final MissingResourceException missingResourceException)	//if the resource does not exist
		{
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
								return new BOMInputStreamReader(new BufferedInputStream(inputStream), UTF_8);	//get an input reader to the file, defaulting to UTF-8 if we don't know its encoding TODO later do better XML-specific checks
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
*/

	/**Determines a URI value either explicitly set or stored in the resources.
	If a value is explicitly specified, it will be used; otherwise, a value will be loaded from the resources if possible.
	@param value The value explicitly set, which will override any resource.
	@param resourceKey The key for looking up a resource if no value is explicitly set.
	@return The URI value, or <code>null</code> if there is no value available, neither explicitly set nor in the resources.
	@exception MissingResourceException if there was an error loading the value from the resources.
	*/
	protected URI getURI(final URI value, final String resourceKey) throws MissingResourceException
	{
		if(value!=null)	//if a value is provided
		{
			return value;	//return the specified value
		}
		else if(resourceKey!=null)	//if no value is provided, but if a resource key is provided
		{
			return getSession().getURIResource(resourceKey);	//lookup the value from the resources 
		}
		else	//if neither a value nor a resource key are provided
		{
			return null;	//there is no value available
		}
	}

}
