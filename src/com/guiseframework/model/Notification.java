package com.guiseframework.model;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.text.TextUtilities.*;

import javax.mail.internet.ContentType;

/**A notification to the user of some event or state, such as an error or invalid user input.
@author Garret Wilson
*/
public class Notification
{

	/**The severity of the notification.
	@author Garret Wilson
	*/
	public enum Severity
	{
		/**Indicates the program's execution path.*/
		TRACE,
		/**Indicates useful information that should nonetheless not be logged.*/
		INFO,
		/**Specific information which should be logged but which are adversity-neutral.*/
		LOG,
		/**Indications that conditions are possibly adverse.*/
		WARN,
		/**Indicates an unexpected condition representing an error.*/
		ERROR;
	};

	/**The relevance of the notification to the application.
	@author Garret Wilson
	*/
/*TODO fix
	public enum Relevance
	{
		UI,
		
		USER,
		
		SYSTEM, etc.
		
	};
*/

	/**The associated error or exception, if any.*/
	private final Throwable error;

		/**@return The associated error or exception, if any.*/
		public Throwable getError() {return error;}

	/**The severity of the notification.*/
	private final Severity severity;

		/**@return The severity of the notification.*/
		public Severity getSeverity() {return severity;}

	/**The message text, or <code>null</code> if there is no message text.*/
	private final String message;

		/**@return The message text, or <code>null</code> if there is no message text.*/
		public String getMessage() {return message;}

	/**The content type of the message text.*/
	private final ContentType messageContentType;

		/**@return The content type of the message text.*/
		public ContentType getMessageContentType() {return messageContentType;}

	/**The message text resource key, or <code>null</code> if there is no message text resource specified.*/
	private final String messageResourceKey;

		/**@return The message text resource key, or <code>null</code> if there is no message text resource specified.*/
		public String getMessageResourceKey() {return messageResourceKey;}

	/**Error constructor with a {@link Severity#ERROR} severity and a <code>text/plain</code> content type.
	If the error provides a message, it is used as the notification message; otherwise, the error's string value is used as the message.
	@param error The associated error or exception, if any.
	@exception NullPointerException if the given error is <code>null</code>.
	*/
	public Notification(final Throwable error)
	{
		this(error, error.getMessage()!=null ? error.getMessage() : error.toString(), null);	//construct the notification with a message from the error
	}

	/**Error, message, and message resource key constructor with a {@link Severity#ERROR} severity and a <code>text/plain</code> content type.
	@param error The associated error or exception.
	@param message The message text, or <code>null</code> if there is no message text.
	@param messageResourceKey The message text resource key, or <code>null</code> if there is no message text resource specified.
	@exception NullPointerException if the given error is <code>null</code>.
	*/
	public Notification(final Throwable error, final String message, final String messageResourceKey)
	{
		this(message, messageResourceKey, Severity.ERROR, error);	//construct the notification indicating an error
	}

	/**Message, and message resource key constructor with a {@link Severity#INFO} severity, no error, and a <code>text/plain</code> content type.
	@param message The message text, or <code>null</code> if there is no message text.
	@param messageResourceKey The message text resource key, or <code>null</code> if there is no message text resource specified.
	@param severity The severity of the notification.
	@exception NullPointerException if the given severity is <code>null</code>.
	*/
	public Notification(final String message, final String messageResourceKey)
	{
		this(message, messageResourceKey, Severity.INFO);	//construct the notification with INFO severity
	}

	/**Message, message resource key, and severity constructor with no error and a <code>text/plain</code> content type.
	@param message The message text, or <code>null</code> if there is no message text.
	@param messageResourceKey The message text resource key, or <code>null</code> if there is no message text resource specified.
	@param severity The severity of the notification.
	@exception NullPointerException if the given severity is <code>null</code>.
	*/
	public Notification(final String message, final String messageResourceKey, final Severity severity)
	{
		this(message, messageResourceKey, severity, null);	//construct the notification with no error
	}

	/**Message, message resource key, severity, and error constructor with a <code>text/plain</code> content type.
	@param message The message text, or <code>null</code> if there is no message text.
	@param messageResourceKey The message text resource key, or <code>null</code> if there is no message text resource specified.
	@param severity The severity of the notification.
	@param error The associated error or exception, or <code>null</code> if there is no related error.
	@exception NullPointerException if the given severity is <code>null</code>.
	*/
	public Notification(final String message, final String messageResourceKey, final Severity severity, final Throwable error)
	{
		this(message, messageResourceKey, Model.PLAIN_TEXT_CONTENT_TYPE, severity, error);	//construct the notification with a plain text message
	}

	/**Message, message resource key, and message content type constructor with a {@link Severity#INFO} severity and no associated error.
	@param message The message text, or <code>null</code> if there is no message text.
	@param messageResourceKey The message text resource key, or <code>null</code> if there is no message text resource specified.
	@param messageContentType The message text content type.
	@param severity The severity of the notification.
	@exception NullPointerException if the given severity is <code>null</code>.
	@exception IllegalArgumentException if the given content type is not a text content type.
	*/
	public Notification(final String message, final String messageResourceKey, final ContentType messageContentType)
	{
		this(message, messageResourceKey, messageContentType, Severity.INFO);	//construct the notification with INFO severity
	}
	
	/**Message, message resource key, message content type, and severity constructor with no associated error.
	@param message The message text, or <code>null</code> if there is no message text.
	@param messageResourceKey The message text resource key, or <code>null</code> if there is no message text resource specified.
	@param messageContentType The message text content type.
	@param severity The severity of the notification.
	@exception NullPointerException if the given severity is <code>null</code>.
	@exception IllegalArgumentException if the given content type is not a text content type.
	*/
	public Notification(final String message, final String messageResourceKey, final ContentType messageContentType, final Severity severity)
	{
		this(message, messageResourceKey, messageContentType, severity, null);	//construct the notification with no error
	}

	/**Message, message resource key, message content type, severity, and error constructor.
	@param message The message text, or <code>null</code> if there is no message text.
	@param messageResourceKey The message text resource key, or <code>null</code> if there is no message text resource specified.
	@param messageContentType The message text content type.
	@param severity The severity of the notification.
	@param error The associated error or exception, or <code>null</code> if there is no related error.
	@exception IllegalArgumentException if the given content type is not a text content type.
	*/
	public Notification(final String message, final String messageResourceKey, final ContentType messageContentType, final Severity severity, final Throwable error)
	{
		this.severity=checkNull(severity, "A severity must be specified.");
		this.message=message;
		this.messageResourceKey=messageResourceKey;
		if(!isText(messageContentType))	//if the new content type is not a text content type
		{
			throw new IllegalArgumentException("Content type "+messageContentType+" is not a text content type.");
		}
		this.messageContentType=messageContentType;
		this.error=error;
	}

}
