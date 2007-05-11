package com.guiseframework.model;

import java.net.URI;
import java.text.MessageFormat;
import java.util.*;
import static java.util.Collections.*;

import javax.mail.internet.ContentType;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.text.TextUtilities.*;

import com.guiseframework.Guise;
import static com.guiseframework.Resources.*;

/**A notification to the user of some event or state, such as an error or invalid user input.
A notification also allows certain options indicating response choices for the user when presented with the notification.
@author Garret Wilson
*/
public class Notification extends DefaultLabelModel
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

		/**@return The resource reference for the severity label.*/
		public String getLabel()
		{
			return createStringResourceReference(MessageFormat.format("theme.notification.severity.{0}.label", getResourceKeyName(this)));	//create a resource reference using the resource key name of this enum value
		}

		/**@return The resource reference for the severity glyph.*/
		public URI getGlyph()
		{
			return createURIResourceReference(MessageFormat.format("theme.notification.severity.{0}.glyph", getResourceKeyName(this)));	//create a resource reference using the resource key name of this enum value
		}

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

	/**The options which can be expected as responses for this notification.
	The option ordinals represent the order in which they should be presented.
	Some options such as {@link Option#ABORT}, {@link Option#STOP}, and {@link Option#CANCEL}, are <dfn>fatal</dfn>,
		indicating that the user has actively expressed that an action be stopped permanently.
	*/
	public enum Option
	{
		OK(false),
		YES(false),
		YES_ALL(false),
		NO(true),
		NO_ALL(true),
		ABORT(true),
		RETRY(false),
		FAIL(true),
		STOP(true),
		REMOVE(false),
		CANCEL(true);

		/**Whether this option is considered fatal.*/
		private final boolean fatal;

			/**@return Whether this option is considered fatal.*/
			public boolean isFatal() {return fatal;}

		/**Fatality consructor.
		@param fatal Whether this option is considered fatal.
		*/
		private Option(final boolean fatal)
		{
			this.fatal=fatal;
		}

		/**@return The resource reference for the option label.*/
		public String getLabel()
		{
			return createStringResourceReference(MessageFormat.format("theme.notification.option.{0}.label", getResourceKeyName(this)));	//create a resource reference using the resource key name of this enum value
		}

		/**@return The resource reference for the option glyph.*/
		public URI getGlyph()
		{
			return createURIResourceReference(MessageFormat.format("theme.notification.option.{0}.glyph", getResourceKeyName(this)));	//create a resource reference using the resource key name of this enum value
		}

	}

	/**The associated error or exception, if any.*/
	private final Throwable error;

		/**@return The associated error or exception, if any.*/
		public Throwable getError() {return error;}

	/**The severity of the notification.*/
	private final Severity severity;

		/**@return The severity of the notification.*/
		public Severity getSeverity() {return severity;}

	/**The read-only list of available response options in order.*/
	private final List<Option> options;

		/**@return The read-only list of available response options in order.*/
		public List<Option> getOptions() {return options;}
		
	/**The message text, which may include a resource reference.*/
	private final String message;

		/**@return The message text, which may include a resource reference.*/
		public String getMessage() {return message;}

	/**The content type of the message text.*/
	private final ContentType messageContentType;

		/**@return The content type of the message text.*/
		public ContentType getMessageContentType() {return messageContentType;}
		
	/**Error constructor with a {@link Severity#ERROR} severity and a <code>text/plain</code> content type.
	If the error provides a message, it is used as the notification message; otherwise, the error's string value is used as the message.
	@param error The associated error or exception.
	@param options The available response options; if no options are given, {@link Option#OK} will be assumed.
	@exception NullPointerException if the given error and/or options is <code>null</code>.
	*/
	public Notification(final Throwable error, final Option... options)
	{
		this(checkInstance(error, "An error must be provided."), error.getMessage()!=null ? error.getMessage() : error.toString(), options);	//construct the notification with a message from the error
	}

	/**Error and message constructor with a {@link Severity#ERROR} severity and a <code>text/plain</code> content type.
	@param error The associated error or exception.
	@param message The message text, which may include a resource reference.
	@param options The available response options; if no options are given, {@link Option#OK} will be assumed.
	@exception NullPointerException if the given error, message, and/or options is <code>null</code>.
	*/
	public Notification(final Throwable error, final String message, final Option... options)
	{
		this(message, Severity.ERROR, error, options);	//construct the notification indicating an error
	}

	/**Message constructor with a {@link Severity#INFO} severity, no error, and a <code>text/plain</code> content type.
	@param message The message text, which may include a resource reference.
	@param options The available response options; if no options are given, {@link Option#OK} will be assumed.
	@exception NullPointerException if the given message and/or options is <code>null</code>.
	*/
	public Notification(final String message, final Option... options)
	{
		this(message, Severity.INFO, options);	//construct the notification with INFO severity
	}

	/**Message and severity constructor with no error and a <code>text/plain</code> content type.
	@param message The message text, which may include a resource reference.
	@param severity The severity of the notification.
	@param options The available response options; if no options are given, {@link Option#OK} will be assumed.
	@exception NullPointerException if the given message, severity, and/or options is <code>null</code>.
	*/
	public Notification(final String message, final Severity severity, final Option... options)
	{
		this(message, severity, null, options);	//construct the notification with no error
	}

	/**Message, severity, and error constructor with a <code>text/plain</code> content type.
	@param message The message text, which may include a resource reference.
	@param severity The severity of the notification.
	@param error The associated error or exception, or <code>null</code> if there is no related error.
	@param options The available response options; if no options are given, {@link Option#OK} will be assumed.
	@exception NullPointerException if the given message, severity, and/or options is <code>null</code>.
	*/
	public Notification(final String message, final Severity severity, final Throwable error, final Option... options)
	{
		this(message, Model.PLAIN_TEXT_CONTENT_TYPE, severity, error, options);	//construct the notification with a plain text message
	}

	/**Message and message content type constructor with a {@link Severity#INFO} severity and no associated error.
	@param message The message text, which may include a resource reference
	@param messageContentType The message text content type.
	@param options The available response options; if no options are given, {@link Option#OK} will be assumed.
	@exception NullPointerException if the given message, severity, and/or options is <code>null</code>.
	@exception IllegalArgumentException if the given content type is not a text content type.
	*/
	public Notification(final String message, final ContentType messageContentType, final Option... options)
	{
		this(message, messageContentType, Severity.INFO, options);	//construct the notification with INFO severity
	}
	
	/**Message, message content type, and severity constructor with no associated error.
	@param message The message text, which may include a resource reference.
	@param messageContentType The message text content type.
	@param severity The severity of the notification.
	@param options The available response options; if no options are given, {@link Option#OK} will be assumed.
	@exception NullPointerException if the given message, severity, and/or options is <code>null</code>.
	@exception IllegalArgumentException if the given content type is not a text content type.
	*/
	public Notification(final String message, final ContentType messageContentType, final Severity severity, final Option... options)
	{
		this(message, messageContentType, severity, null, options);	//construct the notification with no error
	}

	/**Message, message content type, severity, error, and options constructor.
	@param message The message text, which may include a resource reference.
	@param messageContentType The message text content type.
	@param severity The severity of the notification.
	@param error The associated error or exception, or <code>null</code> if there is no related error.
	@param options The available response options; if no options are given, {@link Option#OK} will be assumed.
	@excepion NullPointerException if the given message and/or options is <code>null</code>.
	@exception IllegalArgumentException if the given content type is not a text content type.
	*/
	public Notification(final String message, final ContentType messageContentType, final Severity severity, final Throwable error, final Option... options)
	{
		this.severity=checkInstance(severity, "A severity must be specified.");
		this.message=checkInstance(message, "A message must be specified.");
		if(!isText(messageContentType))	//if the new content type is not a text content type
		{
			throw new IllegalArgumentException("Content type "+messageContentType+" is not a text content type.");
		}
		this.messageContentType=messageContentType;
		this.error=error;
		final List<Option> optionList=new ArrayList<Option>();	//create a list of options
		for(final Option option:options)	//put all the options in the list without duplicates
		{
			if(!optionList.contains(option))	//if this option isn't already in the list
			{
				optionList.add(option);	//add this option to the list
			}
		}
		if(optionList.isEmpty())	//if no options were given
		{
			optionList.add(Option.OK);	//add a default OK option
		}
		this.options=unmodifiableList(optionList);	//save the list of options without duplicates
	}

	/**@return A string representation of this notification.*/
	public String toString()
	{
		return getSeverity()+": "+AbstractModel.getPlainText(Guise.getInstance().getGuiseSession().resolveString(getMessage()), getMessageContentType());	//severity: message (resolve the message and get its plain text form)
	}
}
