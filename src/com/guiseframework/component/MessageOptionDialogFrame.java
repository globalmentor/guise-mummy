package com.guiseframework.component;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.guiseframework.geometry.Extent;
import com.guiseframework.geometry.Unit;
import com.guiseframework.model.Notification;

/**An option dialog frame displaying a message.
@author Garret Wilson
*/
public class MessageOptionDialogFrame extends NotificationOptionDialogFrame	//TODO del when possible; currently used in external projects
{

	/**The message displayed in the frame.*/
	private final Message message;

		/**@return The message displayed in the frame.*/
		public Message getMessage() {return message;}

	/**Options constructor with no message.
	Duplicate options are ignored.
	@param options The available options.
	@exception NullPointerException if the given options is <code>null</code>.
	*/
	public MessageOptionDialogFrame(final Notification.Option... options)
	{
		this((String)null, options);	//construct the component with no message				
	}

	/**Message, and options constructor.
	Duplicate options are ignored.
	@param message The message to display in the frame, or <code>null</code> if no message should be displayed.
	@param options The available options.
	@exception NullPointerException if the given options is <code>null</code>.
	*/
	public MessageOptionDialogFrame(final String message, final Notification.Option... options)
	{
		this(createMessage(message), options);	//construct the component with a default message model
	}

	/**Session, ID, message model, and options constructor.
	Duplicate options are ignored.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param messageModel The model of the message to display in the frame.
	@param options The available options.
	@exception NullPointerException if the given session, message model, and/or options is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
/*TODO fix or del
	public MessageOptionDialogFrame(final GuiseSession session, final String id, final MessageModel messageModel, final Option... options)
	{
		this(session, id, new Message(session, messageModel), options);	//construct the class with a message component
	}
*/

	/**Message component and options constructor.
	Duplicate options are ignored.
	@param messageComponent The message component to display in the frame.
	@param options The available options.
	@exception NullPointerException if the given message component, and/or options is <code>null</code>.
	*/
	public MessageOptionDialogFrame(final Message messageComponent, final Notification.Option... options)
	{
		super(messageComponent, options);	//construct the parent class
		this.message=checkInstance(messageComponent, "Message component cannot be null");
		setLineExtent(new Extent(25, Unit.EM));	//set the default preferred size
		setPageExtent(new Extent(10, Unit.EM));
	}

	/**Creates a message component with a message.
	@param messageText The text of the message component.
	@return A new message component with the given message text.
	*/
	protected static Message createMessage(final String messageText)	//TODO eventually switch to using a Message constructor
	{
		final Message message=new Message();	//create the message component
		message.setMessage(messageText);	//set the message text
		return message;	//return the message component
	}
}
