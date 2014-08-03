/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guiseframework.component;

import static com.globalmentor.java.Classes.*;
import static com.globalmentor.java.Objects.*;
import static com.globalmentor.text.Text.*;

import com.globalmentor.java.Objects;
import com.globalmentor.net.ContentType;

/**
 * A message component showing the message and any label. The message only supports text content types, including:
 * <ul>
 * <li><code>text/*</code></li>
 * <li><code>application/xml</code></li>
 * <li><code>application/*+xml</code></li>
 * </ul>
 * <p>
 * The message defaults to a content type of <code>text/plain</code>.
 * </p>
 * @author Garret Wilson
 */
public class Message extends AbstractComponent //TODO del component if not needed
{

	/** The message bound property. */
	public final static String MESSAGE_PROPERTY = getPropertyName(Message.class, "message");
	/** The message content type bound property. */
	public final static String MESSAGE_CONTENT_TYPE_PROPERTY = getPropertyName(Message.class, "messageContentType");

	/** The message text, which may include a resource reference, or <code>null</code> if there is no message text. */
	private String message = null;

	/** @return The message text, which may include a resource reference, or <code>null</code> if there is no message text. */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the text of the message. This is a bound property.
	 * @param newMessage The new text of the message, which may include a resource reference.
	 * @see #MESSAGE_PROPERTY
	 */
	public void setMessage(final String newMessage) {
		if(!Objects.equals(message, newMessage)) { //if the value is really changing
			final String oldMessage = message; //get the old value
			message = newMessage; //actually change the value
			firePropertyChange(MESSAGE_PROPERTY, oldMessage, newMessage); //indicate that the value changed
		}
	}

	/** The content type of the message text. */
	private ContentType messageContentType = Component.PLAIN_TEXT_CONTENT_TYPE;

	/** @return The content type of the message text. */
	public ContentType getMessageContentType() {
		return messageContentType;
	}

	/**
	 * Sets the content type of the message text. This is a bound property.
	 * @param newMessageContentType The new message text content type.
	 * @throws NullPointerException if the given content type is <code>null</code>.
	 * @throws IllegalArgumentException if the given content type is not a text content type.
	 * @see #MESSAGE_CONTENT_TYPE_PROPERTY
	 */
	public void setMessageContentType(final ContentType newMessageContentType) {
		checkInstance(newMessageContentType, "Content type cannot be null.");
		if(messageContentType != newMessageContentType) { //if the value is really changing
			final ContentType oldMessageContentType = messageContentType; //get the old value
			if(!isText(newMessageContentType)) { //if the new content type is not a text content type
				throw new IllegalArgumentException("Content type " + newMessageContentType + " is not a text content type.");
			}
			messageContentType = newMessageContentType; //actually change the value
			firePropertyChange(MESSAGE_CONTENT_TYPE_PROPERTY, oldMessageContentType, newMessageContentType); //indicate that the value changed
		}
	}

	/** Default constructor. */
	public Message() {
	}

	/**
	 * Message model constructor.
	 * @param messageModel The component message model.
	 * @throws NullPointerException if the given message model is <code>null</code>.
	 */
	/*TODO fix
		public Message(final MessageModel messageModel)
		{
		}
	*/
}
