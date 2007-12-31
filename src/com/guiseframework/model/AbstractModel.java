package com.guiseframework.model;

import static com.garretwilson.lang.Objects.*;
import static com.garretwilson.text.TextUtilities.*;

import javax.mail.internet.ContentType;

import com.garretwilson.beans.BoundPropertyObject;
import com.guiseframework.event.*;

/**A base abstract class implementing helpful functionality for models.
@author Garret Wilson
*/
public abstract class AbstractModel extends BoundPropertyObject implements Model
{
	/**The object managing event listeners.*/
	private final EventListenerManager eventListenerManager=new EventListenerManager();

		/**@return The object managing event listeners.*/
		protected EventListenerManager getEventListenerManager() {return eventListenerManager;}

	/**Default constructor.*/
	public AbstractModel()
	{
	}

	/**Determines the plain text form of the given text, based upon its content type.
	@param text The given text.
	@param contentType The content type of the text.
	@return The plain text form of the given text, based upon the given content type.
	@exception NullPointerException if the given text and/or content type is <code>null</code>.
	@exception IllegalArgumentException if the given content type is not a text content type.
	*/
	public static String getPlainText(final String text, final ContentType contentType)	//TODO del or move
	{
		checkInstance(text, "Text cannot be null");
		checkInstance(contentType, "Content Type cannot be null.");
		if(!isText(contentType))	//if the new content type is not a text content type
		{
			throw new IllegalArgumentException("Content type "+contentType+" is not a text content type.");
		}
		return text;	//TODO fix to actually convert to plain text
	}

}
