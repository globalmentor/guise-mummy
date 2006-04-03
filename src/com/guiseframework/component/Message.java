package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.text.TextUtilities.*;

import javax.mail.internet.ContentType;

import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.GuiseSession;
import com.guiseframework.model.*;

/**A message component showing the message and any label.
The message only supports text content types, including:
<ul>
	<li><code>text/*</code></li>
	<li><code>application/xml</code></li>
	<li><code>application/*+xml</code></li>
</ul>
<p>The message defaults to a content type of <code>text/plain</code>.</p>
@author Garret Wilson
*/
public class Message extends AbstractComponent<Message>
{

	/**The message bound property.*/
	public final static String MESSAGE_PROPERTY=getPropertyName(Message.class, "message");
	/**The message content type bound property.*/
	public final static String MESSAGE_CONTENT_TYPE_PROPERTY=getPropertyName(Message.class, "messageContentType");
	/**The message resource key bound property.*/
	public final static String MESSAGE_RESOURCE_KEY_PROPERTY=getPropertyName(Message.class, "messageResourceKey");

	/**The message text, or <code>null</code> if there is no message text.*/
	private String message=null;

		/**@return The message text, or <code>null</code> if there is no message text.*/
		public String getMessage() {return message;}

		/**Sets the text of the message.
		This is a bound property.
		@param newMessage The new text of the message.
		@see #MESSAGE_PROPERTY
		*/
		public void setMessage(final String newMessage)
		{
			if(!ObjectUtilities.equals(message, newMessage))	//if the value is really changing
			{
				final String oldMessage=message;	//get the old value
				message=newMessage;	//actually change the value
				firePropertyChange(MESSAGE_PROPERTY, oldMessage, newMessage);	//indicate that the value changed
			}			
		}

	/**The content type of the message text.*/
	private ContentType messageContentType=Component.PLAIN_TEXT_CONTENT_TYPE;

		/**@return The content type of the message text.*/
		public ContentType getMessageContentType() {return messageContentType;}

		/**Sets the content type of the message text.
		This is a bound property.
		@param newMessageContentType The new message text content type.
		@exception NullPointerException if the given content type is <code>null</code>.
		@exception IllegalArgumentException if the given content type is not a text content type.
		@see #MESSAGE_CONTENT_TYPE_PROPERTY
		*/
		public void setMessageContentType(final ContentType newMessageContentType)
		{
			checkInstance(newMessageContentType, "Content type cannot be null.");
			if(messageContentType!=newMessageContentType)	//if the value is really changing
			{
				final ContentType oldMessageContentType=messageContentType;	//get the old value
				if(!isText(newMessageContentType))	//if the new content type is not a text content type
				{
					throw new IllegalArgumentException("Content type "+newMessageContentType+" is not a text content type.");
				}
				messageContentType=newMessageContentType;	//actually change the value
				firePropertyChange(MESSAGE_CONTENT_TYPE_PROPERTY, oldMessageContentType, newMessageContentType);	//indicate that the value changed
			}			
		}

	/**The message text resource key, or <code>null</code> if there is no message text resource specified.*/
	private String messageResourceKey=null;

		/**@return The message text resource key, or <code>null</code> if there is no message text resource specified.*/
		public String getMessageResourceKey() {return messageResourceKey;}

		/**Sets the key identifying the text of the message in the resources.
		This is a bound property.
		@param newMessageResourceKey The new message text resource key.
		@see #MESSAGE_RESOURCE_KEY_PROPERTY
		*/
		public void setMessageResourceKey(final String newMessageResourceKey)
		{
			if(!ObjectUtilities.equals(messageResourceKey, newMessageResourceKey))	//if the value is really changing
			{
				final String oldMessageResourceKey=messageResourceKey;	//get the old value
				messageResourceKey=newMessageResourceKey;	//actually change the value
				firePropertyChange(MESSAGE_RESOURCE_KEY_PROPERTY, oldMessageResourceKey, newMessageResourceKey);	//indicate that the value changed
			}
		}

	/**Session constructor with a default model.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public Message(final GuiseSession session)
	{
		this(session, (String)null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor with a default data model.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Message(final GuiseSession session, final String id)
	{
		this(session, id, new DefaultModel(session));	//construct the class with a default model
	}

	/**Session and model constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	*/
	public Message(final GuiseSession session, final Model model)
	{
		this(session, null, model);	//construct the class, indicating that a default ID should be used
	}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Message(final GuiseSession session, final String id, final Model model)
	{
		super(session, id/*TODO add message model, model*/);	//construct the parent class
	}
}
